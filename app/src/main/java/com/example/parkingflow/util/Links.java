package com.example.parkingflow.util;

// Endereços das páginas web abertas dentro do app (AjudaActivity).
// Para trocar alguma página no futuro, mude só aqui.
// (Os endereços das APIs ficam em ReservaApi e VagaApi, no pacote "api".)
public final class Links {

    // Página de dúvidas frequentes. O "?tema=claro" deixa a página clara, combinando com o app.
    public static final String AJUDA =
            "https://webview-xsquad-mts-fnehd0bzbhbbaycm.brazilsouth-01.azurewebsites.net/?tema=claro";

    // Página do anúncio "Encontre seu lanche" (botão preto no rodapé do menu)
    public static final String LANCHE =
            "https://api-web-view-mousseomanos-axhxazckawd4e0be.brazilsouth-01.azurewebsites.net/";

    private Links() {
    }
}
