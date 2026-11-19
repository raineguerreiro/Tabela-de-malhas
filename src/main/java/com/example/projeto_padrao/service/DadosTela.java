package com.example.projeto_padrao.service;

import com.example.projeto_padrao.domain.Fio;
import com.example.projeto_padrao.domain.Piscoffins;
import com.example.projeto_padrao.domain.Rotativo;
import com.example.projeto_padrao.domain.Tecelagem;
import com.example.projeto_padrao.domain.Tingimento;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Reune todos os dados/calculos exibidos nas abas da tela /malhas (Malhas, Fios, Tingimentos,
 * Rotativos, Tecelagem), montados uma unica vez por {@code MalhaWebController.montarDados} e
 * reaproveitados tanto pela renderizacao HTML (`listar`) quanto pela exportacao em Excel
 * (`exportar`/{@link ExcelExportService}), para as duas nunca divergirem.
 */
public record DadosTela(List<MalhaResumo> malhas, int maxLinhas, List<String> fiosAbreviados,
        Map<String, Fio> fiosPorAbreviado, List<Tingimento> tingimentos, List<String> tabelasDisponiveis,
        List<Tecelagem> tecelagens, String fornecedor1Header, String fornecedor2Header, Piscoffins piscoffins,
        Map<String, BigDecimal> tecelagemValorPorMalha, Map<String, BigDecimal> precoMalhaCruaPorMalha,
        Map<Long, BigDecimal> precoMalhaInformadoContratipoPorTingimento, List<Rotativo> rotativos,
        List<String> tabelasDisponiveisRotativos, Map<Long, BigDecimal> precoMalhaInformadoContratipoPorRotativo,
        Map<Long, ResultadoPrecoRotativo> precoRotativoPorRegra, List<BancoDadosRotativoLinha> bancoDadosRotativos,
        Map<String, String> memoriaTecelagemPorMalha, Map<String, String> memoriaPrecoMalhaCruaPorMalha,
        Map<Long, String> memoriaPrecoMalhaPorTingimento) {
}
