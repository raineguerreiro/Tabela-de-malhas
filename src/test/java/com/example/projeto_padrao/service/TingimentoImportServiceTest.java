package com.example.projeto_padrao.service;

import com.example.projeto_padrao.domain.Tingimento;
import com.example.projeto_padrao.repository.TingimentoRepository;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TingimentoImportServiceTest {

    @Mock
    private TingimentoRepository repository;

    @InjectMocks
    private TingimentoImportService service;

    @Test
    void linhaExistenteMantemPosicaoELinhasNovasVaoParaOFim() throws IOException {
        Tingimento ultimaGravada = new Tingimento();
        ultimaGravada.setOrdem(5);
        when(repository.findTopByOrderByOrdemDesc()).thenReturn(Optional.of(ultimaGravada));

        Tingimento existente = new Tingimento();
        existente.setId(10L);
        existente.setOrdem(2);
        when(repository.findByReferenciaAndTamanhoAndCorCodigoAndTabela(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());
        when(repository.findByReferenciaAndTamanhoAndCorCodigoAndTabela("REF1", "", "C1", "TABELA A"))
                .thenReturn(Optional.of(existente));

        MockMultipartFile arquivo = planilha(
                new String[] {"REF1", "C1", "TABELA A", "9.5"},
                new String[] {"REF2", "C2", "TABELA A", "1"},
                new String[] {"REF3", "C3", "TABELA B", "2"});

        int processadas = service.importar(arquivo);

        ArgumentCaptor<Tingimento> salvos = ArgumentCaptor.forClass(Tingimento.class);
        verify(repository, times(3)).save(salvos.capture());
        List<Tingimento> lista = salvos.getAllValues();
        assertThat(processadas).isEqualTo(3);
        assertThat(lista.get(0)).isSameAs(existente);
        assertThat(lista.get(0).getOrdem()).isEqualTo(2);
        assertThat(lista.get(0).getPreco()).isEqualByComparingTo(new BigDecimal("9.5"));
        assertThat(lista.get(1).getOrdem()).isEqualTo(6);
        assertThat(lista.get(1).getTabela()).isEqualTo("TABELA A");
        assertThat(lista.get(2).getOrdem()).isEqualTo(7);
        assertThat(lista.get(2).getTabela()).isEqualTo("TABELA B");
    }

    @Test
    void primeiraImportacaoComecaDoZero() throws IOException {
        when(repository.findTopByOrderByOrdemDesc()).thenReturn(Optional.empty());
        when(repository.findByReferenciaAndTamanhoAndCorCodigoAndTabela(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());

        service.importar(planilha(new String[] {"REF1", "C1", "TABELA A", "1"}));

        ArgumentCaptor<Tingimento> salvo = ArgumentCaptor.forClass(Tingimento.class);
        verify(repository).save(salvo.capture());
        assertThat(salvo.getValue().getOrdem()).isZero();
    }

    @Test
    void importaCsvComPontoEVirgulaEVirgulaDecimal() throws IOException {
        when(repository.findTopByOrderByOrdemDesc()).thenReturn(Optional.empty());
        when(repository.findByReferenciaAndTamanhoAndCorCodigoAndTabela(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());

        String csv = "Referência;Cor;Tabela;Preço\r\nREF1;C1;TABELA A;1.234,50\r\n;;;\r\nREF2;C2;\"TABELA; B\";2\r\n";
        MockMultipartFile arquivo = new MockMultipartFile("arquivo", "teste.csv", "text/csv",
                csv.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        int processadas = service.importar(arquivo);

        ArgumentCaptor<Tingimento> salvos = ArgumentCaptor.forClass(Tingimento.class);
        verify(repository, times(2)).save(salvos.capture());
        List<Tingimento> lista = salvos.getAllValues();
        assertThat(processadas).isEqualTo(2);
        assertThat(lista.get(0).getReferencia()).isEqualTo("REF1");
        assertThat(lista.get(0).getPreco()).isEqualByComparingTo(new BigDecimal("1234.50"));
        assertThat(lista.get(1).getTabela()).isEqualTo("TABELA; B");
        assertThat(lista.get(1).getOrdem()).isEqualTo(1);
    }

    /** Planilha ficticia com cabecalho REFERENCIA, COR, TABELA, PRECO e uma linha por array. */
    private MockMultipartFile planilha(String[]... linhas) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet();
            String[] cabecalho = {"REFERENCIA", "COR", "TABELA", "PRECO"};
            Row row = sheet.createRow(0);
            for (int c = 0; c < cabecalho.length; c++) {
                row.createCell(c).setCellValue(cabecalho[c]);
            }
            for (int r = 0; r < linhas.length; r++) {
                row = sheet.createRow(r + 1);
                for (int c = 0; c < 3; c++) {
                    row.createCell(c).setCellValue(linhas[r][c]);
                }
                row.createCell(3).setCellValue(Double.parseDouble(linhas[r][3]));
            }
            workbook.write(out);
            return new MockMultipartFile("arquivo", "teste.xlsx", null, out.toByteArray());
        }
    }
}
