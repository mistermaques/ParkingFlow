package com.example.parkingflow.viewmodel;

import com.example.parkingflow.model.Reserva;
import com.example.parkingflow.model.Vaga;
import com.example.parkingflow.repository.ReservaRepository;
import com.example.parkingflow.repository.Resultado;
import com.example.parkingflow.repository.VagaRepository;
import com.example.parkingflow.util.Formatos;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Calendar;
import java.util.List;

// ViewModel de "Nova reserva": guarda o formulário (vaga, placa, datas), valida,
// calcula duração e total e envia a reserva para a API.
// Como o ViewModel sobrevive ao girar o celular, o formulário não se perde mais.
public class NovaReservaViewModel extends ViewModel {

    private final VagaRepository vagaRepositorio = new VagaRepository();
    private final ReservaRepository reservaRepositorio = new ReservaRepository();

    // ===== Dados do formulário =====
    private final Calendar entrada = Calendar.getInstance();
    private final Calendar saida = Calendar.getInstance();
    private Vaga vagaEscolhida;
    private String placa = "";

    // usuarioId fixo até existir a User API (vem do login)
    private long usuarioId;

    // ===== O que a tela observa =====
    private final MutableLiveData<Resultado<List<Vaga>>> vagas = new MutableLiveData<>();
    private final MutableLiveData<String> entradaTexto = new MutableLiveData<>();
    private final MutableLiveData<String> saidaTexto = new MutableLiveData<>();
    private final MutableLiveData<String> erroEntrada = new MutableLiveData<>();
    private final MutableLiveData<String> erroSaida = new MutableLiveData<>();
    private final MutableLiveData<String> valorHora = new MutableLiveData<>();
    private final MutableLiveData<Boolean> podeContinuar = new MutableLiveData<>(false);
    private final MutableLiveData<Resultado<Reserva>> envio = new MutableLiveData<>();

    public NovaReservaViewModel() {
        valorHora.setValue("0,00");
        definirDatasPadrao();
    }

    public LiveData<Resultado<List<Vaga>>> getVagas() { return vagas; }
    public LiveData<String> getEntradaTexto() { return entradaTexto; }
    public LiveData<String> getSaidaTexto() { return saidaTexto; }
    public LiveData<String> getErroEntrada() { return erroEntrada; }
    public LiveData<String> getErroSaida() { return erroSaida; }
    public LiveData<String> getValorHora() { return valorHora; }
    public LiveData<Boolean> getPodeContinuar() { return podeContinuar; }
    public LiveData<Resultado<Reserva>> getEnvio() { return envio; }

    public void setUsuarioId(long usuarioId) {
        this.usuarioId = usuarioId;
    }

    // ======================= VAGAS =======================

    // Busca as vagas DISPONIVEL na API
    public void carregarVagas() {
        vagas.setValue(Resultado.carregando());
        vagaRepositorio.listarDisponiveis(vagas::setValue);
    }

    // Escolheu uma vaga na lista -> guarda, mostra o preço e revalida
    public void escolherVaga(Vaga vaga) {
        vagaEscolhida = vaga;
        valorHora.setValue(Formatos.preco(precoDaVaga()));
        validar();
    }

    // Preço por hora da vaga escolhida (0 se nenhuma foi escolhida)
    private double precoDaVaga() {
        if (vagaEscolhida == null || vagaEscolhida.getPrecoHora() == null) return 0;
        return vagaEscolhida.getPrecoHora();
    }

    // ======================= PLACA =======================

    public void setPlaca(String placaDigitada) {
        placa = placaDigitada == null ? "" : placaDigitada;
        validar();
    }

    // ======================= DATAS =======================

    // Cópias, para a tela abrir o calendário no dia/hora atuais sem mexer nos originais
    public Calendar getEntrada() { return (Calendar) entrada.clone(); }
    public Calendar getSaida() { return (Calendar) saida.clone(); }

    // Mudou a entrada -> a saída vai junto, 1 hora depois
    public void definirEntrada(int ano, int mes, int dia, int hora, int minuto) {
        entrada.set(ano, mes, dia, hora, minuto);
        saida.setTime(entrada.getTime());
        saida.add(Calendar.HOUR_OF_DAY, 1);
        atualizarDatas();
    }

    public void definirSaida(int ano, int mes, int dia, int hora, int minuto) {
        saida.set(ano, mes, dia, hora, minuto);
        atualizarDatas();
    }

    // Entrada na próxima meia hora (hoje), com 15 min de folga (ex: 14:07 -> 14:30; 14:20 -> 15:00)
    // e saída 1 hora depois
    private void definirDatasPadrao() {
        entrada.setTimeInMillis(System.currentTimeMillis());
        entrada.set(Calendar.SECOND, 0);
        entrada.set(Calendar.MILLISECOND, 0);
        entrada.add(Calendar.MINUTE, 15);
        int minuto = entrada.get(Calendar.MINUTE);
        entrada.set(Calendar.MINUTE, 0);
        entrada.add(Calendar.MINUTE, minuto <= 30 ? 30 : 60);
        saida.setTime(entrada.getTime());
        saida.add(Calendar.HOUR_OF_DAY, 1);
        atualizarDatas();
    }

    // Atualiza os textos das datas e os erros (entrada no passado / saída antes da entrada)
    private void atualizarDatas() {
        entradaTexto.setValue(Formatos.data(entrada.getTime()));
        saidaTexto.setValue(Formatos.data(saida.getTime()));

        // A API não aceita entrada no passado
        erroEntrada.setValue(entrada.after(Calendar.getInstance())
                ? null : "A entrada precisa ser depois do horário atual.");
        erroSaida.setValue(saida.after(entrada)
                ? null : "A saída precisa ser depois da entrada.");
        validar();
    }

    // ======================= VALIDAÇÃO =======================

    // Continuar só fica ativo com vaga escolhida, placa válida e datas válidas
    private void validar() {
        boolean temVaga = (vagaEscolhida != null);
        boolean temPlaca = placa.replace("-", "").length() == 7;
        boolean entradaNoFuturo = entrada.after(Calendar.getInstance());
        podeContinuar.setValue(temVaga && temPlaca && entradaNoFuturo && saida.after(entrada));
    }

    // ======================= RESUMO (ETAPA 2) =======================

    public String getNomeVaga() {
        return vagaEscolhida == null ? "-" : vagaEscolhida.getNomeVaga();
    }

    public String getPlacaMaiuscula() {
        return placa.toUpperCase();
    }

    // Ex: "2h 30min", "45min", "3h"
    public String getDuracao() {
        long minutosTotais = (saida.getTimeInMillis() - entrada.getTimeInMillis()) / 60000;
        long horas = minutosTotais / 60;
        long minutos = minutosTotais % 60;

        if (horas == 0) return minutos + "min";
        if (minutos == 0) return horas + "h";
        return horas + "h " + minutos + "min";
    }

    // Mesma regra da API: cada hora iniciada é cobrada inteira (1h30 = 2h). Ex: "R$ 30,00"
    public String getTotalFormatado() {
        long minutos = (saida.getTimeInMillis() - entrada.getTimeInMillis()) / 60000;
        double total = 0;
        if (minutos > 0) {
            long horasCobradas = (minutos + 59) / 60;
            total = horasCobradas * precoDaVaga();
        }
        return Formatos.dinheiro(total);
    }

    // ======================= ENVIO =======================

    // Confirmar -> envia a reserva para a API
    public void enviar() {
        if (vagaEscolhida == null) return;
        envio.setValue(Resultado.carregando());

        Reserva nova = new Reserva(
                usuarioId,
                placa.toUpperCase(),
                vagaEscolhida.getId(),
                vagaEscolhida.getNomeVaga(),
                Formatos.dataParaApi(entrada.getTime()),
                Formatos.dataParaApi(saida.getTime()),
                precoDaVaga()
        );

        reservaRepositorio.criar(nova, envio::setValue);
    }

    // A tela chama depois de tratar o resultado, para ele não repetir ao girar o celular
    public void envioTratado() {
        envio.setValue(null);
    }

    // Volta o formulário ao estado inicial e atualiza as vagas
    public void limpar() {
        vagaEscolhida = null;
        placa = "";
        valorHora.setValue("0,00");
        definirDatasPadrao();
        carregarVagas();
    }
}
