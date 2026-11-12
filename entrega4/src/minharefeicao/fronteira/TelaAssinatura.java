package minharefeicao.fronteira;

import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.util.Set;
import java.util.Locale;

import minharefeicao.dominio.Assinatura;
import minharefeicao.dominio.Pedido;
import minharefeicao.dominio.Pagamento;
import minharefeicao.dominio.PreferenciaAlimentar;
import minharefeicao.dominio.CategoriaRefeicao;
import minharefeicao.dominio.ResultadoCodigo;
import minharefeicao.dto.DadosEndereco;
import minharefeicao.dto.DadosCartao;
import minharefeicao.controle.ControladorAssinatura;

/** Fronteira do sistema: recebe entradas ou simula a comunicação externa. */
public final class TelaAssinatura {
    private final BufferedReader entrada;
    private final PrintWriter saida;
    private final ControladorAssinatura ctrl;
    public TelaAssinatura(BufferedReader entrada, PrintWriter saida, ControladorAssinatura ctrl) {
        this.entrada = entrada; this.saida = saida; this.ctrl = ctrl;
    }
    public void executar() throws IOException {
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

    public void informarCelular(String celular) { ctrl.solicitarCodigo(celular); exibirCodigoEnviado(); }
    public ResultadoCodigo informarCodigo(String codigo) { return ctrl.validarCodigo(codigo); }
    public void reenviarCodigo(String celular) { ctrl.solicitarCodigo(celular); exibirCodigoEnviado(); }
    public void selecionarPlano(int id) { ctrl.selecionarPlano(id); }
    public void informarPreferencias(PreferenciaAlimentar preferencia) { ctrl.registrarPreferencias(Set.of(preferencia)); }
    public boolean selecionarRefeicao(String id, int quantidade) { return ctrl.selecionarRefeicao(id, quantidade); }
    public boolean continuarCategoria() { return ctrl.continuarCategoria(); }
    public void confirmarResumo() { ctrl.confirmarResumo(); }
    public void informarEndereco(DadosEndereco dados) { ctrl.definirEndereco(dados); }
    public boolean informarCartao(DadosCartao dados) { return ctrl.processarPagamento(dados); }
    public void desistir() { ctrl.encerrarSemAtivacao(); }

    public void exibirCodigoEnviado() { saida.println("Código de confirmação enviado."); }
    public void exibirErroCodigo(int restantes) { saida.println("Código inválido. Tentativas restantes: " + restantes); }
    public void solicitarNovoCodigo() { saida.println("Limite de três erros atingido. Digite reenviar para solicitar novo código."); }
    public void exibirPlanos() {
        for (var plano : ctrl.listarPlanos())
            saida.printf("%d: %d refeições por semana | R$ %s%n", plano.id(), plano.quantidade(), plano.valor());
    }
    public void solicitarPreferencias() { saida.println("TRADICIONAL, VEGETARIANA ou SEM_LACTOSE"); }
    public void exibirRefeicoes() {
        saida.printf("%s | limite %d%n", ctrl.categoriaAtual(), ctrl.limiteAtual());
        for (var r : ctrl.listarRefeicoes()) saida.printf("%s: %s%n", r.id(), r.nome());
    }
    public void exibirSelecao() { saida.printf("Selecionados na categoria: %d/%d%n", ctrl.quantidadeAtual(), ctrl.limiteAtual()); }
    public void exibirLimite(int limite) {
        saida.printf("Limite de %d excedido. Pedido não alterado (%d selecionados). Ajuste a seleção.%n",
                limite, ctrl.quantidadeAtual());
    }
    public void exibirResumo() {
        var a = ctrl.resumo();
        saida.printf("Resumo: plano %d | %d refeições | %s | R$ %s | %s%n", a.obterPlano().id(), a.obterPlano().quantidade(),
                a.obterPlano().periodicidade(), a.obterValor(), a.obterAssinante().obterPreferencias());
        for (var item : a.obterPedido().obterItens())
            saida.printf("  %s %s x%d%n", item.obterRefeicao().id(), item.obterRefeicao().nome(), item.obterQuantidade());
    }
    public void solicitarEndereco() { saida.println("Endereço de entrega"); }
    public void exibirPagamento() {
        var a = ctrl.resumo();
        saida.printf("Assinatura %s | total R$ %s%n", a.obterStatus(), a.obterValor());
    }
    public void exibirRecusa() {
        saida.println("Pagamento não autorizado. Assinatura AGUARDANDO_PAGAMENTO; pedido EM_MONTAGEM.");
        saida.println("Tente o mesmo identificador, outro identificador ou desista.");
    }
    public void exibirEncerramento() { saida.println("Caso de uso encerrado sem ativação. Assinatura AGUARDANDO_PAGAMENTO."); }
    public void exibirConfirmacao() {
        var a = ctrl.resumo();
        saida.printf("Pagamento confirmado | assinatura %s | pedido %s%n", a.obterStatus(), a.obterPedido().obterStatus());
        exibirResumo();
        saida.printf("Endereço: %s%nPrimeira entrega: %s, das 8h às 12h%nProtocolo: %s%n",
                a.obterEndereco().descricao(), a.obterPedido().obterPrevisao(), a.obterProtocolo());
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
