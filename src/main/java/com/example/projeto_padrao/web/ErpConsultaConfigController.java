package com.example.projeto_padrao.web;

import com.example.projeto_padrao.domain.ErpConsultaConfig;
import com.example.projeto_padrao.repository.ErpConsultaConfigRepository;
import com.example.projeto_padrao.service.ErpSyncService;
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
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/erp/consultas")
public class ErpConsultaConfigController {

    private final ErpConsultaConfigRepository repository;
    private final ErpSyncService erpSyncService;

    public ErpConsultaConfigController(ErpConsultaConfigRepository repository, ErpSyncService erpSyncService) {
        this.repository = repository;
        this.erpSyncService = erpSyncService;
    }

    @GetMapping
    public List<ErpConsultaConfig> listar() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ErpConsultaConfig buscar(@PathVariable Long id) {
        return repository.findById(id).orElseThrow(NoSuchElementException::new);
    }

    @PostMapping
    public ErpConsultaConfig criar(@Valid @RequestBody ErpConsultaConfig config) {
        config.setId(null);
        return repository.save(config);
    }

    @PutMapping("/{id}")
    public ErpConsultaConfig atualizar(@PathVariable Long id, @Valid @RequestBody ErpConsultaConfig config) {
        ErpConsultaConfig existente = repository.findById(id).orElseThrow(NoSuchElementException::new);
        existente.setNome(config.getNome());
        existente.setSqlConsulta(config.getSqlConsulta());
        existente.setColunaMalhaCodigo(config.getColunaMalhaCodigo());
        existente.setColunaServicoNome(config.getColunaServicoNome());
        existente.setColunaFornecedorNome(config.getColunaFornecedorNome());
        existente.setColunaValorUnitario(config.getColunaValorUnitario());
        existente.setColunaQuantidade(config.getColunaQuantidade());
        existente.setColunaUnidadeMedida(config.getColunaUnidadeMedida());
        existente.setColunaDataReferencia(config.getColunaDataReferencia());
        existente.setAtivo(config.isAtivo());
        return repository.save(existente);
    }

    @DeleteMapping("/{id}")
    public void remover(@PathVariable Long id) {
        repository.deleteById(id);
    }

    @PostMapping("/{id}/sincronizar")
    public Map<String, Object> sincronizar(@PathVariable Long id) {
        ErpConsultaConfig config = repository.findById(id).orElseThrow(NoSuchElementException::new);
        int importados = erpSyncService.sincronizar(config);
        repository.save(config);
        return Map.of("registrosImportados", importados);
    }
}
