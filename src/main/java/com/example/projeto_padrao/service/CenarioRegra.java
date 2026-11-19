package com.example.projeto_padrao.service;

import java.util.List;

/**
 * Descricao de um cenario de uma {@link RegraPrecoRotativo}, exibida na sub-aba "Regras de preco":
 * quando o cenario se aplica, a formula, o passo a passo, um exemplo numerico e o que acontece
 * quando falta algum dado.
 */
public record CenarioRegra(String titulo, String quando, String formula, List<String> passos, String exemplo,
        String seFaltar) {
}
