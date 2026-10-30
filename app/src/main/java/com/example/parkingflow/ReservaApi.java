package com.example.parkingflow;

import java.util.List;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

// Diz ao Retrofit onde está a API e quais rotas o app usa
public interface ReservaApi {

    // Endereço da API no Azure (precisa terminar com "/")
    String URL_BASE = "https://api-xsquad-vtr-c3etamghdsgug0ds.brazilsouth-01.azurewebsites.net/";

    // POST /reservas/nova-reserva -> envia uma Reserva e recebe a Reserva criada
    @POST("reservas/nova-reserva")
    Call<Reserva> criarReserva(@Body Reserva reserva);


    // GET /reservas/usuario/{usuarioId} -> lista as reservas de um usuário
    @GET("reservas/usuario/{usuarioId}")
    Call<List<Reserva>> listarPorUsuario(@Path("usuarioId") long usuarioId);

    // Monta o Retrofit pronto para uso
    static ReservaApi criar() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL_BASE)
                .addConverterFactory(GsonConverterFactory.create()) // converte JSON <-> Java
                .build();

        return retrofit.create(ReservaApi.class);
    }
}