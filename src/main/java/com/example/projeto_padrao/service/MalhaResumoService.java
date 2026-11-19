package com.example.projeto_padrao.service;

import com.example.projeto_padrao.domain.TabelaGeralItem;
import com.example.projeto_padrao.repository.TabelaGeralItemRepository;
import com.example.projeto_padrao.service.TabelaGeralAgrupador.LinhaAgrupada;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Agrupa as linhas da Tabela Geral em uma malha por bloco (via {@link TabelaGeralAgrupador}) e
 * completa a lista de fios de cada malha ate a quantidade maxima encontrada, para que todas as
 * malhas exibam o mesmo numero de linhas.
 */
@Service
public class MalhaResumoService {

    private final TabelaGeralItemRepository repository;

    public MalhaResumoService(TabelaGeralItemRepository repository) {
        this.repository = repository;
    }

    public List<MalhaResumo> listar() {
        List<Grupo> grupos = agrupar(repository.findAllByOrderByOrdemAsc());

        int maxLinhas = grupos.stream().mapToInt(g -> g.fios.size()).max().orElse(1);
        if (maxLinhas < 1) {
            maxLinhas = 1;
        }

        List<MalhaResumo> resultado = new ArrayList<>();
        for (Grupo grupo : grupos) {
            List<MalhaResumo.FioParticipacao> fios = new ArrayList<>(grupo.fios);
            while (fios.size() < maxLinhas) {
                fios.add(new MalhaResumo.FioParticipacao(null, null));
            }
            String nome = String.join(" ", grupo.nomes);
            String composicao = String.join(" + ", grupo.composicoes);
            resultado.add(new MalhaResumo(grupo.codigo, nome, composicao, grupo.rendimento, fios,
                    grupo.indice));
        }
        return resultado;
    }

    private List<Grupo> agrupar(List<TabelaGeralItem> itens) {
        List<Grupo> grupos = new ArrayList<>();
        Grupo atual = null;

        for (LinhaAgrupada linha : TabelaGeralAgrupador.agrupar(itens)) {
            if (linha.grupo() == null) {
                atual = null;
                continue;
            }

            TabelaGeralItem item = linha.item();
            if (linha.inicioDeGrupo()) {
                atual = new Grupo(linha.grupo(), item.getCodigo(), item.getRendimento());
                grupos.add(atual);
            }

            String nomeLinha = item.getMalha();
            if (nomeLinha != null && !nomeLinha.isBlank()) {
                atual.nomes.add(nomeLinha);
            }

            String composicaoLinha = item.getComposicaoDaMalha();
            if (composicaoLinha != null && !composicaoLinha.isBlank()) {
                atual.composicoes.add(composicaoLinha);
            }

            String participacao = item.getParticipacaoDeCadaFio();
            String fio = item.getFio();
            if (participacao == null && fio == null) {
                // Linha e' continuacao de uma celula mesclada (mesmo fio da linha anterior do
                // grupo) - nao gera um componente de fio novo.
                continue;
            }
            atual.fios.add(new MalhaResumo.FioParticipacao(participacao, fio));
        }

        return grupos;
    }

    private static final class Grupo {
        final int indice;
        final String codigo;
        final List<String> nomes = new ArrayList<>();
        final List<String> composicoes = new ArrayList<>();
        final String rendimento;
        final List<MalhaResumo.FioParticipacao> fios = new ArrayList<>();

        Grupo(int indice, String codigo, String rendimento) {
            this.indice = indice;
            this.codigo = codigo;
            this.rendimento = rendimento;
        }
    }
}
