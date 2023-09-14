package ru.itmo.dataManager;

import ru.itmo.dataManager.response.DataResponse;
import ru.itmo.models.LabWork;
import ru.itmo.orm.ORM;

import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.Collection;

public class DataBaseManager extends DataManager<LabWork> {


    private ORM orm;

    public DataBaseManager(ORM orm) {
        this.orm = orm;
    }

    @Override
    public DataResponse importData() {


        DataResponse dataResponse = new DataResponse();

        Collection<LabWork> labWorks = orm.findAll(LabWork.class);
        dataResponse.setLabWorks((ArrayDeque<LabWork>) labWorks);

        return dataResponse;
    }

    @Override
    public DataResponse exportData(Collection<LabWork> data) {
        DataResponse dataResponse = new DataResponse();
        for (LabWork labWork : data) {
            orm.save(labWork);
        }
        return dataResponse;
    }


}
