package com.example.parkingflow.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// Utilitário: todas as formatações de data, dinheiro e número do app em um lugar só.
// Antes estavam repetidas na MainActivity e no NovaReservaViewModel.
public final class Formatos {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final String PADRAO_TELA = "dd/MM/yyyy  HH:mm";     // 20/10/2026  19:40
    private static final String PADRAO_API = "yyyy-MM-dd'T'HH:mm:ss";  // 2026-10-20T19:40:00

    // Ninguém precisa criar um "Formatos": todos os métodos são static
    private Formatos() {
    }

    // Data para mostrar na tela. Ex: "20/10/2026  19:40"
    public static String data(Date data) {
        return new SimpleDateFormat(PADRAO_TELA, PT_BR).format(data);
    }

    // Data no formato que a API espera. Ex: "2026-10-20T19:40:00"
    public static String dataParaApi(Date data) {
        return new SimpleDateFormat(PADRAO_API, Locale.US).format(data);
    }

    // Data que veio da API, convertida para a tela. Ex: "2026-10-20T19:40:00" -> "20/10/2026  19:40"
    public static String dataDaApi(String dataApi) {
        if (dataApi == null) return "-";
        try {
            Date data = new SimpleDateFormat(PADRAO_API, Locale.US).parse(dataApi);
            return data == null ? dataApi : data(data);
        } catch (Exception e) {
            return dataApi;
        }
    }

    // Ex: 30.0 -> "R$ 30,00"
    public static String dinheiro(double valor) {
        return String.format(PT_BR, "R$ %.2f", valor);
    }

    // Igual ao dinheiro(), mas mostra "-" se a API não mandar o valor
    public static String dinheiroOuTraco(Double valor) {
        return valor == null ? "-" : dinheiro(valor);
    }

    // Valor sem o "R$", para o campo de preço por hora. Ex: 10.0 -> "10,00"
    public static String preco(double valor) {
        return String.format(PT_BR, "%.2f", valor);
    }

    // Evita mostrar "null" na tela se a API não mandar algum número
    public static String numero(Integer valor) {
        return valor == null ? "0" : String.valueOf(valor);
    }
}
