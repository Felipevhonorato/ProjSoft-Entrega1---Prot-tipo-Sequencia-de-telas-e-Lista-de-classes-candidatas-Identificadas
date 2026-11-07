package minharefeicao;

import java.io.*;
import java.util.Set;
import java.util.function.Supplier;

final class TelaAssinatura {
    private final BufferedReader entrada;
    private final PrintWriter saida;
    private final ControladorAssinatura ctrl;
    TelaAssinatura(BufferedReader entrada, PrintWriter saida, ControladorAssinatura ctrl) {
        this.entrada = entrada; this.saida = saida; this.ctrl = ctrl;
    }
    void executar() throws IOException {
        saida.println("MINHA REFEIÇÃO | ASSINAR PLANO");
        String celular = tentar(() -> {
            String informado = ler("Celular fictício (10 ou 11 dígitos)");
            informarCelular(informado); return informado;
        });
        while (true) {
            String codigo = ler("Código SMS (ou reenviar)");
            if (codigo.equalsIgnoreCase("reenviar")) { reenviarCodigo(celular); continue; }
            var resultado = informarCodigo(codigo);
            if (resultado == ResultadoCodigo.VALIDO) break;
            if (resultado == ResultadoCodigo.BLOQUEADO) solicitarNovoCodigo();
            else exibirErroCodigo(ctrl.tentativasRestantes());
        }
        exibirPlanos();
        tentar(() -> { selecionarPlano(Integer.parseInt(ler("Número do plano"))); return true; });
        solicitarPreferencias();
        tentar(() -> {
            informarPreferencias(PreferenciaAlimentar.valueOf(ler("Preferência").toUpperCase(java.util.Locale.ROOT)));
            return true;
        });
        for (CategoriaRefeicao ignorada : CategoriaRefeicao.values()) {
            exibirRefeicoes();
            while (true) {
                String escolha = ler("ID para marcar, -ID para desmarcar ou continuar");
                if (escolha.equalsIgnoreCase("continuar")) {
                    if (continuarCategoria()) break;
                    saida.println("Complete a seleção desta categoria.");
                } else {
                    tentarUmaVez(() -> {
                        boolean remover = escolha.startsWith("-");
                        if (selecionarRefeicao(remover ? escolha.substring(1) : escolha, remover ? 0 : 1))
                            exibirSelecao();
                        else exibirLimite(ctrl.limiteAtual());
                        return true;
                    });
                }
            }
        }
        exibirResumo();
        while (!ler("Digite confirmar para seguir ao endereço").equalsIgnoreCase("confirmar"))
            saida.println("Confirme a composição do pedido.");
        confirmarResumo();
        solicitarEndereco();
        tentar(() -> {
            informarEndereco(new DadosEndereco(ler("CEP"), ler("Rua"), ler("Número"),
                    ler("Complemento opcional"), ler("Bairro"), ler("Cidade")));
            return true;
        });
        exibirPagamento();
        while (true) {
            String teste = ler("Cartão de teste: 1111 aprova, 0000 recusa ou desistir");
            if (teste.equalsIgnoreCase("desistir")) { desistir(); exibirEncerramento(); return; }
            if (!Set.of("1111","0000").contains(teste)) {
                saida.println("Informe apenas um identificador fictício indicado."); continue;
            }
            if (informarCartao(new DadosCartao(teste))) { exibirConfirmacao(); return; }
            exibirRecusa();
        }
    }

    void informarCelular(String celular) { ctrl.solicitarCodigo(celular); exibirCodigoEnviado(); }
    ResultadoCodigo informarCodigo(String codigo) { return ctrl.validarCodigo(codigo); }
    void reenviarCodigo(String celular) { ctrl.solicitarCodigo(celular); exibirCodigoEnviado(); }
    void selecionarPlano(int id) { ctrl.selecionarPlano(id); }
    void informarPreferencias(PreferenciaAlimentar preferencia) { ctrl.registrarPreferencias(Set.of(preferencia)); }
    boolean selecionarRefeicao(String id, int quantidade) { return ctrl.selecionarRefeicao(id, quantidade); }
    boolean continuarCategoria() { return ctrl.continuarCategoria(); }
    void confirmarResumo() { ctrl.confirmarResumo(); }
    void informarEndereco(DadosEndereco dados) { ctrl.definirEndereco(dados); }
    boolean informarCartao(DadosCartao dados) { return ctrl.processarPagamento(dados); }
    void desistir() { ctrl.encerrarSemAtivacao(); }

    void exibirCodigoEnviado() { saida.println("Código de confirmação enviado."); }
    void exibirErroCodigo(int restantes) { saida.println("Código inválido. Tentativas restantes: " + restantes); }
    void solicitarNovoCodigo() { saida.println("Limite de três erros atingido. Digite reenviar para solicitar novo código."); }
    void exibirPlanos() {
        for (var plano : ctrl.listarPlanos())
            saida.printf("%d: %d refeições por semana | R$ %s%n", plano.id(), plano.quantidade(), plano.valor());
    }
    void solicitarPreferencias() { saida.println("TRADICIONAL, VEGETARIANA ou SEM_LACTOSE"); }
    void exibirRefeicoes() {
        saida.printf("%s | limite %d%n", ctrl.categoriaAtual(), ctrl.limiteAtual());
        for (var r : ctrl.listarRefeicoes()) saida.printf("%s: %s%n", r.id(), r.nome());
    }
    void exibirSelecao() { saida.printf("Selecionados na categoria: %d/%d%n", ctrl.quantidadeAtual(), ctrl.limiteAtual()); }
    void exibirLimite(int limite) {
        saida.printf("Limite de %d excedido. Pedido não alterado (%d selecionados). Ajuste a seleção.%n",
                limite, ctrl.quantidadeAtual());
    }
    void exibirResumo() {
        var a = ctrl.resumo();
        saida.printf("Resumo: plano %d | %d refeições | %s | R$ %s | %s%n", a.plano.id(), a.plano.quantidade(),
                a.plano.periodicidade(), a.obterValor(), a.assinante.preferencias);
        for (var item : a.pedido.obterItens())
            saida.printf("  %s %s x%d%n", item.refeicao.id(), item.refeicao.nome(), item.obterQuantidade());
    }
    void solicitarEndereco() { saida.println("Endereço de entrega"); }
    void exibirPagamento() {
        var a = ctrl.resumo();
        saida.printf("Assinatura %s | total R$ %s%n", a.status, a.obterValor());
    }
    void exibirRecusa() {
        saida.println("Pagamento não autorizado. Assinatura AGUARDANDO_PAGAMENTO; pedido EM_MONTAGEM.");
        saida.println("Tente o mesmo identificador, outro identificador ou desista.");
    }
    void exibirEncerramento() { saida.println("Caso de uso encerrado sem ativação. Assinatura AGUARDANDO_PAGAMENTO."); }
    void exibirConfirmacao() {
        var a = ctrl.resumo();
        saida.printf("Pagamento confirmado | assinatura %s | pedido %s%n", a.status, a.pedido.status);
        exibirResumo();
        saida.printf("Endereço: %s%nPrimeira entrega: %s, das 8h às 12h%nProtocolo: %s%n",
                a.endereco.descricao(), a.pedido.previsao, a.protocolo);
    }
    private String ler(String pergunta) throws IOException {
        saida.print(pergunta + ": "); saida.flush();
        String linha = entrada.readLine();
        if (linha == null) throw new EOFException("Sessão encerrada antes da conclusão.");
        return linha.strip();
    }
    @FunctionalInterface private interface Entrada<T> { T ler() throws IOException; }
    private <T> T tentar(Entrada<T> entrada) throws IOException {
        while (true) {
            try { return entrada.ler(); }
            catch (IllegalArgumentException e) { saida.println("Entrada inválida. " + e.getMessage()); }
        }
    }
    private void tentarUmaVez(Entrada<Boolean> entrada) throws IOException {
        try { entrada.ler(); } catch (IllegalArgumentException e) { saida.println(e.getMessage()); }
    }
}
