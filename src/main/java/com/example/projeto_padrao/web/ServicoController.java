package com.example.projeto_padrao.web;

import com.example.projeto_padrao.domain.Servico;
import com.example.projeto_padrao.repository.ServicoRepository;
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
@RequestMapping("/api/servicos")
public class ServicoController {

    private final ServicoRepository repository;

    public ServicoController(ServicoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Servico> listar() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Servico buscar(@PathVariable Long id) {
        return repository.findById(id).orElseThrow(NoSuchElementException::new);
    }

    @PostMapping
    public Servico criar(@Valid @RequestBody Servico servico) {
        servico.setId(null);
        return repository.save(servico);
    }

    @PutMapping("/{id}")
    public Servico atualizar(@PathVariable Long id, @Valid @RequestBody Servico servico) {
        Servico existente = repository.findById(id).orElseThrow(NoSuchElementException::new);
        existente.setNome(servico.getNome());
        existente.setOrdemFluxo(servico.getOrdemFluxo());
        return repository.save(existente);
    }

    @DeleteMapping("/{id}")
    public void remover(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
