package com.example.projeto_padrao.service;

import java.math.BigDecimal;
import java.util.List;

public record CustoTotalMalha(Long malhaId, String malhaCodigo, BigDecimal total, List<CustoPorEtapa> etapas) {

    public record CustoPorEtapa(String servico, String fornecedor, BigDecimal valor) {
    }
}
