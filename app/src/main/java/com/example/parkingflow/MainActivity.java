package com.example.parkingflow;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.progressindicator.CircularProgressIndicatorSpec;
import com.google.android.material.progressindicator.IndeterminateDrawable;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

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
    private TextInputLayout layoutSaida;
    private MaterialButton btnContinuar;

    // Etapa 2: confirmação
    private TextView txtConfVaga, txtConfEntrada, txtConfSaida, txtConfDuracao, txtConfTotal, txtConfPlaca;

    // Tela de sucesso
    private View telaSucesso;
    private TextView txtSucessoId, txtSucessoVaga, txtSucessoPlaca, txtSucessoStatus, txtSucessoTotal;

    // Minhas reservas
    private View telaMinhasReservas, progressReservas;
    private TextView txtReservasMensagem;
    private LinearLayout listaReservas;


    // Tela Início
    private View telaInicio, layoutProximaReserva;
    private TextView txtResumoTotal, txtResumoDisponiveis, txtResumoOcupadas, txtResumoIndisponiveis;
    private TextView txtProximaMensagem, txtProximaVaga, txtProximaEntrada, txtProximaSaida;

    // Datas escolhidas
    private final Calendar entrada = Calendar.getInstance();
    private final Calendar saida = Calendar.getInstance();
    private final SimpleDateFormat formato =
            new SimpleDateFormat("dd/MM/yyyy  HH:mm", Locale.forLanguageTag("pt-BR"));

    // Formato de data que a API espera: 2026-10-20T19:40:00
    private final SimpleDateFormat formatoApi =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);

    // Conexão com a API e botões da confirmação
    private final ReservaApi api = ReservaApi.criar();
    private MaterialButton btnConfirmar, btnVoltar;

    // API de Vagas e a vaga que o usuário escolheu na lista
    private final VagaApi vagaApi = VagaApi.criar();
    private Vaga vagaEscolhida;
    private TextInputLayout layoutVaga;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configurarMenu();
        configurarFormulario();
        configurarConfirmacao();
        configurarBotaoVoltarDoCelular();


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

        // Clicou na setinha -> abre o menu
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // Clicou em um item do menu -> troca o conteúdo e fecha o menu
        navView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            boolean ehNovaReserva = (id == R.id.nav_nova_reserva);

            if (id == R.id.nav_inicio) {
                mostrarTela("INÍCIO", "Olá, Sophia", "Veja um resumo das suas reservas.");
            } else if (ehNovaReserva) {
                mostrarTela("RESERVAS", "Reserve sua vaga", "Escolha quando e onde você vai estacionar.");
            } else if (id == R.id.nav_minhas_reservas) {
                mostrarTela("RESERVAS", "Minhas reservas", "Acompanhe suas reservas ativas e anteriores.");
            }

            // Etapas e formulário só aparecem em "Nova reserva"
            indicadorEtapas.setVisibility(ehNovaReserva ? View.VISIBLE : View.GONE);
            if (ehNovaReserva) {
                irParaDetalhes();
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

            // Arrastar para atualizar só em "Início" e "Minhas reservas"
            swipeAtualizar.setEnabled(ehInicio || ehMinhasReservas);

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
        campoValor.setText("0,00");
        layoutVaga = findViewById(R.id.layoutVaga);
        TextInputLayout layoutEntrada = findViewById(R.id.layoutEntrada);
        layoutSaida = findViewById(R.id.layoutSaida);
        btnContinuar = findViewById(R.id.btnContinuar);

        // Escolheu uma vaga na lista -> guarda ela, mostra o preço e revalida
        campoVaga.setOnItemClickListener((parent, view, posicao, idLinha) -> {
            vagaEscolhida = (Vaga) parent.getItemAtPosition(posicao);
            campoValor.setText(String.format(Locale.forLanguageTag("pt-BR"), "%.2f", precoDaVaga()));
            validarFormulario();
        });

        // Busca as vagas disponíveis na API
        carregarVagas();

        // Valores iniciais: entrada = amanhã no horário atual, saída = 1 hora depois
        definirDatasPadrao();

        // Tocar no campo OU no ícone de calendário abre o seletor
        campoEntrada.setOnClickListener(v -> escolherDataHora(entrada));
        layoutEntrada.setEndIconOnClickListener(v -> escolherDataHora(entrada));
        campoSaida.setOnClickListener(v -> escolherDataHora(saida));
        layoutSaida.setEndIconOnClickListener(v -> escolherDataHora(saida));

        // Sempre que digitar a placa, verifica se pode liberar o botão
        TextWatcher observador = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                validarFormulario();
            }
        };
        campoPlaca.addTextChangedListener(observador);

        // Continuar -> vai para a etapa 2
        btnContinuar.setOnClickListener(v -> irParaConfirmacao());
    }

    // Entrada amanhã no horário atual, saída 1 hora depois
    private void definirDatasPadrao() {
        entrada.setTimeInMillis(System.currentTimeMillis());
        entrada.set(Calendar.SECOND, 0);
        entrada.set(Calendar.MILLISECOND, 0);
        entrada.add(Calendar.DAY_OF_MONTH, 1); // API não aceita reserva para o mesmo dia
        saida.setTime(entrada.getTime());
        saida.add(Calendar.HOUR_OF_DAY, 1);
        atualizarDatas();
    }

    // Volta o formulário ao estado inicial
    private void limparFormulario() {
        campoVaga.setText("", false); // o "false" evita filtrar a lista ao limpar
        vagaEscolhida = null;
        campoValor.setText("0,00");
        campoPlaca.setText("");
        definirDatasPadrao();
        carregarVagas(); // atualiza a lista de vagas disponíveis
    }

    // Busca as vagas DISPONIVEL e coloca na lista suspensa
    private void carregarVagas() {
        layoutVaga.setHelperText("Carregando vagas...");

        vagaApi.listarDisponiveis().enqueue(new Callback<List<Vaga>>() {
            @Override
            public void onResponse(Call<List<Vaga>> call, Response<List<Vaga>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    layoutVaga.setHelperText("Não foi possível carregar as vagas (erro " + response.code() + ").");
                    return;
                }

                List<Vaga> vagas = response.body();
                if (vagas.isEmpty()) {
                    layoutVaga.setHelperText("Nenhuma vaga disponível no momento.");
                    return;
                }

                // Cada opção da lista usa o toString() da Vaga: "A10 · R$ 10,00/h"
                ArrayAdapter<Vaga> adaptador = new ArrayAdapter<>(MainActivity.this,
                        android.R.layout.simple_list_item_1, vagas);
                campoVaga.setAdapter(adaptador);
                layoutVaga.setHelperText("Escolha uma das vagas disponíveis.");
            }

            @Override
            public void onFailure(Call<List<Vaga>> call, Throwable t) {
                layoutVaga.setHelperText("Sem conexão para carregar as vagas. Tente novamente.");
            }
        });
    }

    // Preço por hora da vaga escolhida (0 se nenhuma foi escolhida)
    private double precoDaVaga() {
        if (vagaEscolhida == null || vagaEscolhida.getPrecoHora() == null) return 0;
        return vagaEscolhida.getPrecoHora();
    }

    // Abre o calendário e, em seguida, o relógio
    private void escolherDataHora(Calendar data) {
        DatePickerDialog calendario = new DatePickerDialog(this, (picker, ano, mes, dia) -> {
            data.set(ano, mes, dia);

            new TimePickerDialog(this, (relogio, hora, minuto) -> {
                data.set(Calendar.HOUR_OF_DAY, hora);
                data.set(Calendar.MINUTE, minuto);
                atualizarDatas();
            }, data.get(Calendar.HOUR_OF_DAY), data.get(Calendar.MINUTE), true).show();

        }, data.get(Calendar.YEAR), data.get(Calendar.MONTH), data.get(Calendar.DAY_OF_MONTH));

        // Primeiro dia permitido: amanhã, à meia-noite
        Calendar amanha = Calendar.getInstance();
        amanha.add(Calendar.DAY_OF_MONTH, 1);
        amanha.set(Calendar.HOUR_OF_DAY, 0);
        amanha.set(Calendar.MINUTE, 0);
        amanha.set(Calendar.SECOND, 0);
        amanha.set(Calendar.MILLISECOND, 0);
        calendario.getDatePicker().setMinDate(amanha.getTimeInMillis());
        calendario.show();
    }

    // Mostra as datas nos campos e checa se a saída é depois da entrada
    private void atualizarDatas() {
        campoEntrada.setText(formato.format(entrada.getTime()));
        campoSaida.setText(formato.format(saida.getTime()));

        if (saida.after(entrada)) {
            layoutSaida.setError(null);
        } else {
            layoutSaida.setError("A saída precisa ser depois da entrada.");
        }
        validarFormulario();
    }

    // Botão Continuar só fica ativo com vaga escolhida, placa válida e datas válidas
    private void validarFormulario() {
        boolean temVaga = (vagaEscolhida != null);
        String placa = campoPlaca.getText() == null ? "" : campoPlaca.getText().toString().replace("-", "");
        boolean temPlaca = placa.length() == 7;
        btnContinuar.setEnabled(temVaga && temPlaca && saida.after(entrada));
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
        btnConfirmar.setOnClickListener(v -> enviarReserva());
    }

    private void irParaConfirmacao() {
        esconderTeclado();

        // Preenche o resumo com o que foi digitado na etapa 1
        txtConfVaga.setText(vagaEscolhida.getNomeVaga());
        txtConfPlaca.setText(String.valueOf(campoPlaca.getText()).toUpperCase());
        txtConfEntrada.setText(formato.format(entrada.getTime()));
        txtConfSaida.setText(formato.format(saida.getTime()));
        txtConfDuracao.setText(calcularDuracao());
        txtConfTotal.setText(formatarDinheiro(calcularTotal()));

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

    // Ex: "2h 30min", "45min", "3h"
    private String calcularDuracao() {
        long minutosTotais = (saida.getTimeInMillis() - entrada.getTimeInMillis()) / 60000;
        long horas = minutosTotais / 60;
        long minutos = minutosTotais % 60;

        if (horas == 0) return minutos + "min";
        if (minutos == 0) return horas + "h";
        return horas + "h " + minutos + "min";
    }

    // Mesma regra da API: cada hora iniciada é cobrada inteira (1h30 = 2h)
    private double calcularTotal() {
        long minutos = (saida.getTimeInMillis() - entrada.getTimeInMillis()) / 60000;
        if (minutos <= 0) return 0;
        long horasCobradas = (minutos + 59) / 60;
        return horasCobradas * precoDaVaga();
    }

    // Ex: 30.0 -> "R$ 30,00"
    private String formatarDinheiro(double valor) {
        return String.format(Locale.forLanguageTag("pt-BR"), "R$ %.2f", valor);
    }

    // ======================= ENVIO PARA A API =======================

    private void enviarReserva() {
        mostrarCarregando(true);

        String placa = String.valueOf(campoPlaca.getText()).toUpperCase();

        // usuarioId fixo até existir a User API; vaga, nome e preço vêm da vaga escolhida
        Reserva nova = new Reserva(
                1L,
                placa,
                vagaEscolhida.getId(),
                vagaEscolhida.getNomeVaga(),
                formatoApi.format(entrada.getTime()),
                formatoApi.format(saida.getTime()),
                precoDaVaga()
        );

        api.criarReserva(nova).enqueue(new Callback<Reserva>() {
            @Override
            public void onResponse(Call<Reserva> call, Response<Reserva> response) {
                mostrarCarregando(false);

                if (response.isSuccessful() && response.body() != null) {
                    mostrarSucesso(response.body());
                } else {
                    // Lê a mensagem que a API manda explicando o motivo da recusa
                    String detalhe = "";
                    try {
                        if (response.errorBody() != null) {
                            detalhe = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        detalhe = "(não foi possível ler o erro)";
                    }
                    Toast.makeText(MainActivity.this,
                            "Erro " + response.code() + ": " + detalhe,
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Reserva> call, Throwable t) {
                mostrarCarregando(false);
                // Mensagem de diagnóstico (mostra o tipo do erro) - útil durante os testes
                Toast.makeText(MainActivity.this,
                        "Erro: " + t.getClass().getSimpleName() + " - " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

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
        txtSucessoTotal.setText(total == null ? "-" : formatarDinheiro(total));

        indicadorEtapas.setVisibility(View.GONE);
        telaConfirmacao.setVisibility(View.GONE);
        telaSucesso.setVisibility(View.VISIBLE);
        scrollConteudo.scrollTo(0, 0);

        // Deixa o formulário zerado para a próxima reserva
        limparFormulario();
    }

    // ======================= TELA INÍCIO =======================

    // Busca os dois cards da tela Início
    private void carregarInicio() {
        carregarResumoVagas();
        carregarProximaReserva();
    }

    // Card "Vagas agora": totais por status vindos da API de Vagas
    private void carregarResumoVagas() {
        txtResumoTotal.setText("Carregando...");
        txtResumoDisponiveis.setText("–");
        txtResumoOcupadas.setText("–");
        txtResumoIndisponiveis.setText("–");

        vagaApi.buscarResumo().enqueue(new Callback<ResumoVagas>() {
            @Override
            public void onResponse(Call<ResumoVagas> call, Response<ResumoVagas> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    txtResumoTotal.setText("Não foi possível carregar (erro " + response.code() + ").");
                    return;
                }

                ResumoVagas resumo = response.body();
                txtResumoTotal.setText(numero(resumo.getTotal()) + " vagas no estacionamento");
                txtResumoDisponiveis.setText(numero(resumo.getDisponiveis()));
                txtResumoOcupadas.setText(numero(resumo.getOcupadas()));
                txtResumoIndisponiveis.setText(numero(resumo.getIndisponiveis()));
            }

            @Override
            public void onFailure(Call<ResumoVagas> call, Throwable t) {
                txtResumoTotal.setText("Sem conexão para carregar o resumo.");
            }
        });
    }

    // Card "Sua próxima reserva": a PENDENTE com a entrada mais próxima
    private void carregarProximaReserva() {
        txtProximaMensagem.setText("Carregando...");
        txtProximaMensagem.setVisibility(View.VISIBLE);
        layoutProximaReserva.setVisibility(View.GONE);

        // usuarioId fixo até existir a User API
        api.listarPorUsuario(1L).enqueue(new Callback<List<Reserva>>() {
            @Override
            public void onResponse(Call<List<Reserva>> call, Response<List<Reserva>> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    txtProximaMensagem.setText("Não foi possível carregar (erro " + response.code() + ").");
                    return;
                }

                // Procura, entre as pendentes, a de entrada mais cedo
                // (as datas vêm no formato 2026-10-20T19:40:00, que dá para comparar como texto)
                Reserva proxima = null;
                for (Reserva reserva : response.body()) {
                    boolean pendente = "PENDENTE".equals(reserva.getStatus()) && reserva.getDataInicio() != null;
                    if (pendente && (proxima == null
                            || reserva.getDataInicio().compareTo(proxima.getDataInicio()) < 0)) {
                        proxima = reserva;
                    }
                }

                if (proxima == null) {
                    txtProximaMensagem.setText("Você não tem reservas pendentes.");
                    return;
                }

                txtProximaVaga.setText(proxima.getNomeVaga());
                txtProximaEntrada.setText(formatarDataApi(proxima.getDataInicio()));
                txtProximaSaida.setText(formatarDataApi(proxima.getDataFim()));
                txtProximaMensagem.setVisibility(View.GONE);
                layoutProximaReserva.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<List<Reserva>> call, Throwable t) {
                txtProximaMensagem.setText("Sem conexão para carregar sua próxima reserva.");
            }
        });
    }

    // Evita mostrar "null" na tela se a API não mandar algum número
    private String numero(Integer valor) {
        return valor == null ? "0" : String.valueOf(valor);
    }

    // ======================= MINHAS RESERVAS =======================

    private void carregarMinhasReservas() {
        progressReservas.setVisibility(View.VISIBLE);
        txtReservasMensagem.setVisibility(View.GONE);
        listaReservas.removeAllViews();

        // usuarioId fixo até existir a User API
        api.listarPorUsuario(1L).enqueue(new Callback<List<Reserva>>() {
            @Override
            public void onResponse(Call<List<Reserva>> call, Response<List<Reserva>> response) {
                progressReservas.setVisibility(View.GONE);

                if (!response.isSuccessful() || response.body() == null) {
                    mostrarMensagemReservas("Não foi possível carregar suas reservas (erro " + response.code() + ").");
                    return;
                }

                List<Reserva> reservas = response.body();
                if (reservas.isEmpty()) {
                    mostrarMensagemReservas("Você ainda não tem reservas.");
                    return;
                }

                // Mais recentes primeiro (maior id no topo)
                reservas.sort((a, b) -> Long.compare(b.getId(), a.getId()));

                for (Reserva reserva : reservas) {
                    listaReservas.addView(criarCardReserva(reserva));
                }
            }

            @Override
            public void onFailure(Call<List<Reserva>> call, Throwable t) {
                progressReservas.setVisibility(View.GONE);
                mostrarMensagemReservas("Sem conexão com o servidor. Tente novamente.");
            }
        });
    }

    // Mostra a mensagem de lista vazia ou de erro
    private void mostrarMensagemReservas(String mensagem) {
        txtReservasMensagem.setText(mensagem);
        txtReservasMensagem.setVisibility(View.VISIBLE);
    }

    // Cria uma cópia do item_reserva.xml e preenche com os dados de uma reserva
    private View criarCardReserva(Reserva reserva) {
        View card = LayoutInflater.from(this).inflate(R.layout.item_reserva, listaReservas, false);

        ((TextView) card.findViewById(R.id.txtItemVaga)).setText("Vaga " + reserva.getNomeVaga());
        ((TextView) card.findViewById(R.id.txtItemId)).setText("Reserva #" + reserva.getId());
        ((TextView) card.findViewById(R.id.txtItemPlaca)).setText(reserva.getPlacaVeiculo());
        ((TextView) card.findViewById(R.id.txtItemEntrada)).setText(formatarDataApi(reserva.getDataInicio()));
        ((TextView) card.findViewById(R.id.txtItemSaida)).setText(formatarDataApi(reserva.getDataFim()));

        Double total = reserva.getPrecoReserva();
        ((TextView) card.findViewById(R.id.txtItemTotal)).setText(total == null ? "-" : formatarDinheiro(total));

        pintarStatus(card.findViewById(R.id.txtItemStatus), reserva.getStatus());
        return card;
    }

    // Texto e cores da etiqueta conforme o status
    private void pintarStatus(TextView etiqueta, String status) {
        String texto;
        String corFundo;
        String corTexto;

        if ("PENDENTE".equals(status)) {
            texto = "Pendente";
            corFundo = "#FEF3C7";
            corTexto = "#92400E";
        } else if ("EM_USO".equals(status)) {
            texto = "Em uso";
            corFundo = "#DCFCE7";
            corTexto = "#166534";
        } else if ("FINALIZADA".equals(status)) {
            texto = "Finalizada";
            corFundo = "#E5E7EB";
            corTexto = "#374151";
        } else if ("CANCELADA".equals(status)) {
            texto = "Cancelada";
            corFundo = "#FEE2E2";
            corTexto = "#991B1B";
        } else {
            texto = (status == null) ? "-" : status;
            corFundo = "#E5E7EB";
            corTexto = "#374151";
        }

        etiqueta.setText(texto);
        etiqueta.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor(corFundo)));
        etiqueta.setTextColor(Color.parseColor(corTexto));
    }

    // "2026-10-20T19:40:00" -> "20/10/2026  19:40"
    private String formatarDataApi(String dataApi) {
        if (dataApi == null) return "-";
        try {
            return formato.format(formatoApi.parse(dataApi));
        } catch (Exception e) {
            return dataApi;
        }
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