package minharefeicao.persistencia;

import java.nio.file.Path;
import java.util.Properties;

import minharefeicao.dominio.Assinatura;
import minharefeicao.dominio.ItemPedido;
import minharefeicao.dominio.Pagamento;

/** Lê ou grava os registros TXT da demonstração. */
public record RepositorioAssinaturas(Path pasta) {
    public void salvar(Assinatura a) {
        var dados = new Properties();
        dados.setProperty("assinatura.id", a.obterId().toString());
        dados.setProperty("assinatura.status", a.obterStatus().name());
        dados.setProperty("assinatura.protocolo", a.obterProtocolo());
        dados.setProperty("assinante.celular", a.obterAssinante().obterCelular());
        dados.setProperty("assinante.preferencias", String.join(",", a.obterAssinante().obterPreferencias().stream().map(Enum::name).toList()));
        dados.setProperty("plano.id", Integer.toString(a.obterPlano().id()));
        dados.setProperty("plano.periodicidade", a.obterPlano().periodicidade());
        dados.setProperty("plano.valor", a.obterValor().toPlainString());
        dados.setProperty("plano.quantidadeRefeicoes", Integer.toString(a.obterPlano().quantidade()));
        dados.setProperty("plano.maxAcompanhamentos", Integer.toString(a.obterPlano().acompanhamentos()));
        dados.setProperty("plano.maxSobremesas", Integer.toString(a.obterPlano().sobremesas()));
        dados.setProperty("pedido.id", a.obterPedido().obterId().toString());
        dados.setProperty("pedido.status", a.obterPedido().obterStatus().name());
        if (a.obterPedido().obterPrevisao() != null) dados.setProperty("pedido.previsao", a.obterPedido().obterPrevisao().toString());
        int i = 0;
        for (ItemPedido item : a.obterPedido().obterItens()) {
            String chave = "pedido.item." + (++i) + ".";
            dados.setProperty(chave+"refeicao", item.obterRefeicao().id());
            dados.setProperty(chave+"nome", item.obterRefeicao().nome());
            dados.setProperty(chave+"categoria", item.obterRefeicao().categoria().name());
            dados.setProperty(chave+"quantidade", Integer.toString(item.obterQuantidade()));
        }
        dados.setProperty("pedido.itens", Integer.toString(i));
        if (a.obterEndereco() != null) {
            var e = a.obterEndereco().dados();
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
            dados.setProperty(chave+"id", pagamento.obterId().toString());
            dados.setProperty(chave+"dataHora", pagamento.obterDataHora().toString());
            dados.setProperty(chave+"status", pagamento.obterStatus().name());
            dados.setProperty(chave+"valor", pagamento.obterValor().toPlainString());
            dados.setProperty(chave+"cartaoFinal", pagamento.obterCartao().ultimosDigitos());
            dados.setProperty(chave+"operadora", pagamento.obterOperadora().nome());
            dados.setProperty(chave+"codigoAutorizacao", pagamento.obterCodigoAutorizacao());
        }
        dados.setProperty("pagamento.tentativas", Integer.toString(i));
        ArquivoTxt.salvar(pasta.resolve("assinaturas").resolve(a.obterId()+".txt"), dados);
    }
}
