package ru.itmo.commands;

import ru.itmo.commands.interactives.login.LoginInteractive;
import ru.itmo.commands.utills.AuthorizeCommandType;
import ru.itmo.managers.commandManager.CommandManager;
import ru.itmo.models.UserDB;
import ru.itmo.request.CommandRequest;
import ru.itmo.request.RequestType;

import java.util.Scanner;

public class LoginCommand extends Command{

    private final Scanner scanner;

    public LoginCommand(Scanner scanner) {
        setTargetTitleForUserInput("login");
        setDescription("login : войти в аккаунт");
        this.scanner = scanner;

        setAuthorizeCommandType(AuthorizeCommandType.NON_AUTHORIZE);
    }

    @Override
    public CommandRequest execute(CommandManager commandManager, String arguments) {
        UserDB userDB = new UserDB();
        LoginInteractive.loginInteractive(userDB, scanner);

        CommandRequest commandRequest = new CommandRequest();

        commandRequest.setRequestType(RequestType.LOGIN);
        commandRequest.setObject(userDB);
        commandRequest.setParameters(null);

        return commandRequest;
    }
}
