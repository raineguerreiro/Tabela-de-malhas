package com.example.projeto_padrao.service;

import java.math.BigDecimal;

/** Um valor calculado e a sua memoria de calculo (a conta com os numeros, exibida ao passar o mouse). */
public record ValorCalculado(BigDecimal valor, String memoria) {
}
