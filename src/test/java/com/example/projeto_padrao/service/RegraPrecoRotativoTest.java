package com.example.projeto_padrao.service;

import com.example.projeto_padrao.service.ResultadoPrecoRotativo.Situacao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RegraPrecoRotativoTest {

    @Test
    void sublimacaoSomaBaseContratipoComPrecoContratipoEIncluiDezPorCento() {
        ResultadoPrecoRotativo resultado = RegraPrecoRotativo.calcularPara(
                linha("SUBLIMAÇÃO", "5.00", "10.00", false, null));

        // (10 + 5 x 0,9175) / 0,90 = 14,5875 / 0,90
        assertThat(resultado.situacao()).isEqualTo(Situacao.CALCULADO);
        assertThat(resultado.valor()).isEqualByComparingTo("16.2083");
        assertThat(resultado.memoria()).isEqualTo("R$ 10,00 + R$ 5,00 × 0,9175 = R$ 14,5875; R$ 14,5875 ÷ 0,90 = R$ 16,2083");
        assertThat(resultado.memoria().split("÷ 0,90", -1)).hasSize(2); // uma unica divisao por 0,90
    }

    @Test
    void sublimacaoSemPrecoBaseNaoCalcula() {
        ResultadoPrecoRotativo resultado = RegraPrecoRotativo.calcularPara(linha("SUBLIMAÇÃO", "5", null, false, null));

        assertThat(resultado.situacao()).isEqualTo(Situacao.SEM_PRECO_BASE);
        assertThat(resultado.valor()).isNull();
    }

    @Test
    void cilindroCenario1UsaPrecoBaseDoBancoDeDados() {
        ResultadoPrecoRotativo resultado = RegraPrecoRotativo.calcularPara(linha("CILINDRO", "5", "10", false, "99"));

        assertThat(resultado.situacao()).isEqualTo(Situacao.CALCULADO);
        assertThat(resultado.valor()).isEqualByComparingTo("16.2083");
        assertThat(resultado.memoria()).startsWith("Cenário 1 (Banco de dados): ");
    }

    @Test
    void cilindroCenario1SemPrecoBase() {
        assertThat(RegraPrecoRotativo.calcularPara(linha("CILINDRO", "5", null, false, null)).situacao())
                .isEqualTo(Situacao.SEM_PRECO_BASE);
    }

    @Test
    void cilindroCenario2UsaPrecoDaMalhaDaTingimentos() {
        ResultadoPrecoRotativo resultado = RegraPrecoRotativo.calcularPara(linha("CILINDRO", "5", "99", true, "20"));

        // (20 x 0,90 + 5 x 0,9175) / 0,90 = 22,5875 / 0,90
        assertThat(resultado.situacao()).isEqualTo(Situacao.CALCULADO);
        assertThat(resultado.valor()).isEqualByComparingTo("25.0972");
        assertThat(resultado.memoria()).isEqualTo(
                "Cenário 2 (Tingimentos, tabela 33.11.15): R$ 20,00 × 0,90 + R$ 5,00 × 0,9175 = R$ 22,5875;"
                        + " R$ 22,5875 ÷ 0,90 = R$ 25,0972");
    }

    @Test
    void cilindroBuscaPrimeiroNa331115() {
        Map<String, BigDecimal> precos = Map.of("33.11.15", new BigDecimal("20"), "33.10.15", new BigDecimal("30"));

        assertThat(RegraPrecoRotativo.buscarNasTabelasCilindro(precos::get))
                .contains(new RegraPrecoRotativo.PrecoTingimento(new BigDecimal("20"), "33.11.15"));
    }

    @Test
    void cilindroUsa331015QuandoNaoHaNa331115() {
        Map<String, BigDecimal> precos = Map.of("33.10.15", new BigDecimal("30"));

        assertThat(RegraPrecoRotativo.buscarNasTabelasCilindro(precos::get))
                .contains(new RegraPrecoRotativo.PrecoTingimento(new BigDecimal("30"), "33.10.15"));
    }

    @Test
    void textoDoCenario2CitaAsDuasTabelasNaOrdem() {
        CenarioRegra cenario2 = RegraPrecoRotativo.CILINDRO.getCenarios().get(1);

        assertThat(cenario2.passos().get(0)).contains("primeiro na tabela 33.11.15 e, se não houver, na 33.10.15");
        assertThat(cenario2.seFaltar()).contains("33.11.15 e 33.10.15");
    }

    @Test
    void cilindroSemNenhumaDasDuasTabelas() {
        assertThat(RegraPrecoRotativo.buscarNasTabelasCilindro(tabela -> null)).isEmpty();
    }

    @Test
    void cilindroCenario2SemLinhaNaTingimentos() {
        ResultadoPrecoRotativo resultado = RegraPrecoRotativo.calcularPara(linha("CILINDRO", "5", "10", true, null));

        assertThat(resultado.situacao()).isEqualTo(Situacao.SEM_TINGIMENTO);
        assertThat(resultado.valor()).isNull();
        assertThat(resultado.memoria()).contains("tabelas 33.11.15 ou 33.10.15");
    }

    @Test
    void reconheceTecnicaSemAcentoEMinuscula() {
        assertThat(RegraPrecoRotativo.daTecnica(" sublimacao ")).contains(RegraPrecoRotativo.SUBLIMACAO);
    }

    @Test
    void tecnicaSemRegraFicaADefinir() {
        // Tecnica desconhecida (ex.: vinda de uma planilha importada) - nao ha regra para ela.
        ResultadoPrecoRotativo resultado = RegraPrecoRotativo.calcularPara(linha("OUTRA TECNICA", "1", "10", false, null));

        assertThat(resultado.situacao()).isEqualTo(Situacao.REGRA_A_DEFINIR);
        assertThat(resultado.valor()).isNull();
    }

    @Test
    void rotQuadroBuscaSomenteNa331015() {
        // Combinacao nas duas tabelas: 33.11.15 = 20, 33.10.15 = 30.
        ResultadoPrecoRotativo quadro = RegraPrecoRotativo.calcularPara(
                linha("COT-001", "ROT. QUADRO", "5", null, true, "20", true, "30"));
        ResultadoPrecoRotativo digital = RegraPrecoRotativo.calcularPara(
                linha("COT-001", "DIGITAL", "5", null, true, "20", true, "30"));

        // (30 x 0,90 + 5 x 0,9175) / 0,90 = 31,5875 / 0,90
        assertThat(quadro.valor()).isEqualByComparingTo("35.0972");
        assertThat(quadro.memoria()).startsWith("Cenário 2 (Tingimentos, tabela 33.10.15): R$ 30,00 × 0,90");
        // A Digital continua com a busca do Cilindro (33.11.15 primeiro).
        assertThat(digital.valor()).isEqualByComparingTo("25.0972");
    }

    @Test
    void rotQuadroSemA331015NaoUsaA331115() {
        ResultadoPrecoRotativo resultado = RegraPrecoRotativo.calcularPara(
                linha("COT-001", "ROT. QUADRO", "5", null, true, "20", true, null));

        assertThat(resultado.situacao()).isEqualTo(Situacao.SEM_TINGIMENTO);
        assertThat(resultado.valor()).isNull();
        assertThat(resultado.memoria()).contains("na tabela 33.10.15").doesNotContain("33.11.15");
    }

    @Test
    void rotQuadroComTEForaDaRegraIgualDigital() {
        ResultadoPrecoRotativo comT = RegraPrecoRotativo.calcularPara(
                linha("T-001", "ROT. QUADRO", "5", "10", false, null, false));
        ResultadoPrecoRotativo fora = RegraPrecoRotativo.calcularPara(
                linha("COT-001", "ROT. QUADRO", "5", "10", true, "20", false, "30"));

        assertThat(comT.valor()).isEqualByComparingTo("16.2083");
        assertThat(fora.situacao()).isEqualTo(Situacao.FORA_DA_REGRA);
        assertThat(fora.memoria()).contains("fora dos cenários da ROT. QUADRO");
    }

    @Test
    void textoDoCartaoRotQuadroCitaSoA331015() {
        CenarioRegra cenario2 = RegraPrecoRotativo.ROT_QUADRO.getCenarios().get(1);

        assertThat(cenario2.passos().get(0)).contains("somente na tabela 33.10.15 (nunca na 33.11.15)");
        assertThat(cenario2.seFaltar()).contains("na tabela 33.10.15").doesNotContain("33.11.15");
    }

    @Test
    void digitalReferenciaComTUsaCalculoDaSublimacao() {
        ResultadoPrecoRotativo resultado = RegraPrecoRotativo.calcularPara(
                linha("t-001", "DIGITAL", "5", "10", true, "20", true));

        // Mesmo se tambem estiver na 33.10.15, o T vale primeiro: (10 + 5 x 0,9175) / 0,90
        assertThat(resultado.situacao()).isEqualTo(Situacao.CALCULADO);
        assertThat(resultado.valor()).isEqualByComparingTo("16.2083");
        assertThat(resultado.memoria()).startsWith("Cenário 1 (referência com T");
    }

    @Test
    void digitalReferenciaComTSemPrecoBase() {
        assertThat(RegraPrecoRotativo.calcularPara(linha("T-001", "DIGITAL", "5", null, false, null, false)).situacao())
                .isEqualTo(Situacao.SEM_PRECO_BASE);
    }

    @Test
    void digitalReferenciaNaTabela331015UsaCalculoDoCilindroCenario2() {
        ResultadoPrecoRotativo resultado = RegraPrecoRotativo.calcularPara(
                linha("COT-001", "DIGITAL", "5", null, true, "20", true));

        // (20 x 0,90 + 5 x 0,9175) / 0,90
        assertThat(resultado.situacao()).isEqualTo(Situacao.CALCULADO);
        assertThat(resultado.valor()).isEqualByComparingTo("25.0972");
    }

    @Test
    void digitalReferenciaNaTabela331015SemLinhaDeTingimento() {
        assertThat(RegraPrecoRotativo.calcularPara(linha("COT-001", "DIGITAL", "5", null, true, null, true)).situacao())
                .isEqualTo(Situacao.SEM_TINGIMENTO);
    }

    @Test
    void digitalForaDosCenarios() {
        ResultadoPrecoRotativo resultado = RegraPrecoRotativo.calcularPara(
                linha("COT-001", "DIGITAL", "5", "10", true, "20", false));

        assertThat(resultado.situacao()).isEqualTo(Situacao.FORA_DA_REGRA);
        assertThat(resultado.valor()).isNull();
    }

    @Test
    void linhaSemTecnica() {
        assertThat(RegraPrecoRotativo.calcularPara(linha(null, "1", "10", false, null)).situacao())
                .isEqualTo(Situacao.SEM_TECNICA);
        assertThat(RegraPrecoRotativo.calcularPara(linha("  ", "1", "10", false, null)).situacao())
                .isEqualTo(Situacao.SEM_TECNICA);
    }

    private static DadosLinhaRotativo linha(String tecnica, String preco, String precoContratipoBase,
            boolean referenciaEmTingimentos, String precoMalhaTingimento) {
        return linha("REF-TESTE", tecnica, preco, precoContratipoBase, referenciaEmTingimentos, precoMalhaTingimento,
                false);
    }

    private static DadosLinhaRotativo linha(String referencia, String tecnica, String preco, String precoContratipoBase,
            boolean referenciaEmTingimentos, String precoMalhaTingimento, boolean referenciaNaTabelaDigital) {
        return linha(referencia, tecnica, preco, precoContratipoBase, referenciaEmTingimentos, precoMalhaTingimento,
                referenciaNaTabelaDigital, null);
    }

    /**
     * {@code precoMalhaTingimento} simula o achado pela busca do Cilindro (como se viesse da 33.11.15);
     * {@code precoMalhaTabelaDigital} e' o preco da combinacao somente na 33.10.15.
     */
    private static DadosLinhaRotativo linha(String referencia, String tecnica, String preco, String precoContratipoBase,
            boolean referenciaEmTingimentos, String precoMalhaTingimento, boolean referenciaNaTabelaDigital,
            String precoMalhaTabelaDigital) {
        return new DadosLinhaRotativo(referencia, "", "BASE-TESTE", "TABELA TESTE", tecnica, numero(preco),
                numero(precoContratipoBase), referenciaEmTingimentos, numero(precoMalhaTingimento),
                precoMalhaTingimento == null ? null : "33.11.15", referenciaNaTabelaDigital,
                numero(precoMalhaTabelaDigital));
    }

    private static BigDecimal numero(String texto) {
        return texto == null ? null : new BigDecimal(texto);
    }
}
