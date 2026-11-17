package com.example.parkingflow.repository;

import com.example.parkingflow.api.VagaApi;
import com.example.parkingflow.model.ResumoVagas;
import com.example.parkingflow.model.Vaga;

import java.util.List;

// Único lugar do app que conversa com a API de Vagas.
public class VagaRepository extends RepositorioBase {

    private final VagaApi api = VagaApi.criar();

    // GET /vagas/status/DISPONIVEL
    public void listarDisponiveis(Resultado.Retorno<List<Vaga>> retorno) {
        api.listarDisponiveis().enqueue(repassar(retorno));
    }

    // GET /vagas/resumo
    public void buscarResumo(Resultado.Retorno<ResumoVagas> retorno) {
        api.buscarResumo().enqueue(repassar(retorno));
    }
}