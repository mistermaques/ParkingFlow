package com.example.parkingflow.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;
import androidx.lifecycle.ViewModelProvider;

import com.example.parkingflow.R;
import com.example.parkingflow.model.Usuario;
import com.example.parkingflow.viewmodel.LoginViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

// View do login: lê os campos, manda para o LoginViewModel e reage ao resultado
// (mostra o erro no campo ou abre a MainActivity com o ID e o nome do usuário).
public class LoginActivity extends AppCompatActivity {

    // Nomes usados para "entregar" os dados do usuário para a MainActivity
    public static final String EXTRA_USUARIO_ID = "usuarioId";
    public static final String EXTRA_NOME_USUARIO = "nomeUsuario";

    private TextInputEditText campoUsuario, campoSenha;
    private TextInputLayout layoutUsuario, layoutSenha;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Mostra a tela de abertura (logo do app) enquanto o app abre; precisa vir antes do super.onCreate
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        campoUsuario = findViewById(R.id.campoUsuario);
        campoSenha = findViewById(R.id.campoSenha);
        layoutUsuario = findViewById(R.id.layoutUsuario);
        layoutSenha = findViewById(R.id.layoutSenha);
        MaterialButton btnEntrar = findViewById(R.id.btnEntrar);

        // Liga a tela ao ViewModel e reage a cada mudança que ele avisar
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        viewModel.getErroUsuario().observe(this, layoutUsuario::setError);
        viewModel.getErroSenha().observe(this, layoutSenha::setError);
        viewModel.getUsuarioLogado().observe(this, this::abrirTelaPrincipal);

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

    // Manda o que foi digitado para o ViewModel validar
    private void entrar() {
        String login = campoUsuario.getText() == null ? "" : campoUsuario.getText().toString();
        String senha = campoSenha.getText() == null ? "" : campoSenha.getText().toString();
        viewModel.entrar(login, senha);
    }

    // Login certo -> abre a tela principal levando o ID e o nome
    private void abrirTelaPrincipal(Usuario usuario) {
        if (usuario == null) return;

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(EXTRA_USUARIO_ID, usuario.getId());
        intent.putExtra(EXTRA_NOME_USUARIO, usuario.getNome());
        startActivity(intent);

        // Fecha o login, para o botão voltar não retornar para ele
        finish();
    }
}
