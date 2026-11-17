package com.example.parkingflow.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.parkingflow.model.Reserva;
import com.example.parkingflow.repository.ReservaRepository;
import com.example.parkingflow.repository.Resultado;

import java.util.ArrayList;
import java.util.List;

// ViewModel de "Minhas reservas": busca a lista, ordena e cancela reservas.
// A tela só observa a lista (carregando / sucesso / erro) e os avisos (Toast).
public class MinhasReservasViewModel extends ViewModel {

    private final ReservaRepository repositorio = new ReservaRepository();

    private final MutableLiveData<Resultado<List<Reserva>>> reservas = new MutableLiveData<>();
    private final MutableLiveData<String> aviso = new MutableLiveData<>();

    // usuarioId fixo até existir a User API (vem do login)
    private long usuarioId;

    public LiveData<Resultado<List<Reserva>>> getReservas() { return reservas; }
    public LiveData<String> getAviso() { return aviso; }

    public void setUsuarioId(long usuarioId) {
        this.usuarioId = usuarioId;
    }

    // Busca as reservas do usuário na API
    public void carregar() {
        reservas.setValue(Resultado.carregando());

        repositorio.listarPorUsuario(usuarioId, resultado -> {
            if (resultado.isSucesso()) {
                // Mais recentes primeiro (maior id no topo)
                List<Reserva> ordenada = new ArrayList<>(resultado.getDado());
                ordenada.sort((a, b) -> Long.compare(b.getId(), a.getId()));
                reservas.setValue(Resultado.sucesso(ordenada));
            } else {
                reservas.setValue(resultado);
            }
        });
    }

    // Cancela uma reserva PENDENTE; se o motivo vier vazio, usa o texto padrão
    public void cancelar(Reserva reserva, String motivoDigitado) {
        String motivo = motivoDigitado == null ? "" : motivoDigitado.trim();
        if (motivo.isEmpty()) motivo = "Cancelada pelo motorista pelo app";

        repositorio.cancelar(reserva.getId(), motivo, resultado -> {
            if (resultado.isSucesso()) {
                trocarNaLista(resultado.getDado());
                aviso.setValue("Reserva #" + reserva.getId() + " cancelada.");
            } else {
                aviso.setValue("Não foi possível cancelar: " + resultado.getMensagem());
            }
        });
    }

    // A tela chama depois de mostrar o Toast, para ele não aparecer de novo ao girar o celular
    public void avisoExibido() {
        aviso.setValue(null);
    }

    // Troca a reserva cancelada na lista atual; se não achar, recarrega tudo da API
    private void trocarNaLista(Reserva atualizada) {
        Resultado<List<Reserva>> atual = reservas.getValue();
        if (atual == null || !atual.isSucesso()) {
            carregar();
            return;
        }

        List<Reserva> nova = new ArrayList<>(atual.getDado());
        for (int i = 0; i < nova.size(); i++) {
            if (nova.get(i).getId().equals(atualizada.getId())) {
                nova.set(i, atualizada);
                reservas.setValue(Resultado.sucesso(nova));
                return;
            }
        }
        carregar();
    }
}
