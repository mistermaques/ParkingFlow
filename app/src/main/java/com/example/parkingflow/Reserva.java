package com.example.parkingflow;

// Representa uma reserva, com os mesmos nomes de campos do JSON da API.
// Usada para ENVIAR (nova reserva) e para RECEBER (resposta da API).
public class Reserva {

    // ===== Campos que o app envia =====
    private Long usuarioId;
    private String placaVeiculo;
    private Long vagaId;
    private String nomeVaga;
    private String dataInicio;   // formato: 2026-10-20T19:40:00
    private String dataFim;      // formato: 2026-10-20T22:10:00
    private Double precoVaga;

    // ===== Campos que só a API preenche (vêm na resposta) =====
    private Long id;
    private String status;       // PENDENTE, EM_USO, FINALIZADA, CANCELADA
    private Double precoReserva;
    private String createdAt;
    private String checkinAt;
    private String checkoutAt;

    // Construtor usado para criar uma nova reserva no app
    public Reserva(Long usuarioId, String placaVeiculo, Long vagaId, String nomeVaga,
                   String dataInicio, String dataFim, Double precoVaga) {
        this.usuarioId = usuarioId;
        this.placaVeiculo = placaVeiculo;
        this.vagaId = vagaId;
        this.nomeVaga = nomeVaga;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.precoVaga = precoVaga;
    }

    // ===== Getters (para ler os dados da resposta) =====
    public Long getId() { return id; }
    public String getStatus() { return status; }
    public Double getPrecoReserva() { return precoReserva; }
    public Long getVagaId() { return vagaId; }
    public String getNomeVaga() { return nomeVaga; }
    public String getPlacaVeiculo() { return placaVeiculo; }
    public String getDataInicio() { return dataInicio; }
    public String getDataFim() { return dataFim; }
    public Double getPrecoVaga() { return precoVaga; }
    public String getCreatedAt() { return createdAt; }
}