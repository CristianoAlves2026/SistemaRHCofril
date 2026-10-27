/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package br.com.Utilidades;

import java.security.MessageDigest;

/**
 *
 * @author Lavanderia
 */
public class Criptografia {
    public static String criptografarSenha(String senha) {

        try {

            MessageDigest md = MessageDigest.getInstance("SHA-256");

            byte[] hash = md.digest(senha.getBytes("UTF-8"));

            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {

                String hex = Integer.toHexString(0xff & b);

                if (hex.length() == 1) {
                    hexString.append('0');
                }

                hexString.append(hex);
            }

            return hexString.toString();

        } catch (Exception e) {

            throw new RuntimeException("Erro ao criptografar senha");

        }
    }
}
