package com.example.projeto_padrao.web;

import com.example.projeto_padrao.domain.CustoServico;
import com.example.projeto_padrao.domain.OrigemCusto;
import com.example.projeto_padrao.repository.CustoServicoRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/custos")
public class CustoServicoController {

    private final CustoServicoRepository repository;

    public CustoServicoController(CustoServicoRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<CustoServico> listar() {
        return repository.findAll();
    }

    @PostMapping
    public CustoServico criar(@Valid @RequestBody CustoServico custo) {
        custo.setId(null);
        custo.setOrigem(OrigemCusto.MANUAL);
        return repository.save(custo);
    }

    @DeleteMapping("/{id}")
    public void remover(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
