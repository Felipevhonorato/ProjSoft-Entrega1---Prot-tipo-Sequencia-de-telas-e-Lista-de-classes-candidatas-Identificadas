package minharefeicao.persistencia;

import java.nio.file.Path;
import java.util.Properties;
import java.util.EnumSet;

import minharefeicao.dominio.Assinante;
import minharefeicao.dominio.PreferenciaAlimentar;

/** Lê ou grava os registros TXT da demonstração. */
public record RepositorioAssinantes(Path pasta) {
    private Path caminho(String celular) { return pasta.resolve("assinantes").resolve(celular + ".txt"); }
    public Assinante obterOuCadastrar(String celular) {
        var dados = ArquivoTxt.ler(caminho(celular));
        var assinante = new Assinante(celular);
        if (Boolean.parseBoolean(dados.getProperty("validado", "false"))) assinante.confirmarCelular();
        var preferencias = EnumSet.noneOf(PreferenciaAlimentar.class);
        for (String item : dados.getProperty("preferencias","").split(","))
            if (!item.isBlank()) preferencias.add(PreferenciaAlimentar.valueOf(item));
        if (!preferencias.isEmpty()) assinante.registrarPreferencias(preferencias);
        return assinante;
    }
    public void salvar(Assinante assinante) {
        var dados = new Properties();
        dados.setProperty("celular", assinante.obterCelular());
        dados.setProperty("validado", Boolean.toString(assinante.obterCelularValidado()));
        dados.setProperty("preferencias", String.join(",", assinante.obterPreferencias().stream().map(Enum::name).toList()));
        ArquivoTxt.salvar(caminho(assinante.obterCelular()), dados);
    }
}
