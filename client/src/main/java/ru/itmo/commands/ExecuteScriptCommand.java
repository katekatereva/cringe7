package ru.itmo.commands;


import ru.itmo.commands.utills.CommandSplitService;
import ru.itmo.managers.commandManager.CommandManager;
import ru.itmo.models.UserDB;
import ru.itmo.request.CommandRequest;
import ru.itmo.request.RequestType;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;

public class ExecuteScriptCommand extends Command{

    private final Scanner scanner;
    private ArrayList<String> executedFiles;

    private UserDB userDB;
    public ExecuteScriptCommand(Scanner scanner, ArrayList<String> executedFiles, UserDB userDB) {
        this.scanner = scanner;
        this.executedFiles = executedFiles;
        this.userDB = userDB;
        setTargetTitleForUserInput("execute_script");
        setDescription("execute_script file_name : считать и исполнить скрипт из указанного файла. В скрипте содержатся команды в таком же виде, в котором их вводит пользователь в интерактивном режиме.");
    }


    @Override
    public CommandRequest execute(CommandManager commandManager, String arguments) {

        CommandRequest commandRequest = new CommandRequest();
        String pathToFileWithScript = "";
        if (arguments == null) {
            while (pathToFileWithScript.isBlank()) {
                System.out.print("Введите путь до файла со скриптом: ");
                pathToFileWithScript = scanner.nextLine();
            }
        } else {
            pathToFileWithScript = arguments;
        }


        Path file = Paths.get(pathToFileWithScript);


        if (Files.exists(file) && Files.isReadable(file)) {
            executedFiles.add(pathToFileWithScript);
            try (Scanner scannerForFile = new Scanner(file.toFile())) {
                while (scannerForFile.hasNext()) {
                    String command = scannerForFile.nextLine();

                    String[] commandSplit = CommandSplitService.splitCommandType(command);

                    if (Objects.equals(commandSplit[0], getTargetTitleForUserInput())) {
                        if (executedFiles.contains(commandSplit[1])) {
                            System.out.printf("Файл %s уже был исполнен", commandSplit[1]);
                            System.out.println();
                        }
                        else {
                            commandManager.handleCommandType(command, userDB);
                            executedFiles.add(commandSplit[1]);
                        }
                    }
                    else {
                        commandManager.handleCommandType(command, userDB);
                    }

                }
            } catch (FileNotFoundException e) {
                System.out.println("Во время чтения файла со скриптами произошла ошибка");
            }
        }

        else {
            System.out.println("Программа не смогла получить доступ к файлу для чтения");
        }

        commandRequest.setRequestType(RequestType.INTERNAL);
        executedFiles.clear();
        return commandRequest;

    }
}
