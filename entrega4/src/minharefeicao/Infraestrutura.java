package minharefeicao;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

final class CatalogoPlanos {
    private final List<PlanoAssinatura> planos = List.of(
            new PlanoAssinatura(1, 6, 4, 2, new BigDecimal("119.40")),
            new PlanoAssinatura(2, 12, 4, 2, new BigDecimal("214.80")),
            new PlanoAssinatura(3, 20, 4, 2, new BigDecimal("318.00")));
    List<PlanoAssinatura> listarDisponiveis() { return planos; }
    PlanoAssinatura obter(int id) {
        return planos.stream().filter(p -> p.id() == id).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Plano inválido."));
    }
}

final class CatalogoRefeicoes {
    private final List<Refeicao> refeicoes = new ArrayList<>();
    CatalogoRefeicoes() {
        for (var preferencia : PreferenciaAlimentar.values()) {
            for (var categoria : CategoriaRefeicao.values()) {
                int total = switch (categoria) { case PRINCIPAL -> 21; case ACOMPANHAMENTO -> 5; case SOBREMESA -> 3; };
                String nome = switch (categoria) {
                    case PRINCIPAL -> preferencia == PreferenciaAlimentar.VEGETARIANA ? "Prato vegetal" : "Prato principal";
                    case ACOMPANHAMENTO -> "Acompanhamento";
                    case SOBREMESA -> "Sobremesa";
                };
                String prefixo = preferencia.name().substring(0,1) + categoria.name().substring(0,1);
                for (int i = 1; i <= total; i++)
                    refeicoes.add(new Refeicao(prefixo + i, nome + " " + i, categoria,
                            Set.of(1, 2, 3), Set.of(preferencia), true));
            }
        }
    }
    List<Refeicao> filtrar(PlanoAssinatura plano, Set<PreferenciaAlimentar> preferencias, CategoriaRefeicao categoria) {
        return refeicoes.stream().filter(r -> r.compativel(plano, preferencias, categoria)).toList();
    }
    Refeicao obter(String id) {
        return refeicoes.stream().filter(r -> r.id().equalsIgnoreCase(id)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Refeição não encontrada."));
    }
}

record Cardapio(CatalogoRefeicoes catalogo) {
    List<Refeicao> listarCompativeis(PlanoAssinatura plano, Set<PreferenciaAlimentar> preferencias, CategoriaRefeicao categoria) {
        return catalogo.filtrar(plano, preferencias, categoria);
    }
}

record GatewaySMS(PrintWriter saida) {
    void enviar(String celular, String codigo) {
        saida.printf("[SMS simulado] código para final %s: %s%n", celular.substring(celular.length()-4), codigo);
        saida.flush();
    }
}

final class GatewayOperadora {
    final OperadoraCartaoCredito operadora = new OperadoraCartaoCredito("Operadora simulada");
    ResultadoAutorizacao autorizar(Pagamento pagamento, DadosCartao dados) {
        if (!Set.of("1111","0000").contains(dados.identificadorTeste()))
            throw new IllegalArgumentException("Use 1111 (aprova) ou 0000 (recusa).");
        boolean autorizado = dados.identificadorTeste().equals("1111");
        return new ResultadoAutorizacao(autorizado, autorizado ? "AUTH-" + pagamento.id : "");
    }
}

final class ServicoConfirmacao {
    String gerarProtocolo() { return "MR-" + UUID.randomUUID(); }
    LocalDate preverPrimeiraEntrega(Endereco endereco, String periodicidade) {
        Objects.requireNonNull(endereco);
        if (!"SEMANAL".equals(periodicidade)) throw new IllegalArgumentException("Periodicidade não disponível.");
        return LocalDate.now(ZoneId.of("America/Sao_Paulo")).with(TemporalAdjusters.next(DayOfWeek.MONDAY));
    }
}

final class ArquivoTxt {
    static Properties ler(Path arquivo) {
        Properties dados = new Properties();
        if (!Files.exists(arquivo)) return dados;
        try (var reader = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
            dados.load(reader);
            return dados;
        } catch(IOException e) { throw new UncheckedIOException(e); }
    }
    static void salvar(Path arquivo, Properties dados) {
        Path temporario = null;
        try {
            Files.createDirectories(arquivo.toAbsolutePath().getParent());
            temporario = Files.createTempFile(arquivo.toAbsolutePath().getParent(), "gravacao-", ".tmp");
            try(var writer = Files.newBufferedWriter(temporario, StandardCharsets.UTF_8)) {
                dados.store(writer, "Minha Refeição - dados da demonstração");
            }
            try { Files.move(temporario, arquivo, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException e) { Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING); }
        } catch (IOException e) { throw new UncheckedIOException("Não foi possível gravar " + arquivo, e); }
        finally {
            if (temporario != null) try { Files.deleteIfExists(temporario); } catch (IOException ignored) {}
        }
    }
}

record RepositorioAssinantes(Path pasta) {
    private Path caminho(String celular) { return pasta.resolve("assinantes").resolve(celular + ".txt"); }
    Assinante obterOuCadastrar(String celular) {
        var dados = ArquivoTxt.ler(caminho(celular));
        var assinante = new Assinante(celular);
        assinante.celularValidado = Boolean.parseBoolean(dados.getProperty("validado", "false"));
        var preferencias = EnumSet.noneOf(PreferenciaAlimentar.class);
        for (String item : dados.getProperty("preferencias","").split(","))
            if (!item.isBlank()) preferencias.add(PreferenciaAlimentar.valueOf(item));
        if (!preferencias.isEmpty()) assinante.registrarPreferencias(preferencias);
        return assinante;
    }
    void salvar(Assinante assinante) {
        var dados = new Properties();
        dados.setProperty("celular", assinante.celular);
        dados.setProperty("validado", Boolean.toString(assinante.celularValidado));
        dados.setProperty("preferencias", String.join(",", assinante.preferencias.stream().map(Enum::name).toList()));
        ArquivoTxt.salvar(caminho(assinante.celular), dados);
    }
}

record RepositorioAssinaturas(Path pasta) {
    void salvar(Assinatura a) {
        var dados = new Properties();
        dados.setProperty("assinatura.id", a.id.toString());
        dados.setProperty("assinatura.status", a.status.name());
        dados.setProperty("assinatura.protocolo", a.protocolo);
        dados.setProperty("assinante.celular", a.assinante.celular);
        dados.setProperty("assinante.preferencias", String.join(",", a.assinante.preferencias.stream().map(Enum::name).toList()));
        dados.setProperty("plano.id", Integer.toString(a.plano.id()));
        dados.setProperty("plano.periodicidade", a.plano.periodicidade());
        dados.setProperty("plano.valor", a.obterValor().toPlainString());
        dados.setProperty("plano.quantidadeRefeicoes", Integer.toString(a.plano.quantidade()));
        dados.setProperty("plano.maxAcompanhamentos", Integer.toString(a.plano.acompanhamentos()));
        dados.setProperty("plano.maxSobremesas", Integer.toString(a.plano.sobremesas()));
        dados.setProperty("pedido.id", a.pedido.id.toString());
        dados.setProperty("pedido.status", a.pedido.status.name());
        if (a.pedido.previsao != null) dados.setProperty("pedido.previsao", a.pedido.previsao.toString());
        int i = 0;
        for (ItemPedido item : a.pedido.obterItens()) {
            String chave = "pedido.item." + (++i) + ".";
            dados.setProperty(chave+"refeicao", item.refeicao.id());
            dados.setProperty(chave+"nome", item.refeicao.nome());
            dados.setProperty(chave+"categoria", item.refeicao.categoria().name());
            dados.setProperty(chave+"quantidade", Integer.toString(item.obterQuantidade()));
        }
        dados.setProperty("pedido.itens", Integer.toString(i));
        if (a.endereco != null) {
            var e = a.endereco.dados();
            dados.setProperty("entrega.cep", e.cep());
            dados.setProperty("entrega.logradouro", e.logradouro());
            dados.setProperty("entrega.numero", e.numero());
            dados.setProperty("entrega.complemento", e.complemento());
            dados.setProperty("entrega.bairro", e.bairro());
            dados.setProperty("entrega.cidade", e.cidade());
        }
        i = 0;
        for (Pagamento pagamento : a.pagamentos()) {
            String chave = "pagamento." + (++i) + ".";
            dados.setProperty(chave+"id", pagamento.id.toString());
            dados.setProperty(chave+"dataHora", pagamento.dataHora.toString());
            dados.setProperty(chave+"status", pagamento.status.name());
            dados.setProperty(chave+"valor", pagamento.valor.toPlainString());
            dados.setProperty(chave+"cartaoFinal", pagamento.cartao.ultimosDigitos());
            dados.setProperty(chave+"operadora", pagamento.operadora.nome());
            dados.setProperty(chave+"codigoAutorizacao", pagamento.codigoAutorizacao);
        }
        dados.setProperty("pagamento.tentativas", Integer.toString(i));
        ArquivoTxt.salvar(pasta.resolve("assinaturas").resolve(a.id+".txt"), dados);
    }
}
