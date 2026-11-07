# Correspondência entre UML e Java

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
