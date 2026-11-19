package com.example.projeto_padrao.service;

import com.example.projeto_padrao.domain.TabelaGeralItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Agrupa as linhas da Tabela Geral por malha, na mesma ordem original: uma malha pode ocupar
 * mais de uma linha fisica na planilha quando mistura mais de um tipo de fio. O criterio de
 * fronteira de bloco e' a cor de preenchimento da linha (TabelaGeralItem.corLinha, capturada na
 * importacao): linhas com a mesma cor da linha anterior sao continuacao do mesmo bloco, cor
 * diferente comeca um bloco novo - e' o mesmo criterio visual que o usuario usa na planilha
 * origem para separar uma malha da outra, mais confiavel que o texto (que pode ter erros de
 * digitacao, como um codigo repetido por engano numa linha de continuacao). Usado tanto pela tela
 * Tabela Geral (para pintar cada bloco de malha com uma cor e uma linha divisora) quanto pela tela
 * Malhas (que resume cada grupo em um bloco por malha).
 */
public final class TabelaGeralAgrupador {

    private TabelaGeralAgrupador() {
    }

    public record LinhaAgrupada(TabelaGeralItem item, Integer grupo, boolean inicioDeGrupo) {
    }

    public static List<LinhaAgrupada> agrupar(List<TabelaGeralItem> itens) {
        List<LinhaAgrupada> resultado = new ArrayList<>();
        int grupoAtual = -1;
        boolean emGrupo = false;
        Short corAnterior = null;

        for (TabelaGeralItem item : itens) {
            if (item.isTituloSecao()) {
                emGrupo = false;
                corAnterior = null;
                resultado.add(new LinhaAgrupada(item, null, false));
                continue;
            }

            Short corAtual = item.getCorLinha();
            boolean iniciaNovoGrupo;
            if (corAtual != null) {
                // Criterio principal: cor de preenchimento diferente da linha anterior = bloco novo.
                iniciaNovoGrupo = !emGrupo || !corAtual.equals(corAnterior);
            } else {
                // Fallback para dados legados (ainda nao reimportados apos esta mudanca, sem cor
                // capturada): volta ao criterio antigo, codigo preenchido = bloco novo.
                iniciaNovoGrupo = !emGrupo || vazioParaNull(item.getCodigo()) != null;
            }

            if (iniciaNovoGrupo) {
                grupoAtual++;
                emGrupo = true;
            }
            corAnterior = corAtual;

            resultado.add(new LinhaAgrupada(item, grupoAtual, iniciaNovoGrupo));
        }

        return resultado;
    }

    private static String vazioParaNull(String valor) {
        if (valor == null) {
            return null;
        }
        String tratado = valor.trim();
        return tratado.isEmpty() ? null : tratado;
    }
}
