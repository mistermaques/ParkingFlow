package com.example.parkingflow;

import java.util.Locale;

// Representa uma vaga, com os mesmos nomes de campos do JSON da API de Vagas
public class Vaga {

    private Long id;
    private String nomeVaga;     // ex: A10
    private Double precoHora;    // ex: 10.00
    private String status;       // DISPONIVEL, OCUPADA, INDISPONIVEL
    private String createdAt;

    // ===== Getters =====
    public Long getId() { return id; }
    public String getNomeVaga() { return nomeVaga; }
    public Double getPrecoHora() { return precoHora; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }

    // Texto mostrado em cada opção da lista suspensa: "A10 · R$ 10,00/h"
    @Override
    public String toString() {
        double preco = (precoHora == null) ? 0 : precoHora;
        return nomeVaga + " · " + String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f/h", preco);
    }
}