package com.example.parkingflow;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

// Tela de Ajuda: mostra a página de dúvidas frequentes (FAQ) dentro do app, usando uma WebView
public class AjudaActivity extends AppCompatActivity {

    // Endereço da página de ajuda. Para trocar a página no futuro, mude só aqui.
    // O "?tema=claro" deixa a página clara, combinando com o resto do app.
    private static final String URL_AJUDA =
            "https://webview-xsquad-mts-fnehd0bzbhbbaycm.brazilsouth-01.azurewebsites.net/?tema=claro";

    // Dados que quem abre esta tela pode enviar: o título da barra e o endereço da página.
    // Se nada for enviado, a tela abre a Ajuda (como sempre fez).
    public static final String EXTRA_TITULO = "titulo";
    public static final String EXTRA_URL = "url";
    private WebView webAjuda;
    private View progressAjuda, layoutErroAjuda;

    // Marca se o carregamento atual deu erro (para não mostrar a página em branco depois)
    private boolean deuErro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajuda);

        webAjuda = findViewById(R.id.webAjuda);
        progressAjuda = findViewById(R.id.progressAjuda);
        layoutErroAjuda = findViewById(R.id.layoutErroAjuda);
        MaterialToolbar toolbar = findViewById(R.id.toolbarAjuda);
        MaterialButton btnTentarNovamente = findViewById(R.id.btnTentarNovamente);

        // Seta de voltar da barra -> fecha a Ajuda e volta para onde estava
        toolbar.setNavigationOnClickListener(v -> finish());

        // A página usa JavaScript para montar a lista de perguntas
        WebSettings config = webAjuda.getSettings();
        config.setJavaScriptEnabled(true);
        config.setDomStorageEnabled(true);

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

    // Mostra a rodinha e carrega a página
    private void carregar() {
        deuErro = false;
        layoutErroAjuda.setVisibility(View.GONE);
        webAjuda.setVisibility(View.INVISIBLE);
        progressAjuda.setVisibility(View.VISIBLE);
        webAjuda.loadUrl(URL_AJUDA);
    }

    // Esconde a página e a rodinha e mostra a mensagem de erro com o botão Tentar novamente
    private void mostrarErro() {
        deuErro = true;
        progressAjuda.setVisibility(View.GONE);
        webAjuda.setVisibility(View.INVISIBLE);
        layoutErroAjuda.setVisibility(View.VISIBLE);
    }
}