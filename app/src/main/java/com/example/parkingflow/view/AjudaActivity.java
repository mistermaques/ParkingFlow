package com.example.parkingflow.view;

import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.example.parkingflow.R;
import com.example.parkingflow.util.Links;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

// Tela de Ajuda: mostra a página de dúvidas frequentes (FAQ) dentro do app, usando uma WebView
public class AjudaActivity extends AppCompatActivity {

    // Dados que quem abre esta tela pode enviar: o título da barra e o endereço da página.
    // Se nada for enviado, a tela abre a Ajuda (como sempre fez).
    public static final String EXTRA_TITULO = "titulo";
    public static final String EXTRA_URL = "url";
    private WebView webAjuda;
    private View progressAjuda, layoutErroAjuda;

    // Endereço que esta tela vai abrir (a Ajuda ou outro enviado por quem abriu)
    private String urlPagina;


    // Marca se o carregamento atual deu erro (para não mostrar a página em branco depois)
    private boolean deuErro;

    // Pedido de localização feito pela página, esperando a resposta do usuário
    private String origemLocalizacao;
    private GeolocationPermissions.Callback callbackLocalizacao;

    // Janela do Android pedindo permissão de localização; a resposta é repassada para a página
    private final ActivityResultLauncher<String[]> pedirLocalizacao =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), resultado -> {
                boolean permitiu = Boolean.TRUE.equals(resultado.get(Manifest.permission.ACCESS_FINE_LOCATION))
                        || Boolean.TRUE.equals(resultado.get(Manifest.permission.ACCESS_COARSE_LOCATION));
                if (callbackLocalizacao != null) {
                    callbackLocalizacao.invoke(origemLocalizacao, permitiu, false);
                    callbackLocalizacao = null;
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajuda);

        webAjuda = findViewById(R.id.webAjuda);
        progressAjuda = findViewById(R.id.progressAjuda);
        layoutErroAjuda = findViewById(R.id.layoutErroAjuda);
        MaterialToolbar toolbar = findViewById(R.id.toolbarAjuda);
        MaterialButton btnTentarNovamente = findViewById(R.id.btnTentarNovamente);

        // Título e endereço enviados por quem abriu a tela (sem nada -> Ajuda)
        String titulo = getIntent().getStringExtra(EXTRA_TITULO);
        urlPagina = getIntent().getStringExtra(EXTRA_URL);
        if (urlPagina == null) urlPagina = Links.AJUDA; // endereço fica em util/Links
        if (titulo != null) toolbar.setTitle(titulo);

        // Seta de voltar da barra -> fecha a Ajuda e volta para onde estava
        toolbar.setNavigationOnClickListener(v -> finish());

        // A página usa JavaScript para montar a lista de perguntas
        WebSettings config = webAjuda.getSettings();
        config.setJavaScriptEnabled(true);
        config.setDomStorageEnabled(true);

        config.setGeolocationEnabled(true);

        // A página pediu a localização -> confere a permissão do Android (e pede, se precisar)
        webAjuda.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origem, GeolocationPermissions.Callback callback) {
                if (temPermissaoLocalizacao()) {
                    callback.invoke(origem, true, false);
                } else {
                    origemLocalizacao = origem;
                    callbackLocalizacao = callback;
                    pedirLocalizacao.launch(new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    });
                }
            }
        });

        // Acompanha o carregamento para mostrar a rodinha, a página ou o erro
        webAjuda.setWebViewClient(new WebViewClient() {

            // Terminou de carregar -> esconde a rodinha e mostra a página (se não deu erro)
            @Override
            public void onPageFinished(WebView view, String url) {
                progressAjuda.setVisibility(View.GONE);
                if (!deuErro) {
                    webAjuda.setVisibility(View.VISIBLE);
                }
            }

            // Sem internet, servidor fora do ar etc.
            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    mostrarErro();
                }
            }

            // O servidor respondeu com erro (ex: 503 enquanto o Azure "acorda")
            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse resposta) {
                if (request.isForMainFrame()) {
                    mostrarErro();
                }
            }
        });

        // Tentar novamente -> carrega a página de novo
        btnTentarNovamente.setOnClickListener(v -> carregar());

        // Botão voltar do celular: primeiro volta dentro da página, depois fecha a Ajuda
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (webAjuda.canGoBack()) {
                    webAjuda.goBack();
                } else {
                    finish();
                }
            }
        });

        carregar();
    }

    // O usuário já permitiu a localização (precisa ou aproximada)?
    private boolean temPermissaoLocalizacao() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    // Mostra a rodinha e carrega a página
    private void carregar() {
        deuErro = false;
        layoutErroAjuda.setVisibility(View.GONE);
        webAjuda.setVisibility(View.INVISIBLE);
        progressAjuda.setVisibility(View.VISIBLE);
        webAjuda.loadUrl(urlPagina);
    }

    // Esconde a página e a rodinha e mostra a mensagem de erro com o botão Tentar novamente
    private void mostrarErro() {
        deuErro = true;
        progressAjuda.setVisibility(View.GONE);
        webAjuda.setVisibility(View.INVISIBLE);
        layoutErroAjuda.setVisibility(View.VISIBLE);
    }
}