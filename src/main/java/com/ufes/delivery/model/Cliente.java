package com.ufes.delivery.model;

public class Cliente {
    private String nome;
    private String tipo;
    private double fidelidade;
    private String logradouro;
    private String bairro;
    private String cidade;

    public Cliente(String nome, String tipo, double fidelidade, String logradouro, String bairro, String cidade) {
        validarTextoObrigatorio(nome, "Nome do cliente nao pode ser vazio");
        validarTextoObrigatorio(tipo, "Tipo do cliente nao pode ser vazio");
        validarTextoObrigatorio(logradouro, "Logradouro do cliente nao pode ser vazio");
        validarTextoObrigatorio(bairro, "Bairro do cliente nao pode ser vazio");
        validarTextoObrigatorio(cidade, "Cidade do cliente nao pode ser vazia");

        if (fidelidade < 0) {
            throw new IllegalArgumentException("Fidelidade do cliente nao pode ser negativa");
        }

        this.nome = nome;
        this.tipo = tipo;
        this.fidelidade = fidelidade;
        this.logradouro = logradouro;
        this.bairro = bairro;
        this.cidade = cidade;
    }

    public String getNome() {
        return nome;
    }

    public String getTipo() {
        return tipo;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public String getBairro() {
        return bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public double getFidelidade() {
        return fidelidade;
    }

    public void setFidelidade(double fidelidade) {
        if (fidelidade < 0) {
            throw new IllegalArgumentException("Fidelidade do cliente nao pode ser negativa");
        }

        this.fidelidade = fidelidade;
    }

    private void validarTextoObrigatorio(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(mensagem);
        }
    }

    @Override
    public String toString() {
        return "Cliente{"
                + "nome='" + nome + '\''
                + ", tipo='" + tipo + '\''
                + ", fidelidade=" + fidelidade
                + ", logradouro='" + logradouro + '\''
                + ", bairro='" + bairro + '\''
                + ", cidade='" + cidade + '\''
                + "}";
    }
}
