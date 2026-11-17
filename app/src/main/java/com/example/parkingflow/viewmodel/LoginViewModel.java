package com.example.parkingflow.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.parkingflow.model.Usuario;
import com.example.parkingflow.repository.UsuarioRepository;

// ViewModel do login: valida o que foi digitado e avisa a tela do resultado.
// A tela só observa: erro no usuário, erro na senha ou usuário logado.
public class LoginViewModel extends ViewModel {

    private final UsuarioRepository repositorio = new UsuarioRepository();

    private final MutableLiveData<String> erroUsuario = new MutableLiveData<>();
    private final MutableLiveData<String> erroSenha = new MutableLiveData<>();
    private final MutableLiveData<Usuario> usuarioLogado = new MutableLiveData<>();

    // A tela recebe a versão "só leitura" (LiveData), quem altera é só o ViewModel
    public LiveData<String> getErroUsuario() { return erroUsuario; }
    public LiveData<String> getErroSenha() { return erroSenha; }
    public LiveData<Usuario> getUsuarioLogado() { return usuarioLogado; }

    public void entrar(String loginDigitado, String senhaDigitada) {
        // Limpa os erros da tentativa anterior
        erroUsuario.setValue(null);
        erroSenha.setValue(null);

        // O usuário não diferencia maiúsculas e minúsculas ("Sophia" = "sophia")
        String login = loginDigitado == null ? "" : loginDigitado.trim().toLowerCase();
        String senha = senhaDigitada == null ? "" : senhaDigitada;

        if (login.isEmpty()) {
            erroUsuario.setValue("Informe o usuário.");
            return;
        }
        if (senha.isEmpty()) {
            erroSenha.setValue("Informe a senha.");
            return;
        }

        Usuario encontrado = repositorio.autenticar(login, senha);
        if (encontrado == null) {
            erroSenha.setValue("Usuário ou senha inválidos.");
            return;
        }

        // Login certo -> a tela percebe e abre a tela principal
        usuarioLogado.setValue(encontrado);
    }
}
