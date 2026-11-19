package com.example.projeto_padrao.service;

import java.util.List;

/**
 * Uma tabela da tela enviada pelo navegador para virar .xlsx: titulos das colunas e, por linha,
 * o texto de cada celula como aparece na tela. {@code numerica} marca celulas de colunas de
 * valor (alinhadas a direita), que viram numero no Excel quando o texto for um numero.
 */
public record TabelaExportada(String arquivo, List<String> cabecalhos, List<List<Celula>> linhas) {

    public record Celula(String texto, boolean numerica) {
    }
}
