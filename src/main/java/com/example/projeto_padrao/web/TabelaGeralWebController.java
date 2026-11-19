package com.example.projeto_padrao.web;

import com.example.projeto_padrao.repository.TabelaGeralItemRepository;
import com.example.projeto_padrao.service.TabelaGeralAgrupador;
import com.example.projeto_padrao.service.TabelaGeralImportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/tabela-geral")
public class TabelaGeralWebController {

    private final TabelaGeralItemRepository repository;
    private final TabelaGeralImportService importService;

    public TabelaGeralWebController(TabelaGeralItemRepository repository, TabelaGeralImportService importService) {
        this.repository = repository;
        this.importService = importService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("linhas", TabelaGeralAgrupador.agrupar(repository.findAllByOrderByOrdemAsc()));
        return "tabelageral/lista";
    }

    /**
     * Reimporta a planilha, substituindo so' as linhas de TabelaGeralItem - nao mexe em Fio,
     * Malha, Fornecedor, Servico ou CustoServico, entao nada que o usuario cadastrou na mao e'
     * perdido.
     */
    @PostMapping("/reimportar")
    public String reimportar() {
        repository.deleteAll();
        importService.importar();
        return "redirect:/tabela-geral";
    }
}
