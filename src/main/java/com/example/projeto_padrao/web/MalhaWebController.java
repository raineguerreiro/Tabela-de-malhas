package com.example.projeto_padrao.web;

import com.example.projeto_padrao.domain.Fio;
import com.example.projeto_padrao.domain.Malha;
import com.example.projeto_padrao.domain.Piscoffins;
import com.example.projeto_padrao.domain.PrecoBaseTecido;
import com.example.projeto_padrao.domain.Rotativo;
import com.example.projeto_padrao.domain.Tecelagem;
import com.example.projeto_padrao.domain.Tingimento;
import com.example.projeto_padrao.repository.FioRepository;
import com.example.projeto_padrao.repository.MalhaRepository;
import com.example.projeto_padrao.repository.PiscoffinsRepository;
import com.example.projeto_padrao.repository.PrecoBaseTecidoRepository;
import com.example.projeto_padrao.repository.RotativoRepository;
import com.example.projeto_padrao.repository.TecelagemRepository;
import com.example.projeto_padrao.repository.TingimentoRepository;
import com.example.projeto_padrao.service.BancoDadosRotativoLinha;
import com.example.projeto_padrao.service.DadosLinhaRotativo;
import com.example.projeto_padrao.service.DadosTela;
import com.example.projeto_padrao.service.ExcelExportService;
import com.example.projeto_padrao.service.MalhaResumo;
import com.example.projeto_padrao.service.MalhaResumoService;
import com.example.projeto_padrao.service.MemoriaCalculo;
import com.example.projeto_padrao.service.RegraPrecoRotativo;
import com.example.projeto_padrao.service.ResultadoPrecoRotativo;
import com.example.projeto_padrao.service.RotativoImportService;
import com.example.projeto_padrao.service.TecelagemImportService;
import com.example.projeto_padrao.service.TingimentoImportService;
import com.example.projeto_padrao.service.ValorCalculado;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/malhas")
public class MalhaWebController {

    private final MalhaRepository repository;
    private final MalhaResumoService malhaResumoService;
    private final FioRepository fioRepository;
    private final TingimentoRepository tingimentoRepository;
    private final TingimentoImportService tingimentoImportService;
    private final TecelagemRepository tecelagemRepository;
    private final TecelagemImportService tecelagemImportService;
    private final PiscoffinsRepository piscoffinsRepository;
    private final RotativoRepository rotativoRepository;
    private final RotativoImportService rotativoImportService;
    private final PrecoBaseTecidoRepository precoBaseTecidoRepository;
    private final ExcelExportService excelExportService;

    public MalhaWebController(MalhaRepository repository, MalhaResumoService malhaResumoService,
            FioRepository fioRepository, TingimentoRepository tingimentoRepository,
            TingimentoImportService tingimentoImportService, TecelagemRepository tecelagemRepository,
            TecelagemImportService tecelagemImportService, PiscoffinsRepository piscoffinsRepository,
            RotativoRepository rotativoRepository, RotativoImportService rotativoImportService,
            PrecoBaseTecidoRepository precoBaseTecidoRepository, ExcelExportService excelExportService) {
        this.repository = repository;
        this.malhaResumoService = malhaResumoService;
        this.fioRepository = fioRepository;
        this.tingimentoRepository = tingimentoRepository;
        this.tingimentoImportService = tingimentoImportService;
        this.tecelagemRepository = tecelagemRepository;
        this.tecelagemImportService = tecelagemImportService;
        this.piscoffinsRepository = piscoffinsRepository;
        this.rotativoRepository = rotativoRepository;
        this.rotativoImportService = rotativoImportService;
        this.precoBaseTecidoRepository = precoBaseTecidoRepository;
        this.excelExportService = excelExportService;
    }

    @GetMapping
    public String listar(@RequestParam(name = "aba", defaultValue = "malhas") String aba,
            @RequestParam(required = false) String tabela,
            @RequestParam(required = false) String tabelaRotativo,
            @RequestParam(required = false) String subaba, Model model) {
        DadosTela dados = montarDados(tabela, tabelaRotativo);
        model.addAttribute("malhas", dados.malhas());
        model.addAttribute("maxLinhas", dados.maxLinhas());
        model.addAttribute("abaAtiva", aba);
        model.addAttribute("fiosAbreviados", dados.fiosAbreviados());
        model.addAttribute("fiosPorAbreviado", dados.fiosPorAbreviado());
        model.addAttribute("tingimentos", dados.tingimentos());
        model.addAttribute("tingimentosPorTabela", agruparPorTabela(dados.tingimentos(), Tingimento::getTabela));
        model.addAttribute("tabelasDisponiveis", dados.tabelasDisponiveis());
        model.addAttribute("tabelaSelecionada", tabela);
        model.addAttribute("tecelagens", dados.tecelagens());
        model.addAttribute("fornecedor1Header", dados.fornecedor1Header());
        model.addAttribute("fornecedor2Header", dados.fornecedor2Header());
        model.addAttribute("piscoffins", dados.piscoffins());
        model.addAttribute("tecelagemValorPorMalha", dados.tecelagemValorPorMalha());
        model.addAttribute("precoMalhaCruaPorMalha", dados.precoMalhaCruaPorMalha());
        model.addAttribute("precoMalhaInformadoContratipoPorTingimento", dados.precoMalhaInformadoContratipoPorTingimento());
        model.addAttribute("memoriaTecelagemPorMalha", dados.memoriaTecelagemPorMalha());
        model.addAttribute("memoriaPrecoMalhaCruaPorMalha", dados.memoriaPrecoMalhaCruaPorMalha());
        model.addAttribute("memoriaPrecoMalhaPorTingimento", dados.memoriaPrecoMalhaPorTingimento());
        model.addAttribute("rotativos", dados.rotativos());
        model.addAttribute("rotativosPorTabela", agruparPorTabela(dados.rotativos(), Rotativo::getTabela));
        model.addAttribute("tabelasDisponiveisRotativos", dados.tabelasDisponiveisRotativos());
        model.addAttribute("tabelaRotativoSelecionada", tabelaRotativo);
        model.addAttribute("precoRotativoPorRegra", dados.precoRotativoPorRegra());
        model.addAttribute("regrasPrecoRotativo", RegraPrecoRotativo.values());
        model.addAttribute("bancoDadosRotativos", dados.bancoDadosRotativos());
        model.addAttribute("subabaRotativos", "banco".equals(subaba) || "regras".equals(subaba) ? subaba : "tabela");
        return "malhas/lista";
    }

    @GetMapping("/exportar")
    public ResponseEntity<byte[]> exportar(@RequestParam String aba,
            @RequestParam(required = false) String tabela,
            @RequestParam(required = false) String tabelaRotativo) throws IOException {
        DadosTela dados = montarDados(tabela, tabelaRotativo);
        byte[] arquivo = switch (aba) {
            case "fios" -> excelExportService.exportarFios(dados);
            case "tingimentos" -> excelExportService.exportarTingimentos(dados);
            case "rotativos" -> excelExportService.exportarRotativos(dados);
            case "tecelagem" -> excelExportService.exportarTecelagem(dados.tecelagens(), dados.fornecedor1Header(),
                    dados.fornecedor2Header());
            default -> excelExportService.exportarMalhas(dados);
        };
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + aba + ".xlsx\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(arquivo);
    }

    private DadosTela montarDados(String tabela, String tabelaRotativo) {
        List<MalhaResumo> malhas = malhaResumoService.listar();
        int maxLinhas = malhas.isEmpty() ? 1 : malhas.get(0).fios().size();
        List<String> fiosAbreviados = fiosAbreviadosUsadosNasMalhas(malhas);
        Map<String, Fio> fiosPorAbreviado = fiosPorAbreviado();
        Set<String> codigosMalha = codigosMalha(malhas);
        List<Tingimento> tingimentos = tingimentos(tabela, codigosMalha);
        List<String> tabelasDisponiveis = tabelasDisponiveis(codigosMalha);
        List<Tecelagem> tecelagens = tecelagemRepository.findAllByOrderByOrdemAsc();
        String fornecedor1Header = cabecalhoFornecedor(tecelagens, true);
        String fornecedor2Header = cabecalhoFornecedor(tecelagens, false);
        Piscoffins piscoffins = piscoffinsRepository.findAll().stream().findFirst().orElseGet(Piscoffins::new);
        Map<String, ValorCalculado> tecelagemCalculada = tecelagemValorPorMalha(malhas, tecelagens, piscoffins.getValor());
        Map<String, BigDecimal> tecelagemValorPorMalha = valores(tecelagemCalculada);
        Map<String, ValorCalculado> precoMalhaCruaCalculado =
                precoMalhaCruaPorMalha(malhas, fiosPorAbreviado, tecelagemValorPorMalha);
        Map<String, BigDecimal> precoMalhaCruaPorMalha = valores(precoMalhaCruaCalculado);
        Map<Long, ValorCalculado> precoMalhaTingimentoCalculado =
                precoMalhaInformadoContratipoPorTingimento(tingimentos, precoMalhaCruaPorMalha);
        Map<Long, BigDecimal> precoMalhaInformadoContratipoPorTingimento = valores(precoMalhaTingimentoCalculado);
        List<Rotativo> rotativos = rotativos(tabelaRotativo, codigosMalha);
        List<String> tabelasDisponiveisRotativos = tabelasDisponiveisRotativos(codigosMalha);
        Map<String, PrecoBaseTecido> precosBasePorChave = precosBasePorChave();

        // Tingimentos de todas as tabelas (sem o filtro da tela), usados pelas regras de preco dos Rotativos.
        List<Tingimento> todosTingimentos = (tabela == null || tabela.isBlank()) ? tingimentos
                : tingimentos(null, codigosMalha);
        Map<Long, BigDecimal> precoMalhaTodosTingimentos = todosTingimentos == tingimentos
                ? precoMalhaInformadoContratipoPorTingimento
                : valores(precoMalhaInformadoContratipoPorTingimento(todosTingimentos, precoMalhaCruaPorMalha));
        Set<String> referenciasTingimento = todosTingimentos.stream()
                .map(Tingimento::getReferencia)
                .filter(Objects::nonNull)
                .map(referencia -> referencia.trim().toUpperCase())
                .collect(Collectors.toSet());
        Map<String, BigDecimal> precoMalhaTingimentoPorChave = new HashMap<>();
        for (Tingimento tingimento : todosTingimentos) {
            precoMalhaTingimentoPorChave.putIfAbsent(chaveTingimento(tingimento.getReferencia(), tingimento.getCorCodigo(),
                    tingimento.getTamanho(), tingimento.getTabela()), precoMalhaTodosTingimentos.get(tingimento.getId()));
        }

        Set<String> referenciasTabelaDigital = todosTingimentos.stream()
                .filter(t -> normalizarChave(t.getTabela()).equals(
                        normalizarChave(RegraPrecoRotativo.TABELA_TINGIMENTO_DIGITAL)))
                .map(t -> normalizarChave(t.getReferencia()))
                .collect(Collectors.toSet());

        Map<Long, ResultadoPrecoRotativo> precoRotativoPorRegra = precoRotativoPorRegra(rotativos, precosBasePorChave,
                referenciasTingimento, precoMalhaTingimentoPorChave, referenciasTabelaDigital);
        // Para o Excel exportado: so' os valores efetivamente calculados.
        Map<Long, BigDecimal> precoMalhaInformadoContratipoPorRotativo = new HashMap<>();
        precoRotativoPorRegra.forEach((id, resultado) -> {
            if (resultado.valor() != null) {
                precoMalhaInformadoContratipoPorRotativo.put(id, resultado.valor());
            }
        });
        List<BancoDadosRotativoLinha> bancoDadosRotativos =
                bancoDadosRotativos(codigosMalha, referenciasTingimento, precosBasePorChave);

        return new DadosTela(malhas, maxLinhas, fiosAbreviados, fiosPorAbreviado, tingimentos, tabelasDisponiveis,
                tecelagens, fornecedor1Header, fornecedor2Header, piscoffins, tecelagemValorPorMalha,
                precoMalhaCruaPorMalha, precoMalhaInformadoContratipoPorTingimento, rotativos,
                tabelasDisponiveisRotativos, precoMalhaInformadoContratipoPorRotativo, precoRotativoPorRegra,
                bancoDadosRotativos, memorias(tecelagemCalculada), memorias(precoMalhaCruaCalculado),
                memorias(precoMalhaTingimentoCalculado));
    }

    /**
     * "Banco de dados" da aba Rotativos: uma linha por combinacao unica referencia + tamanho +
     * tecido base entre os rotativos visiveis (todas as tabelas de precos, mesmo filtro de
     * referencia da aba), na ordem do Excel. Rotativos sem Tecido Base ficam de fora; cores e
     * tabelas diferentes nao duplicam linha. Referencias que ja constam em alguma tabela da aba
     * Tingimentos (comparacao case-insensitive, so' pela referencia) tambem ficam de fora - o preco
     * salvo delas continua em {@link PrecoBaseTecido} e volta a aparecer se sairem de Tingimentos.
     * O Preco de cada combinacao vem de {@link PrecoBaseTecido}.
     */
    private List<BancoDadosRotativoLinha> bancoDadosRotativos(Set<String> codigosMalha,
            Set<String> referenciasTingimento, Map<String, PrecoBaseTecido> precosSalvos) {
        Map<String, Rotativo> combinacoes = new LinkedHashMap<>();
        for (Rotativo rotativo : rotativos(null, codigosMalha)) {
            if (rotativo.getTecidoBase() == null || rotativo.getTecidoBase().isBlank()) {
                continue;
            }
            if (rotativo.getReferencia() != null
                    && referenciasTingimento.contains(rotativo.getReferencia().trim().toUpperCase())) {
                continue;
            }
            combinacoes.putIfAbsent(
                    chaveBaseTecido(rotativo.getReferencia(), rotativo.getTamanho(), rotativo.getTecidoBase()), rotativo);
        }

        return combinacoes.entrySet().stream()
                .map(entrada -> {
                    Rotativo rotativo = entrada.getValue();
                    PrecoBaseTecido salvo = precosSalvos.get(entrada.getKey());
                    return new BancoDadosRotativoLinha(rotativo.getReferencia(),
                            Objects.toString(rotativo.getTamanho(), ""), rotativo.getTecidoBase(),
                            salvo != null ? salvo.getPreco() : null, salvo != null ? salvo.getPrecoContratipo() : null,
                            salvo != null ? salvo.getMemoriaPrecoContratipo() : null);
                })
                .toList();
    }

    @PostMapping("/rotativos/banco-dados/salvar")
    public String salvarPrecoBaseTecido(@RequestParam String referencia,
            @RequestParam(required = false, defaultValue = "") String tamanho, @RequestParam String tecidoBase,
            @RequestParam(required = false) BigDecimal preco, @RequestParam(required = false) String tabelaRotativo) {
        PrecoBaseTecido precoBase = precoBaseTecidoRepository
                .findByReferenciaAndTamanhoAndTecidoBase(referencia, tamanho, tecidoBase)
                .orElseGet(PrecoBaseTecido::new);
        precoBase.setReferencia(referencia);
        precoBase.setTamanho(tamanho);
        precoBase.setTecidoBase(tecidoBase);
        precoBase.setPreco(preco);
        precoBaseTecidoRepository.save(precoBase);
        String redirect = "redirect:/malhas?aba=rotativos&subaba=banco";
        if (tabelaRotativo != null && !tabelaRotativo.isBlank()) {
            redirect += "&tabelaRotativo=" + UriUtils.encode(tabelaRotativo, StandardCharsets.UTF_8);
        }
        return redirect;
    }

    @PostMapping("/fios/salvar")
    public String salvarFio(@RequestParam String nomeAbreviado, @RequestParam String cod1, @RequestParam String cod2,
            @RequestParam String cod3, @RequestParam String cod4, @RequestParam(required = false) String descricao,
            @RequestParam(required = false) BigDecimal custoInformado) {
        Fio fio = fioRepository.findByNomeAbreviado(nomeAbreviado).orElseGet(Fio::new);
        fio.setNomeAbreviado(nomeAbreviado);
        fio.setCodigo(cod1 + cod2 + cod3 + cod4);
        fio.setDescricao(descricao);
        fio.setCustoInformado(custoInformado);
        fioRepository.save(fio);
        return "redirect:/malhas?aba=fios";
    }

    @PostMapping("/tingimentos/importar")
    public String importarTingimentos(@RequestParam("arquivo") MultipartFile arquivo) throws IOException {
        tingimentoImportService.importar(arquivo);
        return "redirect:/malhas?aba=tingimentos";
    }

    @PostMapping("/rotativos/importar")
    public String importarRotativos(@RequestParam("arquivo") MultipartFile arquivo) throws IOException {
        rotativoImportService.importar(arquivo);
        return "redirect:/malhas?aba=rotativos";
    }

    @PostMapping("/rotativos/{id}/tecnica")
    public String salvarTecnicaRotativo(@PathVariable Long id, @RequestParam(required = false) String tecnica,
            @RequestParam(required = false) String tabelaRotativo) {
        rotativoRepository.findById(id).ifPresent(rotativo -> {
            rotativo.setTecnica((tecnica == null || tecnica.isBlank()) ? null : tecnica);
            rotativoRepository.save(rotativo);
        });
        String redirect = "redirect:/malhas?aba=rotativos";
        if (tabelaRotativo != null && !tabelaRotativo.isBlank()) {
            redirect += "&tabelaRotativo=" + UriUtils.encode(tabelaRotativo, StandardCharsets.UTF_8);
        }
        return redirect;
    }

    @PostMapping("/rotativos/{id}/tecido-base")
    public String salvarTecidoBaseRotativo(@PathVariable Long id, @RequestParam(required = false) String tecidoBase,
            @RequestParam(required = false) String tabelaRotativo) {
        rotativoRepository.findById(id).ifPresent(rotativo -> {
            rotativo.setTecidoBase((tecidoBase == null || tecidoBase.isBlank()) ? null : tecidoBase.trim());
            rotativoRepository.save(rotativo);
        });
        String redirect = "redirect:/malhas?aba=rotativos";
        if (tabelaRotativo != null && !tabelaRotativo.isBlank()) {
            redirect += "&tabelaRotativo=" + UriUtils.encode(tabelaRotativo, StandardCharsets.UTF_8);
        }
        return redirect;
    }

    @PostMapping("/tecelagem/importar")
    public String importarTecelagem(@RequestParam("arquivo") MultipartFile arquivo) throws IOException {
        tecelagemImportService.importar(arquivo);
        return "redirect:/malhas?aba=tecelagem";
    }

    @PostMapping("/piscoffins/salvar")
    public String salvarPiscoffins(@RequestParam String valor) {
        String texto = valor.trim();
        if (texto.contains(",")) {
            texto = texto.replace(".", "").replace(",", ".");
        }
        BigDecimal valorConvertido = new BigDecimal(texto);
        Piscoffins piscoffins = piscoffinsRepository.findAll().stream().findFirst().orElseGet(Piscoffins::new);
        piscoffins.setValor(valorConvertido);
        piscoffinsRepository.save(piscoffins);
        return "redirect:/malhas";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("malha", new Malha());
        return "malhas/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Malha malha = repository.findById(id).orElseThrow(NoSuchElementException::new);
        model.addAttribute("malha", malha);
        return "malhas/form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Malha malha) {
        repository.save(malha);
        return "redirect:/malhas";
    }

    /** Nomes abreviados de fio (ex.: "10 OE", "30 PES") usados em pelo menos uma malha, sem repetir. */
    private List<String> fiosAbreviadosUsadosNasMalhas(List<MalhaResumo> malhas) {
        return malhas.stream()
                .flatMap(m -> m.fios().stream())
                .map(MalhaResumo.FioParticipacao::fio)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    /** Fios ja cadastrados, indexados pela abreviacao (ex.: "10 OE" -> Fio), para a tela correlacionar. */
    private Map<String, Fio> fiosPorAbreviado() {
        Map<String, Fio> mapa = new HashMap<>();
        for (Fio fio : fioRepository.findAll()) {
            if (fio.getNomeAbreviado() != null) {
                mapa.put(fio.getNomeAbreviado(), fio);
            }
        }
        return mapa;
    }

    /** Codigos de malha (aba Malhas) normalizados (trim + maiusculo), para os filtros de Tingimentos/Rotativos. */
    private Set<String> codigosMalha(List<MalhaResumo> malhas) {
        Set<String> codigos = new HashSet<>();
        for (MalhaResumo malha : malhas) {
            if (malha.codigo() != null) {
                codigos.add(malha.codigo().trim().toUpperCase());
            }
        }
        return codigos;
    }

    /**
     * true se o texto (ex.: corCodigo) comecar com "ET", ou com "M" seguido de um digito (ex.:
     * "M00142") - "M" seguido de outra letra (ex.: "MR0001") nao conta, ja que nesse caso o "M"
     * faz parte de outra coisa, nao e' o prefixo que importa.
     */
    private boolean corComecaComMOuEt(String corCodigo) {
        if (corCodigo == null) {
            return false;
        }
        String normalizado = corCodigo.trim().toUpperCase();
        boolean mSeguidoDeNumero = normalizado.length() >= 2 && normalizado.charAt(0) == 'M'
                && Character.isDigit(normalizado.charAt(1));
        return mSeguidoDeNumero || normalizado.startsWith("ET");
    }

    /** true se a referencia comecar com "T", sem diferenciar maiusculas/minusculas. */
    private boolean referenciaComecaComT(String referencia) {
        if (referencia == null) {
            return false;
        }
        return referencia.trim().toUpperCase().startsWith("T");
    }

    /**
     * Tingimentos na mesma ordem do Excel original, filtrados por tabela de precos se informada -
     * e so' os que tem referencia cadastrada na aba Malhas, excluindo tambem cor comecando com
     * "M" ou "ET".
     */
    private List<Tingimento> tingimentos(String tabela, Set<String> codigosMalha) {
        List<Tingimento> lista = (tabela == null || tabela.isBlank())
                ? tingimentoRepository.findAllByOrderByOrdemAsc()
                : tingimentoRepository.findByTabelaOrderByOrdemAsc(tabela);
        List<Tingimento> filtrados = lista.stream()
                .filter(t -> t.getReferencia() != null && codigosMalha.contains(t.getReferencia().trim().toUpperCase()))
                .filter(t -> !corComecaComMOuEt(t.getCorCodigo()))
                .toList();
        return ordenarPorGrupoDeTabela(filtrados, Tingimento::getTabela, Tingimento::getId, Tingimento::getOrdem);
    }

    /**
     * Ordena as linhas agrupadas por tabela de precos: as tabelas aparecem na ordem em que foram
     * criadas (menor id de cada uma - tabela importada depois fica abaixo) e, dentro de cada tabela,
     * pela ordem de importacao (linhas novas no fim do grupo). Assim a tela, o Excel exportado e o
     * Banco de dados dos Rotativos seguem a mesma sequencia.
     */
    private static <T> List<T> ordenarPorGrupoDeTabela(List<T> linhas, Function<T, String> tabela,
            Function<T, Long> id, Function<T, Integer> ordem) {
        Map<String, Long> menorIdPorTabela = new HashMap<>();
        for (T linha : linhas) {
            Long idLinha = id.apply(linha);
            if (idLinha != null) {
                menorIdPorTabela.merge(Objects.toString(tabela.apply(linha), ""), idLinha, Math::min);
            }
        }
        Comparator<T> comparador = Comparator
                .comparing((T linha) -> menorIdPorTabela.getOrDefault(Objects.toString(tabela.apply(linha), ""), Long.MAX_VALUE))
                .thenComparing(linha -> Objects.toString(tabela.apply(linha), ""))
                .thenComparing(ordem, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(id, Comparator.nullsLast(Comparator.naturalOrder()));
        return linhas.stream().sorted(comparador).toList();
    }

    /** Agrupa as linhas (ja ordenadas por {@link #ordenarPorGrupoDeTabela}) por tabela, preservando a ordem. */
    private static <T> Map<String, List<T>> agruparPorTabela(List<T> linhas, Function<T, String> tabela) {
        return linhas.stream().collect(Collectors.groupingBy(linha -> Objects.toString(tabela.apply(linha), ""),
                LinkedHashMap::new, Collectors.toList()));
    }

    /** Nomes de tabela de precos ja usados em algum Tingimento visivel (mesmos filtros de referencia/cor), sem repetir. */
    private List<String> tabelasDisponiveis(Set<String> codigosMalha) {
        return tingimentos(null, codigosMalha).stream()
                .map(Tingimento::getTabela)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    /** Monta o cabecalho da coluna de custo (ex.: "Wilbert - a partir de julho") a partir da primeira linha carregada. */
    private String cabecalhoFornecedor(List<Tecelagem> tecelagens, boolean primeiroFornecedor) {
        if (tecelagens.isEmpty()) {
            return primeiroFornecedor ? "Custo Fornecedor 1" : "Custo Fornecedor 2";
        }
        Tecelagem referencia = tecelagens.get(0);
        String nome = primeiroFornecedor ? referencia.getFornecedor1Nome() : referencia.getFornecedor2Nome();
        String vigencia = primeiroFornecedor ? referencia.getFornecedor1Vigencia() : referencia.getFornecedor2Vigencia();
        if (nome == null) {
            nome = primeiroFornecedor ? "Custo Fornecedor 1" : "Custo Fornecedor 2";
        }
        return vigencia == null ? nome : nome + " - " + vigencia;
    }

    /**
     * Memoria de calculo da coluna "Tecelagem" na tabela de Malhas: para cada malha, busca a
     * linha de Tecelagem pelo mesmo codigo (case-insensitive), pega o custo preenchido (fornecedor
     * 1 se houver, senao fornecedor 2) e multiplica pelo valor de Piscoffins. Sem correspondencia
     * ou sem Piscoffins cadastrado, o resultado e' zero (ex.: MM002, custo 1,95 x piscoffins 0,9175
     * = 1,79).
     */
    private static final Map<String, String> ALIASES_CODIGO_TECELAGEM = Map.of(
            "MMOPT", "MM002",
            "MLTPT", "MOL11",
            "MOLPT", "MOL06"
    );

    private Map<String, ValorCalculado> tecelagemValorPorMalha(List<MalhaResumo> malhas, List<Tecelagem> tecelagens,
            BigDecimal piscoffinsValor) {
        Map<String, Tecelagem> tecelagemPorCodigo = new HashMap<>();
        for (Tecelagem tecelagem : tecelagens) {
            if (tecelagem.getCodigo() != null) {
                tecelagemPorCodigo.putIfAbsent(tecelagem.getCodigo().trim().toUpperCase(), tecelagem);
            }
        }

        Map<String, ValorCalculado> resultado = new HashMap<>();
        for (MalhaResumo malha : malhas) {
            if (malha.codigo() == null) {
                continue;
            }
            String codigoBusca = malha.codigo().trim().toUpperCase();
            codigoBusca = ALIASES_CODIGO_TECELAGEM.getOrDefault(codigoBusca, codigoBusca);
            Tecelagem tecelagem = tecelagemPorCodigo.get(codigoBusca);
            boolean usaFornecedor1 = tecelagem != null && tecelagem.getCustoFornecedor1() != null;
            BigDecimal custo = tecelagem == null ? null
                    : (usaFornecedor1 ? tecelagem.getCustoFornecedor1() : tecelagem.getCustoFornecedor2());
            BigDecimal valor = (custo == null || piscoffinsValor == null) ? BigDecimal.ZERO : custo.multiply(piscoffinsValor);

            String memoria;
            if (tecelagem == null) {
                memoria = "Sem linha na aba Tecelagem para o código " + codigoBusca + " - Tecelagem considerada R$ 0,00.";
            } else if (custo == null) {
                memoria = "Tecelagem " + codigoBusca + " sem custo de fornecedor preenchido - considerada R$ 0,00.";
            } else if (piscoffinsValor == null) {
                memoria = "Piscoffins não cadastrado na aba Tecelagem - Tecelagem considerada R$ 0,00.";
            } else {
                memoria = "Tecelagem " + codigoBusca + " (fornecedor " + (usaFornecedor1 ? "1" : "2") + ") R$ "
                        + MemoriaCalculo.reais(custo) + " × Piscoffins " + MemoriaCalculo.numero(piscoffinsValor)
                        + " = R$ " + MemoriaCalculo.reais(valor);
            }
            resultado.put(malha.codigo(), new ValorCalculado(valor, memoria));
        }
        return resultado;
    }

    /** So' os valores de um mapa de {@link ValorCalculado} (para quem nao precisa da memoria de calculo). */
    private static <K> Map<K, BigDecimal> valores(Map<K, ValorCalculado> calculados) {
        Map<K, BigDecimal> valores = new HashMap<>();
        calculados.forEach((chave, calculado) -> valores.put(chave, calculado.valor()));
        return valores;
    }

    /** So' as memorias de calculo de um mapa de {@link ValorCalculado} (para a tela). */
    private static <K> Map<K, String> memorias(Map<K, ValorCalculado> calculados) {
        Map<K, String> memorias = new HashMap<>();
        calculados.forEach((chave, calculado) -> memorias.put(chave, calculado.memoria()));
        return memorias;
    }

    /** Divisor de quebra de producao (2%) usado no calculo do Preco Malha Crua. */
    private static final BigDecimal DIVISOR_QUEBRA_PRODUCAO = new BigDecimal("0.98");

    /**
     * Memoria de calculo da coluna "Preco Malha Crua": para cada malha, soma
     * (Custo do Fio x % Participacao) de cada componente de fio, soma o valor de Tecelagem da
     * malha, e divide o total por 0,98 (2% de quebra de producao). Fio sem custo cadastrado ou
     * participacao nao interpretavel entra como zero na soma, em vez de quebrar o calculo.
     */
    private Map<String, ValorCalculado> precoMalhaCruaPorMalha(List<MalhaResumo> malhas,
            Map<String, Fio> fiosPorAbreviado, Map<String, BigDecimal> tecelagemValorPorMalha) {
        Map<String, ValorCalculado> resultado = new HashMap<>();
        for (MalhaResumo malha : malhas) {
            if (malha.codigo() == null) {
                continue;
            }
            ValorCalculado somaFios = somaCustoFios(malha, fiosPorAbreviado);
            BigDecimal tecelagem = tecelagemValorPorMalha.getOrDefault(malha.codigo(), BigDecimal.ZERO);
            BigDecimal total = somaFios.valor().add(tecelagem);
            BigDecimal precoMalhaCrua = total.divide(DIVISOR_QUEBRA_PRODUCAO, 4, RoundingMode.HALF_UP);
            String memoria = "Fios: " + somaFios.memoria() + " = R$ " + MemoriaCalculo.reais(somaFios.valor())
                    + "; R$ " + MemoriaCalculo.reais(somaFios.valor()) + " + Tecelagem R$ " + MemoriaCalculo.reais(tecelagem)
                    + " = R$ " + MemoriaCalculo.reais(total) + "; R$ " + MemoriaCalculo.reais(total) + " ÷ 0,98 = R$ "
                    + MemoriaCalculo.reais(precoMalhaCrua);
            resultado.put(malha.codigo(), new ValorCalculado(precoMalhaCrua, memoria));
        }
        return resultado;
    }

    /**
     * Soma (Custo do Fio x % Participacao) dos componentes de fio de uma malha.
     *
     * <p>Caso especial: quando o bloco tem mais de um percentual de participacao mas so' um deles
     * vem com o Fio preenchido (os outros ficam com a celula Fio vazia), o sistema entende que
     * esse mesmo fio se aplica a soma de todos os percentuais do bloco - ja que, nesses casos, os
     * percentuais juntos somam 100% (ex.: 97% + 3%, so' o primeiro com fio preenchido -> custo do
     * fio x 100%, nao so' x 97%). Quando ha' 2 ou mais fios distintos preenchidos, ou so' um
     * componente no bloco, mantem o calculo linha a linha de sempre (componentes sem fio nao
     * contribuem).
     */
    private ValorCalculado somaCustoFios(MalhaResumo malha, Map<String, Fio> fiosPorAbreviado) {
        List<MalhaResumo.FioParticipacao> componentesReais = malha.fios().stream()
                .filter(c -> c.fio() != null || c.participacao() != null)
                .toList();

        Set<String> fiosDistintos = componentesReais.stream()
                .map(MalhaResumo.FioParticipacao::fio)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (fiosDistintos.size() == 1 && componentesReais.size() > 1) {
            String nomeFio = fiosDistintos.iterator().next();
            Fio fio = fiosPorAbreviado.get(nomeFio);
            BigDecimal custoFio = fio != null ? fio.getCustoContratipo() : null;
            if (custoFio == null) {
                return new ValorCalculado(BigDecimal.ZERO, nomeFio + " sem custo cadastrado (R$ 0,00)");
            }
            BigDecimal somaParticipacoes = BigDecimal.ZERO;
            for (MalhaResumo.FioParticipacao componente : componentesReais) {
                BigDecimal participacao = parsePercentual(componente.participacao());
                if (participacao != null) {
                    somaParticipacoes = somaParticipacoes.add(participacao);
                }
            }
            BigDecimal soma = custoFio.multiply(somaParticipacoes);
            return new ValorCalculado(soma, nomeFio + " R$ " + MemoriaCalculo.reais(custoFio) + " × "
                    + MemoriaCalculo.percentual(somaParticipacoes) + " (um fio para todos os percentuais do bloco)");
        }

        BigDecimal soma = BigDecimal.ZERO;
        List<String> partes = new ArrayList<>();
        for (MalhaResumo.FioParticipacao componente : componentesReais) {
            if (componente.fio() == null) {
                continue;
            }
            Fio fio = fiosPorAbreviado.get(componente.fio());
            BigDecimal custoFio = fio != null ? fio.getCustoContratipo() : null;
            BigDecimal participacao = parsePercentual(componente.participacao());
            if (custoFio != null && participacao != null) {
                BigDecimal parcela = custoFio.multiply(participacao);
                soma = soma.add(parcela);
                partes.add(componente.fio() + " R$ " + MemoriaCalculo.reais(custoFio) + " × "
                        + MemoriaCalculo.percentual(participacao));
            } else {
                partes.add(componente.fio() + (custoFio == null ? " sem custo cadastrado" : " sem % válido")
                        + " (R$ 0,00)");
            }
        }
        return new ValorCalculado(soma, partes.isEmpty() ? "nenhum fio" : String.join(" + ", partes));
    }

    /** Converte um percentual em texto (ex.: "95,0%") para fracao decimal (ex.: 0.95). Retorna null se nao der pra converter. */
    private BigDecimal parsePercentual(String texto) {
        if (texto == null) {
            return null;
        }
        String limpo = texto.replace("%", "").trim();
        if (limpo.isEmpty()) {
            return null;
        }
        if (limpo.contains(",")) {
            limpo = limpo.replace(".", "").replace(",", ".");
        }
        try {
            return new BigDecimal(limpo).divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** Mesmo fator contratipo usado em Fio.getCustoContratipo() (custoInformado x 0,9175). */
    private static final BigDecimal FATOR_CONTRATIPO_MALHA = new BigDecimal("0.9175");

    /** Divisor de quebra (10%) usado no calculo do Preco da Malha (Informado e Contratipo). */
    private static final BigDecimal DIVISOR_QUEBRA_TINGIMENTO = new BigDecimal("0.90");

    /**
     * Memoria de calculo da coluna "Preco da Malha (Informado e Contratipo)" na aba Tingimentos:
     * para cada linha, busca o Preco Malha Crua da malha cujo codigo bate com a Referencia
     * (case-insensitive, zero se nao achar), soma com (Preco x 0,9175) e divide o total por 0,90.
     */
    private Map<Long, ValorCalculado> precoMalhaInformadoContratipoPorTingimento(List<Tingimento> tingimentos,
            Map<String, BigDecimal> precoMalhaCruaPorMalha) {
        Map<String, BigDecimal> precoMalhaCruaPorCodigo = new HashMap<>();
        precoMalhaCruaPorMalha.forEach((codigo, valor) -> {
            if (codigo != null) {
                precoMalhaCruaPorCodigo.putIfAbsent(codigo.trim().toUpperCase(), valor);
            }
        });

        Map<Long, ValorCalculado> resultado = new HashMap<>();
        for (Tingimento tingimento : tingimentos) {
            BigDecimal precoMalhaCrua = tingimento.getReferencia() == null ? null
                    : precoMalhaCruaPorCodigo.get(tingimento.getReferencia().trim().toUpperCase());
            BigDecimal cruaSegura = precoMalhaCrua != null ? precoMalhaCrua : BigDecimal.ZERO;
            BigDecimal precoSeguro = tingimento.getPreco() != null ? tingimento.getPreco() : BigDecimal.ZERO;
            BigDecimal soma = cruaSegura.add(precoSeguro.multiply(FATOR_CONTRATIPO_MALHA));
            BigDecimal valor = soma.divide(DIVISOR_QUEBRA_TINGIMENTO, 4, RoundingMode.HALF_UP);
            String memoria = "Preço Malha Crua R$ " + MemoriaCalculo.reais(cruaSegura)
                    + (precoMalhaCrua == null ? " (malha " + tingimento.getReferencia() + " não encontrada na aba Malhas)" : "")
                    + " + Preço R$ " + MemoriaCalculo.reais(precoSeguro) + " × 0,9175 = R$ " + MemoriaCalculo.reais(soma)
                    + "; R$ " + MemoriaCalculo.reais(soma) + " ÷ 0,90 = R$ " + MemoriaCalculo.reais(valor);
            resultado.put(tingimento.getId(), new ValorCalculado(valor, memoria));
        }
        return resultado;
    }

    /**
     * Rotativos na mesma ordem do Excel original, filtrados por tabela de precos se informada -
     * mostra so' as linhas que atendem a uma das duas condicoes: referencia comecando com "T"
     * (independente de bater com a Malhas ou da cor), ou referencia cadastrada na aba Malhas com
     * cor comecando com "M" ou "ET" (o complemento do filtro de cor usado em Tingimentos).
     */
    private List<Rotativo> rotativos(String tabela, Set<String> codigosMalha) {
        List<Rotativo> lista = (tabela == null || tabela.isBlank())
                ? rotativoRepository.findAllByOrderByOrdemAsc()
                : rotativoRepository.findByTabelaOrderByOrdemAsc(tabela);
        List<Rotativo> filtrados = lista.stream()
                .filter(r -> referenciaComecaComT(r.getReferencia())
                        || (r.getReferencia() != null && codigosMalha.contains(r.getReferencia().trim().toUpperCase())
                                && corComecaComMOuEt(r.getCorCodigo())))
                .toList();
        return ordenarPorGrupoDeTabela(filtrados, Rotativo::getTabela, Rotativo::getId, Rotativo::getOrdem);
    }

    /** Nomes de tabela de precos ja usados em algum Rotativo visivel (mesmo filtro de referencia), sem repetir. */
    private List<String> tabelasDisponiveisRotativos(Set<String> codigosMalha) {
        return rotativos(null, codigosMalha).stream()
                .map(Rotativo::getTabela)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    /**
     * Coluna "Preco da Malha (Informado e Contratipo)" na aba Rotativos: cada linha e' calculada
     * pela regra da sua Tecnica ({@link RegraPrecoRotativo}). Aqui so' se reunem os dados de cada
     * linha: o Preco Contratipo cadastrado no Banco de dados para a mesma Referencia + Tamanho +
     * Tecido Base (mesmo que a combinacao esteja oculta no Banco de dados por constar em
     * Tingimentos), se a referencia consta na Tingimentos e o Preco da Malha da linha da
     * Tingimentos com a mesma Referencia, Cor = Tecido Base e Tamanho, buscado nas tabelas
     * {@link RegraPrecoRotativo#TABELAS_TINGIMENTO_CILINDRO} (33.11.15 e, se nao houver, 33.10.15),
     * e se a referencia consta na tabela {@link RegraPrecoRotativo#TABELA_TINGIMENTO_DIGITAL}.
     */
    private Map<Long, ResultadoPrecoRotativo> precoRotativoPorRegra(List<Rotativo> rotativos,
            Map<String, PrecoBaseTecido> precosBasePorChave, Set<String> referenciasTingimento,
            Map<String, BigDecimal> precoMalhaTingimentoPorChave, Set<String> referenciasTabelaDigital) {
        Map<Long, ResultadoPrecoRotativo> resultado = new HashMap<>();
        for (Rotativo rotativo : rotativos) {
            PrecoBaseTecido base = precosBasePorChave.get(
                    chaveBaseTecido(rotativo.getReferencia(), rotativo.getTamanho(), rotativo.getTecidoBase()));
            boolean referenciaEmTingimentos = rotativo.getReferencia() != null
                    && referenciasTingimento.contains(rotativo.getReferencia().trim().toUpperCase());
            Optional<RegraPrecoRotativo.PrecoTingimento> precoTingimento = RegraPrecoRotativo.buscarNasTabelasCilindro(
                    tabela -> precoMalhaTingimentoPorChave.get(chaveTingimento(rotativo.getReferencia(),
                            rotativo.getTecidoBase(), rotativo.getTamanho(), tabela)));
            DadosLinhaRotativo linha = new DadosLinhaRotativo(rotativo.getReferencia(), rotativo.getTamanho(),
                    rotativo.getTecidoBase(), rotativo.getTabela(), rotativo.getTecnica(), rotativo.getPreco(),
                    base != null ? base.getPrecoContratipo() : null, referenciaEmTingimentos,
                    precoTingimento.map(RegraPrecoRotativo.PrecoTingimento::preco).orElse(null),
                    precoTingimento.map(RegraPrecoRotativo.PrecoTingimento::tabela).orElse(null),
                    referenciasTabelaDigital.contains(normalizarChave(rotativo.getReferencia())),
                    precoMalhaTingimentoPorChave.get(chaveTingimento(rotativo.getReferencia(), rotativo.getTecidoBase(),
                            rotativo.getTamanho(), RegraPrecoRotativo.TABELA_TINGIMENTO_DIGITAL)));
            resultado.put(rotativo.getId(), RegraPrecoRotativo.calcularPara(linha));
        }
        return resultado;
    }

    /** Chave Referencia + Cor + Tamanho + Tabela (sem diferenciar maiusculas, sem espacos nas pontas) de uma linha da Tingimentos. */
    private static String chaveTingimento(String referencia, String cor, String tamanho, String tabela) {
        return normalizarChave(referencia) + "\u0000" + normalizarChave(cor) + "\u0000" + normalizarChave(tamanho)
                + "\u0000" + normalizarChave(tabela);
    }

    private static String normalizarChave(String texto) {
        return texto == null ? "" : texto.trim().toUpperCase();
    }

    /** Precos base salvos no Banco de dados dos Rotativos, indexados por {@link #chaveBaseTecido}. */
    private Map<String, PrecoBaseTecido> precosBasePorChave() {
        Map<String, PrecoBaseTecido> precos = new HashMap<>();
        for (PrecoBaseTecido preco : precoBaseTecidoRepository.findAll()) {
            precos.put(chaveBaseTecido(preco.getReferencia(), preco.getTamanho(), preco.getTecidoBase()), preco);
        }
        return precos;
    }

    /** Chave Referencia + Tamanho + Tecido Base que liga um Rotativo ao seu preco base. */
    private static String chaveBaseTecido(String referencia, String tamanho, String tecidoBase) {
        return referencia + "\u0000" + Objects.toString(tamanho, "") + "\u0000" + tecidoBase;
    }
}
