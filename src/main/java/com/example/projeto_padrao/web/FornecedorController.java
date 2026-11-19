package com.example.projeto_padrao.web;

import com.example.projeto_padrao.domain.Fornecedor;
import com.example.projeto_padrao.repository.FornecedorRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/fornecedores")
public class FornecedorController {

    private final FornecedorRepository repository;

    public FornecedorController(FornecedorRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Fornecedor> listar() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Fornecedor buscar(@PathVariable Long id) {
        return repository.findById(id).orElseThrow(NoSuchElementException::new);
    }

    @PostMapping
    public Fornecedor criar(@Valid @RequestBody Fornecedor fornecedor) {
        fornecedor.setId(null);
        return repository.save(fornecedor);
    }

    @PutMapping("/{id}")
    public Fornecedor atualizar(@PathVariable Long id, @Valid @RequestBody Fornecedor fornecedor) {
        Fornecedor existente = repository.findById(id).orElseThrow(NoSuchElementException::new);
        existente.setNome(fornecedor.getNome());
        existente.setAtivo(fornecedor.isAtivo());
        return repository.save(existente);
    }

    @DeleteMapping("/{id}")
    public void remover(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
