package com.example.projeto_padrao.web;

import com.example.projeto_padrao.service.ExcelExportService;
import com.example.projeto_padrao.service.TabelaExportada;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Recebe a tabela que esta na tela (montada pelo script dos botoes "Baixar como Excel" em
 * {@code fragments/layout.html}) e devolve o .xlsx correspondente.
 */
@RestController
public class ExportacaoExcelController {

    private final ExcelExportService excelExportService;

    public ExportacaoExcelController(ExcelExportService excelExportService) {
        this.excelExportService = excelExportService;
    }

    @PostMapping("/exportar-excel")
    public ResponseEntity<byte[]> exportar(@RequestBody TabelaExportada tabela) {
        String nome = tabela.arquivo() == null ? "" : tabela.arquivo().replaceAll("[^A-Za-z0-9_-]", "");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + (nome.isEmpty() ? "tabela" : nome) + ".xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelExportService.exportarTabela(tabela));
    }
}
