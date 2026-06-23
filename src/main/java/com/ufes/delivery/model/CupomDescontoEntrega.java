package com.ufes.delivery.model;

public class CupomDescontoEntrega {
    private String nomeMetodo;
    private double valorDesconto;
    private boolean aplicado;

    public CupomDescontoEntrega(String nomeMetodo, double valorDesconto) {
        if (nomeMetodo == null || nomeMetodo.isBlank()) {
            throw new IllegalArgumentException("Nome do metodo de desconto nao pode ser vazio");
        }

        if (valorDesconto < 0) {
            throw new IllegalArgumentException("Desconto na taxa de entrega nao pode ser negativo");
        }

        this.nomeMetodo = nomeMetodo;
        this.valorDesconto = valorDesconto;
    }

    public double getValorDesconto() {
        return valorDesconto;
    }

    public String nomeMetodo() {
        return nomeMetodo;
    }

    public String getNomeMetodo() {
        return nomeMetodo;
    }

    public boolean isAplicado() {
        return aplicado;
    }

    public void aplicar(double valorAplicado) {
        if (aplicado) {
            throw new IllegalStateException("Cupom de desconto da entrega ja foi aplicado");
        }

        if (valorAplicado < 0) {
            throw new IllegalArgumentException("Desconto aplicado nao pode ser negativo");
        }

        if (valorAplicado > valorDesconto) {
            throw new IllegalArgumentException("Desconto aplicado nao pode ser maior que o desconto calculado");
        }

        if (valorAplicado < valorDesconto) {
            nomeMetodo = nomeMetodo + " - Parcial";
        } else {
            nomeMetodo = nomeMetodo + " - Total";
        }

        valorDesconto = valorAplicado;
        aplicado = true;
    }

    @Override
    public String toString() {
        return "CupomDescontoEntrega{"
                + "nomeMetodo='" + nomeMetodo + '\''
                + ", valorDesconto=" + valorDesconto
                + ", aplicado=" + aplicado
                + "}";
    }
}
