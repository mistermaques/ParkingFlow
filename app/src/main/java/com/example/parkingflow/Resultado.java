package com.example.parkingflow;

// Representa o estado de uma chamada à API: carregando, deu certo (com o dado) ou deu erro (com a mensagem).
// A tela observa isso para decidir se mostra a rodinha, os dados ou a mensagem de erro.
public class Resultado<T> {

    public enum Estado { CARREGANDO, SUCESSO, ERRO }

    // Quem chama o Repository recebe a resposta por aqui
    public interface Retorno<T> {
        void receber(Resultado<T> resultado);
    }

    private final Estado estado;
    private final T dado;
    private final String mensagem;

    private Resultado(Estado estado, T dado, String mensagem) {
        this.estado = estado;
        this.dado = dado;
        this.mensagem = mensagem;
    }

    // ===== Atalhos para criar cada estado =====
    public static <T> Resultado<T> carregando() {
        return new Resultado<>(Estado.CARREGANDO, null, null);
    }

    public static <T> Resultado<T> sucesso(T dado) {
        return new Resultado<>(Estado.SUCESSO, dado, null);
    }

    public static <T> Resultado<T> erro(String mensagem) {
        return new Resultado<>(Estado.ERRO, null, mensagem);
    }

    // ===== Leitura =====
    public Estado getEstado() { return estado; }
    public T getDado() { return dado; }
    public String getMensagem() { return mensagem; }

    public boolean isCarregando() { return estado == Estado.CARREGANDO; }
    public boolean isSucesso() { return estado == Estado.SUCESSO; }
    public boolean isErro() { return estado == Estado.ERRO; }
}