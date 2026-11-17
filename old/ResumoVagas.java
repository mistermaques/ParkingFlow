package com.example.parkingflow;

// Totais de vagas por status, com os mesmos nomes de campos do JSON de /vagas/resumo
// Ex: {"total":3,"disponiveis":3,"ocupadas":0,"indisponiveis":0}
public class ResumoVagas {

    private Integer total;
    private Integer disponiveis;
    private Integer ocupadas;
    private Integer indisponiveis;

    // ===== Getters =====
    public Integer getTotal() { return total; }
    public Integer getDisponiveis() { return disponiveis; }
    public Integer getOcupadas() { return ocupadas; }
    public Integer getIndisponiveis() { return indisponiveis; }
}