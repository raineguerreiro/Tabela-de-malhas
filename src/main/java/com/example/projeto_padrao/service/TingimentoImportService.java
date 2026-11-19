package com.example.projeto_padrao.service;

import com.example.projeto_padrao.domain.Tingimento;
import com.example.projeto_padrao.repository.TingimentoRepository;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

/**
 * Importa uma planilha .xlsx ou .csv (ver {@link PlanilhaUpload}) de custos de tingimento (upload manual, ver
 * web.MalhaWebController#importarTingimentos) para {@link Tingimento}.
 *
 * <p>Layout flat, uma linha por combinacao: a linha 0 e' o cabecalho, com as colunas
 * REFERENCIA, TAMANHO, COR, DESCRICAO, NOMECOR, PRECO (ou PREÇO) e TABELA, em qualquer ordem -
 * as colunas sao casadas pelo nome do cabecalho (normalizado: maiusculo, sem acento), nao por
 * posicao fixa, para tolerar variacao de ordem/acentuacao entre arquivos. A partir da linha 1,
 * cada linha nao-vazia vira um upsert em Tingimento, chaveado por
 * (referencia, tamanho, corCodigo, tabela) - permite reenviar a planilha para atualizar precos
 * sem duplicar registros.
 *
 * <p>A coluna TABELA define o grupo da linha: tabela nova vira um bloco novo na tela (abaixo dos
 * existentes); tabela ja cadastrada recebe as linhas no proprio grupo. Linha que ja existia mantem
 * a {@code ordem} (fica na mesma posicao, so' os dados sao atualizados); linha nova recebe uma
 * ordem maior que todas as ja gravadas, entrando no fim do seu grupo.
 */
@Service
public class TingimentoImportService {

    private static final Map<String, String> ALIASES_COLUNA = Map.of(
            "REFERENCIA", "REFERENCIA",
            "TAMANHO", "TAMANHO",
            "COR", "COR",
            "DESCRICAO", "DESCRICAO",
            "NOMECOR", "NOMECOR",
            "PRECO", "PRECO",
            "TABELA", "TABELA"
    );

    private final TingimentoRepository repository;

    public TingimentoImportService(TingimentoRepository repository) {
        this.repository = repository;
    }

    /** Le o arquivo e grava/atualiza as linhas em Tingimento. Retorna quantas linhas de dado foram processadas. */
    public int importar(MultipartFile arquivo) throws IOException {
        DataFormatter formatter = new DataFormatter();
        int processadas = 0;
        int proximaOrdem = repository.findTopByOrderByOrdemDesc()
                .map(Tingimento::getOrdem)
                .map(ordem -> ordem + 1)
                .orElse(0);

        try (Workbook workbook = PlanilhaUpload.abrir(arquivo)) {
            Sheet sheet = workbook.getSheetAt(0);
            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();

            Row cabecalho = sheet.getRow(0);
            if (cabecalho == null) {
                throw new IllegalStateException("Planilha vazia - nao encontrei a linha de cabecalho.");
            }
            Map<String, Integer> colunas = casarColunas(formatter, cabecalho);
            for (String obrigatoria : new String[] {"REFERENCIA", "COR", "TABELA"}) {
                if (!colunas.containsKey(obrigatoria)) {
                    throw new IllegalStateException(
                            "Coluna obrigatoria '" + obrigatoria + "' nao encontrada no cabecalho da planilha.");
                }
            }

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (linhaEmBranco(row)) {
                    continue;
                }

                String referencia = valorTexto(formatter, celula(row, colunas, "REFERENCIA"));
                String corCodigo = valorTexto(formatter, celula(row, colunas, "COR"));
                String tabela = valorTexto(formatter, celula(row, colunas, "TABELA"));
                if (referencia == null || corCodigo == null || tabela == null) {
                    continue;
                }
                String tamanho = valorTexto(formatter, celula(row, colunas, "TAMANHO"));
                if (tamanho == null) {
                    tamanho = "";
                }

                Tingimento tingimento = repository
                        .findByReferenciaAndTamanhoAndCorCodigoAndTabela(referencia, tamanho, corCodigo, tabela)
                        .orElseGet(Tingimento::new);
                tingimento.setReferencia(referencia);
                tingimento.setTamanho(tamanho);
                tingimento.setCorCodigo(corCodigo);
                tingimento.setDescricao(valorTexto(formatter, celula(row, colunas, "DESCRICAO")));
                tingimento.setCorNome(valorTexto(formatter, celula(row, colunas, "NOMECOR")));
                tingimento.setPreco(valorNumerico(evaluator, celula(row, colunas, "PRECO")));
                tingimento.setTabela(tabela);
                if (tingimento.getId() == null || tingimento.getOrdem() == null) {
                    tingimento.setOrdem(proximaOrdem++);
                }
                repository.save(tingimento);
                processadas++;
            }
        }

        return processadas;
    }

    /** Casa cada coluna do cabecalho (nome normalizado) com seu indice, usando ALIASES_COLUNA. */
    private Map<String, Integer> casarColunas(DataFormatter formatter, Row cabecalho) {
        Map<String, Integer> colunas = new HashMap<>();
        for (int c = 0; c < cabecalho.getLastCellNum(); c++) {
            Cell cell = cabecalho.getCell(c);
            if (cell == null) {
                continue;
            }
            String nome = normalizar(formatter.formatCellValue(cell));
            String chave = ALIASES_COLUNA.get(nome);
            if (chave != null) {
                colunas.put(chave, c);
            }
        }
        return colunas;
    }

    /** Maiusculo, sem acento, sem espacos nas bordas - para casar cabecalhos com variacao de acentuacao. */
    private String normalizar(String texto) {
        String semAcento = Normalizer.normalize(texto.trim().toUpperCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return semAcento.replaceAll("\\s+", "");
    }

    private Cell celula(Row row, Map<String, Integer> colunas, String nomeColuna) {
        Integer indice = colunas.get(nomeColuna);
        return indice == null ? null : row.getCell(indice);
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

    private BigDecimal valorNumerico(FormulaEvaluator evaluator, Cell cell) {
        if (cell == null) {
            return null;
        }
        CellType tipo = cell.getCellType() == CellType.FORMULA ? evaluator.evaluateFormulaCell(cell)
                : cell.getCellType();
        if (tipo == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        }
        if (tipo == CellType.STRING) {
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
