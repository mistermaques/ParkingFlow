package com.example.parkingflow.viewmodel;

import com.example.parkingflow.model.Reserva;
import com.example.parkingflow.model.ResumoVagas;
import com.example.parkingflow.repository.ReservaRepository;
import com.example.parkingflow.repository.Resultado;
import com.example.parkingflow.repository.VagaRepository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

// ViewModel da tela Início: busca o resumo das vagas e a próxima reserva do usuário.
// A tela só observa os dois cards (carregando / sucesso / erro).
public class InicioViewModel extends ViewModel {

    private final VagaRepository vagaRepositorio = new VagaRepository();
    private final ReservaRepository reservaRepositorio = new ReservaRepository();

    private final MutableLiveData<Resultado<ResumoVagas>> resumo = new MutableLiveData<>();
    // Sucesso com dado null = o usuário não tem reservas pendentes
    private final MutableLiveData<Resultado<Reserva>> proximaReserva = new MutableLiveData<>();

    // usuarioId fixo até existir a User API (vem do login)
    private long usuarioId;

    public LiveData<Resultado<ResumoVagas>> getResumo() { return resumo; }
    public LiveData<Resultado<Reserva>> getProximaReserva() { return proximaReserva; }

    public void setUsuarioId(long usuarioId) {
        this.usuarioId = usuarioId;
    }

    // Busca os dois cards ao mesmo tempo
    public void carregar() {
        carregarResumo();
        carregarProximaReserva();
    }

    // Card "Vagas agora": totais por status vindos da API de Vagas
    private void carregarResumo() {
        resumo.setValue(Resultado.carregando());
        vagaRepositorio.buscarResumo(resumo::setValue);
    }

    // Card "Sua próxima reserva": a PENDENTE com a entrada mais próxima
    private void carregarProximaReserva() {
        proximaReserva.setValue(Resultado.carregando());

        reservaRepositorio.listarPorUsuario(usuarioId, resultado -> {
            if (resultado.isErro()) {
                proximaReserva.setValue(Resultado.erro(resultado.getMensagem()));
                return;
            }
            proximaReserva.setValue(Resultado.sucesso(acharProxima(resultado.getDado())));
        });
    }

    // Procura, entre as pendentes, a de entrada mais cedo (ou null se não houver)
    // (as datas vêm no formato 2026-10-20T19:40:00, que dá para comparar como texto)
    private Reserva acharProxima(List<Reserva> reservas) {
        Reserva proxima = null;
        for (Reserva reserva : reservas) {
            boolean pendente = "PENDENTE".equals(reserva.getStatus()) && reserva.getDataInicio() != null;
            if (pendente && (proxima == null
                    || reserva.getDataInicio().compareTo(proxima.getDataInicio()) < 0)) {
                proxima = reserva;
            }
        }
        return proxima;
    }
}
