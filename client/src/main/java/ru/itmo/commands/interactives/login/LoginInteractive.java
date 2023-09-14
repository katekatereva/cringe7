package ru.itmo.commands.interactives.login;

import ru.itmo.models.UserDB;

import java.util.Scanner;

public class LoginInteractive {

    public static void loginInteractive(UserDB userDB, Scanner scanner) {

        System.out.print("Логин: ");
        userDB.setUsername(scanner.nextLine());

        System.out.print("Пароль: ");
        userDB.setPassword(scanner.nextLine());

    }

}
