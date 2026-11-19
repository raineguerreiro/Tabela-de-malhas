package com.example.projeto_padrao.service;

import java.math.BigDecimal;

/**
 * Uma linha do "Banco de dados" da aba Rotativos: combinacao unica referencia + tamanho + tecido
 * base vinda dos rotativos, com o Preco digitado (null se ainda nao preenchido) e o Preco
 * Contratipo calculado (Preco x 0,9175), com a memoria de calculo exibida ao passar o mouse.
 */
public record BancoDadosRotativoLinha(String referencia, String tamanho, String tecidoBase, BigDecimal preco,
        BigDecimal precoContratipo, String memoriaPrecoContratipo) {
}
