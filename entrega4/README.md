# Minha Refeição · Entrega 4

Implementação Java do caso de uso **Assinar Plano de Refeições**. Inclui o fluxo principal, os três fluxos alternativos e persistência em arquivos TXT. A fronteira é um terminal interativo. O SMS e a operadora de cartão são simulados localmente, sem serviços externos.

- [Modelagem da N2 na Wiki](https://github.com/Felipevhonorato/ProjSoft-Entrega1---Prot-tipo-Sequencia-de-telas-e-Lista-de-classes-candidatas-Identificadas/wiki/Entrega-N2-Modelagem-de-Projeto)
- [Correspondência UML e Java](docs/Rastreabilidade.md)
- [Fontes PlantUML](docs/uml/)
- [Vídeo com explicação e execução dos cenários](docs/demonstracao.mp4)

## Executar

Requisito: **JDK 17 ou superior**, com `java` e `javac` no PATH ou `JAVA_HOME` configurado. Não há dependências externas.

No PowerShell, na pasta `entrega4`:

```powershell
.\executar.ps1
```

No Linux ou macOS, na mesma pasta:

```bash
mkdir -p build
javac --release 17 -encoding UTF-8 -d build src/minharefeicao/*.java
java -cp build minharefeicao.Main dados
```

Use dados fictícios. Exemplo: celular `11999990000`; o código SMS aparece no terminal. Escolha plano `1` e preferência `TRADICIONAL`. Marque `TP1` a `TP6`, uma entrada por linha, e digite `continuar`. Marque `TA1` e avance; marque `TS1` e avance. Digite `confirmar` no resumo e informe CEP fictício de oito dígitos, rua, número, complemento opcional, bairro e cidade. `1111` autoriza e `0000` recusa. Esses identificadores não são números reais de cartão.

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

```powershell
.\executar.ps1 -Testar
```

```bash
mkdir -p build
javac --release 17 -encoding UTF-8 -d build src/minharefeicao/*.java test/minharefeicao/*.java
java -cp build minharefeicao.Cenarios evidencias
```

Os cinco cenários usam a mesma `TelaAssinatura` do programa interativo: sucesso, SMS bloqueado e reenviado, limite e ajuste, recusa com nova tentativa e recusa com desistência. Outras nove execuções pelo controlador cobrem os três planos com as três preferências, os limites de cada categoria e a ordem das etapas, incluindo um assinante já cadastrado. Os testes relêem os arquivos TXT e verificam estados, itens, endereço, protocolo, identificação do pedido, data/hora e histórico de pagamentos. Os registros gerados ficam em `evidencias/`.

## Organização

| Arquivo | Responsabilidade |
|---|---|
| `Main.java` | Inicia a aplicação e fornece a entrada e a saída do terminal |
| `TelaAssinatura.java` | Fronteira que recebe as ações e exibe os resultados |
| `ControladorAssinatura.java` | Coordena as etapas e delega as regras |
| `Dominio.java` | Entidades, valores e estados do caso de uso |
| `Infraestrutura.java` | Catálogos, gateways simulados, serviço de confirmação e repositórios TXT |
| `Cenarios.java` | Demonstrações automatizadas e verificações |
