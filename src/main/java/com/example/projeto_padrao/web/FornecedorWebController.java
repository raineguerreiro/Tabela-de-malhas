package com.example.projeto_padrao.web;

import com.example.projeto_padrao.domain.Fornecedor;
import com.example.projeto_padrao.repository.FornecedorRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.NoSuchElementException;

@Controller
@RequestMapping("/fornecedores")
public class FornecedorWebController {

    private final FornecedorRepository repository;

    public FornecedorWebController(FornecedorRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("fornecedores", repository.findAll());
        return "fornecedores/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("fornecedor", new Fornecedor());
        return "fornecedores/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("fornecedor", repository.findById(id).orElseThrow(NoSuchElementException::new));
        return "fornecedores/form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Fornecedor fornecedor) {
        repository.save(fornecedor);
        return "redirect:/fornecedores";
    }
}
