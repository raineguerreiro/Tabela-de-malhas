package com.example.projeto_padrao.service;

import java.math.BigDecimal;

/**
 * Tudo que as regras de preco ({@link RegraPrecoRotativo}) precisam de uma linha de Rotativo, ja
 * buscado pelo controller: os dados da propria linha, o Preco Contratipo da base no Banco de dados
 * (null se nao cadastrado), se a referencia consta na aba Tingimentos e o Preco da Malha da linha
 * correspondente na Tingimentos (null se nenhuma bater), com a tabela onde ele foi achado, e se a
 * referencia consta na tabela {@link RegraPrecoRotativo#TABELA_TINGIMENTO_DIGITAL} da Tingimentos, com
 * o Preco da Malha da combinacao somente nessa tabela (usado pela Rot. Quadro; null se nao houver).
 */
public record DadosLinhaRotativo(String referencia, String tamanho, String tecidoBase, String tabela, String tecnica,
        BigDecimal precoRotativo, BigDecimal precoContratipoBase, boolean referenciaEmTingimentos,
        BigDecimal precoMalhaTingimento, String tabelaTingimento, boolean referenciaNaTabelaDigital,
        BigDecimal precoMalhaTabelaDigital) {
}
