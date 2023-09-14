package ru.itmo.utilsCommon;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordHash {

    public static String getPasswordHash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD2");
            byte[] hashedPassword = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedPassword) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Не удалось создать хэш для пароля");
        }
        return null;
    }

}
