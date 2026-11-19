package com.example.projeto_padrao.service;

import com.example.projeto_padrao.domain.Fio;
import com.example.projeto_padrao.domain.Rotativo;
import com.example.projeto_padrao.domain.Tecelagem;
import com.example.projeto_padrao.domain.Tingimento;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Gera os arquivos .xlsx baixados pelos botoes "Baixar como Excel" de cada aba de /malhas, a
 * partir do mesmo {@link DadosTela} usado para renderizar a pagina - garante que o Excel sempre
 * reflete exatamente o que esta na tela (mesmos calculos, mesmos filtros ja aplicados).
 */
@Service
public class ExcelExportService {

    public byte[] exportarMalhas(DadosTela dados) {
        return gerarWorkbook(sheet -> {
            escreverCabecalho(sheet, 0, "Codigo", "Malha", "Composicao", "Rendimento", "Participacao do Fio", "Fio",
                    "Custo do Fio", "Tecelagem", "Preco Malha Crua");
            int linha = 1;
            for (MalhaResumo malha : dados.malhas()) {
                BigDecimal tecelagem = dados.tecelagemValorPorMalha().get(malha.codigo());
                BigDecimal precoMalhaCrua = dados.precoMalhaCruaPorMalha().get(malha.codigo());
                for (MalhaResumo.FioParticipacao componente : malha.fios()) {
                    Fio fioCadastrado = componente.fio() == null ? null : dados.fiosPorAbreviado().get(componente.fio());
                    BigDecimal custoFio = fioCadastrado != null ? fioCadastrado.getCustoContratipo() : null;
                    Row row = sheet.createRow(linha++);
                    escreverTexto(row, 0, malha.codigo());
                    escreverTexto(row, 1, malha.nome());
                    escreverTexto(row, 2, malha.composicao());
                    escreverTexto(row, 3, malha.rendimento());
                    escreverTexto(row, 4, componente.participacao());
                    escreverTexto(row, 5, componente.fio());
                    escreverNumero(row, 6, custoFio);
                    escreverNumero(row, 7, tecelagem);
                    escreverNumero(row, 8, precoMalhaCrua);
                }
            }
        });
    }

    public byte[] exportarFios(DadosTela dados) {
        return gerarWorkbook(sheet -> {
            escreverCabecalho(sheet, 0, "Fio (abreviacao)", "Codigo (ERP)", "Descricao", "Custo Informado",
                    "Custo Contratipo");
            int linha = 1;
            for (String abreviacao : dados.fiosAbreviados()) {
                Fio fio = dados.fiosPorAbreviado().get(abreviacao);
                Row row = sheet.createRow(linha++);
                escreverTexto(row, 0, abreviacao);
                escreverTexto(row, 1, fio != null ? fio.getCodigo() : null);
                escreverTexto(row, 2, fio != null ? fio.getDescricao() : null);
                escreverNumero(row, 3, fio != null ? fio.getCustoInformado() : null);
                escreverNumero(row, 4, fio != null ? fio.getCustoContratipo() : null);
            }
        });
    }

    public byte[] exportarTingimentos(DadosTela dados) {
        return exportarTingimentoOuRotativo(dados.tingimentos(), Tingimento::getId, Tingimento::getReferencia,
                Tingimento::getTamanho, Tingimento::getCorCodigo, Tingimento::getDescricao, Tingimento::getCorNome,
                Tingimento::getPreco, Tingimento::getTabela, dados.precoMalhaInformadoContratipoPorTingimento());
    }

    public byte[] exportarRotativos(DadosTela dados) {
        return exportarTingimentoOuRotativo(dados.rotativos(), Rotativo::getId, Rotativo::getReferencia,
                Rotativo::getTamanho, Rotativo::getCorCodigo, Rotativo::getDescricao, Rotativo::getCorNome,
                Rotativo::getPreco, Rotativo::getTabela, dados.precoMalhaInformadoContratipoPorRotativo());
    }

    private <T> byte[] exportarTingimentoOuRotativo(List<T> linhas, java.util.function.Function<T, Long> id,
            java.util.function.Function<T, String> referencia, java.util.function.Function<T, String> tamanho,
            java.util.function.Function<T, String> cor, java.util.function.Function<T, String> descricao,
            java.util.function.Function<T, String> nomeCor, java.util.function.Function<T, BigDecimal> preco,
            java.util.function.Function<T, String> tabela, Map<Long, BigDecimal> precoMalha) {
        return gerarWorkbook(sheet -> {
            escreverCabecalho(sheet, 0, "Referencia", "Tamanho", "Cor", "Descricao", "Nomecor", "Preco", "Tabela",
                    "Preco da Malha (Informado e Contratipo)");
            int linha = 1;
            for (T item : linhas) {
                Row row = sheet.createRow(linha++);
                escreverTexto(row, 0, referencia.apply(item));
                escreverTexto(row, 1, tamanho.apply(item));
                escreverTexto(row, 2, cor.apply(item));
                escreverTexto(row, 3, descricao.apply(item));
                escreverTexto(row, 4, nomeCor.apply(item));
                escreverNumero(row, 5, preco.apply(item));
                escreverTexto(row, 6, tabela.apply(item));
                escreverNumero(row, 7, precoMalha.get(id.apply(item)));
            }
        });
    }

    public byte[] exportarTecelagem(List<Tecelagem> tecelagens, String fornecedor1Header, String fornecedor2Header) {
        return gerarWorkbook(sheet -> {
            escreverCabecalho(sheet, 0, "Artigo", "Codigo", "Tear", fornecedor1Header, fornecedor2Header);
            int linha = 1;
            for (Tecelagem tecelagem : tecelagens) {
                Row row = sheet.createRow(linha++);
                escreverTexto(row, 0, tecelagem.getArtigo());
                escreverTexto(row, 1, tecelagem.getCodigo());
                escreverTexto(row, 2, tecelagem.getTear());
                escreverNumero(row, 3, tecelagem.getCustoFornecedor1());
                escreverNumero(row, 4, tecelagem.getCustoFornecedor2());
            }
        });
    }

    /**
     * Exporta uma tabela exatamente como esta na tela (botoes "Baixar como Excel" genericos, ver
     * {@code fragments/layout.html}): o navegador manda os textos das celulas na ordem exibida
     * (ja com a ordenacao escolhida pelo usuario). Celulas marcadas como numericas (colunas
     * alinhadas a direita) viram numero no Excel quando o texto e' um numero pt-BR ("R$ 1.234,56");
     * as demais - e avisos como "sem preco base" - ficam como texto.
     */
    public byte[] exportarTabela(TabelaExportada tabela) {
        return gerarWorkbook(sheet -> {
            escreverCabecalho(sheet, 0, tabela.cabecalhos().toArray(String[]::new));
            int linha = 1;
            for (List<TabelaExportada.Celula> celulas : tabela.linhas()) {
                Row row = sheet.createRow(linha++);
                for (int c = 0; c < celulas.size(); c++) {
                    TabelaExportada.Celula celula = celulas.get(c);
                    BigDecimal numero = celula.numerica() ? numeroPtBr(celula.texto()) : null;
                    if (numero != null) {
                        escreverNumero(row, c, numero);
                    } else {
                        escreverTexto(row, c, celula.texto() == null || celula.texto().isEmpty() ? null : celula.texto());
                    }
                }
            }
            ajustarLarguras(sheet, tabela.cabecalhos().size());
        });
    }

    /** "R$ 1.234,56" / "1234,5" / "-2" -> numero; qualquer outra coisa -> null. */
    static BigDecimal numeroPtBr(String texto) {
        if (texto == null) {
            return null;
        }
        String limpo = texto.replace("R$", "").replace(' ', ' ').trim();
        if (!limpo.matches("-?(\\d{1,3}(\\.\\d{3})+|\\d+)(,\\d+)?")) {
            return null;
        }
        return new BigDecimal(limpo.replace(".", "").replace(',', '.'));
    }

    private void ajustarLarguras(XSSFSheet sheet, int colunas) {
        for (int c = 0; c < colunas; c++) {
            int maior = 8;
            for (Row row : sheet) {
                Cell cell = row.getCell(c);
                if (cell != null) {
                    maior = Math.max(maior, cell.toString().length());
                }
            }
            sheet.setColumnWidth(c, Math.min(maior + 2, 60) * 256);
        }
    }

    private byte[] gerarWorkbook(java.util.function.Consumer<XSSFSheet> preenchimento) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream saida = new ByteArrayOutputStream()) {
            XSSFSheet sheet = workbook.createSheet("Dados");
            preenchimento.accept(sheet);
            sheet.createFreezePane(0, 1);
            workbook.write(saida);
            return saida.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao gerar planilha de exportacao", e);
        }
    }

    private void escreverCabecalho(XSSFSheet sheet, int indiceLinha, String... titulos) {
        Row row = sheet.createRow(indiceLinha);
        CellStyle negrito = sheet.getWorkbook().createCellStyle();
        Font fonte = sheet.getWorkbook().createFont();
        fonte.setBold(true);
        negrito.setFont(fonte);
        for (int c = 0; c < titulos.length; c++) {
            Cell cell = row.createCell(c);
            cell.setCellValue(titulos[c]);
            cell.setCellStyle(negrito);
        }
    }

    private void escreverTexto(Row row, int coluna, String valor) {
        Cell cell = row.createCell(coluna);
        if (valor != null) {
            cell.setCellValue(valor);
        }
    }

    private void escreverNumero(Row row, int coluna, BigDecimal valor) {
        Cell cell = row.createCell(coluna);
        if (valor != null) {
            cell.setCellValue(valor.doubleValue());
        }
    }
}
