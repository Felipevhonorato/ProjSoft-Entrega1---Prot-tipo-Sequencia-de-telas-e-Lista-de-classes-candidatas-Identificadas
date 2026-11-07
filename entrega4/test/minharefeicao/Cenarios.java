package minharefeicao;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Executa a mesma TelaAssinatura usada no modo interativo, com entradas reproduzíveis. */
public final class Cenarios {
    public static void main(String[] args) throws IOException {
        Path evidencias = args.length > 0 ? Path.of(args[0]) : Path.of("evidencias");
        Files.createDirectories(evidencias);
        executar(evidencias, "principal", List.of("1234"), false, List.of("1111"), "ATIVA");
        executar(evidencias, "alternativo-1", List.of("9999","9999","9999","1234","reenviar","1234","5678"),
                false, List.of("1111"), "ATIVA");
        executar(evidencias, "alternativo-2", List.of("1234"), true, List.of("1111"), "ATIVA");
        executar(evidencias, "alternativo-3-tentar", List.of("1234"), false, List.of("0000","0000","1111"), "ATIVA");
        executar(evidencias, "alternativo-3-desistir", List.of("1234"), false, List.of("0000","desistir"), "AGUARDANDO_PAGAMENTO");
    }
    private static void executar(Path evidencias, String nome, List<String> sms,
                                 boolean exceder, List<String> cartoes, String status) throws IOException {
        Path dados = Files.createTempDirectory(evidencias.toAbsolutePath(), nome+"-");
        List<String> acoes = new ArrayList<>(List.of("11999990000"));
        acoes.addAll(sms);
        acoes.addAll(List.of("1","TRADICIONAL","TP1","TP2","TP3","TP4","TP5","TP6"));
        if (exceder) acoes.addAll(List.of("TP7","-TP6","TP7"));
        acoes.addAll(List.of("continuar","TA1","continuar","TS1","continuar","confirmar",
                "00000000","Rua Fictícia","100","","Bairro de Teste","Cidade de Teste"));
        acoes.addAll(cartoes);
        var log = new StringWriter();
        var saida = new PrintWriter(log, true);
        var codigos = List.of("1234","5678").iterator();
        var ctrl = new ControladorAssinatura(dados,new GatewaySMS(saida),codigos::next);
        try(var entrada = new BufferedReader(new StringReader(String.join("\n",acoes)+"\n")) {
            @Override public String readLine() throws IOException {
                String linha = super.readLine();
                if (linha != null) saida.println(linha);
                return linha;
            }
        }) { new TelaAssinatura(entrada,saida,ctrl).executar(); }
        var a = ctrl.resumo();
        var arquivo = dados.resolve("assinaturas").resolve(a.id+".txt");
        var persistido = ArquivoTxt.ler(arquivo);
        exigir(status.equals(persistido.getProperty("assinatura.status")), "Estado da assinatura");
        boolean ativo = status.equals("ATIVA");
        exigir((ativo ? "APROVADO" : "EM_MONTAGEM").equals(persistido.getProperty("pedido.status")), "Estado do pedido");
        exigir(ativo == !persistido.getProperty("assinatura.protocolo").isEmpty(), "Protocolo somente após confirmação");
        exigir("8".equals(persistido.getProperty("pedido.itens")), "Itens persistidos");
        exigir("119.40".equals(persistido.getProperty("plano.valor")), "Preço do plano preservado");
        exigir("00000000".equals(persistido.getProperty("entrega.cep")), "Endereço gravado");
        var leitor = new RepositorioAssinantes(dados).obterOuCadastrar("11999990000");
        exigir(leitor.celularValidado && leitor.preferencias.contains(PreferenciaAlimentar.TRADICIONAL), "Assinante relido");
        var tentativas = cartoes.stream().filter(c -> !c.equals("desistir")).toList();
        exigir(Integer.toString(tentativas.size()).equals(persistido.getProperty("pagamento.tentativas")), "Histórico das tentativas");
        for (int i=0;i<tentativas.size();i++) {
            String esperado = tentativas.get(i).equals("0000") ? "NAO_AUTORIZADO" : "CONFIRMADO";
            exigir(esperado.equals(persistido.getProperty("pagamento."+(i+1)+".status")), "Resultado persistido");
        }
        if (nome.equals("alternativo-1")) {
            exigir(log.toString().contains("Limite de três erros atingido"), "Bloqueio do SMS");
            exigir(log.toString().contains("Código inválido. Tentativas restantes: 2"), "Código anterior invalidado");
            exigir(log.toString().contains("final 0000: 5678"), "Novo código enviado");
        }
        if (exceder) {
            exigir(log.toString().contains("Pedido não alterado (6 selecionados)"), "Excesso rejeitado sem mutação");
            exigir(a.pedido.obterItens().stream().anyMatch(i -> i.refeicao.id().equals("TP7")), "Ajuste aceito");
            exigir(a.pedido.obterItens().stream().noneMatch(i -> i.refeicao.id().equals("TP6")), "Item removido");
        }
        try {
            ctrl.processarPagamento(new DadosCartao("1111"));
            throw new AssertionError("Pagamento após encerramento foi aceito");
        } catch(IllegalStateException esperado) { /* sessão encerrada ou assinatura ativa */ }
        Files.writeString(evidencias.resolve(nome+".txt"), log.toString() +
                "\n[Persistência relida]\nassinatura.status=" + status +
                "\npedido.status=" + persistido.getProperty("pedido.status") +
                "\npagamento.tentativas=" + tentativas.size() + "\n", StandardCharsets.UTF_8);
        System.out.println("OK " + nome + " | " + status + " | tentativas=" + tentativas.size());
    }
    private static void exigir(boolean ok, String regra) {
        if (!ok) throw new AssertionError(regra);
    }
}
