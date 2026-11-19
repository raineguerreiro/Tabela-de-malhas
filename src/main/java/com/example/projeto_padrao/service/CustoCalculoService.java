package com.example.projeto_padrao.service;

import com.example.projeto_padrao.domain.CustoServico;
import com.example.projeto_padrao.domain.Malha;
import com.example.projeto_padrao.repository.CustoServicoRepository;
import com.example.projeto_padrao.repository.MalhaRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CustoCalculoService {

    private final MalhaRepository malhaRepository;
    private final CustoServicoRepository custoServicoRepository;

    public CustoCalculoService(MalhaRepository malhaRepository, CustoServicoRepository custoServicoRepository) {
        this.malhaRepository = malhaRepository;
        this.custoServicoRepository = custoServicoRepository;
    }

    public CustoTotalMalha calcularCustoTotal(Long malhaId) {
        Malha malha = malhaRepository.findById(malhaId)
                .orElseThrow(() -> new NoSuchElementException("Malha nao encontrada: " + malhaId));

        List<CustoServico> custos = custoServicoRepository.findByMalhaId(malhaId);

        List<CustoTotalMalha.CustoPorEtapa> etapas = custos.stream()
                .map(c -> new CustoTotalMalha.CustoPorEtapa(
                        c.getServico().getNome(),
                        c.getFornecedor().getNome(),
                        c.getValorTotal()))
                .toList();

        BigDecimal total = custos.stream()
                .map(CustoServico::getValorTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CustoTotalMalha(malha.getId(), malha.getCodigo(), total, etapas);
    }
}
