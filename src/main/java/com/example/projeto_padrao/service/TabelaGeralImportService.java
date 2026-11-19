package com.example.projeto_padrao.service;

import com.example.projeto_padrao.domain.TabelaGeralItem;
import com.example.projeto_padrao.repository.TabelaGeralItemRepository;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Importa, uma unica vez (na subida da aplicacao, se a tabela estiver vazia), a planilha
 * "import/TABELA GERAL .xls" para {@link TabelaGeralItem}. Cada linha da planilha (a partir da
 * primeira linha de dados, apos o cabecalho de 3 niveis) vira um registro, com os valores
 * mantidos exatamente como exibidos no Excel (via {@link DataFormatter}).
 */
@Service
public class TabelaGeralImportService implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(TabelaGeralImportService.class);

    private static final String CAMINHO_ARQUIVO = "import/TABELA GERAL .xls";
    private static final int PRIMEIRA_LINHA_DE_DADOS = 6;
    private static final int COLUNA_FIO = 6;

    /** Padronizacao de nomes de fio pedida pelo usuario - aplicada uma vez, na importacao. */
    private static final Map<String, String> RENOMEACOES_FIO = Map.of(
            "30 P", "30 PEN",
            "30 CO", "30 PEN",
            "40 P", "40 PEN"
    );

    private final TabelaGeralItemRepository repository;

    public TabelaGeralImportService(TabelaGeralItemRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        importar();
    }

    /**
     * Le a planilha e grava as linhas em {@link TabelaGeralItem}, do jeito que a tabela estiver no
     * momento (nao apaga nada sozinho). Chamado na subida (se a tabela estiver vazia) e tambem sob
     * demanda por {@code POST /tabela-geral/reimportar} - nesse segundo caso, quem aciona e'
     * responsavel por limpar a tabela antes, se quiser substituir os dados (ver
     * {@code web.TabelaGeralWebController}). Nunca mexe em nenhuma outra tabela (Fio, Malha,
     * Fornecedor, Servico, CustoServico) - so' em {@link TabelaGeralItem}.
     */
    public void importar() {
        File arquivo = new File(CAMINHO_ARQUIVO);
        if (!arquivo.exists()) {
            log.warn("Arquivo '{}' nao encontrado - aba 'Tabela Geral' ficara vazia ate o arquivo ser colocado la.",
                    CAMINHO_ARQUIVO);
            return;
        }

        List<TabelaGeralItem> itens = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (FileInputStream fis = new FileInputStream(arquivo);
                HSSFWorkbook workbook = new HSSFWorkbook(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            List<CellRangeAddress> regioesMescladas = sheet.getMergedRegions();
            int ordem = 0;
            for (int r = PRIMEIRA_LINHA_DE_DADOS; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || row.getLastCellNum() <= 0) {
                    continue;
                }

                // 1a passada: classifica a linha (titulo de secao x linha de dados) a partir do
                // conteudo bruto das celulas, sem considerar mesclagem - a classificacao nao pode
                // ser afetada pela deduplicacao feita na 2a passada.
                boolean temNumero = false;
                String primeiroTexto = null;
                for (int c = 0; c < 19; c++) {
                    Cell cell = row.getCell(c);
                    if (cell == null) {
                        continue;
                    }
                    if (cell.getCellType() == CellType.NUMERIC) {
                        temNumero = true;
                    }
                    if (primeiroTexto == null && cell.getCellType() == CellType.STRING) {
                        String texto = formatter.formatCellValue(cell).trim();
                        if (!texto.isEmpty()) {
                            primeiroTexto = texto;
                        }
                    }
                }

                // 2a passada: monta os valores a gravar. Uma celula que faz parte de uma regiao
                // mesclada so' entra na linha ancora (primeira linha da mesclagem) - nas linhas
                // seguintes da mesma mesclagem o valor fica em branco, para nao duplicar o que na
                // planilha e' um unico valor visual.
                String[] valores = new String[19];
                for (int c = 0; c < 19; c++) {
                    CellRangeAddress regiao = regiaoMesclada(regioesMescladas, r, c);
                    if (regiao != null && r != regiao.getFirstRow()) {
                        valores[c] = null;
                        continue;
                    }
                    Cell cell = row.getCell(c);
                    if (cell == null) {
                        continue;
                    }
                    String texto = formatter.formatCellValue(cell).trim();
                    if (c == COLUNA_FIO && RENOMEACOES_FIO.containsKey(texto)) {
                        texto = RENOMEACOES_FIO.get(texto);
                    }
                    valores[c] = texto.isEmpty() ? null : texto;
                }

                TabelaGeralItem item = new TabelaGeralItem();
                item.setOrdem(ordem++);

                if (!temNumero) {
                    item.setTituloSecao(true);
                    item.setTextoTituloSecao(primeiroTexto);
                } else {
                    Cell celulaMalha = row.getCell(0);
                    if (celulaMalha != null) {
                        item.setCorLinha(celulaMalha.getCellStyle().getFillForegroundColor());
                    }
                    item.setMalha(valores[0]);
                    item.setCodigo(valores[1]);
                    item.setRendimento(valores[2]);
                    item.setTearFinuraDiametroAgulhas(valores[3]);
                    item.setComposicaoDaMalha(valores[4]);
                    item.setParticipacaoDeCadaFio(valores[5]);
                    item.setFio(valores[6]);
                    item.setLfa(valores[7]);
                    item.setTamanhoDoPonto(valores[8]);
                    item.setFatorCobertura(valores[9]);
                    item.setFluxo(valores[10]);
                    item.setLarguraFinalAcabado(valores[11]);
                    item.setLarguraParaEncaixe(valores[12]);
                    item.setGramaturaPadrao(valores[13]);
                    item.setGramaturaLimites(valores[14]);
                    item.setEncolhimentoSecadoraC(valores[15]);
                    item.setEncolhimentoSecadoraL(valores[16]);
                    item.setEncolhimentoVaralC(valores[17]);
                    item.setEncolhimentoVaralL(valores[18]);
                }
                itens.add(item);
            }
        } catch (Exception e) {
            log.error("Falha ao importar '{}': {}", CAMINHO_ARQUIVO, e.getMessage(), e);
            return;
        }

        repository.saveAll(itens);
        log.info("Tabela Geral: {} linhas importadas de '{}'.", itens.size(), CAMINHO_ARQUIVO);
    }

    private CellRangeAddress regiaoMesclada(List<CellRangeAddress> regioes, int linha, int coluna) {
        for (CellRangeAddress regiao : regioes) {
            if (regiao.isInRange(linha, coluna)) {
                return regiao;
            }
        }
        return null;
    }
}
