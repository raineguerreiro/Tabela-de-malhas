package com.example.projeto_padrao.web;

import com.example.projeto_padrao.domain.Servico;
import com.example.projeto_padrao.repository.ServicoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.NoSuchElementException;

@Controller
@RequestMapping("/servicos")
public class ServicoWebController {

    private final ServicoRepository repository;

    public ServicoWebController(ServicoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("servicos", repository.findAll());
        return "servicos/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("servico", new Servico());
        return "servicos/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("servico", repository.findById(id).orElseThrow(NoSuchElementException::new));
        return "servicos/form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Servico servico) {
        repository.save(servico);
        return "redirect:/servicos";
    }
}
