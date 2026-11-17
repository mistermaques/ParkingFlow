package com.example.parkingflow;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

// Diz ao Retrofit onde está a API de Vagas e quais rotas o app usa
public interface VagaApi {

    // Endereço da API de Vagas no Azure (precisa terminar com "/")
    String URL_BASE = "https://api-xsquad-mts-asfmf9eyg4bsd8d0.brazilsouth-01.azurewebsites.net/";

    // GET /vagas/status/DISPONIVEL -> lista só as vagas livres para reservar


    // GET /vagas/status/DISPONIVEL -> lista só as vagas livres para reservar
    @GET("vagas/status/DISPONIVEL")
    Call<List<Vaga>> listarDisponiveis();

    // GET /vagas/resumo -> totais de vagas por status
    @GET("vagas/resumo")
    Call<ResumoVagas> buscarResumo();




    // Monta o Retrofit pronto para uso
    static VagaApi criar() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL_BASE)
                .addConverterFactory(GsonConverterFactory.create()) // converte JSON <-> Java
                .build();

        return retrofit.create(VagaApi.class);
    }
}
