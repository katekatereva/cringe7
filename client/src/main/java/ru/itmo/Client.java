package ru.itmo;

import ru.itmo.commands.*;

import ru.itmo.commands.interactives.login.LoginInteractive;
import ru.itmo.commands.utills.AuthorizeCommandType;
import ru.itmo.managers.commandManager.CommandManager;
import ru.itmo.models.UserDB;
import ru.itmo.response.CommandResponse;
import ru.itmo.response.ResponseType;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Client {
    private static boolean isActiveClient = true;


    private static final int SERVER_PORT = 6000;
    private static final int BUFFER_SIZE = 60000;
    private static final int TIMEOUT_MS = 5000;


    public static void exit() {
        Client.isActiveClient = false;
    }

    public static void main(String[] args) {


        UserDB userDB = null;
        Scanner scanner = new Scanner(System.in);

        CommandManager commandManager = new CommandManager();


//        Command saveCommand = new SaveCommand();

        Command helpCommand = new HelpCommand();
        Command addCommand = new AddCommand(scanner);
        Command showCommand = new ShowCommand();
        Command removeByIdCommand = new RemoveByIdCommand(scanner);
        Command exitCommand = new ExitCommand();
        Command updateCommand = new UpdateCommand(scanner);
        Command clearCommand = new ClearCommand();
        Command removeHeadCommand = new RemoveHeadCommand();
        Command getMinCommand = new GetMinCommand();

        Command filterByAuthorCommand = new FilterByAuthorCommand(scanner);
        Command infoCommand = new InfoCommand();
        Command removeAnyByDifficultyCommand = new RemoveAnyByDifficultyCommand(scanner);
        Command removeGreaterCommand = new RemoveGreaterCommand(scanner);
        Command printFieldDescendingMinimalPointCommand = new PrintFieldDescendingMinimalPointCommand();

        ArrayList<String> executedFiles = new ArrayList<>();
        Command executeScriptCommand = new ExecuteScriptCommand(scanner, executedFiles, userDB);



        Command loginCommand = new LoginCommand(scanner);
        Command registerCommand = new RegisterCommand(scanner);

        //        commandManager.addCommand(saveCommand);

        commandManager.setScanner(scanner);

        commandManager.addCommand(showCommand);
        commandManager.addCommand(helpCommand);
        commandManager.addCommand(addCommand);
        commandManager.addCommand(removeByIdCommand);
        commandManager.addCommand(exitCommand);
        commandManager.addCommand(updateCommand);
        commandManager.addCommand(clearCommand);
        commandManager.addCommand(removeHeadCommand);
        commandManager.addCommand(getMinCommand);

        commandManager.addCommand(filterByAuthorCommand);
        commandManager.addCommand(infoCommand);
        commandManager.addCommand(removeAnyByDifficultyCommand);
        commandManager.addCommand(removeGreaterCommand);
        commandManager.addCommand(executeScriptCommand);
        commandManager.addCommand(printFieldDescendingMinimalPointCommand);
        commandManager.addCommand(loginCommand);
        commandManager.addCommand(registerCommand);






        try {

            Socket socket = new Socket(InetAddress.getLocalHost(), SERVER_PORT);

            socket.setSoTimeout(TIMEOUT_MS);

            commandManager.setSocket(socket);
            commandManager.setBufferSize(BUFFER_SIZE);

            while (isActiveClient) {


                if (userDB == null) {
                    for (Map.Entry<String, Command> command : commandManager.getCommands().entrySet()) {
                        if (
                                command.getValue().getIsAuthorizeCommand() == AuthorizeCommandType.NON_AUTHORIZE
                                        ||
                                        command.getValue().getIsAuthorizeCommand() == AuthorizeCommandType.ALL
                        )
                            System.out.println(command.getValue().getDescription());
                    }
                }


                System.out.print("Введите команду: ");
                String commandType = scanner.nextLine();


                if (!commandType.isBlank()) {
                    CommandResponse commandResponse = commandManager.handleCommandType(commandType, userDB);

                    if (commandResponse == null) {
                        throw new IOException();
                    } else {
                        if (commandResponse.getResponseType() == ResponseType.OK) {
                            System.out.println("Команда успешно выполнена");
                        } else if (commandResponse.getResponseType() == ResponseType.BAD_REQUEST) {
                            System.out.println("Менеджер не смог обработать запрос");
                        } else if (commandResponse.getResponseType() == ResponseType.MANAGER_ERROR) {
                            System.out.println("Во время работы менеджера произошла ошибка");
                        } else if (commandResponse.getResponseType() == ResponseType.NOT_FOUND) {
                            System.out.println("Работа не найдена");
                        } else if (commandResponse.getResponseType() == ResponseType.NOT_FOUND_COMMAND) {
                            System.out.println("Такая команда не найдена");
                        } else if (commandResponse.getResponseType() == ResponseType.NOT_AUTHORIZE) {
                            System.out.println("Вы не вошли в систему");
                        }
                        System.out.println();
                    }
                }


            }


        } catch (NoSuchElementException ignored) {
        } catch (IOException e) {
            System.out.println("Не удалось установить соединение с сервером");
        }

    }
}