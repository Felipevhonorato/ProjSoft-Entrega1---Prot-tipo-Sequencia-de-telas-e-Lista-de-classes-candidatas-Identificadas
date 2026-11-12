package minharefeicao.dto;

/** Transporta dados entre a fronteira, o controlador e os objetos do cenário. */
public record ResultadoAutorizacao(boolean autorizado, String codigoAutorizacao) {}
