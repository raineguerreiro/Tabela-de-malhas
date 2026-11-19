package com.example.projeto_padrao.service;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlanilhaUploadTest {

    private final DataFormatter formatter = new DataFormatter();

    @Test
    void csvComVirgulaEAspas() throws IOException {
        Workbook wb = abrirCsv("A,B,C\n1,\"x, y\",\"diz \"\"oi\"\"\"\n".getBytes(StandardCharsets.UTF_8));
        Sheet sheet = wb.getSheetAt(0);
        assertThat(texto(sheet, 0, 2)).isEqualTo("C");
        assertThat(texto(sheet, 1, 1)).isEqualTo("x, y");
        assertThat(texto(sheet, 1, 2)).isEqualTo("diz \"oi\"");
    }

    @Test
    void csvComPontoEVirgulaQuebraDeLinhaEntreAspasELinhaEmBranco() throws IOException {
        Workbook wb = abrirCsv("A;B\r\n\"linha1\nlinha2\";1,5\r\n\r\n;x\r\n".getBytes(StandardCharsets.UTF_8));
        Sheet sheet = wb.getSheetAt(0);
        assertThat(texto(sheet, 1, 0)).isEqualTo("linha1\nlinha2");
        assertThat(texto(sheet, 1, 1)).isEqualTo("1,5");
        assertThat(sheet.getRow(2)).isNull();
        Row ultima = sheet.getRow(3);
        assertThat(ultima.getCell(0)).isNull();
        assertThat(texto(sheet, 3, 1)).isEqualTo("x");
    }

    @Test
    void csvUtf8ComBom() throws IOException {
        byte[] corpo = "Preço;Descrição\n1;a\n".getBytes(StandardCharsets.UTF_8);
        byte[] comBom = new byte[corpo.length + 3];
        comBom[0] = (byte) 0xEF;
        comBom[1] = (byte) 0xBB;
        comBom[2] = (byte) 0xBF;
        System.arraycopy(corpo, 0, comBom, 3, corpo.length);

        Sheet sheet = abrirCsv(comBom).getSheetAt(0);
        assertThat(texto(sheet, 0, 0)).isEqualTo("Preço");
        assertThat(texto(sheet, 0, 1)).isEqualTo("Descrição");
    }

    @Test
    void csvWindows1252() throws IOException {
        Sheet sheet = abrirCsv("Preço;Descrição\n".getBytes(Charset.forName("windows-1252"))).getSheetAt(0);
        assertThat(texto(sheet, 0, 0)).isEqualTo("Preço");
        assertThat(texto(sheet, 0, 1)).isEqualTo("Descrição");
    }

    @Test
    void formatoNaoSuportado() {
        MockMultipartFile txt = new MockMultipartFile("arquivo", "dados.txt", "text/plain", new byte[] {1});
        assertThatThrownBy(() -> PlanilhaUpload.abrir(txt))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(".csv");
    }

    private Workbook abrirCsv(byte[] bytes) throws IOException {
        return PlanilhaUpload.abrir(new MockMultipartFile("arquivo", "dados.CSV", null, bytes));
    }

    private String texto(Sheet sheet, int linha, int coluna) {
        return formatter.formatCellValue(sheet.getRow(linha).getCell(coluna));
    }
}
