package com.ufes.delivery.util;

import java.time.LocalDateTime;
import java.util.Random;

public class UsuarioLogadoService {

    private static final Random geradorAleatorio = new Random();
    private static String nomeUsuarioLogado = null;
    private static String perfilUsuarioLogado = null;
    private static LocalDateTime dataHoraLogin = null;

    public static String getNomeUsuario() {
        if (nomeUsuarioLogado != null) {
            return nomeUsuarioLogado;
        }
        int valor = geradorAleatorio.nextInt(100);
        if (valor < 33) {
            return "Balcão PDV 1";
        } else if (valor < 66) {
            return "Gerente";
        } else {
            return "Fulano de tal";
        }
    }

    public static void setUsuarioLogado(String nome, String perfil, LocalDateTime dataHora) {
        nomeUsuarioLogado = nome;
        perfilUsuarioLogado = perfil;
        dataHoraLogin = dataHora;
    }

    public static String getPerfilUsuarioLogado() {
        return perfilUsuarioLogado;
    }

    public static LocalDateTime getDataHoraLogin() {
        return dataHoraLogin;
    }

    public static void limparSessao() {
        nomeUsuarioLogado = null;
        perfilUsuarioLogado = null;
        dataHoraLogin = null;
    }
}
