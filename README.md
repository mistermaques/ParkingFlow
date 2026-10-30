# 🅿️ Parking Flow

Aplicativo Android para **reserva de vagas de estacionamento**, voltado ao motorista. Com ele é possível ver a disponibilidade de vagas em tempo real, escolher uma vaga, definir o período de uso, conferir o valor antes de confirmar e acompanhar todas as reservas feitas.

O app consome duas APIs REST hospedadas no Azure: a **API de Vagas** e a **API de Reservas**.

---

## 📱 Funcionalidades

### Início
- Resumo das vagas do estacionamento em tempo real: **disponíveis**, **ocupadas** e **indisponíveis**.
- Card com a **próxima reserva pendente** do usuário (vaga, entrada e saída).
- Atalho para fazer uma nova reserva.

### Nova reserva (fluxo em 2 etapas)
**Etapa 1 – Detalhes**
- Escolha da vaga em uma **lista suspensa**, com apenas as vagas disponíveis e o preço por hora de cada uma (ex.: `A10 · R$ 10,00/h`).
- Campo de **placa do veículo**, aceitando o padrão antigo (`ABC-1234`) e o Mercosul (`ABC1D23`).
- Seleção de **entrada e saída** com calendário e relógio.
- Valor por hora preenchido automaticamente a partir da vaga escolhida.
- Validações: o botão **Continuar** só é liberado com vaga escolhida, placa válida e saída posterior à entrada.

**Etapa 2 – Confirmação**
- Resumo com vaga, placa, entrada, saída, **duração calculada** (ex.: `2h 30min`) e **prévia do total**.
- Botão **Confirmar** com indicador de carregamento enquanto a reserva é enviada para a API.
- Botão **Voltar** retorna ao formulário mantendo os dados preenchidos.

**Reserva confirmada**
- Tela de sucesso com número da reserva, vaga, placa, status e o **total calculado pela API**.
- Atalho para **Minhas reservas**.
- Formulário é limpo automaticamente para a próxima reserva.

### Minhas reservas
- Lista de todas as reservas do usuário, das mais recentes para as mais antigas.
- Cada card mostra vaga, número da reserva, placa, entrada, saída, total e uma **etiqueta colorida de status**:

| Status | Cor |
|---|---|
| Pendente | 🟡 Amarelo |
| Em uso | 🟢 Verde |
| Finalizada | ⚪ Cinza |
| Cancelada | 🔴 Vermelho |

### Geral
- **Menu lateral recolhível** (abre pela setinha no topo ou arrastando da borda esquerda).
- **Arrastar para atualizar** nas telas Início e Minhas reservas.
- Visual baseado no layout da versão web do sistema.

---

## 🛠️ Tecnologias

| Tecnologia | Uso |
|---|---|
| **Java** | Linguagem do app |
| **XML Layouts** (Views) | Construção das telas |
| **Material Components** | Campos, botões, cards, menu lateral |
| **Retrofit 2.11** + **Gson** | Comunicação com as APIs REST e conversão de JSON |
| **DrawerLayout / NavigationView** | Menu lateral |
| **SwipeRefreshLayout** | Gesto de arrastar para atualizar |
| **Gradle (Kotlin DSL)** | Build do projeto |

- **SDK mínimo:** Android 7.0 (API 24)
- **IDE:** Android Studio

---

## 🔌 APIs integradas

O app consome duas APIs desenvolvidas em Spring Boot e hospedadas no Azure.

### API de Vagas
Repositório: [mtsnobrega/x-vaga-api](https://github.com/mtsnobrega/x-vaga-api)

| Método | Rota | Uso no app |
|---|---|---|
| `GET` | `/vagas/status/DISPONIVEL` | Lista de vagas na tela de nova reserva |
| `GET` | `/vagas/resumo` | Card "Vagas agora" da tela Início |

### API de Reservas
Repositório: [mtsnobrega/x-reserva-api](https://github.com/mtsnobrega/x-reserva-api)

| Método | Rota | Uso no app |
|---|---|---|
| `POST` | `/reservas/nova-reserva` | Criação da reserva (botão Confirmar) |
| `GET` | `/reservas/usuario/{usuarioId}` | Minhas reservas e próxima reserva da tela Início |

Os endereços das APIs ficam nas interfaces `VagaApi.java` e `ReservaApi.java`, na constante `URL_BASE`.

---

## 📂 Estrutura do projeto

```
app/src/main/
├── AndroidManifest.xml
├── java/com/example/parkingflow/
│   ├── MainActivity.java     # Telas, navegação do menu e regras do app
│   ├── Reserva.java          # Modelo de reserva (espelha o JSON da API)
│   ├── ReservaApi.java       # Rotas da API de Reservas (Retrofit)
│   ├── Vaga.java             # Modelo de vaga
│   ├── VagaApi.java          # Rotas da API de Vagas (Retrofit)
│   └── ResumoVagas.java      # Totais de vagas por status
└── res/
    ├── layout/
    │   ├── activity_main.xml       # Estrutura principal (menu + conteúdo)
    │   ├── cabecalho_menu.xml      # Topo do menu lateral
    │   ├── tela_inicio.xml         # Tela Início
    │   ├── indicador_etapas.xml    # "1 Detalhes → 2 Confirmação"
    │   ├── tela_nova_reserva.xml   # Formulário da reserva
    │   ├── tela_confirmacao.xml    # Etapa de confirmação
    │   ├── tela_sucesso.xml        # Reserva confirmada
    │   └── item_reserva.xml        # Card de cada reserva na lista
    ├── menu/menu_lateral.xml       # Itens do menu lateral
    ├── drawable/                   # Ícones e fundos
    ├── color/                      # Cores por estado (campos, botões, menu)
    └── values/                     # Cores, estilos e tema
```

---

## ▶️ Como rodar o projeto

### Pré-requisitos
- [Android Studio](https://developer.android.com/studio) instalado
- Um celular Android (7.0 ou superior) com **Depuração USB** ativada, ou um emulador
- Conexão com a internet (as APIs ficam no Azure)

### Passo a passo
1. Clone o repositório:
   ```bash
   git clone https://github.com/SEU-USUARIO/Parkingflow.git
   ```
2. Abra a pasta do projeto no Android Studio (**File → Open**).
3. Aguarde a sincronização do Gradle terminar.
4. Conecte o celular (ou inicie um emulador) e selecione o dispositivo na barra superior.
5. Clique em **▶ Run**.

### Preparando dados de teste
As APIs usam banco de dados **em memória**, então as vagas somem sempre que o servidor reinicia. Se a lista de vagas aparecer vazia, cadastre algumas pelo terminal:

```bash
curl -X POST https://api-xsquad-mts-asfmf9eyg4bsd8d0.brazilsouth-01.azurewebsites.net/vagas/nova-vaga \
  -H "Content-Type: application/json" \
  -d '{"nomeVaga": "A10", "precoHora": 10.00}'
```

> O valor da vaga vai com **ponto** (`10.00`), não com vírgula.

---

## 📏 Regras de negócio

- **Cobrança por hora iniciada:** cada hora começada é cobrada inteira. Exemplo: 2h30 de reserva em uma vaga de R$ 10,00/h custa **R$ 30,00** (3 horas). O app mostra uma prévia com essa regra, mas o valor oficial é sempre o calculado pela API.
- **Reservas a partir do dia seguinte:** a API não aceita reservas para o mesmo dia, por isso o app já sugere a data de amanhã e bloqueia o dia atual no calendário.
- **Vaga precisa existir e estar disponível:** ao criar uma reserva, a API de Reservas consulta a API de Vagas para validar a vaga e obter o nome e o preço.
- **Sem conflito de horário:** a API não permite duas reservas da mesma vaga no mesmo período.

---

## 🚧 Limitações conhecidas e próximos passos

- [ ] **Login e usuário real:** o usuário ainda é fixo (`usuarioId = 1`), até a criação da API de usuários.
- [ ] **Autenticação nas APIs:** as rotas ainda não exigem autenticação.
- [ ] **Mensagens de erro:** exibir erros da API em uma janela completa, em vez de mensagens curtas.
- [ ] **Tempo de espera:** aumentar o tempo limite das requisições para quando o servidor do Azure demora a responder.
- [ ] **Botão voltar do celular:** melhorar a navegação nas telas de sucesso, Início e Minhas reservas.
- [ ] **Tema:** trocar as cores padrão que ainda aparecem no calendário e no relógio pelas cores do app.

---

## 👤 Autor

Desenvolvido por **Gabriel**.
