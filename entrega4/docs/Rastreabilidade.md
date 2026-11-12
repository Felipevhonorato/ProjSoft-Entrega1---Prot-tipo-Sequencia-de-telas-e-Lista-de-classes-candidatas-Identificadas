# Correspondência entre UML e Java

## Localização dos objetos no projeto

Cada tipo possui um arquivo próprio em `src/minharefeicao/`. Os pacotes são `dominio` (entidades, valores e estados), `fronteira` (tela e gateways), `controle` (controlador), `colecao` (catálogos), `persistencia` (repositórios e TXT), `servico` (confirmação) e `dto` (dados de entrada e resposta). `Main.java` inicia a aplicação; `test/minharefeicao/Cenarios.java` verifica os cenários. O [README](../README.md#organização) contém links para todas as classes do modelo de domínio.

Os nomes e responsabilidades dos diagramas são mantidos. A separação introduziu imports, visibilidade pública nos pontos usados entre pacotes e métodos de leitura dos atributos; o comportamento do caso de uso permanece o mesmo. No vídeo, trechos exibidos nos antigos arquivos agrupadores correspondem agora aos arquivos individuais com o nome de cada classe.

Os números de mensagem são os da [modelagem publicada na Wiki](https://github.com/Felipevhonorato/ProjSoft-Entrega1---Prot-tipo-Sequencia-de-telas-e-Lista-de-classes-candidatas-Identificadas/wiki/Entrega-N2-Modelagem-de-Projeto). As cinco primeiras interações são partes de uma mesma contratação; o controlador conserva a mesma assinatura e o mesmo pedido.

| Interação | Mensagens representativas | Implementação |
|---|---|---|
| UC01.1 — Identificação | 01.01–01.05: informar celular, criar e enviar código; 01.06–01.13: informar, validar, cadastrar e apresentar planos | `TelaAssinatura.informarCelular/informarCodigo` → `ControladorAssinatura.solicitarCodigo/validarCodigo` → `CodigoConfirmacao.validar`, `GatewaySMS.enviar`, `RepositorioAssinantes` e `CatalogoPlanos.listarDisponiveis` |
| UC01.2 — Plano e preferências | Selecionar plano, instanciar assinatura e pedido, registrar preferências | `ControladorAssinatura.selecionarPlano/registrarPreferencias`, `CatalogoPlanos.obter`, `Assinatura.vincularPedido`, `Assinante.registrarPreferencias` |
| UC01.3 — Cardápio | Listar compatíveis, selecionar, calcular total projetado e continuar categoria | `Cardapio.listarCompativeis` → `CatalogoRefeicoes.filtrar` → `Refeicao.compativel`; `Pedido.definirSelecao/totalProjetado`, `PlanoAssinatura.aceitaQuantidade/categoriaCompleta` |
| UC01.4 — Resumo e endereço | Confirmar resumo, criar endereço, aguardar pagamento e salvar | `TelaAssinatura.confirmarResumo/informarEndereco` → `ControladorAssinatura.confirmarResumo/definirEndereco` → `Assinatura.definirEndereco/aguardarPagamento` → `RepositorioAssinaturas.salvar` |
| UC01.5 — Pagamento | 05.01–05.08: receber dados, criar tentativa, autorizar e registrar resultado; 05.09–05.16: protocolo, previsão, aprovação, ativação e gravação | `TelaAssinatura.informarCartao` → `ControladorAssinatura.processarPagamento` → `Pagamento.registrarResultado`, `GatewayOperadora.autorizar`, `ServicoConfirmacao`, `Pedido.aprovar`, `Assinatura.ativar` e `RepositorioAssinaturas.salvar` |
| Fluxo alternativo 1 | Validar repetidamente, bloquear no terceiro erro e reenviar | `CodigoConfirmacao.validar/invalidar`; `TelaAssinatura.solicitarNovoCodigo/reenviarCodigo`; `ControladorAssinatura.solicitarCodigo` |
| Fluxo alternativo 2 | Rejeitar excesso, informar limite, desmarcar e retornar à seleção | `Pedido.totalProjetado/definirSelecao` valida antes de alterar a composição; `TelaAssinatura.exibirLimite` mantém a etapa atual |
| Fluxo alternativo 3 | Exibir recusa, permitir nova tentativa e desistir sem ativar | `TelaAssinatura.exibirRecusa/desistir`; `ControladorAssinatura.processarPagamento/encerrarSemAtivacao`; cada `Pagamento` é persistido |

## Adaptações da implementação

- A interação é por terminal. O controlador devolve resultados para que a fronteira exiba a próxima mensagem; isso preserva a ordem do cenário sem acoplar o controlador às operações de impressão.
- O catálogo local usa identificadores curtos para planos e refeições, e a preferência é um enum. Os limites de acompanhamentos e sobremesas são dados de demonstração: 4 e 2 por plano. As opções do catálogo são fictícias.
- `BigDecimal` representa valores monetários, `LocalDate` representa a previsão e `UUID` identifica assinaturas e pagamentos.
- O desafio SMS fica na sessão. O gateway de pagamento aceita identificadores fictícios e não realiza cobrança. O serviço de confirmação prevê a próxima segunda-feira e gera protocolo único.
- O repositório mantém a composição e as tentativas em um TXT por assinatura. Um pedido contém seus itens e é associado à assinatura; os catálogos apenas disponibilizam opções.

## Correspondência estrutural e diferenças de representação

O modelo de domínio da Entrega 2 descreve os conceitos do negócio, sem controladores nem operações de software. A página de domínio com métodos é uma visão complementar; os diagramas de classes de projeto da N2 descrevem as responsabilidades implementadas. O fluxo principal continua único, dividido em cinco imagens; os três fluxos alternativos conservam a numeração das entregas anteriores.

| Elemento do projeto | Representação Java e justificativa |
|---|---|
| Assinante, PlanoAssinatura e Refeicao identificados no modelo | Celular identifica o assinante; `int` identifica planos e `String` identifica refeições. Esses identificadores locais substituem UUIDs do desenho, mantendo a identidade e as referências. Assinatura, Pedido e Pagamento usam UUID. |
| Assinatura → PlanoAssinatura | O plano é um `record` imutável conservado na contratação. Valor e periodicidade são obtidos dessa referência; quantidade e limites também são gravados no TXT. |
| Pedido ◆— ItemPedido → Refeicao | O mapa privado do pedido possui os itens, sem compartilhá-los entre pedidos. Cada item mantém referência à refeição do catálogo. Desmarcar remove o item; itens existentes têm quantidade 1. |
| PreferenciaAlimentar | Enum representa as três opções do protótipo. A coleção permite o vínculo conceitual; a interface solicita uma opção. Toda contratação exige confirmação, mesmo com cadastro anterior. |
| Cardapio, CodigoConfirmacao e estados | Cardapio consulta o catálogo; o desafio SMS é um objeto da sessão. `erros` e `encerrado` implementam bloqueio/invalidação, sem persistir o código. A implementação não mantém IDs para esses objetos transitórios. |
| DadosEndereco e Endereco | Os campos ficam agrupados em um `record` DadosEndereco, guardado pelo Endereco. A entidade valida os campos obrigatórios antes de alterar a assinatura. |
| DadosCartao e CartaoCredito | O adaptador de demonstração usa apenas `1111`/`0000`, sem solicitar número completo, titular, validade ou CVV do desenho da integração. CartaoCredito guarda esse identificador fictício. GatewayOperadora mantém a resposta de autorização usada no cenário. |
| Pagamento | Cada tentativa tem UUID, valor, data/hora com fuso, cartão, operadora, resultado e código de autorização. Esses dados são gravados separadamente no histórico da assinatura. |
| Fronteira e controlador | Os métodos de exibição sem argumentos consultam o contexto pelo controlador. Operações que no modelo devolvem a resposta à tela retornam booleanos ou ResultadoCodigo; a TelaAssinatura decide qual método de exibição chamar. Os nomes das ações do caso de uso são preservados. |

Não há cobrança ou SMS real. A gravação é atômica por arquivo quando suportada; não existe transação entre os arquivos de assinante e assinatura. Não há retomada de contratação em outra sessão. Essas limitações delimitam a demonstração e não acrescentam novos fluxos ao caso de uso.

## Verificação da implementação

`executar.ps1 -Testar` executa os cinco cenários pela fronteira de terminal e outras nove combinações de três planos com três preferências pelo controlador. As verificações adicionais incluem cadastro existente, confirmação de preferências na sessão atual, ordem das etapas, filtros, limites das três categorias, seleção repetida sem duplicação, quantidade inválida sem alteração, bloqueio de pagamento repetido, próxima segunda-feira e leitura do TXT com identificador do pedido e data/hora do pagamento.
