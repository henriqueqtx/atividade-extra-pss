package com.ufes.delivery.model;

import com.ufes.delivery.util.SecurityUtils;

public class Usuario {
    private String nome;
    private String usuario;
    private String senha; // Hashed password
    private String perfil; // ADMINISTRADOR or ATENDENTE
    private String situacao; // AUTORIZADO, PENDENTE, NAO_AUTORIZADO

    // Constructor for creating new users (validates and hashes the clean password)
    public static Usuario criarNovoUsuario(String nome, String usuario, String senhaLimpa) {
        validarNome(nome);
        validarUsuario(usuario);
        validarSenhaLimpa(senhaLimpa);
        
        String senhaHashed = SecurityUtils.hashSenha(senhaLimpa);
        return new Usuario(nome, usuario, senhaHashed, "ATENDENTE", "PENDENTE");
    }

    // Constructor for creating the first user (validates and hashes the clean password)
    public static Usuario criarPrimeiroUsuario(String nome, String usuario, String senhaLimpa) {
        validarNome(nome);
        validarUsuario(usuario);
        validarSenhaLimpa(senhaLimpa);
        
        String senhaHashed = SecurityUtils.hashSenha(senhaLimpa);
        return new Usuario(nome, usuario, senhaHashed, "ADMINISTRADOR", "AUTORIZADO");
    }

    // Constructor for loading existing user from DB
    public Usuario(String nome, String usuario, String senhaHashed, String perfil, String situacao) {
        this.nome = nome;
        this.usuario = usuario;
        this.senha = senhaHashed;
        this.perfil = perfil;
        this.situacao = situacao;
    }

    public String getNome() {
        return nome;
    }

    public String getUsuario() {
        return usuario;
    }

    public String getSenha() {
        return senha;
    }

    public String getPerfil() {
        return perfil;
    }

    public String getSituacao() {
        return situacao;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public void setSituacao(String situacao) {
        this.situacao = situacao;
    }

    public static void validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome nao pode ser vazio");
        }
        if (nome.length() < 2 || nome.length() > 120) {
            throw new IllegalArgumentException("Nome deve conter de 2 a 120 caracteres");
        }
        // Accepts only letters, spaces, apostrophes and hyphens
        if (!nome.matches("^[a-zA-ZáéíóúâêîôûãõçÁÉÍÓÚÂÊÎÔÛÃÕÇ'\\s\\-]+$")) {
            throw new IllegalArgumentException("Nome deve aceitar apenas letras, espacos, apostrofos e hifens");
        }
    }

    public static void validarUsuario(String usuario) {
        if (usuario == null || usuario.isBlank()) {
            throw new IllegalArgumentException("Nome de usuario nao pode ser vazio");
        }
        if (usuario.length() < 3 || usuario.length() > 30) {
            throw new IllegalArgumentException("Nome de usuario deve conter de 3 a 30 caracteres");
        }
        // Only lowercase letters and numbers, no spaces
        if (!usuario.matches("^[a-z0-9]+$")) {
            throw new IllegalArgumentException("Nome de usuario deve usar apenas letras minusculas e algarismos sem espacos");
        }
    }

    public static void validarSenhaLimpa(String senhaLimpa) {
        if (senhaLimpa == null || senhaLimpa.isEmpty()) {
            throw new IllegalArgumentException("Senha nao pode ser vazia");
        }
        if (senhaLimpa.length() < 8 || senhaLimpa.length() > 64) {
            throw new IllegalArgumentException("Senha deve conter de 8 a 64 caracteres");
        }
    }
}
