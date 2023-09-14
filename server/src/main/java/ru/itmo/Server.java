package ru.itmo;

import ru.itmo.dao.DAO;
import ru.itmo.dao.LabWorkDbDAO;
import ru.itmo.dataManager.DataBaseManager;
import ru.itmo.dataManager.DataManager;
import ru.itmo.models.LabWork;
import ru.itmo.models.UserDB;
import ru.itmo.network.SelectorsOfServer.ServerConnect;
import ru.itmo.orm.ORM;

import java.util.*;

public class Server {
    private static final int BUFFER_SIZE = 60000;
    private static final int PORT = 6000;
    private static DAO<LabWork> dao = new LabWorkDbDAO();
    public static boolean isRunServer = true;
    private static final Scanner scanner = new Scanner(System.in);

    private static void stopServer() {
        isRunServer = false;
    }


    public static void main(String[] args) {


        ORM orm = new ORM("jdbc:postgresql://localhost:5432/LabWorks",
                "postgres", "postgres");

        orm.createTable(LabWork.class);
        orm.createTable(UserDB.class);

        DataManager<LabWork> dataManager = new DataBaseManager(orm);

        if (isRunServer) {
            ServerConnect serverConnect = new ServerConnect(PORT, BUFFER_SIZE, dataManager, dao, orm);
            serverConnect.work();
        }


    }

}