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
find src -name '*.java' > build/fontes.txt
javac --release 17 -encoding UTF-8 -d build @build/fontes.txt
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
find src test -name '*.java' > build/fontes.txt
javac --release 17 -encoding UTF-8 -d build @build/fontes.txt
java -cp build minharefeicao.Cenarios evidencias
```

Os cinco cenários usam a mesma `TelaAssinatura` do programa interativo: sucesso, SMS bloqueado e reenviado, limite e ajuste, recusa com nova tentativa e recusa com desistência. Outras nove execuções pelo controlador cobrem os três planos com as três preferências, os limites de cada categoria e a ordem das etapas, incluindo um assinante já cadastrado. Os testes relêem os arquivos TXT e verificam estados, itens, endereço, protocolo, identificação do pedido, data/hora e histórico de pagamentos. Os registros gerados ficam em `evidencias/`.

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
