# Código antigo (antes do MVVM)

Esta pasta guarda o código Java do app **como era antes da aplicação do MVVM**
(commit "projeto completo sem o mvvm"), só para consulta e comparação.

- Ela fica **fora** de `app/src`, então o Android Studio **não compila** estes arquivos
  e eles não interferem no app.
- O código em uso está em `app/src/main/java/com/example/parkingflow/`, separado nos pacotes
  `model`, `view`, `viewmodel`, `repository`, `api` e `util`.

Principal diferença: antes, a `MainActivity` (~955 linhas) fazia tudo — telas, chamadas às APIs,
validações e cálculos. Agora ela só cuida da tela (~690 linhas) e as regras ficam nos ViewModels.
