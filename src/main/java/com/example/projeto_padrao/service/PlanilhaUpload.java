package com.example.projeto_padrao.service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Abre um arquivo enviado pelos uploads manuais (Tingimento, Rotativo, Tecelagem) como um
 * {@link Workbook} do POI, aceitando .xlsx ou .csv.
 *
 * <p>O CSV e' convertido em memoria numa planilha de uma aba so', mantendo a posicao de cada
 * linha/coluna; cada valor vira celula de texto (valor vazio nao cria celula, para que linhas em
 * branco continuem sendo detectadas como BLANK). Assim os importadores tratam CSV e xlsx com a
 * mesma logica - inclusive a conversao de texto com virgula decimal ("1.234,56").
 *
 * <p>Encoding: UTF-8 (com ou sem BOM); se o arquivo nao for UTF-8 valido, Windows-1252 (padrao do
 * "CSV (separado por virgulas)" do Excel em portugues). Separador: ';' ou ',', detectado pela
 * primeira linha nao vazia.
 */
public final class PlanilhaUpload {

    private static final Charset WINDOWS_1252 = Charset.forName("windows-1252");

    private PlanilhaUpload() {
    }

    public static Workbook abrir(MultipartFile arquivo) throws IOException {
        String nome = arquivo.getOriginalFilename() == null ? ""
                : arquivo.getOriginalFilename().toLowerCase(Locale.ROOT);
        String contentType = arquivo.getContentType() == null ? "" : arquivo.getContentType();

        if (nome.endsWith(".csv") || contentType.startsWith("text/csv")) {
            return lerCsv(arquivo.getBytes());
        }
        if (nome.endsWith(".xlsx") || nome.isEmpty()) {
            try (InputStream in = arquivo.getInputStream()) {
                return new XSSFWorkbook(in);
            }
        }
        throw new IllegalStateException("Formato de arquivo nao suportado: '" + arquivo.getOriginalFilename()
                + "'. Envie uma planilha .xlsx ou .csv.");
    }

    static Workbook lerCsv(byte[] bytes) {
        String texto = decodificar(bytes);
        char separador = detectarSeparador(texto);
        List<List<String>> linhas = parse(texto, separador);

        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("CSV");
        for (int r = 0; r < linhas.size(); r++) {
            List<String> campos = linhas.get(r);
            Row row = null;
            for (int c = 0; c < campos.size(); c++) {
                String valor = campos.get(c).trim();
                if (valor.isEmpty()) {
                    continue;
                }
                if (row == null) {
                    row = sheet.createRow(r);
                }
                row.createCell(c).setCellValue(valor);
            }
        }
        return workbook;
    }

    private static String decodificar(byte[] bytes) {
        if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xEF && (bytes[1] & 0xFF) == 0xBB
                && (bytes[2] & 0xFF) == 0xBF) {
            return new String(bytes, 3, bytes.length - 3, StandardCharsets.UTF_8);
        }
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        } catch (CharacterCodingException e) {
            return new String(bytes, WINDOWS_1252);
        }
    }

    /** ';' se a primeira linha nao vazia tiver mais ';' que ',' (fora de aspas); senao ','. */
    private static char detectarSeparador(String texto) {
        int pontoEVirgula = 0;
        int virgula = 0;
        boolean entreAspas = false;
        boolean temConteudo = false;
        for (int i = 0; i < texto.length(); i++) {
            char ch = texto.charAt(i);
            if (ch == '"') {
                entreAspas = !entreAspas;
            } else if (!entreAspas && (ch == '\n' || ch == '\r')) {
                if (temConteudo) {
                    break;
                }
            } else if (!entreAspas && ch == ';') {
                pontoEVirgula++;
            } else if (!entreAspas && ch == ',') {
                virgula++;
            }
            if (!Character.isWhitespace(ch)) {
                temConteudo = true;
            }
        }
        return pontoEVirgula > virgula ? ';' : ',';
    }

    /** Parser RFC 4180: campos entre aspas, "" escapado, quebra de linha dentro de aspas, \r\n ou \n. */
    private static List<List<String>> parse(String texto, char separador) {
        List<List<String>> linhas = new ArrayList<>();
        List<String> atual = new ArrayList<>();
        StringBuilder campo = new StringBuilder();
        boolean entreAspas = false;

        for (int i = 0; i < texto.length(); i++) {
            char ch = texto.charAt(i);
            if (entreAspas) {
                if (ch == '"') {
                    if (i + 1 < texto.length() && texto.charAt(i + 1) == '"') {
                        campo.append('"');
                        i++;
                    } else {
                        entreAspas = false;
                    }
                } else {
                    campo.append(ch);
                }
            } else if (ch == '"') {
                entreAspas = true;
            } else if (ch == separador) {
                atual.add(campo.toString());
                campo.setLength(0);
            } else if (ch == '\r' || ch == '\n') {
                if (ch == '\r' && i + 1 < texto.length() && texto.charAt(i + 1) == '\n') {
                    i++;
                }
                atual.add(campo.toString());
                campo.setLength(0);
                linhas.add(atual);
                atual = new ArrayList<>();
            } else {
                campo.append(ch);
            }
        }
        if (campo.length() > 0 || !atual.isEmpty()) {
            atual.add(campo.toString());
            linhas.add(atual);
        }
        return linhas;
    }
}
