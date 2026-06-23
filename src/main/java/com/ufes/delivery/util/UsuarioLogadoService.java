/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ufes.delivery.util;

import java.util.Random;


public class UsuarioLogadoService {


    private static final Random geradorAleatorio = new Random();


    public static String getNomeUsuario() {
        int valor = geradorAleatorio.nextInt(100);
        if (valor < 33) {
            return "Balcão PDV 1";
        } else if (valor < 66) {
            return "Gerente";
        } else {
            return "Fulano de tal";
        }
    }
}

