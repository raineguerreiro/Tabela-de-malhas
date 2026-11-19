package com.example.projeto_padrao.service;

import com.example.projeto_padrao.domain.CustoServico;
import com.example.projeto_padrao.domain.Fio;
import com.example.projeto_padrao.domain.PrecoBaseTecido;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MemoriaCalculoTest {

    @Test
    void formataReaisComDuasAQuatroCasas() {
        assertThat(MemoriaCalculo.reais(new BigDecimal("2"))).isEqualTo("2,00");
        assertThat(MemoriaCalculo.reais(new BigDecimal("9.175"))).isEqualTo("9,175");
        assertThat(MemoriaCalculo.reais(new BigDecimal("1234.5"))).isEqualTo("1.234,50");
    }

    @Test
    void formataPercentual() {
        assertThat(MemoriaCalculo.percentual(new BigDecimal("0.95"))).isEqualTo("95%");
        assertThat(MemoriaCalculo.percentual(new BigDecimal("0.975"))).isEqualTo("97,5%");
    }

    @Test
    void memoriaDoCustoContratipoDoFio() {
        Fio fio = new Fio();
        assertThat(fio.getMemoriaCustoContratipo()).isEqualTo("Fio sem Custo Informado cadastrado.");

        fio.setCustoInformado(new BigDecimal("10"));
        assertThat(fio.getMemoriaCustoContratipo()).isEqualTo("Custo Informado R$ 10,00 × 0,9175 = R$ 9,175");
    }

    @Test
    void memoriaDoPrecoContratipoDoBancoDeDados() {
        PrecoBaseTecido preco = new PrecoBaseTecido();
        assertThat(preco.getMemoriaPrecoContratipo()).isNull();

        preco.setPreco(new BigDecimal("20"));
        assertThat(preco.getMemoriaPrecoContratipo()).isEqualTo("Preço R$ 20,00 × 0,9175 = R$ 18,35");
    }

    @Test
    void memoriaDoTotalDoCusto() {
        CustoServico custo = new CustoServico();
        custo.setValorUnitario(new BigDecimal("2.5"));
        custo.setQuantidade(new BigDecimal("4"));

        assertThat(custo.getMemoriaValorTotal()).isEqualTo("R$ 2,50 × 4 = R$ 10,00");
    }
}
