package com.example.parkingflow;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

// Tela de login simples: sem banco e sem API, com dois usuários fixos no código.
// Ao entrar, abre a MainActivity passando o ID e o nome do usuário.
public class LoginActivity extends AppCompatActivity {

    // Nomes usados para "entregar" os dados do usuário para a MainActivity
    public static final String EXTRA_USUARIO_ID = "usuarioId";
    public static final String EXTRA_NOME_USUARIO = "nomeUsuario";

    // Representa um login fixo: usuário, senha, nome exibido e ID usado nas APIs
    private static class Usuario {
        final String login;
        final String senha;
        final String nome;
        final long id;

        Usuario(String login, String senha, String nome, long id) {
            this.login = login;
            this.senha = senha;
            this.nome = nome;
            this.id = id;
        }
    }

    // ===== Logins fixos (até existir a User API) =====
    private static final Usuario[] USUARIOS = {
            new Usuario("sophia", "1234", "Sophia", 1L),
            new Usuario("gabriel", "1234", "Gabriel", 4L)
    };

    private TextInputEditText campoUsuario, campoSenha;
    private TextInputLayout layoutUsuario, layoutSenha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        campoUsuario = findViewById(R.id.campoUsuario);
        campoSenha = findViewById(R.id.campoSenha);
        layoutUsuario = findViewById(R.id.layoutUsuario);
        layoutSenha = findViewById(R.id.layoutSenha);
        MaterialButton btnEntrar = findViewById(R.id.btnEntrar);

        // Tocar em Entrar -> tenta fazer o login
        btnEntrar.setOnClickListener(v -> entrar());

        // Apertar "OK" no teclado depois de digitar a senha também faz o login
        campoSenha.setOnEditorActionListener((v, acao, evento) -> {
            if (acao == EditorInfo.IME_ACTION_DONE) {
                entrar();
                return true;
            }
            return false;
        });
    }

    private void entrar() {
        // Limpa os erros da tentativa anterior
        layoutUsuario.setError(null);
        layoutSenha.setError(null);

        // O usuário não diferencia maiúsculas e minúsculas ("Sophia" = "sophia")
        String login = campoUsuario.getText() == null ? "" : campoUsuario.getText().toString().trim().toLowerCase();
        String senha = campoSenha.getText() == null ? "" : campoSenha.getText().toString();

        if (login.isEmpty()) {
            layoutUsuario.setError("Informe o usuário.");
            return;
        }
        if (senha.isEmpty()) {
            layoutSenha.setError("Informe a senha.");
            return;
        }

        // Procura um usuário com esse login e senha
        Usuario encontrado = null;
        for (Usuario usuario : USUARIOS) {
            if (usuario.login.equals(login) && usuario.senha.equals(senha)) {
                encontrado = usuario;
                break;
            }
        }

        if (encontrado == null) {
            layoutSenha.setError("Usuário ou senha inválidos.");
            return;
        }

        // Login certo -> abre a tela principal levando o ID e o nome
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(EXTRA_USUARIO_ID, encontrado.id);
        intent.putExtra(EXTRA_NOME_USUARIO, encontrado.nome);
        startActivity(intent);

        // Fecha o login, para o botão voltar não retornar para ele
        finish();
    }
}