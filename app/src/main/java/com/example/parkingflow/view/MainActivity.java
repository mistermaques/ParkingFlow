package com.example.parkingflow.view;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.lifecycle.ViewModelProvider;


import com.example.parkingflow.R;
import com.example.parkingflow.model.Reserva;
import com.example.parkingflow.model.ResumoVagas;
import com.example.parkingflow.model.Vaga;
import com.example.parkingflow.util.Formatos;
import com.example.parkingflow.util.Links;
import com.example.parkingflow.viewmodel.InicioViewModel;
import com.example.parkingflow.viewmodel.MinhasReservasViewModel;
import com.example.parkingflow.viewmodel.NovaReservaViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.progressindicator.CircularProgressIndicatorSpec;
import com.google.android.material.progressindicator.IndeterminateDrawable;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Usuário logado (ID e nome entregues pela tela de login)
    private long usuarioId;
    private String nomeUsuario;

    // Menu, títulos e áreas da tela
    private DrawerLayout drawerLayout;
    private NavigationView navView;
    private NestedScrollView scrollConteudo;
    private SwipeRefreshLayout swipeAtualizar;
    private TextView txtSecao, txtTituloTela, txtSubtitulo;
    private View indicadorEtapas, telaNovaReserva, telaConfirmacao;

    // Indicador de etapas
    private TextView etapa1Circulo, etapa2Circulo, etapa2Texto;

    // Etapa 1: formulário
    private MaterialAutoCompleteTextView campoVaga;
    private TextInputEditText campoEntrada, campoSaida, campoValor, campoPlaca;
    private TextInputLayout layoutEntrada, layoutSaida;
    private MaterialButton btnContinuar;

    // Etapa 2: confirmação
    private TextView txtConfVaga, txtConfEntrada, txtConfSaida, txtConfDuracao, txtConfTotal, txtConfPlaca;

    // Tela de sucesso
    private View telaSucesso;
    private TextView txtSucessoId, txtSucessoVaga, txtSucessoPlaca, txtSucessoStatus, txtSucessoTotal;

    // Minhas reservas
    private View telaMinhasReservas, progressReservas;
    private TextView txtReservasMensagem;
    private RecyclerView listaReservas;
    private ReservaAdapter adaptadorReservas;
    private MinhasReservasViewModel minhasReservasVM;
    private InicioViewModel inicioVM;


    // Tela Início
    private View telaInicio, layoutProximaReserva;
    private TextView txtResumoTotal, txtResumoDisponiveis, txtResumoOcupadas, txtResumoIndisponiveis;
    private TextView txtProximaMensagem, txtProximaVaga, txtProximaEntrada, txtProximaSaida;

    // Botões da confirmação e campo da vaga
    private MaterialButton btnConfirmar, btnVoltar;
    private TextInputLayout layoutVaga;

    // ViewModel de "Nova reserva" (formulário, confirmação e envio)
    private NovaReservaViewModel novaReservaVM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Quem entrou: dados vindos da LoginActivity
        usuarioId = getIntent().getLongExtra(LoginActivity.EXTRA_USUARIO_ID, 1L);
        nomeUsuario = getIntent().getStringExtra(LoginActivity.EXTRA_NOME_USUARIO);
        if (nomeUsuario == null) nomeUsuario = "Motorista";

        configurarMenu();
        configurarFormulario();
        configurarConfirmacao();
        configurarBotaoVoltarDoCelular();
        ligarMinhasReservasAoViewModel();
        ligarInicioAoViewModel();
        ligarNovaReservaAoViewModel();

        // Tela inicial ao abrir o app: "Início" (só depois de tudo configurado)
        navView.setCheckedItem(R.id.nav_inicio);
        navView.getMenu().performIdentifierAction(R.id.nav_inicio, 0);
    }

    // ======================= MENU LATERAL =======================

    private void configurarMenu() {
        drawerLayout = findViewById(R.id.drawer_layout);
        scrollConteudo = findViewById(R.id.scrollConteudo);
        swipeAtualizar = findViewById(R.id.swipeAtualizar);
        swipeAtualizar.setColorSchemeColors(ContextCompat.getColor(this, R.color.texto_principal));

        // Puxou a tela para baixo -> busca de novo os dados da tela atual
        swipeAtualizar.setOnRefreshListener(() -> {
            if (telaInicio.getVisibility() == View.VISIBLE) {
                carregarInicio();
            } else if (telaMinhasReservas.getVisibility() == View.VISIBLE) {
                carregarMinhasReservas();
            } else if (telaNovaReserva.getVisibility() == View.VISIBLE) {
                carregarVagas(); // atualiza a lista de vagas disponíveis
            }
            // As telas já mostram o próprio "Carregando...", então a rodinha do topo pode parar
            swipeAtualizar.setRefreshing(false);
        });
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        navView = findViewById(R.id.nav_view);
        txtSecao = findViewById(R.id.txtSecao);
        txtTituloTela = findViewById(R.id.txtTituloTela);
        txtSubtitulo = findViewById(R.id.txtSubtitulo);
        indicadorEtapas = findViewById(R.id.indicadorEtapas);
        telaNovaReserva = findViewById(R.id.telaNovaReserva);
        telaConfirmacao = findViewById(R.id.telaConfirmacao);
        telaMinhasReservas = findViewById(R.id.telaMinhasReservas);
        progressReservas = findViewById(R.id.progressReservas);
        txtReservasMensagem = findViewById(R.id.txtReservasMensagem);
        listaReservas = findViewById(R.id.listaReservas);
        // Lista em RecyclerView: um card por reserva (o ReservaAdapter monta cada card)
        listaReservas.setLayoutManager(new LinearLayoutManager(this));
        adaptadorReservas = new ReservaAdapter();
        listaReservas.setAdapter(adaptadorReservas);
        // Arrastar um card PENDENTE para a esquerda -> pede para cancelar a reserva
        new ArrasteParaCancelar(this, adaptadorReservas, this::confirmarCancelamento).ligarNa(listaReservas);
        telaInicio = findViewById(R.id.telaInicio);
        layoutProximaReserva = findViewById(R.id.layoutProximaReserva);
        txtResumoTotal = findViewById(R.id.txtResumoTotal);
        txtResumoDisponiveis = findViewById(R.id.txtResumoDisponiveis);
        txtResumoOcupadas = findViewById(R.id.txtResumoOcupadas);
        txtResumoIndisponiveis = findViewById(R.id.txtResumoIndisponiveis);
        txtProximaMensagem = findViewById(R.id.txtProximaMensagem);
        txtProximaVaga = findViewById(R.id.txtProximaVaga);
        txtProximaEntrada = findViewById(R.id.txtProximaEntrada);
        txtProximaSaida = findViewById(R.id.txtProximaSaida);
        etapa1Circulo = findViewById(R.id.etapa1Circulo);
        etapa2Circulo = findViewById(R.id.etapa2Circulo);
        etapa2Texto = findViewById(R.id.etapa2Texto);

        // "Reservar uma vaga" faz o mesmo que tocar em "Nova reserva" no menu
        MaterialButton btnIrReservar = findViewById(R.id.btnIrReservar);
        btnIrReservar.setOnClickListener(v -> {
            navView.setCheckedItem(R.id.nav_nova_reserva);
            navView.getMenu().performIdentifierAction(R.id.nav_nova_reserva, 0);
        });


        // "Encontre seu lanche" (rodapé do menu) -> abre o anúncio na tela de página web
        MaterialButton btnEncontreLanche = findViewById(R.id.btnEncontreLanche);
        btnEncontreLanche.setOnClickListener(v -> {
            Intent intent = new Intent(this, AjudaActivity.class);
            intent.putExtra(AjudaActivity.EXTRA_TITULO, "Encontre seu lanche");
            intent.putExtra(AjudaActivity.EXTRA_URL, Links.LANCHE);
            startActivity(intent);
            drawerLayout.closeDrawer(GravityCompat.START);
        });

        // Clicou na setinha -> abre o menu
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Clicou em um item do menu -> troca o conteúdo e fecha o menu
        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            // Sair -> volta para o login e fecha esta tela
            if (id == R.id.nav_sair) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return true;
            }

            // Ajuda -> abre a tela da página de ajuda (FAQ), sem trocar a tela atual
            if (id == R.id.nav_ajuda) {
                startActivity(new Intent(this, AjudaActivity.class));
                drawerLayout.closeDrawer(GravityCompat.START);
                return false; // false: o menu continua marcando a tela em que você estava
            }
            boolean ehNovaReserva = (id == R.id.nav_nova_reserva);

            if (id == R.id.nav_inicio) {
                mostrarTela("INÍCIO", "Olá, " + nomeUsuario, "Veja um resumo das suas reservas.");
            } else if (ehNovaReserva) {
                mostrarTela("RESERVAS", "Reserve sua vaga", "Escolha quando e onde você vai estacionar.");
            } else if (id == R.id.nav_minhas_reservas) {
                mostrarTela("RESERVAS", "Minhas reservas", "Acompanhe suas reservas ativas e anteriores.");
            }

            // Etapas e formulário só aparecem em "Nova reserva"
            indicadorEtapas.setVisibility(ehNovaReserva ? View.VISIBLE : View.GONE);
            if (ehNovaReserva) {
                irParaDetalhes();
                carregarVagas(); // sempre busca a lista atualizada de vagas
            } else {
                telaNovaReserva.setVisibility(View.GONE);
                telaConfirmacao.setVisibility(View.GONE);
                telaSucesso.setVisibility(View.GONE);
            }

            // Lista só aparece em "Minhas reservas", e sempre busca dados novos
            boolean ehMinhasReservas = (id == R.id.nav_minhas_reservas);
            telaMinhasReservas.setVisibility(ehMinhasReservas ? View.VISIBLE : View.GONE);
            if (ehMinhasReservas) {
                carregarMinhasReservas();
            }

            // Tela Início só aparece em "Início", e sempre busca dados novos
            // Tela Início só aparece em "Início", e sempre busca dados novos
            boolean ehInicio = (id == R.id.nav_inicio);
            telaInicio.setVisibility(ehInicio ? View.VISIBLE : View.GONE);
            if (ehInicio) {
                carregarInicio();
            }

            // Arrastar para atualizar em "Início", "Minhas reservas" e "Nova reserva"

            swipeAtualizar.setEnabled(ehInicio || ehMinhasReservas || ehNovaReserva);

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    // Atualiza os textos do topo da área de conteúdo
    private void mostrarTela(String secao, String titulo, String subtitulo) {
        txtSecao.setText(secao);
        txtTituloTela.setText(titulo);
        txtSubtitulo.setText(subtitulo);
    }

    // ======================= ETAPA 1: FORMULÁRIO =======================

    private void configurarFormulario() {
        campoVaga = findViewById(R.id.campoVaga);
        campoPlaca = findViewById(R.id.campoPlaca);
        campoEntrada = findViewById(R.id.campoEntrada);
        campoSaida = findViewById(R.id.campoSaida);
        campoValor = findViewById(R.id.campoValor);
        layoutVaga = findViewById(R.id.layoutVaga);
        layoutEntrada = findViewById(R.id.layoutEntrada);
        layoutSaida = findViewById(R.id.layoutSaida);
        btnContinuar = findViewById(R.id.btnContinuar);

        // Escolheu uma vaga na lista -> avisa o ViewModel (ele mostra o preço e revalida)
        campoVaga.setOnItemClickListener((parent, view, posicao, idLinha) ->
                novaReservaVM.escolherVaga((Vaga) parent.getItemAtPosition(posicao)));

        // Tocar no campo OU no ícone de calendário abre o seletor
        campoEntrada.setOnClickListener(v -> escolherDataHora(true));
        layoutEntrada.setEndIconOnClickListener(v -> escolherDataHora(true));
        campoSaida.setOnClickListener(v -> escolherDataHora(false));
        layoutSaida.setEndIconOnClickListener(v -> escolherDataHora(false));

        // Sempre que digitar a placa, avisa o ViewModel (ele decide se libera o botão)
        TextWatcher observador = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                novaReservaVM.setPlaca(s.toString());
            }
        };
        campoPlaca.addTextChangedListener(observador);

        // Continuar -> vai para a etapa 2
        btnContinuar.setOnClickListener(v -> irParaConfirmacao());
    }

    // Liga "Nova reserva" ao ViewModel e reage a cada mudança que ele avisar
    private void ligarNovaReservaAoViewModel() {
        novaReservaVM = new ViewModelProvider(this).get(NovaReservaViewModel.class);
        novaReservaVM.setUsuarioId(usuarioId);

        // Lista de vagas: carregando / erro / sem vagas / lista suspensa
        novaReservaVM.getVagas().observe(this, resultado -> {
            if (resultado.isCarregando()) {
                layoutVaga.setHelperText("Carregando vagas...");
                return;
            }
            if (resultado.isErro()) {
                layoutVaga.setHelperText("Não foi possível carregar as vagas: " + resultado.getMensagem());
                return;
            }

            List<Vaga> vagas = resultado.getDado();
            if (vagas.isEmpty()) {
                layoutVaga.setHelperText("Nenhuma vaga disponível no momento.");
                return;
            }

            // Cada opção da lista usa o toString() da Vaga: "A10 · R$ 10,00/h"
            ArrayAdapter<Vaga> adaptador = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1, vagas);
            campoVaga.setAdapter(adaptador);
            layoutVaga.setHelperText("Escolha uma das vagas disponíveis.");
        });

        // Campos e erros do formulário
        novaReservaVM.getValorHora().observe(this, campoValor::setText);
        novaReservaVM.getEntradaTexto().observe(this, campoEntrada::setText);
        novaReservaVM.getSaidaTexto().observe(this, campoSaida::setText);
        novaReservaVM.getErroEntrada().observe(this, layoutEntrada::setError);
        novaReservaVM.getErroSaida().observe(this, layoutSaida::setError);
        novaReservaVM.getPodeContinuar().observe(this, btnContinuar::setEnabled);

        // Envio da reserva: carregando no botão / sucesso / erro
        novaReservaVM.getEnvio().observe(this, resultado -> {
            if (resultado == null) return;

            if (resultado.isCarregando()) {
                mostrarCarregando(true);
                return;
            }

            mostrarCarregando(false);
            if (resultado.isSucesso()) {
                mostrarSucesso(resultado.getDado());
            } else {
                Toast.makeText(this, "Não foi possível confirmar a reserva: " + resultado.getMensagem(),
                        Toast.LENGTH_LONG).show();
            }
            novaReservaVM.envioTratado();
        });
    }

    // Pede ao ViewModel a lista atualizada de vagas
    private void carregarVagas() {
        novaReservaVM.carregarVagas();
    }

    // Volta o formulário ao estado inicial
    private void limparFormulario() {
        campoVaga.setText("", false); // o "false" evita filtrar a lista ao limpar
        campoPlaca.setText("");
        novaReservaVM.limpar();
    }

    // Abre o calendário e, em seguida, o relógio; a data escolhida vai para o ViewModel
    private void escolherDataHora(boolean ehEntrada) {
        Calendar data = ehEntrada ? novaReservaVM.getEntrada() : novaReservaVM.getSaida();

        DatePickerDialog calendario = new DatePickerDialog(this, R.style.Parkly_Calendario, (picker, ano, mes, dia) -> {

            TimePickerDialog relogio = new TimePickerDialog(this, R.style.Parkly_Calendario, (seletor, hora, minuto) -> {
                // Só grava a data e a hora juntas, depois de escolher as duas
                if (ehEntrada) {
                    novaReservaVM.definirEntrada(ano, mes, dia, hora, minuto);
                } else {
                    novaReservaVM.definirSaida(ano, mes, dia, hora, minuto);
                }
            }, data.get(Calendar.HOUR_OF_DAY), data.get(Calendar.MINUTE), true);

            relogio.show();
            pintarBotoes(relogio);

        }, data.get(Calendar.YEAR), data.get(Calendar.MONTH), data.get(Calendar.DAY_OF_MONTH));

        // Primeiro dia permitido: hoje (o horário é conferido depois, no ViewModel)
        calendario.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        calendario.show();
        pintarBotoes(calendario);
    }

    // Deixa os botões OK e Cancelar com a cor escura do app (eles vêm brancos por padrão)
    private void pintarBotoes(android.app.AlertDialog janela) {
        int cor = ContextCompat.getColor(this, R.color.texto_principal);
        janela.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(cor);
        janela.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(cor);
    }

    // ======================= ETAPA 2: CONFIRMAÇÃO =======================

    private void configurarConfirmacao() {
        txtConfVaga = findViewById(R.id.txtConfVaga);
        txtConfPlaca = findViewById(R.id.txtConfPlaca);
        txtConfEntrada = findViewById(R.id.txtConfEntrada);
        txtConfSaida = findViewById(R.id.txtConfSaida);
        txtConfDuracao = findViewById(R.id.txtConfDuracao);
        txtConfTotal = findViewById(R.id.txtConfTotal);
        btnVoltar = findViewById(R.id.btnVoltar);
        btnConfirmar = findViewById(R.id.btnConfirmar);
        telaSucesso = findViewById(R.id.telaSucesso);
        txtSucessoId = findViewById(R.id.txtSucessoId);
        txtSucessoVaga = findViewById(R.id.txtSucessoVaga);
        txtSucessoPlaca = findViewById(R.id.txtSucessoPlaca);
        txtSucessoStatus = findViewById(R.id.txtSucessoStatus);
        txtSucessoTotal = findViewById(R.id.txtSucessoTotal);

        // "Ver minhas reservas" faz o mesmo que tocar no item do menu
        MaterialButton btnVerMinhasReservas = findViewById(R.id.btnVerMinhasReservas);
        btnVerMinhasReservas.setOnClickListener(v -> {
            navView.setCheckedItem(R.id.nav_minhas_reservas);
            navView.getMenu().performIdentifierAction(R.id.nav_minhas_reservas, 0);
        });

        // Voltar -> retorna ao formulário com os dados preenchidos
        btnVoltar.setOnClickListener(v -> irParaDetalhes());

        // Confirmar -> envia a reserva para a API
        btnConfirmar.setOnClickListener(v -> novaReservaVM.enviar());
    }

    private void irParaConfirmacao() {
        esconderTeclado();

        // Preenche o resumo com o que foi digitado na etapa 1
        txtConfVaga.setText(novaReservaVM.getNomeVaga());
        txtConfPlaca.setText(novaReservaVM.getPlacaMaiuscula());
        txtConfEntrada.setText(novaReservaVM.getEntradaTexto().getValue());
        txtConfSaida.setText(novaReservaVM.getSaidaTexto().getValue());
        txtConfDuracao.setText(novaReservaVM.getDuracao());
        txtConfTotal.setText(novaReservaVM.getTotalFormatado());

        telaNovaReserva.setVisibility(View.GONE);
        telaConfirmacao.setVisibility(View.VISIBLE);
        atualizarEtapas(2);
        scrollConteudo.scrollTo(0, 0);
    }

    private void irParaDetalhes() {
        telaConfirmacao.setVisibility(View.GONE);
        telaSucesso.setVisibility(View.GONE);
        telaNovaReserva.setVisibility(View.VISIBLE);
        atualizarEtapas(1);
        scrollConteudo.scrollTo(0, 0);
    }

    // Pinta as bolinhas "1" e "2" conforme a etapa atual
    private void atualizarEtapas(int etapaAtual) {
        boolean naEtapa2 = (etapaAtual == 2);
        int corEscura = ContextCompat.getColor(this, R.color.texto_principal);
        int corCinza = ContextCompat.getColor(this, R.color.texto_secundario);
        int corBranca = ContextCompat.getColor(this, R.color.white);

        etapa1Circulo.setText(naEtapa2 ? "✓" : "1");

        etapa2Circulo.setBackgroundResource(naEtapa2 ? R.drawable.bg_etapa_ativa : R.drawable.bg_etapa_inativa);
        etapa2Circulo.setTextColor(naEtapa2 ? corBranca : corCinza);
        etapa2Texto.setTextColor(naEtapa2 ? corEscura : corCinza);
    }

    // ======================= ENVIO PARA A API =======================

    // Liga/desliga o carregamento no botão Confirmar
    private void mostrarCarregando(boolean carregando) {
        btnConfirmar.setEnabled(!carregando);
        btnVoltar.setEnabled(!carregando);

        if (carregando) {
            CircularProgressIndicatorSpec spec = new CircularProgressIndicatorSpec(this, null, 0,
                    com.google.android.material.R.style.Widget_MaterialComponents_CircularProgressIndicator_ExtraSmall);
            spec.indicatorColors = new int[]{ContextCompat.getColor(this, R.color.white)};

            btnConfirmar.setIcon(IndeterminateDrawable.createCircularDrawable(this, spec));
            btnConfirmar.setText("Confirmando...");
        } else {
            btnConfirmar.setIcon(null);
            btnConfirmar.setText("Confirmar");
        }
    }

    // Mostra a tela "Reserva confirmada" com os dados que a API devolveu
    private void mostrarSucesso(Reserva reserva) {
        txtSucessoId.setText("#" + reserva.getId());
        txtSucessoVaga.setText(reserva.getNomeVaga());
        txtSucessoPlaca.setText(reserva.getPlacaVeiculo());
        txtSucessoStatus.setText(reserva.getStatus());

        Double total = reserva.getPrecoReserva();
        txtSucessoTotal.setText(Formatos.dinheiroOuTraco(total));

        indicadorEtapas.setVisibility(View.GONE);
        telaConfirmacao.setVisibility(View.GONE);
        telaSucesso.setVisibility(View.VISIBLE);
        scrollConteudo.scrollTo(0, 0);

        // Deixa o formulário zerado para a próxima reserva
        limparFormulario();
    }

    // ======================= TELA INÍCIO =======================

    // Liga a tela Início ao ViewModel e reage ao que ele avisar
    private void ligarInicioAoViewModel() {
        inicioVM = new ViewModelProvider(this).get(InicioViewModel.class);
        inicioVM.setUsuarioId(usuarioId);

        // Card "Vagas agora": totais por status
        inicioVM.getResumo().observe(this, resultado -> {
            if (resultado.isCarregando()) {
                txtResumoTotal.setText("Carregando...");
                txtResumoDisponiveis.setText("–");
                txtResumoOcupadas.setText("–");
                txtResumoIndisponiveis.setText("–");
                return;
            }
            if (resultado.isErro()) {
                txtResumoTotal.setText("Não foi possível carregar o resumo: " + resultado.getMensagem());
                return;
            }

            ResumoVagas resumo = resultado.getDado();
            txtResumoTotal.setText(Formatos.numero(resumo.getTotal()) + " vagas no estacionamento");
            txtResumoDisponiveis.setText(Formatos.numero(resumo.getDisponiveis()));
            txtResumoOcupadas.setText(Formatos.numero(resumo.getOcupadas()));
            txtResumoIndisponiveis.setText(Formatos.numero(resumo.getIndisponiveis()));
        });

        // Card "Sua próxima reserva": a PENDENTE com a entrada mais próxima (ou nenhuma)
        inicioVM.getProximaReserva().observe(this, resultado -> {
            if (resultado.isCarregando()) {
                txtProximaMensagem.setText("Carregando...");
                txtProximaMensagem.setVisibility(View.VISIBLE);
                layoutProximaReserva.setVisibility(View.GONE);
                return;
            }
            if (resultado.isErro()) {
                txtProximaMensagem.setText("Não foi possível carregar sua próxima reserva: " + resultado.getMensagem());
                return;
            }

            Reserva proxima = resultado.getDado();
            if (proxima == null) {
                txtProximaMensagem.setText("Você não tem reservas pendentes.");
                return;
            }

            txtProximaVaga.setText(proxima.getNomeVaga());
            txtProximaEntrada.setText(Formatos.dataDaApi(proxima.getDataInicio()));
            txtProximaSaida.setText(Formatos.dataDaApi(proxima.getDataFim()));
            txtProximaMensagem.setVisibility(View.GONE);
            layoutProximaReserva.setVisibility(View.VISIBLE);
        });
    }

    // Pede ao ViewModel para buscar os dois cards da tela Início
    private void carregarInicio() {
        inicioVM.carregar();
    }

    // ======================= MINHAS RESERVAS =======================

    // Liga "Minhas reservas" ao ViewModel e reage ao que ele avisar
    private void ligarMinhasReservasAoViewModel() {
        minhasReservasVM = new ViewModelProvider(this).get(MinhasReservasViewModel.class);
        minhasReservasVM.setUsuarioId(usuarioId);

        // Lista: carregando -> rodinha; erro -> mensagem; sucesso -> cards (ou "sem reservas")
        minhasReservasVM.getReservas().observe(this, resultado -> {
            progressReservas.setVisibility(resultado.isCarregando() ? View.VISIBLE : View.GONE);
            txtReservasMensagem.setVisibility(View.GONE);

            if (resultado.isCarregando()) {
                adaptadorReservas.atualizar(new ArrayList<>());
                return;
            }
            if (resultado.isErro()) {
                adaptadorReservas.atualizar(new ArrayList<>());
                mostrarMensagemReservas("Não foi possível carregar suas reservas: " + resultado.getMensagem());
                return;
            }

            List<Reserva> reservas = resultado.getDado();
            adaptadorReservas.atualizar(reservas);
            if (reservas.isEmpty()) {
                mostrarMensagemReservas("Você ainda não tem reservas.");
            }
        });

        // Avisos do cancelamento -> Toast (e marca como exibido)
        minhasReservasVM.getAviso().observe(this, aviso -> {
            if (aviso == null) return;
            Toast.makeText(this, aviso, Toast.LENGTH_LONG).show();
            minhasReservasVM.avisoExibido();
        });
    }

    // Pede ao ViewModel para buscar a lista de novo
    private void carregarMinhasReservas() {
        minhasReservasVM.carregar();
    }

    // Abre a janela de confirmação do cancelamento, com o campo de motivo
    private void confirmarCancelamento(int posicao) {
        if (posicao == RecyclerView.NO_POSITION) return;
        Reserva reserva = adaptadorReservas.getReserva(posicao);

        View conteudo = LayoutInflater.from(this).inflate(R.layout.dialogo_cancelamento, null);
        TextInputEditText campoMotivo = conteudo.findViewById(R.id.campoMotivo);

        new MaterialAlertDialogBuilder(this, R.style.Parkly_Dialogo)
                .setBackground(ContextCompat.getDrawable(this, R.drawable.bg_dialogo))
                .setTitle("Cancelar reserva?")
                .setMessage("Reserva #" + reserva.getId() + " da vaga " + reserva.getNomeVaga()
                        + "\nEntrada: " + Formatos.dataDaApi(reserva.getDataInicio()))
                .setView(conteudo)
                // Desistiu -> card volta ao lugar
                .setNegativeButton("Voltar", (janela, botao) -> adaptadorReservas.restaurar(posicao))
                .setOnCancelListener(janela -> adaptadorReservas.restaurar(posicao))
                // Confirmou -> envia para a API
                .setPositiveButton("Cancelar reserva", (janela, botao) -> {
                    adaptadorReservas.restaurar(posicao); // card volta para a lista enquanto a API responde
                    String motivo = campoMotivo.getText() == null ? "" : campoMotivo.getText().toString();
                    minhasReservasVM.cancelar(reserva, motivo);
                })
                .show()
                .getButton(DialogInterface.BUTTON_POSITIVE)
                .setTextColor(Color.parseColor("#DC2626"));
    }

    // Mostra a mensagem de lista vazia ou de erro
    private void mostrarMensagemReservas(String mensagem) {
        txtReservasMensagem.setText(mensagem);
        txtReservasMensagem.setVisibility(View.VISIBLE);
    }

    // ======================= UTILITÁRIOS =======================

    // Botão voltar do celular: fecha o menu, volta da confirmação ou sai do app
    private void configurarBotaoVoltarDoCelular() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else if (telaConfirmacao.getVisibility() == View.VISIBLE) {
                    irParaDetalhes();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    // Fecha o teclado (usado ao ir para a confirmação)
    private void esconderTeclado() {
        View foco = getCurrentFocus();
        if (foco != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(foco.getWindowToken(), 0);
            foco.clearFocus();
        }
    }
}