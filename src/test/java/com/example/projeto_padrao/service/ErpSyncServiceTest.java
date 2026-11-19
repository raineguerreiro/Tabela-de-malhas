package com.example.projeto_padrao.service;

import com.example.projeto_padrao.domain.ErpConsultaConfig;
import com.example.projeto_padrao.domain.Fornecedor;
import com.example.projeto_padrao.domain.Malha;
import com.example.projeto_padrao.domain.Servico;
import com.example.projeto_padrao.repository.CustoServicoRepository;
import com.example.projeto_padrao.repository.FornecedorRepository;
import com.example.projeto_padrao.repository.MalhaRepository;
import com.example.projeto_padrao.repository.ServicoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Simula o "banco do ERP" com um JdbcTemplate mockado, ja que ainda nao ha credenciais reais.
 * Valida que a consulta configurada e mapeada corretamente para Malha/Servico/Fornecedor/Custo.
 */
@ExtendWith(MockitoExtension.class)
class ErpSyncServiceTest {

    @Mock
    private MalhaRepository malhaRepository;

    @Mock
    private ServicoRepository servicoRepository;

    @Mock
    private FornecedorRepository fornecedorRepository;

    @Mock
    private CustoServicoRepository custoServicoRepository;

    @Mock
    private JdbcTemplate erpJdbcTemplate;

    @Test
    void mapeiaLinhasDaConsultaErpParaCustoServico() {
        ErpSyncService erpSyncService = new ErpSyncService(
                malhaRepository, servicoRepository, fornecedorRepository, custoServicoRepository);
        ReflectionTestUtils.setField(erpSyncService, "erpJdbcTemplate", erpJdbcTemplate);

        ErpConsultaConfig config = new ErpConsultaConfig();
        config.setNome("Custos de producao");
        config.setSqlConsulta("SELECT * FROM tabela_erp");
        config.setColunaMalhaCodigo("cod_malha");
        config.setColunaServicoNome("servico");
        config.setColunaFornecedorNome("fornecedor");
        config.setColunaValorUnitario("valor");
        config.setColunaQuantidade("qtd");

        Map<String, Object> linha = Map.of(
                "cod_malha", "M001",
                "servico", "Tecelagem",
                "fornecedor", "Fornecedor XPTO",
                "valor", new BigDecimal("12.34"),
                "qtd", new BigDecimal("50"));

        when(erpJdbcTemplate.queryForList(anyString())).thenReturn(List.of(linha));
        when(malhaRepository.findByCodigoIgnoreCase("M001")).thenReturn(Optional.empty());
        when(malhaRepository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(inv -> {
            Malha m = inv.getArgument(0);
            m.setId(1L);
            return m;
        });
        when(servicoRepository.findByNomeIgnoreCase("Tecelagem")).thenReturn(Optional.empty());
        when(servicoRepository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(inv -> {
            Servico s = inv.getArgument(0);
            s.setId(2L);
            return s;
        });
        when(fornecedorRepository.findByNomeIgnoreCase("Fornecedor XPTO")).thenReturn(Optional.empty());
        when(fornecedorRepository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(inv -> {
            Fornecedor f = inv.getArgument(0);
            f.setId(3L);
            return f;
        });
        when(custoServicoRepository.findByMalhaIdAndServicoIdAndFornecedorIdAndDataReferencia(
                org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.eq(2L),
                org.mockito.ArgumentMatchers.eq(3L), org.mockito.ArgumentMatchers.any()))
                .thenReturn(Optional.empty());

        int importados = erpSyncService.sincronizar(config);

        assertThat(importados).isEqualTo(1);
        verify(custoServicoRepository).save(org.mockito.ArgumentMatchers.argThat(custo ->
                custo.getValorUnitario().compareTo(new BigDecimal("12.34")) == 0
                        && custo.getQuantidade().compareTo(new BigDecimal("50")) == 0
                        && custo.getMalha().getCodigo().equals("M001")));
    }

    @Test
    void lancaErroQuandoDatasourceDoErpNaoEstaConfigurado() {
        ErpSyncService erpSyncService = new ErpSyncService(
                malhaRepository, servicoRepository, fornecedorRepository, custoServicoRepository);

        ErpConsultaConfig config = new ErpConsultaConfig();
        config.setSqlConsulta("SELECT * FROM tabela_erp");

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> erpSyncService.sincronizar(config))
                .isInstanceOf(IllegalStateException.class);
    }
}
