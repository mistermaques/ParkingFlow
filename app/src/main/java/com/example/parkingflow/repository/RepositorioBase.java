package com.example.parkingflow.repository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Base dos Repositories: transforma a resposta do Retrofit em um Resultado (sucesso ou erro).
// Assim a leitura do erro da API fica em um lugar só, em vez de repetida em cada tela.
abstract class RepositorioBase {

    // Cria o Callback do Retrofit que repassa a resposta já como Resultado
    protected <T> Callback<T> repassar(Resultado.Retorno<T> retorno) {
        return new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> response) {
                if (response.isSuccessful() && response.body() != null) {
                    retorno.receber(Resultado.sucesso(response.body()));
                } else {
                    retorno.receber(Resultado.erro(lerErro(response)));
                }
            }

            // Sem internet, servidor fora do ar etc.
            @Override
            public void onFailure(Call<T> call, Throwable t) {
                retorno.receber(Resultado.erro("Sem conexão com o servidor. Tente novamente."));
            }
        };
    }

    // Lê a mensagem que a API manda explicando a recusa; se não vier nada, usa "erro 500" etc.
    private String lerErro(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String texto = response.errorBody().string();
                if (!texto.trim().isEmpty()) return texto;
            }
        } catch (Exception ignorado) {
            // cai no texto padrão abaixo
        }
        return "erro " + response.code();
    }
}