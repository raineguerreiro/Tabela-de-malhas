package com.example.projeto_padrao.service;

import com.example.projeto_padrao.domain.CustoServico;
import com.example.projeto_padrao.domain.ErpConsultaConfig;
import com.example.projeto_padrao.domain.Fornecedor;
import com.example.projeto_padrao.domain.Malha;
import com.example.projeto_padrao.domain.OrigemCusto;
import com.example.projeto_padrao.domain.Servico;
import com.example.projeto_padrao.repository.CustoServicoRepository;
import com.example.projeto_padrao.repository.FornecedorRepository;
import com.example.projeto_padrao.repository.MalhaRepository;
import com.example.projeto_padrao.repository.ServicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Executa a consulta configurada em {@link ErpConsultaConfig} contra o banco do ERP e traz os
 * custos para dentro da aplicacao (upsert em {@link CustoServico}, origem ERP_SYNC).
 */
@Service
public class ErpSyncService {

    private final MalhaRepository malhaRepository;
    private final ServicoRepository servicoRepository;
    private final FornecedorRepository fornecedorRepository;
    private final CustoServicoRepository custoServicoRepository;

    @Autowired(required = false)
    @Qualifier("erpJdbcTemplate")
    private JdbcTemplate erpJdbcTemplate;

    public ErpSyncService(MalhaRepository malhaRepository, ServicoRepository servicoRepository,
            FornecedorRepository fornecedorRepository, CustoServicoRepository custoServicoRepository) {
        this.malhaRepository = malhaRepository;
        this.servicoRepository = servicoRepository;
        this.fornecedorRepository = fornecedorRepository;
        this.custoServicoRepository = custoServicoRepository;
    }

    @Transactional
    public int sincronizar(ErpConsultaConfig config) {
        if (erpJdbcTemplate == null) {
            throw new IllegalStateException(
                    "Datasource do ERP nao configurado. Preencha erp.datasource.* em application.properties.");
        }

        List<Map<String, Object>> linhas = erpJdbcTemplate.queryForList(config.getSqlConsulta());
        int importados = 0;
        for (Map<String, Object> linha : linhas) {
            aplicarLinha(config, linha);
            importados++;
        }

        config.setUltimaExecucao(LocalDateTime.now());
        return importados;
    }

    private void aplicarLinha(ErpConsultaConfig config, Map<String, Object> linha) {
        String malhaCodigo = valorTexto(linha, config.getColunaMalhaCodigo());
        String servicoNome = valorTexto(linha, config.getColunaServicoNome());
        String fornecedorNome = valorTexto(linha, config.getColunaFornecedorNome());
        BigDecimal valorUnitario = valorDecimal(linha, config.getColunaValorUnitario());
        BigDecimal quantidade = config.getColunaQuantidade() != null
                ? valorDecimal(linha, config.getColunaQuantidade())
                : BigDecimal.ONE;
        String unidadeMedida = config.getColunaUnidadeMedida() != null
                ? valorTexto(linha, config.getColunaUnidadeMedida())
                : null;
        LocalDate dataReferencia = config.getColunaDataReferencia() != null
                ? valorData(linha, config.getColunaDataReferencia())
                : LocalDate.now();

        Malha malha = malhaRepository.findByCodigoIgnoreCase(malhaCodigo)
                .orElseGet(() -> {
                    Malha nova = new Malha();
                    nova.setCodigo(malhaCodigo);
                    nova.setNome(malhaCodigo);
                    return malhaRepository.save(nova);
                });

        Servico servico = servicoRepository.findByNomeIgnoreCase(servicoNome)
                .orElseGet(() -> {
                    Servico novo = new Servico();
                    novo.setNome(servicoNome);
                    return servicoRepository.save(novo);
                });

        Fornecedor fornecedor = fornecedorRepository.findByNomeIgnoreCase(fornecedorNome)
                .orElseGet(() -> {
                    Fornecedor novo = new Fornecedor();
                    novo.setNome(fornecedorNome);
                    return fornecedorRepository.save(novo);
                });

        CustoServico custo = custoServicoRepository
                .findByMalhaIdAndServicoIdAndFornecedorIdAndDataReferencia(
                        malha.getId(), servico.getId(), fornecedor.getId(), dataReferencia)
                .orElseGet(CustoServico::new);

        custo.setMalha(malha);
        custo.setServico(servico);
        custo.setFornecedor(fornecedor);
        custo.setValorUnitario(valorUnitario);
        custo.setQuantidade(quantidade);
        custo.setUnidadeMedida(unidadeMedida);
        custo.setDataReferencia(dataReferencia);
        custo.setOrigem(OrigemCusto.ERP_SYNC);
        custoServicoRepository.save(custo);
    }

    private String valorTexto(Map<String, Object> linha, String coluna) {
        Object valor = linha.get(coluna);
        return valor != null ? valor.toString() : null;
    }

    private BigDecimal valorDecimal(Map<String, Object> linha, String coluna) {
        Object valor = linha.get(coluna);
        if (valor == null) {
            return BigDecimal.ZERO;
        }
        if (valor instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        return new BigDecimal(valor.toString());
    }

    private LocalDate valorData(Map<String, Object> linha, String coluna) {
        Object valor = linha.get(coluna);
        if (valor == null) {
            return LocalDate.now();
        }
        if (valor instanceof LocalDate localDate) {
            return localDate;
        }
        if (valor instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        return LocalDate.parse(valor.toString());
    }
}
