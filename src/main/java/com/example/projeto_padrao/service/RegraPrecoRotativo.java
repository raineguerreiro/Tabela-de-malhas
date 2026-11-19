package com.example.projeto_padrao.service;

import static com.example.projeto_padrao.service.MemoriaCalculo.reais;

import com.example.projeto_padrao.service.ResultadoPrecoRotativo.Situacao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Regras de construcao do "Preco da Malha" da aba Rotativos, uma por tecnica. Cada regra guarda,
 * junto com o calculo, a sua descricao por cenario (quando vale, formula, passo a passo, exemplo) -
 * a sub-aba "Regras de preco" e' montada a partir daqui, entao a explicacao na tela nunca diverge
 * da conta.
 *
 * <p>Todas as tecnicas do select ja tem regra (Rot. Quadro = Digital, com preco so' da 33.10.15). Uma tecnica
 * gravada que nao bata com nenhuma delas (ex.: vinda de planilha) fica sem valor e a tela mostra
 * "regra a definir". Tecnica nova: nova constante + um case no switch de {@link #calcular}.
 */
public enum RegraPrecoRotativo {

    SUBLIMACAO("SUBLIMAÇÃO", true, List.of(Cenarios.BANCO_DE_DADOS_SUBLIMACAO)),
    CILINDRO("CILINDRO", true, List.of(Cenarios.BANCO_DE_DADOS_CILINDRO, Cenarios.TINGIMENTOS_CILINDRO)),
    DIGITAL("DIGITAL", true, List.of(Cenarios.REFERENCIA_T_DIGITAL, Cenarios.TABELA_331015_DIGITAL,
            Cenarios.FORA_DA_REGRA_DIGITAL)),
    ROT_QUADRO("ROT. QUADRO", true, List.of(Cenarios.REFERENCIA_T_DIGITAL, Cenarios.TABELA_331015_ROT_QUADRO,
            Cenarios.FORA_DA_REGRA_DIGITAL));

    /**
     * Tabelas da aba Tingimentos onde o Cilindro (cenario 2) busca o Preco da Malha, em ordem de
     * prioridade: a primeira que tiver a combinacao vale.
     */
    public static final List<String> TABELAS_TINGIMENTO_CILINDRO = TabelasCilindro.TABELAS;

    /** Tabela da aba Tingimentos que define o cenario 2 da Digital (referencia que consta nela). */
    public static final String TABELA_TINGIMENTO_DIGITAL = "33.10.15";

    private static final BigDecimal FATOR_CONTRATIPO = new BigDecimal("0.9175");
    private static final BigDecimal FATOR_10_POR_CENTO = new BigDecimal("0.90");

    private final String tecnica;
    private final boolean definida;
    private final List<CenarioRegra> cenarios;

    RegraPrecoRotativo(String tecnica, boolean definida, List<CenarioRegra> cenarios) {
        this.tecnica = tecnica;
        this.definida = definida;
        this.cenarios = cenarios;
    }

    /** Regra da tecnica gravada na linha, comparando sem acento e sem diferenciar maiusculas ("Sublimacao" vale). */
    public static Optional<RegraPrecoRotativo> daTecnica(String tecnica) {
        if (tecnica == null || tecnica.isBlank()) {
            return Optional.empty();
        }
        String normalizada = normalizar(tecnica);
        return Arrays.stream(values()).filter(regra -> normalizar(regra.tecnica).equals(normalizada)).findFirst();
    }

    /** Preco da Malha achado na Tingimentos e a tabela de onde veio. */
    public record PrecoTingimento(BigDecimal preco, String tabela) {
    }

    /**
     * Busca o Preco da Malha nas {@link #TABELAS_TINGIMENTO_CILINDRO}, na ordem de prioridade:
     * {@code precoPorTabela} devolve o preco da combinacao naquela tabela (ou null se nao houver).
     */
    public static Optional<PrecoTingimento> buscarNasTabelasCilindro(Function<String, BigDecimal> precoPorTabela) {
        for (String tabela : TABELAS_TINGIMENTO_CILINDRO) {
            BigDecimal preco = precoPorTabela.apply(tabela);
            if (preco != null) {
                return Optional.of(new PrecoTingimento(preco, tabela));
            }
        }
        return Optional.empty();
    }

    /** Calcula o Preco da Malha de uma linha de rotativo: escolhe a regra pela tecnica e aplica. */
    public static ResultadoPrecoRotativo calcularPara(DadosLinhaRotativo linha) {
        if (linha.tecnica() == null || linha.tecnica().isBlank()) {
            return new ResultadoPrecoRotativo(null, Situacao.SEM_TECNICA, "Escolha a Técnica da linha.");
        }
        return daTecnica(linha.tecnica())
                .map(regra -> regra.calcular(linha))
                .orElseGet(() -> new ResultadoPrecoRotativo(null, Situacao.REGRA_A_DEFINIR,
                        "Técnica \"" + linha.tecnica() + "\" ainda sem regra de preço."));
    }

    public ResultadoPrecoRotativo calcular(DadosLinhaRotativo linha) {
        return switch (this) {
            case SUBLIMACAO -> cenarioBancoDeDados(linha, null);
            case CILINDRO -> linha.referenciaEmTingimentos()
                    ? cenarioTingimentos(linha)
                    : cenarioBancoDeDados(linha, "Cenário 1 (Banco de dados): ");
            case DIGITAL -> regraDigital(linha, tecnica, false);
            // Rot. Quadro: mesma regra da Digital, mas o Preco da Malha vem somente da 33.10.15.
            case ROT_QUADRO -> regraDigital(linha, tecnica, true);
        };
    }

    /**
     * Regra da Digital (e da Rot. Quadro): referencia com T -> calculo da Sublimacao; referencia na
     * tabela {@link #TABELA_TINGIMENTO_DIGITAL} da Tingimentos -> conta do Cilindro cenario 2; senao,
     * fora da regra. Na Digital o Preco da Malha e' buscado como no Cilindro (33.11.15, depois 33.10.15);
     * com {@code somenteTabelaDigital} (Rot. Quadro) vem so' da 33.10.15.
     */
    private static ResultadoPrecoRotativo regraDigital(DadosLinhaRotativo linha, String tecnica,
            boolean somenteTabelaDigital) {
        if (linha.referencia() != null && linha.referencia().trim().toUpperCase().startsWith("T")) {
            return cenarioBancoDeDados(linha, "Cenário 1 (referência com T, igual Sublimação): ");
        }
        if (linha.referenciaNaTabelaDigital()) {
            return somenteTabelaDigital
                    ? cenarioTingimentos(linha, linha.precoMalhaTabelaDigital(), TABELA_TINGIMENTO_DIGITAL,
                            List.of(TABELA_TINGIMENTO_DIGITAL))
                    : cenarioTingimentos(linha);
        }
        return new ResultadoPrecoRotativo(null, Situacao.FORA_DA_REGRA, "Referência " + linha.referencia()
                + " não começa com T e não consta na tabela " + TABELA_TINGIMENTO_DIGITAL
                + " da Tingimentos - fora dos cenários da " + tecnica + ".");
    }

    /** (Preco Contratipo da base + Preco do rotativo x 0,9175) / 0,90 */
    private static ResultadoPrecoRotativo cenarioBancoDeDados(DadosLinhaRotativo linha, String prefixo) {
        if (linha.precoContratipoBase() == null) {
            return new ResultadoPrecoRotativo(null, Situacao.SEM_PRECO_BASE,
                    textoOuVazio(prefixo) + "Cadastre o preço desta Referência + Tamanho + Tecido Base no Banco de dados.");
        }
        BigDecimal preco = precoRotativo(linha);
        BigDecimal soma = linha.precoContratipoBase().add(preco.multiply(FATOR_CONTRATIPO));
        BigDecimal valor = soma.divide(FATOR_10_POR_CENTO, 4, RoundingMode.HALF_UP);
        String memoria = textoOuVazio(prefixo) + "R$ " + reais(linha.precoContratipoBase()) + " + R$ " + reais(preco)
                + " × 0,9175 = R$ " + reais(soma) + "; R$ " + reais(soma) + " ÷ 0,90 = R$ " + reais(valor);
        return new ResultadoPrecoRotativo(valor, Situacao.CALCULADO, memoria);
    }

    /** (Preco da Malha da Tingimentos x 0,90 + Preco do rotativo x 0,9175) / 0,90 */
    private static ResultadoPrecoRotativo cenarioTingimentos(DadosLinhaRotativo linha) {
        return cenarioTingimentos(linha, linha.precoMalhaTingimento(), linha.tabelaTingimento(),
                TABELAS_TINGIMENTO_CILINDRO);
    }

    /**
     * Mesma conta, com o Preco da Malha ja buscado em {@code tabelasProcuradas} ({@code precoMalha} null se
     * nao achou; {@code tabelaUsada} e' a tabela de onde veio, para a memoria de calculo).
     */
    private static ResultadoPrecoRotativo cenarioTingimentos(DadosLinhaRotativo linha, BigDecimal precoMalha,
            String tabelaUsada, List<String> tabelasProcuradas) {
        if (precoMalha == null) {
            String tabelas = tabelasProcuradas.size() == 1 ? "na tabela " + tabelasProcuradas.get(0)
                    : "nas tabelas " + String.join(" ou ", tabelasProcuradas);
            return new ResultadoPrecoRotativo(null, Situacao.SEM_TINGIMENTO, "Cenário 2 (Tingimentos): "
                    + "Nenhuma linha " + tabelas + " da Tingimentos com Referência " + linha.referencia()
                    + ", Cor (Tecido Base) " + textoOuTraco(linha.tecidoBase()) + " e Tamanho "
                    + textoOuTraco(linha.tamanho()) + ".");
        }
        String prefixo = "Cenário 2 (Tingimentos, tabela " + tabelaUsada + "): ";
        BigDecimal preco = precoRotativo(linha);
        BigDecimal soma = precoMalha.multiply(FATOR_10_POR_CENTO).add(preco.multiply(FATOR_CONTRATIPO));
        BigDecimal valor = soma.divide(FATOR_10_POR_CENTO, 4, RoundingMode.HALF_UP);
        String memoria = prefixo + "R$ " + reais(precoMalha) + " × 0,90 + R$ " + reais(preco)
                + " × 0,9175 = R$ " + reais(soma) + "; R$ " + reais(soma) + " ÷ 0,90 = R$ " + reais(valor);
        return new ResultadoPrecoRotativo(valor, Situacao.CALCULADO, memoria);
    }

    public String getTecnica() {
        return tecnica;
    }

    public boolean isDefinida() {
        return definida;
    }

    public List<CenarioRegra> getCenarios() {
        return cenarios;
    }

    /** Preco do rotativo vazio entra como zero. */
    private static BigDecimal precoRotativo(DadosLinhaRotativo linha) {
        return linha.precoRotativo() != null ? linha.precoRotativo() : BigDecimal.ZERO;
    }

    private static String textoOuVazio(String texto) {
        return texto == null ? "" : texto;
    }

    private static String textoOuTraco(String texto) {
        return texto == null || texto.isBlank() ? "-" : texto;
    }

    private static String normalizar(String texto) {
        return Normalizer.normalize(texto.trim().toUpperCase(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    }

    /**
     * Fica numa classe a parte porque as constantes do enum (e os textos dos cenarios) sao criadas
     * antes dos campos estaticos do proprio enum - aqui a lista ja existe quando os textos a usam.
     */
    private static final class TabelasCilindro {

        static final List<String> TABELAS = List.of("33.11.15", "33.10.15");

        private TabelasCilindro() {
        }
    }

    /** Textos dos cenarios (separados para nao poluir as constantes do enum). */
    private static final class Cenarios {

        private static final String FORMULA_TINGIMENTOS =
                "(Preço da Malha da Tingimentos × 0,90 + Preço do rotativo × 0,9175) ÷ 0,90";
        private static final String EXEMPLO_TINGIMENTOS =
                "R$ 20,00 × 0,90 + R$ 5,00 × 0,9175 = R$ 22,5875; R$ 22,5875 ÷ 0,90 = R$ 25,10";

        private static final String PASSO_BUSCA_BASE = "Buscar no Banco de dados a linha com a mesma Referência +"
                + " Tamanho + Tecido Base do rotativo e usar o valor da coluna Preço Contratipo.";
        private static final String PASSO_PRECO_CONTRATIPO =
                "Pegar o Preço do rotativo (coluna Preço da linha) e multiplicar por 0,9175.";
        private static final String FORMULA_BANCO = "(Preço Contratipo da base + Preço do rotativo × 0,9175) ÷ 0,90";
        private static final String EXEMPLO_BANCO = "R$ 10,00 + R$ 5,00 × 0,9175 = R$ 14,5875; R$ 14,5875 ÷ 0,90 = R$ 16,21";
        private static final String SEM_BASE = "Se a combinação ainda não tem preço no Banco de dados, o valor não é"
                + " calculado e a célula mostra \"sem preço base\".";

        static final CenarioRegra BANCO_DE_DADOS_SUBLIMACAO = new CenarioRegra("Cálculo",
                "Toda linha com técnica Sublimação.", FORMULA_BANCO,
                List.of(PASSO_BUSCA_BASE, PASSO_PRECO_CONTRATIPO,
                        "Somar o resultado com o Preço Contratipo da base.",
                        "Incluir 10%: dividir o total por 0,90."),
                EXEMPLO_BANCO, SEM_BASE);

        static final CenarioRegra BANCO_DE_DADOS_CILINDRO = new CenarioRegra("Cenário 1 - tecido no Banco de dados",
                "A Referência não consta em nenhuma tabela da aba Tingimentos (ou seja, aparece no Banco de dados).",
                FORMULA_BANCO,
                List.of(PASSO_BUSCA_BASE, PASSO_PRECO_CONTRATIPO,
                        "Somar o resultado com o Preço Contratipo da base.",
                        "Incluir 10%: dividir o total por 0,90."),
                EXEMPLO_BANCO, SEM_BASE);

        private static final List<String> PASSOS_TINGIMENTOS = List.of(
                "Buscar na aba Tingimentos, primeiro na tabela " + TabelasCilindro.TABELAS.get(0)
                        + " e, se não houver, na " + TabelasCilindro.TABELAS.get(1) + ", a linha com a"
                        + " mesma Referência, Cor igual ao Tecido Base do rotativo e mesmo Tamanho, e usar o"
                        + " valor da coluna Preço da Malha (Informado e Contratipo).",
                "Multiplicar esse Preço da Malha por 0,90.",
                PASSO_PRECO_CONTRATIPO,
                "Somar os dois resultados.",
                "Incluir 10%: dividir o total por 0,90.");
        private static final String SEM_TINGIMENTO = "Se a combinação (Referência + Cor/Tecido Base + Tamanho) não"
                + " estiver em nenhuma das tabelas " + String.join(" e ", TabelasCilindro.TABELAS)
                + " da Tingimentos, o valor não é calculado e a célula mostra \"sem tingimento\".";

        static final CenarioRegra TINGIMENTOS_CILINDRO = new CenarioRegra("Cenário 2 - tecido na Tingimentos",
                "A Referência consta em alguma tabela da aba Tingimentos.",
                FORMULA_TINGIMENTOS, PASSOS_TINGIMENTOS, EXEMPLO_TINGIMENTOS, SEM_TINGIMENTO);

        static final CenarioRegra REFERENCIA_T_DIGITAL = new CenarioRegra(
                "Cenário 1 - referência começa com T (igual Sublimação)",
                "A Referência começa com a letra T. Vale mesmo que ela também conste na tabela "
                        + TABELA_TINGIMENTO_DIGITAL + ".",
                FORMULA_BANCO,
                List.of(PASSO_BUSCA_BASE, PASSO_PRECO_CONTRATIPO,
                        "Somar o resultado com o Preço Contratipo da base.",
                        "Incluir 10%: dividir o total por 0,90."),
                EXEMPLO_BANCO, SEM_BASE);

        static final CenarioRegra TABELA_331015_DIGITAL = new CenarioRegra(
                "Cenário 2 - referência na tabela " + TABELA_TINGIMENTO_DIGITAL + " (igual Cilindro cenário 2)",
                "A Referência não começa com T e consta na tabela " + TABELA_TINGIMENTO_DIGITAL
                        + " da aba Tingimentos.",
                FORMULA_TINGIMENTOS, PASSOS_TINGIMENTOS, EXEMPLO_TINGIMENTOS, SEM_TINGIMENTO);

        static final CenarioRegra TABELA_331015_ROT_QUADRO = new CenarioRegra(
                "Cenário 2 - referência na tabela " + TABELA_TINGIMENTO_DIGITAL + " (preço só da "
                        + TABELA_TINGIMENTO_DIGITAL + ")",
                "A Referência não começa com T e consta na tabela " + TABELA_TINGIMENTO_DIGITAL
                        + " da aba Tingimentos.",
                FORMULA_TINGIMENTOS,
                List.of("Buscar na aba Tingimentos, somente na tabela " + TABELA_TINGIMENTO_DIGITAL
                                + " (nunca na " + TabelasCilindro.TABELAS.get(0) + "), a linha com a mesma"
                                + " Referência, Cor igual ao Tecido Base do rotativo e mesmo Tamanho, e usar o valor"
                                + " da coluna Preço da Malha (Informado e Contratipo).",
                        "Multiplicar esse Preço da Malha por 0,90.",
                        PASSO_PRECO_CONTRATIPO,
                        "Somar os dois resultados.",
                        "Incluir 10%: dividir o total por 0,90."),
                EXEMPLO_TINGIMENTOS,
                "Se a combinação (Referência + Cor/Tecido Base + Tamanho) não estiver na tabela "
                        + TABELA_TINGIMENTO_DIGITAL + " da Tingimentos, o valor não é calculado e a célula mostra"
                        + " \"sem tingimento\".");

        static final CenarioRegra FORA_DA_REGRA_DIGITAL = new CenarioRegra("Fora dos cenários",
                "A Referência não começa com T e não consta na tabela " + TABELA_TINGIMENTO_DIGITAL
                        + " da Tingimentos.",
                "Sem cálculo", List.of("O valor não é calculado; a célula mostra \"fora da regra\"."),
                "-", "Revise a Referência / Técnica da linha, ou peça a definição de um novo cenário.");

        private Cenarios() {
        }
    }
}
