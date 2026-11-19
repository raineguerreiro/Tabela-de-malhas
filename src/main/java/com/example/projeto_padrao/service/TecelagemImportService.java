package com.example.projeto_padrao.service;

import com.example.projeto_padrao.domain.Tecelagem;
import com.example.projeto_padrao.repository.TecelagemRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Importa a planilha .xlsx ou .csv (ver {@link PlanilhaUpload}) de custos de tecelagem (upload manual, ver
 * web.TecelagemWebController#importar) para {@link Tecelagem}.
 *
 * <p>Layout real do arquivo (cabecalho em varias linhas fixas, nao uma linha so - por isso as
 * colunas sao lidas por posicao, nao por nome):
 * <ul>
 *   <li>linha indice 1, colunas E/F: vigencia de cada fornecedor (ex.: "a partir de julho");</li>
 *   <li>linha indice 2, colunas E/F: nome de cada fornecedor (ex.: "Wilbert"/"Maggior");</li>
 *   <li>linha indice 3: cabecalho das colunas de dado (Artigo/Codigo/Tear/Custo);</li>
 *   <li>a partir da linha indice 4: dados - coluna B=Artigo, C=Codigo, D=Tear, E=custo do
 *       fornecedor 1, F=custo do fornecedor 2 (cada linha normalmente preenche so uma das duas,
 *       mas o parser aceita as duas preenchidas).</li>
 * </ul>
 *
 * <p>Cada linha de dado faz upsert por (artigo, codigo, tear), permitindo reenviar a planilha
 * para atualizar os custos sem duplicar registros.
 */
@Service
public class TecelagemImportService {

    private static final int LINHA_VIGENCIA = 1;
    private static final int LINHA_FORNECEDOR = 2;
    private static final int PRIMEIRA_LINHA_DE_DADOS = 4;
    private static final int COLUNA_ARTIGO = 1;
    private static final int COLUNA_CODIGO = 2;
    private static final int COLUNA_TEAR = 3;
    private static final int COLUNA_CUSTO_FORNECEDOR1 = 4;
    private static final int COLUNA_CUSTO_FORNECEDOR2 = 5;

    private final TecelagemRepository repository;

    public TecelagemImportService(TecelagemRepository repository) {
        this.repository = repository;
    }

    /** Le o arquivo e grava/atualiza as linhas em Tecelagem. Retorna quantas linhas de dado foram processadas. */
    public int importar(MultipartFile arquivo) throws IOException {
        DataFormatter formatter = new DataFormatter();
        int processadas = 0;
        int ordemAtual = 0;

        try (Workbook workbook = PlanilhaUpload.abrir(arquivo)) {
            Sheet sheet = workbook.getSheetAt(0);

            String fornecedor1Nome = valorTexto(formatter, celula(sheet, LINHA_FORNECEDOR, COLUNA_CUSTO_FORNECEDOR1));
            String fornecedor1Vigencia = valorTexto(formatter, celula(sheet, LINHA_VIGENCIA, COLUNA_CUSTO_FORNECEDOR1));
            String fornecedor2Nome = valorTexto(formatter, celula(sheet, LINHA_FORNECEDOR, COLUNA_CUSTO_FORNECEDOR2));
            String fornecedor2Vigencia = valorTexto(formatter, celula(sheet, LINHA_VIGENCIA, COLUNA_CUSTO_FORNECEDOR2));

            for (int r = PRIMEIRA_LINHA_DE_DADOS; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (linhaEmBranco(row)) {
                    continue;
                }

                String artigo = valorTexto(formatter, row.getCell(COLUNA_ARTIGO));
                String codigo = valorTexto(formatter, row.getCell(COLUNA_CODIGO));
                if (artigo == null || codigo == null) {
                    continue;
                }
                String tear = valorTexto(formatter, row.getCell(COLUNA_TEAR));

                Tecelagem tecelagem = repository.findByArtigoAndCodigoAndTear(artigo, codigo, tear)
                        .orElseGet(Tecelagem::new);
                tecelagem.setArtigo(artigo);
                tecelagem.setCodigo(codigo);
                tecelagem.setTear(tear);
                tecelagem.setCustoFornecedor1(valorNumerico(row.getCell(COLUNA_CUSTO_FORNECEDOR1)));
                tecelagem.setCustoFornecedor2(valorNumerico(row.getCell(COLUNA_CUSTO_FORNECEDOR2)));
                tecelagem.setFornecedor1Nome(fornecedor1Nome);
                tecelagem.setFornecedor1Vigencia(fornecedor1Vigencia);
                tecelagem.setFornecedor2Nome(fornecedor2Nome);
                tecelagem.setFornecedor2Vigencia(fornecedor2Vigencia);
                tecelagem.setOrdem(ordemAtual++);
                repository.save(tecelagem);
                processadas++;
            }
        }

        return processadas;
    }

    private Cell celula(Sheet sheet, int linha, int coluna) {
        Row row = sheet.getRow(linha);
        return row == null ? null : row.getCell(coluna);
    }

    private boolean linhaEmBranco(Row row) {
        if (row == null) {
            return true;
        }
        for (int c = 0; c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private String valorTexto(DataFormatter formatter, Cell cell) {
        if (cell == null) {
            return null;
        }
        String texto = formatter.formatCellValue(cell).trim();
        return texto.isEmpty() ? null : texto;
    }

    private BigDecimal valorNumerico(Cell cell) {
        if (cell == null) {
            return null;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        }
        if (cell.getCellType() == CellType.STRING) {
            String texto = cell.getStringCellValue().trim();
            if (texto.isEmpty()) {
                return null;
            }
            if (texto.contains(",")) {
                texto = texto.replace(".", "").replace(",", ".");
            }
            try {
                return new BigDecimal(texto);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
