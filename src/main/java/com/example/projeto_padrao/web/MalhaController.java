package com.example.projeto_padrao.web;

import com.example.projeto_padrao.domain.Malha;
import com.example.projeto_padrao.repository.MalhaRepository;
import com.example.projeto_padrao.service.CustoCalculoService;
import com.example.projeto_padrao.service.CustoTotalMalha;
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
@RequestMapping("/api/malhas")
public class MalhaController {

    private final MalhaRepository repository;
    private final CustoCalculoService custoCalculoService;

    public MalhaController(MalhaRepository repository, CustoCalculoService custoCalculoService) {
        this.repository = repository;
        this.custoCalculoService = custoCalculoService;
    }

    @GetMapping
    public List<Malha> listar() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Malha buscar(@PathVariable Long id) {
        return repository.findById(id).orElseThrow(NoSuchElementException::new);
    }

    @PostMapping
    public Malha criar(@Valid @RequestBody Malha malha) {
        malha.setId(null);
        return repository.save(malha);
    }

    @PutMapping("/{id}")
    public Malha atualizar(@PathVariable Long id, @Valid @RequestBody Malha malha) {
        Malha existente = repository.findById(id).orElseThrow(NoSuchElementException::new);
        existente.setCodigo(malha.getCodigo());
        existente.setNome(malha.getNome());
        existente.setComposicaoFio(malha.getComposicaoFio());
        existente.setGramatura(malha.getGramatura());
        existente.setLargura(malha.getLargura());
        existente.setTipoPonto(malha.getTipoPonto());
        existente.setCorEstampa(malha.getCorEstampa());
        existente.setObservacoes(malha.getObservacoes());
        return repository.save(existente);
    }

    @DeleteMapping("/{id}")
    public void remover(@PathVariable Long id) {
        repository.deleteById(id);
    }

    @GetMapping("/{id}/custo-total")
    public CustoTotalMalha custoTotal(@PathVariable Long id) {
        return custoCalculoService.calcularCustoTotal(id);
    }
}
