package com.example.projeto_padrao.service;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExcelExportServiceTest {

    @Test
    void leNumerosNoFormatoPtBr() {
        assertThat(ExcelExportService.numeroPtBr("R$ 1.234,56")).isEqualByComparingTo("1234.56");
        assertThat(ExcelExportService.numeroPtBr("20,3889")).isEqualByComparingTo("20.3889");
        assertThat(ExcelExportService.numeroPtBr("-2")).isEqualByComparingTo("-2");
        // Codigos e avisos continuam texto.
        assertThat(ExcelExportService.numeroPtBr("33.11.15")).isNull();
        assertThat(ExcelExportService.numeroPtBr("sem preço base")).isNull();
        assertThat(ExcelExportService.numeroPtBr("")).isNull();
    }

    @Test
    void exportaTabelaDaTelaComNumerosETextos() throws Exception {
        TabelaExportada tabela = new TabelaExportada("rotativos", List.of("Referencia", "Tabela", "Preco"), List.of(
                List.of(celula("REF1", false), celula("33.10.15", false), celula("R$ 20,39", true)),
                List.of(celula("REF2", false), celula("33.11.15", false), celula("sem preço base", true)),
                List.of(celula("TITULO", false))));

        byte[] arquivo = new ExcelExportService().exportarTabela(tabela);

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(arquivo))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertThat(sheet.getRow(0).getCell(2).getStringCellValue()).isEqualTo("Preco");
            assertThat(sheet.getRow(1).getCell(1).getStringCellValue()).isEqualTo("33.10.15");
            assertThat(sheet.getRow(1).getCell(2).getCellType()).isEqualTo(CellType.NUMERIC);
            assertThat(BigDecimal.valueOf(sheet.getRow(1).getCell(2).getNumericCellValue())).isEqualByComparingTo("20.39");
            assertThat(sheet.getRow(2).getCell(2).getStringCellValue()).isEqualTo("sem preço base");
            assertThat(sheet.getRow(3).getCell(0).getStringCellValue()).isEqualTo("TITULO");
        }
    }

    private static TabelaExportada.Celula celula(String texto, boolean numerica) {
        return new TabelaExportada.Celula(texto, numerica);
    }
}
