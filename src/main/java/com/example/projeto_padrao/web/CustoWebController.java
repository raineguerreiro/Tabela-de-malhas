package com.example.projeto_padrao.web;

import com.example.projeto_padrao.domain.CustoServico;
import com.example.projeto_padrao.domain.Fornecedor;
import com.example.projeto_padrao.domain.Malha;
import com.example.projeto_padrao.domain.OrigemCusto;
import com.example.projeto_padrao.domain.Servico;
import com.example.projeto_padrao.repository.CustoServicoRepository;
import com.example.projeto_padrao.repository.FornecedorRepository;
import com.example.projeto_padrao.repository.MalhaRepository;
import com.example.projeto_padrao.repository.ServicoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("/custos")
public class CustoWebController {

    private final CustoServicoRepository custoServicoRepository;
    private final MalhaRepository malhaRepository;
    private final ServicoRepository servicoRepository;
    private final FornecedorRepository fornecedorRepository;

    public CustoWebController(CustoServicoRepository custoServicoRepository, MalhaRepository malhaRepository,
            ServicoRepository servicoRepository, FornecedorRepository fornecedorRepository) {
        this.custoServicoRepository = custoServicoRepository;
        this.malhaRepository = malhaRepository;
        this.servicoRepository = servicoRepository;
        this.fornecedorRepository = fornecedorRepository;
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("malhas", malhaRepository.findAll());
        model.addAttribute("servicos", servicoRepository.findAll());
        model.addAttribute("fornecedores", fornecedorRepository.findAll());
        model.addAttribute("custos", custoServicoRepository.findAll());
        return "custos/form";
    }

    @PostMapping("/salvar")
    public String salvar(@RequestParam Long malhaId, @RequestParam Long servicoId, @RequestParam Long fornecedorId,
            @RequestParam BigDecimal valorUnitario, @RequestParam BigDecimal quantidade,
            @RequestParam(required = false) String unidadeMedida,
            @RequestParam(required = false) LocalDate dataReferencia) {

        Malha malha = malhaRepository.findById(malhaId).orElseThrow(NoSuchElementException::new);
        Servico servico = servicoRepository.findById(servicoId).orElseThrow(NoSuchElementException::new);
        Fornecedor fornecedor = fornecedorRepository.findById(fornecedorId).orElseThrow(NoSuchElementException::new);

        CustoServico custo = new CustoServico();
        custo.setMalha(malha);
        custo.setServico(servico);
        custo.setFornecedor(fornecedor);
        custo.setValorUnitario(valorUnitario);
        custo.setQuantidade(quantidade);
        custo.setUnidadeMedida(unidadeMedida);
        custo.setDataReferencia(dataReferencia != null ? dataReferencia : LocalDate.now());
        custo.setOrigem(OrigemCusto.MANUAL);
        custoServicoRepository.save(custo);

        return "redirect:/custos/novo";
    }
}
