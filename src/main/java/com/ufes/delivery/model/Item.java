package com.ufes.delivery.model;

public class Item {
    private String nome;
    private int quantidade;
    private double valorUnitario;
    private String tipo;

    public Item(String nome, int quantidade, double valorUnitario, String tipo) {
        validarTextoObrigatorio(nome, "Nome do item nao pode ser vazio");
        validarTextoObrigatorio(tipo, "Tipo do item nao pode ser vazio");

        if (quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade do item deve ser maior que zero");
        }

        if (valorUnitario < 0) {
            throw new IllegalArgumentException("Valor unitario do item nao pode ser negativo");
        }

        this.nome = nome;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
        this.tipo = tipo;
    }

    public double valorTotal() {
        return valorUnitario * quantidade;
    }

    public String getNome() {
        return nome;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public double getValorUnitario() {
        return valorUnitario;
    }

    public String getTipo() {
        return tipo;
    }

    private void validarTextoObrigatorio(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensagem);
        }
    }

    @Override
    public String toString() {
        return "Item{"
                + "nome='" + nome + '\''
                + ", quantidade=" + quantidade
                + ", valorUnitario=" + valorUnitario
                + ", tipo='" + tipo + '\''
                + ", valorTotal=" + valorTotal()
                + "}";
    }
}
