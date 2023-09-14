package ru.itmo.commands;

import ru.itmo.commands.interactives.login.LoginInteractive;
import ru.itmo.commands.interactives.login.RegisterInteractive;
import ru.itmo.commands.utills.AuthorizeCommandType;
import ru.itmo.managers.commandManager.CommandManager;
import ru.itmo.models.UserDB;
import ru.itmo.request.CommandRequest;
import ru.itmo.request.RequestType;

import java.util.Scanner;

public class RegisterCommand extends Command{
    private final Scanner scanner;

    public RegisterCommand(Scanner scanner) {
        setTargetTitleForUserInput("register");
        setDescription("register : регистрация аккаунта");
        this.scanner = scanner;

        setAuthorizeCommandType(AuthorizeCommandType.NON_AUTHORIZE);
    }

    @Override
    public CommandRequest execute(CommandManager commandManager, String arguments) {
        UserDB userDB = new UserDB();
        RegisterInteractive.registerInteractive(userDB, scanner);

        CommandRequest commandRequest = new CommandRequest();

        commandRequest.setRequestType(RequestType.REGISTER);
        commandRequest.setObject(userDB);
        commandRequest.setParameters(null);

        return commandRequest;
    }
}
