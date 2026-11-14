# Minha Refeição · Entrega 4

Implementação Java do caso de uso **Assinar Plano de Refeições**. Inclui o fluxo principal, os três fluxos alternativos e persistência em arquivos TXT. A fronteira é um terminal interativo. O SMS e a operadora de cartão são simulados localmente, sem serviços externos.

- [Modelagem da N2 na Wiki](https://github.com/Felipevhonorato/ProjSoft-Projeto-Marmitas/wiki/Entrega-N2-Modelagem-de-Projeto)
- [Correspondência UML e Java](docs/Rastreabilidade.md)
- [Fontes PlantUML](docs/uml/)
- [Vídeo de apresentação do grupo](https://www.youtube.com/watch?v=sefnWrgGPCI)

## Como executar em cada ambiente

### Antes de começar

1. Abra o repositório do projeto, não o repositório da Wiki.
2. Use **JDK 17 ou superior**. É necessário ter tanto `java` quanto `javac` disponíveis no terminal. Não há Maven, Gradle ou bibliotecas externas.
3. Escolha somente o bloco correspondente ao terminal utilizado. Windows PowerShell e Prompt de Comando (CMD) têm comandos diferentes de Bash/Zsh.
4. No Windows, Linux ou macOS, os comandos abaixo partem da **raiz do repositório**, que contém a pasta `entrega4`. Se o terminal já estiver em `entrega4`, pule a linha `cd entrega4` ou `Set-Location entrega4`.

Confira o Java em qualquer ambiente:

```text
java -version
javac -version
```

As duas versões devem ser 17 ou superiores. `JAVA_HOME`, quando utilizado pelo script PowerShell, deve apontar para a pasta do JDK, não para sua subpasta `bin`. Nos comandos diretos, `java` e `javac` precisam estar no PATH; configurar apenas JAVA_HOME não basta.

| Ambiente / terminal | Seção |
|---|---|
| Windows PowerShell | [Windows: PowerShell](#windows-powershell) |
| Windows Prompt de Comando | [Windows: CMD](#windows-cmd) |
| Linux Bash ou macOS Zsh/Bash | [Linux e macOS](#linux-e-macos-bash-ou-zsh) |
| Terminal Bash do GitHub Codespaces | [Codespaces](#github-codespaces-bash) |

### Windows: PowerShell

**Iniciar o sistema:**

```powershell
Set-Location entrega4
.\executar.ps1
```

**Executar os testes**, já dentro de `entrega4`:

```powershell
.\executar.ps1 -Testar
```

Se a política local impedir a execução de `.ps1`, use os comandos diretos abaixo no PowerShell, dentro de `entrega4`. Eles dispensam o script e não exigem mudar a política de execução.

**Sistema sem script:**

```powershell
New-Item -ItemType Directory -Force build | Out-Null
javac --release 17 -encoding UTF-8 -d build -sourcepath src src/minharefeicao/Main.java
if ($LASTEXITCODE -eq 0) { java -cp build minharefeicao.Main dados }
```

**Testes sem script:**

```powershell
New-Item -ItemType Directory -Force build | Out-Null
javac --release 17 -encoding UTF-8 -d build -sourcepath "src;test" test/minharefeicao/Cenarios.java
if ($LASTEXITCODE -eq 0) { java -cp build minharefeicao.Cenarios evidencias }
```

### Windows: CMD

**Iniciar o sistema no Prompt de Comando:**

```bat
cd entrega4
if not exist build mkdir build
javac --release 17 -encoding UTF-8 -d build -sourcepath src src/minharefeicao/Main.java && java -cp build minharefeicao.Main dados
```

**Executar os testes**, já dentro de `entrega4`:

```bat
if not exist build mkdir build
javac --release 17 -encoding UTF-8 -d build -sourcepath "src;test" test/minharefeicao/Cenarios.java && java -cp build minharefeicao.Cenarios evidencias
```

### Linux e macOS: Bash ou Zsh

Os mesmos comandos funcionam em Linux e no terminal padrão do macOS. Use um JDK instalado e disponível no terminal.

**Iniciar o sistema:**

```bash
cd entrega4
mkdir -p build && javac --release 17 -encoding UTF-8 -d build -sourcepath src src/minharefeicao/Main.java && java -cp build minharefeicao.Main dados
```

**Executar os testes**, já dentro de `entrega4`:

```bash
mkdir -p build && javac --release 17 -encoding UTF-8 -d build -sourcepath "src:test" test/minharefeicao/Cenarios.java && java -cp build minharefeicao.Cenarios evidencias
```

No WSL, siga esta seção, utilizando o JDK do ambiente Linux. No Git Bash do Windows com JDK Windows, a aplicação usa a mesma linha acima, mas o teste deve usar `-sourcepath "src;test"`, com ponto e vírgula, pois quem interpreta o caminho é o JDK Windows.

### GitHub Codespaces: Bash

Abra o Codespace **do repositório do projeto** e depois um terminal Bash. Não é necessário abrir porta, acessar localhost ou instalar dependências de site: o programa roda no próprio terminal.

Os comandos abaixo encontram a pasta do projeto a partir de qualquer subpasta do repositório. Execute a navegação primeiro; se ela apresentar erro, confira o repositório aberto antes de continuar.

**Iniciar o sistema:**

```bash
cd "$(git rev-parse --show-toplevel)/entrega4"
java -version
javac -version
mkdir -p build && javac --release 17 -encoding UTF-8 -d build -sourcepath src src/minharefeicao/Main.java && java -cp build minharefeicao.Main dados
```

**Executar os testes:**

```bash
cd "$(git rev-parse --show-toplevel)/entrega4"
mkdir -p build && javac --release 17 -encoding UTF-8 -d build -sourcepath "src:test" test/minharefeicao/Cenarios.java && java -cp build minharefeicao.Cenarios evidencias
```

Se `java` ou `javac` não existir, ou estiver abaixo da versão 17, disponibilize um JDK 17 ou superior no Codespace antes de compilar. A compilação só inicia a aplicação quando `javac` termina sem erro, por causa do `&&`.

Para receber alterações recentes do projeto em um clone existente, execute `git pull --ff-only` e compile novamente. Se a atualização falhar, resolva a mensagem do Git antes de tentar rodar uma versão que ainda não foi atualizada.

### Como usar depois de iniciar

1. Informe um celular fictício, por exemplo `11999990000`.
2. Digite o código mostrado no SMS simulado no terminal; ele não é fixo.
3. Escolha plano `1` e preferência `TRADICIONAL`.
4. Selecione os pratos. Exemplo: `TP1 3` e `TP2 3` completam seis unidades. Repetir `TP1` soma uma; `-TP1` retira uma; `TP1 0` remove o item. Digite `continuar`.
5. Escolha `TA1` e `continuar`; depois `TS1` e `continuar`.
6. Digite `confirmar`, preencha o endereço e pressione Enter sem texto no complemento, se não houver. Um campo inválido é solicitado novamente imediatamente.
7. No pagamento, `1111` simula autorização e `0000` simula recusa. Após recusa, tente novamente ou digite `desistir`.

### Saídas, persistência e solução de erros

- Os testes devem apresentar **15 linhas iniciadas por `OK`**: seis cenários pela tela e nove combinações de plano e preferência pelo controlador.
- O sistema grava cadastros em `entrega4/dados/assinantes/` e contratações em `entrega4/dados/assinaturas/`. Os testes usam `entrega4/evidencias/`. Esses dados são locais.
- O SMS e a operadora de cartão são simulados. Nenhum código SMS nem dado completo de cartão é persistido. A retomada de uma contratação de outra sessão não faz parte deste cenário.

| Mensagem ou sintoma | Como conferir e corrigir |
|---|---|
| `java` ou `javac` não encontrado / não reconhecido | Instale ou disponibilize um JDK 17+ no ambiente e ajuste o PATH; abra novamente o terminal e confira as duas versões. |
| `release version 17 not supported` / `invalid source release: 17` | O `javac` selecionado é antigo. Use um JDK 17 ou superior. |
| `UnsupportedClassVersionError` | O `java` usado para executar é antigo. Confira se java e javac pertencem ao JDK adequado. |
| `file not found: src/minharefeicao/Main.java` | A pasta atual está errada ou o projeto não foi atualizado. Dentro de entrega4 deve existir src/minharefeicao/Main.java. |
| `cd: entrega4: No such file or directory` | Se já estiver em entrega4, não repita esse cd. Caso contrário, localize a raiz do projeto. No Codespaces, use a navegação pela raiz do Git indicada acima. |
| `Could not find or load main class minharefeicao.Main` | A compilação não terminou, ou o terminal saiu de entrega4. Refaça o bloco completo e confira se build/minharefeicao/Main.class foi gerado. |
| `package minharefeicao... does not exist` | Não compile Main.java isoladamente sem `-sourcepath src`. Use a linha completa desta página. |
| PowerShell bloqueia executar.ps1 | Use a seção “Sistema sem script” ou “Testes sem script”; não é necessário alterar a política. |
| A aplicação parece antiga | Atualize o clone e recompile os fontes antes de executar. O comando java sozinho não recompila o código. |

No Linux/macOS/Codespaces, confira a pasta com `pwd` e `ls`. No PowerShell, use `Get-Location` e `Get-ChildItem`; no CMD, `cd` e `dir`. Se persistir um erro, informe o terminal usado, o comando completo e a saída do erro.

## Uso do cardápio e validação

### Quantidades e nomes no cardápio

Cada opção mostra ID e nome, como `TP1: Frango grelhado com arroz e feijão`, `TA1: Arroz branco` e `TS1: Pudim de leite`. Os catálogos de demonstração respeitam o filtro da preferência selecionada.

| Entrada | Resultado |
|---|---|
| `TP1` | Acrescenta uma unidade; repetir acrescenta outra |
| `-TP1` | Retira uma unidade; ao chegar a zero, remove o item |
| `TP1 3` | Define o total desse prato como três unidades |
| `TP1 0` | Remove todas as unidades desse prato |
| `continuar` | Avança apenas se a categoria estiver completa |

Os limites contam **unidades**, não pratos distintos. No plano 1, `TP1 3` e `TP2 3` completam os seis pratos principais. Acompanhamentos e sobremesas também podem repetir e mantêm seus limites próprios. Alterações acima do limite são recusadas sem modificar as quantidades anteriores. O resumo, o total selecionado e os arquivos TXT conservam as quantidades de cada item. O preço continua sendo o do plano.

### Validação imediata do endereço

A tela repete somente o campo inválido, sem pedir novamente os campos já aceitos. A regra fica em `Endereco.validarCampo`; o controlador a encaminha e a entidade também revalida todos os campos no construtor.

- CEP: oito dígitos, com ou sem hífen. É uma validação de formato, sem consulta a serviço postal.
- Rua, bairro e cidade: preenchimento com letras; nomes como `Rua 25 de Março` são aceitos.
- Número: exemplos aceitos `100`, `100-A` e `S/N`.
- Complemento: opcional, aceita Enter sem preenchimento.

## Exercitar os fluxos alternativos

| Fluxo | Como reproduzir | Resultado esperado |
|---|---|---|
| 1 — Código inválido | Digite três códigos incorretos; depois use `reenviar` e informe o novo código exibido | O desafio anterior é bloqueado; o novo permite prosseguir |
| 2 — Limite de refeições | Com seis pratos selecionados no plano 1, tente `TP7`; desmarque `-TP6` e selecione `TP7` | O excesso não altera a seleção; o ajuste é aceito |
| 3 — Pagamento recusado | Digite `0000`, tente novamente ou use `1111`. Em outra sessão, recuse e digite `desistir` | A recusa não ativa a assinatura. Nova autorização ativa; desistência encerra sem protocolo |

## Persistência

`dados/assinantes/<celular>.txt` guarda a validação e a preferência. `dados/assinaturas/<id>.txt` guarda o plano contratado, os itens, o endereço, os estados e o histórico de pagamentos. A gravação usa um arquivo temporário e substitui o registro; quando o sistema de arquivos suporta, a substituição é atômica. O programa relê o cadastro na validação de um novo acesso. A retomada de uma assinatura de outra sessão não faz parte deste cenário.

Nenhum código SMS ou dado completo de cartão é persistido. A pasta de dados locais fica fora do Git.

## Testes reproduzíveis

Use o bloco de testes da seção correspondente ao seu terminal acima. A saída esperada contém 15 linhas OK.

Os cinco cenários originais usam a mesma `TelaAssinatura` do programa interativo: sucesso, SMS bloqueado e reenviado, limite e ajuste, recusa com nova tentativa e recusa com desistência. Um sexto cenário pela tela testa refeições repetidas, definição e redução de quantidades, nomes no resumo e correção imediata de cada campo do endereço. Outras nove execuções pelo controlador cobrem os três planos com as três preferências, os limites de cada categoria e a ordem das etapas, incluindo um assinante já cadastrado. Os testes relêem os arquivos TXT e verificam estados, itens, endereço, protocolo, identificação do pedido, data/hora e histórico de pagamentos. Os registros gerados ficam em `evidencias/`.

## Organização

Cada classe, record ou enum tem seu próprio arquivo, com o mesmo nome do tipo Java. As pastas correspondem aos pacotes e às responsabilidades do projeto.

| Pasta ou arquivo | Responsabilidade |
|---|---|
| [Main.java](src/minharefeicao/Main.java) | Ponto de entrada da aplicação |
| [dominio/](src/minharefeicao/dominio/) | Classes do modelo de domínio e seus estados |
| [fronteira/](src/minharefeicao/fronteira/) | TelaAssinatura e gateways simulados de SMS e pagamento |
| [controle/](src/minharefeicao/controle/) | ControladorAssinatura: coordena a contratação |
| [colecao/](src/minharefeicao/colecao/) | CatalogoPlanos e CatalogoRefeicoes |
| [persistencia/](src/minharefeicao/persistencia/) | RepositorioAssinantes, RepositorioAssinaturas e ArquivoTxt |
| [servico/](src/minharefeicao/servico/) | ServicoConfirmacao: protocolo e previsão da entrega |
| [dto/](src/minharefeicao/dto/) | DadosEndereco, DadosCartao e ResultadoAutorizacao |
| [Cenarios.java](test/minharefeicao/Cenarios.java) | Demonstrações automatizadas e verificações |

### Onde encontrar as classes do modelo de domínio

Todas estão em `src/minharefeicao/dominio/`:

| Conceito do modelo | Arquivo Java |
|---|---|
| Assinante | [Assinante.java](src/minharefeicao/dominio/Assinante.java) |
| Plano de assinatura | [PlanoAssinatura.java](src/minharefeicao/dominio/PlanoAssinatura.java) |
| Assinatura | [Assinatura.java](src/minharefeicao/dominio/Assinatura.java) |
| Preferência alimentar | [PreferenciaAlimentar.java](src/minharefeicao/dominio/PreferenciaAlimentar.java), enum com as opções do protótipo |
| Cardápio | [Cardapio.java](src/minharefeicao/dominio/Cardapio.java) |
| Refeição | [Refeicao.java](src/minharefeicao/dominio/Refeicao.java) |
| Pedido | [Pedido.java](src/minharefeicao/dominio/Pedido.java) |
| Item do pedido | [ItemPedido.java](src/minharefeicao/dominio/ItemPedido.java) |
| Endereço | [Endereco.java](src/minharefeicao/dominio/Endereco.java) |
| Pagamento | [Pagamento.java](src/minharefeicao/dominio/Pagamento.java) |
| Cartão de crédito | [CartaoCredito.java](src/minharefeicao/dominio/CartaoCredito.java) |
| Operadora de cartão | [OperadoraCartaoCredito.java](src/minharefeicao/dominio/OperadoraCartaoCredito.java) |

Para acompanhar o fluxo no código, comece por `Main` → `fronteira/TelaAssinatura` → `controle/ControladorAssinatura`. O controlador chama o domínio, os catálogos, os serviços e a persistência conforme as etapas dos diagramas. Os antigos arquivos agrupadores `Dominio.java` e `Infraestrutura.java` foram substituídos pelos arquivos individuais.
