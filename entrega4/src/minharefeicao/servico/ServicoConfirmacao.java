package minharefeicao.servico;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.UUID;
import java.util.Objects;

import minharefeicao.dominio.Endereco;

/** Gera o protocolo e calcula a previsão da primeira entrega. */
public final class ServicoConfirmacao {
    public String gerarProtocolo() { return "MR-" + UUID.randomUUID(); }
    public LocalDate preverPrimeiraEntrega(Endereco endereco, String periodicidade) {
        Objects.requireNonNull(endereco);
        if (!"SEMANAL".equals(periodicidade)) throw new IllegalArgumentException("Periodicidade não disponível.");
        return LocalDate.now(ZoneId.of("America/Sao_Paulo")).with(TemporalAdjusters.next(DayOfWeek.MONDAY));
    }
}
