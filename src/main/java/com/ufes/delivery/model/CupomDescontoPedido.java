package com.ufes.delivery.model;

import java.time.LocalDateTime;

public class CupomDescontoPedido {
    private String codigo;
    private double percentual;
    private LocalDateTime dataHoraInicio;
    private LocalDateTime dataHoraFim;

    public CupomDescontoPedido(String codigo, double percentual, LocalDateTime dataHoraInicio,
            LocalDateTime dataHoraFim) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("Codigo do cupom nao pode ser vazio");
        }

        if (percentual <= 0) {
            throw new IllegalArgumentException("Percentual do cupom deve ser maior que zero");
        }

        if (dataHoraInicio == null || dataHoraFim == null) {
            throw new IllegalArgumentException("Periodo de validade do cupom deve ser informado");
        }

        if (dataHoraFim.isBefore(dataHoraInicio)) {
            throw new IllegalArgumentException("Data final do cupom nao pode ser anterior a data inicial");
        }

        this.codigo = codigo;
        this.percentual = percentual;
        this.dataHoraInicio = dataHoraInicio;
        this.dataHoraFim = dataHoraFim;
    }

    public String getCodigo() {
        return codigo;
    }

    public double getPercentual() {
        return percentual;
    }

    public LocalDateTime getDataHoraInicio() {
        return dataHoraInicio;
    }

    public LocalDateTime getDataHoraFim() {
        return dataHoraFim;
    }

    @Override
    public String toString() {
        return "CupomDescontoPedido{"
                + "codigo='" + codigo + '\''
                + ", percentual=" + percentual
                + ", dataHoraInicio=" + dataHoraInicio
                + ", dataHoraFim=" + dataHoraFim
                + "}";
    }
}
