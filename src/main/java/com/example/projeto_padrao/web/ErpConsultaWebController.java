package com.example.projeto_padrao.web;

import com.example.projeto_padrao.domain.ErpConsultaConfig;
import com.example.projeto_padrao.repository.ErpConsultaConfigRepository;
import com.example.projeto_padrao.service.ErpSyncService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

@Controller
@RequestMapping("/erp/consultas")
public class ErpConsultaWebController {

    private final ErpConsultaConfigRepository repository;
    private final ErpSyncService erpSyncService;

    public ErpConsultaWebController(ErpConsultaConfigRepository repository, ErpSyncService erpSyncService) {
        this.repository = repository;
        this.erpSyncService = erpSyncService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("consultas", repository.findAll());
        return "erp/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("config", new ErpConsultaConfig());
        return "erp/form";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("config", repository.findById(id).orElseThrow(NoSuchElementException::new));
        return "erp/form";
    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute ErpConsultaConfig config) {
        repository.save(config);
        return "redirect:/erp/consultas";
    }

    @PostMapping("/{id}/sincronizar")
    public String sincronizar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        ErpConsultaConfig config = repository.findById(id).orElseThrow(NoSuchElementException::new);
        try {
            int importados = erpSyncService.sincronizar(config);
            repository.save(config);
            redirectAttributes.addFlashAttribute("mensagem", "Sincronizacao concluida: " + importados + " registro(s) importado(s).");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("mensagem", e.getMessage());
        }
        return "redirect:/erp/consultas";
    }
}
