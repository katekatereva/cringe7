package ru.itmo.commands;

import ru.itmo.Client;

import ru.itmo.commands.utills.AuthorizeCommandType;
import ru.itmo.managers.commandManager.CommandManager;
import ru.itmo.request.CommandRequest;
import ru.itmo.request.RequestType;

public class ExitCommand extends Command{


    public ExitCommand() {
        setTargetTitleForUserInput("exit");
        setDescription("exit : завершить программу");

        setAuthorizeCommandType(AuthorizeCommandType.ALL);
    }

    @Override
    public CommandRequest execute(CommandManager commandManager, String arguments) {
        CommandRequest commandRequest = new CommandRequest();
        commandRequest.setRequestType(RequestType.INTERNAL);
        Client.exit();
        return commandRequest;
    }
}
