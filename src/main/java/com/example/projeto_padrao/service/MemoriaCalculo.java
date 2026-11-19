package com.example.projeto_padrao.service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Formatacao dos numeros nas memorias de calculo - o texto exibido ao passar o mouse sobre um
 * valor calculado na tela. Padrao pt-BR, 2 a 4 casas (ex.: "1.234,5" -> "1.234,50", "9,175").
 */
public final class MemoriaCalculo {

    private static final DecimalFormatSymbols PT_BR = DecimalFormatSymbols.getInstance(Locale.of("pt", "BR"));

    private MemoriaCalculo() {
    }

    /** Valor em reais sem o "R$" (ex.: 9.175 -> "9,175"; 2 -> "2,00"). */
    public static String reais(BigDecimal valor) {
        return new DecimalFormat("#,##0.00##", PT_BR).format(valor);
    }

    /** Fracao como percentual (ex.: 0.95 -> "95%"; 0.975 -> "97,5%"). */
    public static String percentual(BigDecimal fracao) {
        return new DecimalFormat("#,##0.##", PT_BR).format(fracao.multiply(BigDecimal.valueOf(100))) + "%";
    }

    /** Numero simples (ex.: fator 0.9175 -> "0,9175"). */
    public static String numero(BigDecimal valor) {
        return new DecimalFormat("#,##0.########", PT_BR).format(valor);
    }
}
