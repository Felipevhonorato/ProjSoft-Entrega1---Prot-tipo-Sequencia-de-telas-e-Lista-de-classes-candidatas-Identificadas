package minharefeicao;

import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.StringWriter;
import java.io.StringReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.DayOfWeek;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import minharefeicao.dominio.Assinante;
import minharefeicao.dominio.Pedido;
import minharefeicao.dominio.ItemPedido;
import minharefeicao.dominio.Pagamento;
import minharefeicao.dominio.PreferenciaAlimentar;
import minharefeicao.dominio.CategoriaRefeicao;
import minharefeicao.dominio.Endereco;
import minharefeicao.dominio.CampoEndereco;
import minharefeicao.dto.DadosEndereco;
import minharefeicao.dto.DadosCartao;
import minharefeicao.fronteira.TelaAssinatura;
import minharefeicao.fronteira.GatewaySMS;
import minharefeicao.controle.ControladorAssinatura;
import minharefeicao.colecao.CatalogoPlanos;
import minharefeicao.colecao.CatalogoRefeicoes;
import minharefeicao.persistencia.ArquivoTxt;
import minharefeicao.persistencia.RepositorioAssinantes;

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
        executar(evidencias, "quantidades-e-endereco", List.of("1234"), false, List.of("1111"), "ATIVA");
        verificarCatalogoEEtapas(evidencias);
    }
    private static void executar(Path evidencias, String nome, List<String> sms,
                                 boolean exceder, List<String> cartoes, String status) throws IOException {
        Path dados = Files.createTempDirectory(evidencias.toAbsolutePath(), nome+"-");
        List<String> acoes = new ArrayList<>(List.of("11999990000"));
        boolean repetidos = nome.equals("quantidades-e-endereco");
        acoes.addAll(sms);
        acoes.addAll(List.of("1","TRADICIONAL"));
        if (repetidos) acoes.addAll(List.of("TP1", "TP1", "TP1 4", "TP1 7", "TP2 3", "-TP1", "TP2 3"));
        else acoes.addAll(List.of("TP1","TP2","TP3","TP4","TP5","TP6"));
        if (exceder) acoes.addAll(List.of("TP7","-TP6","TP7"));
        if (repetidos) {
            acoes.addAll(List.of("continuar", "TA1", "TA1", "-TA1", "TA1 2", "continuar", "TS1", "TS1", "continuar", "confirmar",
                    "abc", "123", "00000000", "", "123", "Rua Fictícia", "abc", "-1", "100", "",
                    "", "123", "Bairro de Teste", "", "999", "Cidade de Teste"));
        } else acoes.addAll(List.of("continuar","TA1","continuar","TS1","continuar","confirmar",
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
        var arquivo = dados.resolve("assinaturas").resolve(a.obterId()+".txt");
        var persistido = ArquivoTxt.ler(arquivo);
        exigir(status.equals(persistido.getProperty("assinatura.status")), "Estado da assinatura");
        boolean ativo = status.equals("ATIVA");
        exigir((ativo ? "APROVADO" : "EM_MONTAGEM").equals(persistido.getProperty("pedido.status")), "Estado do pedido");
        exigir(ativo == !persistido.getProperty("assinatura.protocolo").isEmpty(), "Protocolo somente após confirmação");
        exigir((repetidos ? "4" : "8").equals(persistido.getProperty("pedido.itens")), "Itens distintos persistidos");
        exigir("119.40".equals(persistido.getProperty("plano.valor")), "Preço do plano preservado");
        exigir("00000000".equals(persistido.getProperty("entrega.cep")), "Endereço gravado");
        var leitor = new RepositorioAssinantes(dados).obterOuCadastrar("11999990000");
        exigir(leitor.obterCelularValidado() && leitor.obterPreferencias().contains(PreferenciaAlimentar.TRADICIONAL), "Assinante relido");
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
            exigir(a.obterPedido().obterItens().stream().anyMatch(i -> i.obterRefeicao().id().equals("TP7")), "Ajuste aceito");
            exigir(a.obterPedido().obterItens().stream().noneMatch(i -> i.obterRefeicao().id().equals("TP6")), "Item removido");
        }
        if (repetidos) {
            var catalogo = new CatalogoRefeicoes();
            exigir(a.obterPedido().quantidadeDaRefeicao(catalogo.obter("TP1")) == 3, "Repetir, definir total e retirar unidade");
            exigir(a.obterPedido().quantidadeDaRefeicao(catalogo.obter("TP2")) == 3, "Limite conta unidades, não itens distintos");
            exigir(a.obterPedido().quantidadeDaRefeicao(catalogo.obter("TA1")) == 2, "Acompanhamento repetido");
            exigir(a.obterPedido().quantidadeDaRefeicao(catalogo.obter("TS1")) == 2, "Sobremesa repetida");
            exigir(log.toString().contains("Pedido não alterado (4 selecionados)"), "Excesso mantém quantidade anterior");
            for (String campo : List.of("CEP", "Rua", "Número", "Bairro", "Cidade")) {
                exigir(log.toString().split(java.util.regex.Pattern.quote(campo + ": "), -1).length - 1 == 3,
                        "Repete somente o campo inválido: " + campo);
            }
            exigir(log.toString().split("Complemento opcional: ", -1).length - 1 == 1, "Complemento vazio aceito");
            for (int i = 1; i <= 4; i++) exigir(!persistido.getProperty("pedido.item."+i+".nome").matches("(Prato principal|Acompanhamento|Sobremesa) [0-9]+"), "Nomes de refeições persistidos");
            exigir(log.toString().contains("Frango grelhado com arroz e feijão x3"), "Resumo mostra nome e quantidade");
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
    private static void verificarCatalogoEEtapas(Path evidencias) throws IOException {
        for (var plano : new CatalogoPlanos().listarDisponiveis()) {
            for (var preferencia : PreferenciaAlimentar.values()) {
                Path dados = Files.createTempDirectory(evidencias.toAbsolutePath(), "catalogo-");
                var repositorio = new RepositorioAssinantes(dados);
                var existente = new Assinante("11999990000");
                existente.confirmarCelular();
                existente.registrarPreferencias(Set.of(PreferenciaAlimentar.TRADICIONAL));
                repositorio.salvar(existente);
                var ctrl = new ControladorAssinatura(dados, new GatewaySMS(new PrintWriter(new StringWriter())), () -> "1234");
                rejeitar(() -> ctrl.selecionarPlano(plano.id()), "Plano antes do SMS");
                ctrl.solicitarCodigo(existente.obterCelular());
                ctrl.validarCodigo("1234");
                ctrl.selecionarPlano(plano.id());
                rejeitar(() -> ctrl.listarRefeicoes(), "Preferência antiga não confirma nova contratação");
                rejeitar(() -> ctrl.selecionarRefeicao("TP1", 1), "Seleção antes das preferências");
                rejeitar(() -> ctrl.continuarCategoria(), "Avanço antes das preferências");
                rejeitar(() -> ctrl.confirmarResumo(), "Resumo prematuro");
                rejeitar(() -> ctrl.processarPagamento(new DadosCartao("1111")), "Pagamento prematuro");
                ctrl.registrarPreferencias(Set.of(preferencia));
                for (var categoria : CategoriaRefeicao.values()) {
                    var opcoes = ctrl.listarRefeicoes();
                    exigir(opcoes.stream().allMatch(r -> r.compativel(plano, Set.of(preferencia), categoria)), "Filtro de catálogo");
                    exigir(!ctrl.continuarCategoria(), "Categoria vazia não avança");
                    int limite = plano.limite(categoria);
                    for (int i = 0; i < limite; i++) exigir(ctrl.selecionarRefeicao(opcoes.get(i).id(), 1), "Seleção até o limite");
                    exigir(ctrl.selecionarRefeicao(opcoes.get(0).id(), 1) && ctrl.quantidadeAtual() == limite, "Marcar novamente não duplica");
                    exigir(!ctrl.selecionarRefeicao(opcoes.get(limite).id(), 1) && ctrl.quantidadeAtual() == limite, "Excesso não altera pedido");
                    exigir(!ctrl.selecionarRefeicao(opcoes.get(0).id(), 2), "Unidade adicional excede categoria já completa");
                    exigir(ctrl.selecionarRefeicao(opcoes.get(0).id(), 0), "Quantidade zero remove o item");
                    exigir(ctrl.ajustarQuantidade(opcoes.get(1).id(), 1) && ctrl.quantidadeAtual() == limite,
                            "Repetir refeição preenche a vaga liberada");
                    exigir(!ctrl.ajustarQuantidade(opcoes.get(1).id(), 1) && ctrl.quantidadeAtual() == limite,
                            "Repetir novamente não ultrapassa o limite");
                    exigir(ctrl.selecionarRefeicao(opcoes.get(1).id(), 1), "Restaura quantidade do segundo item");
                    exigir(ctrl.selecionarRefeicao(opcoes.get(0).id(), 1), "Restaura o primeiro item");
                    rejeitar(() -> ctrl.registrarPreferencias(Set.of(preferencia)), "Preferência após seleção");
                    exigir(ctrl.continuarCategoria(), "Categoria completa avança");
                }
                ctrl.confirmarResumo();
                ctrl.definirEndereco(new DadosEndereco("00000000", "Rua Fictícia", "100", "", "Bairro", "Cidade"));
                exigir(ctrl.processarPagamento(new DadosCartao("1111")), "Autorização");
                var a = ctrl.resumo();
                var persistido = ArquivoTxt.ler(dados.resolve("assinaturas").resolve(a.obterId() + ".txt"));
                exigir(a.obterPedido().obterId().toString().equals(persistido.getProperty("pedido.id")), "Pedido identificado em disco");
                exigir(Integer.toString(plano.quantidade()).equals(persistido.getProperty("plano.quantidadeRefeicoes")), "Quantidade contratada em disco");
                exigir(plano.valor().toPlainString().equals(persistido.getProperty("plano.valor")), "Valor contratado");
                exigir(Integer.toString(plano.quantidade()+plano.acompanhamentos()+plano.sobremesas()).equals(persistido.getProperty("pedido.itens")), "Composição persistida");
                java.time.OffsetDateTime.parse(persistido.getProperty("pagamento.1.dataHora"));
                exigir(a.obterPedido().obterPrevisao().isAfter(java.time.LocalDate.now(java.time.ZoneId.of("America/Sao_Paulo")))
                        && a.obterPedido().obterPrevisao().getDayOfWeek() == java.time.DayOfWeek.MONDAY, "Próxima segunda-feira");
                rejeitar(() -> ctrl.processarPagamento(new DadosCartao("1111")), "Pagamento duplicado");
                System.out.println("OK catálogo e contratos | plano=" + plano.id() + " | " + preferencia);
            }
        }
        rejeitar(() -> new ItemPedido(new CatalogoRefeicoes().obter("TP1"), -1), "Quantidade negativa na entidade");
        rejeitar(() -> new ItemPedido(new CatalogoRefeicoes().obter("TP1"), 0), "Item armazenado exige quantidade positiva");
        rejeitar(() -> new Endereco(new DadosEndereco("abc", "Rua", "100", "", "Bairro", "Cidade")), "Domínio rejeita CEP inválido sem a tela");
        rejeitar(() -> new Endereco(new DadosEndereco("00000000", "Rua", "abc", "", "Bairro", "Cidade")), "Domínio rejeita número inválido sem a tela");
        Endereco.validarCampo(CampoEndereco.NUMERO, "S/N");
        Endereco.validarCampo(CampoEndereco.NUMERO, "100-A");
        Endereco.validarCampo(CampoEndereco.LOGRADOURO, "Rua 25 de Março");
    }
    private static void rejeitar(Runnable operacao, String regra) {
        try { operacao.run(); } catch (IllegalStateException | IllegalArgumentException esperado) { return; }
        throw new AssertionError(regra);
    }
    private static void exigir(boolean ok, String regra) {
        if (!ok) throw new AssertionError(regra);
    }
}

