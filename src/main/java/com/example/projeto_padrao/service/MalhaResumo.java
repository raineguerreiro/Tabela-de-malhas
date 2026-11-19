package com.example.projeto_padrao.service;

import java.util.List;

/**
 * Uma malha, com seus componentes de fio agrupados a partir das linhas da Tabela Geral que
 * pertencem a ela (uma malha pode ocupar mais de uma linha na planilha quando mistura mais de
 * um tipo de fio).
 */
public record MalhaResumo(String codigo, String nome, String composicao, String rendimento,
        List<FioParticipacao> fios, int corIndice) {

    public record FioParticipacao(String participacao, String fio) {
    }
}
