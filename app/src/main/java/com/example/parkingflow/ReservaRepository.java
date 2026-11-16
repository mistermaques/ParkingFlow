package com.example.parkingflow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Único lugar do app que conversa com a API de Reservas.
// Os ViewModels pedem os dados aqui e recebem um Resultado.
public class ReservaRepository extends RepositorioBase {

    private final ReservaApi api = ReservaApi.criar();

    // GET /reservas/usuario/{usuarioId}
    public void listarPorUsuario(long usuarioId, Resultado.Retorno<List<Reserva>> retorno) {
        api.listarPorUsuario(usuarioId).enqueue(repassar(retorno));
    }

    // POST /reservas/nova-reserva
    public void criar(Reserva reserva, Resultado.Retorno<Reserva> retorno) {
        api.criarReserva(reserva).enqueue(repassar(retorno));
    }

    // PATCH /reservas/{id}/cancelar (só funciona para reservas PENDENTE)
    public void cancelar(long reservaId, String motivo, Resultado.Retorno<Reserva> retorno) {
        Map<String, String> corpo = new HashMap<>();
        corpo.put("motivo", motivo);
        api.cancelarReserva(reservaId, corpo).enqueue(repassar(retorno));
    }
}