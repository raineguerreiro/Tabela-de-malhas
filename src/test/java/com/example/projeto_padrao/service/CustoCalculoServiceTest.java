package com.example.projeto_padrao.service;

import com.example.projeto_padrao.domain.CustoServico;
import com.example.projeto_padrao.domain.Fornecedor;
import com.example.projeto_padrao.domain.Malha;
import com.example.projeto_padrao.domain.Servico;
import com.example.projeto_padrao.repository.CustoServicoRepository;
import com.example.projeto_padrao.repository.MalhaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustoCalculoServiceTest {

    @Mock
    private MalhaRepository malhaRepository;

    @Mock
    private CustoServicoRepository custoServicoRepository;

    @InjectMocks
    private CustoCalculoService custoCalculoService;

    @Test
    void somaOsCustosDeTodasAsEtapasDaMalha() {
        Malha malha = new Malha();
        malha.setId(1L);
        malha.setCodigo("M001");

        Servico tecelagem = new Servico();
        tecelagem.setNome("Tecelagem");
        Servico tingimento = new Servico();
        tingimento.setNome("Tingimento e Acabamento");

        Fornecedor fornecedorA = new Fornecedor();
        fornecedorA.setNome("Fornecedor A");
        Fornecedor fornecedorB = new Fornecedor();
        fornecedorB.setNome("Fornecedor B");

        CustoServico custo1 = new CustoServico();
        custo1.setMalha(malha);
        custo1.setServico(tecelagem);
        custo1.setFornecedor(fornecedorA);
        custo1.setValorUnitario(new BigDecimal("10.00"));
        custo1.setQuantidade(new BigDecimal("100"));

        CustoServico custo2 = new CustoServico();
        custo2.setMalha(malha);
        custo2.setServico(tingimento);
        custo2.setFornecedor(fornecedorB);
        custo2.setValorUnitario(new BigDecimal("5.50"));
        custo2.setQuantidade(new BigDecimal("100"));

        when(malhaRepository.findById(1L)).thenReturn(Optional.of(malha));
        when(custoServicoRepository.findByMalhaId(1L)).thenReturn(List.of(custo1, custo2));

        CustoTotalMalha resultado = custoCalculoService.calcularCustoTotal(1L);

        assertThat(resultado.malhaCodigo()).isEqualTo("M001");
        assertThat(resultado.total()).isEqualByComparingTo("1550.00");
        assertThat(resultado.etapas()).hasSize(2);
    }
}
