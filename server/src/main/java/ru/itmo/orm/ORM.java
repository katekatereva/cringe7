package ru.itmo.orm;

import ru.itmo.orm.relatives.ManyToOne;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ORM {

    private Connection connection;

    private static final Map<Class<?>, String> typeMapping = new HashMap<>();
    static {
        typeMapping.put(Integer.class, "INT");
        typeMapping.put(String.class, "VARCHAR(512)");
        typeMapping.put(Double.class, "DOUBLE");
        typeMapping.put(Float.class, "FLOAT");
        typeMapping.put(Long.class, "BIGINT");
    }

    public ORM(String url, String username, String password) {
        try {
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createTable(Class<?> clazz) {
        String tableName = clazz.getSimpleName();
        String sequenceName = tableName + "_sequence"; // Имя последовательности, основанное на имени таблицы
        StringBuilder queryBuilder = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");

        for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            String columnName = field.getName();
            Class<?> fieldType = field.getType();
            String columnType = typeMapping.get(fieldType);

            if (columnType == null) {
                columnType = "VARCHAR(512)";
            }

            queryBuilder.append(columnName).append(" ").append(columnType).append(", ");
        }

        queryBuilder.append("PRIMARY KEY (id)");

        queryBuilder.append(")");

        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(queryBuilder.toString());

            String sequenceQuery = "CREATE SEQUENCE IF NOT EXISTS " + sequenceName;
            statement.executeUpdate(sequenceQuery);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createSequence(String sequenceName) {
        String query = "CREATE SEQUENCE " + sequenceName;

        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate(query);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public <T> void insertData(T object, String sequenceName) {
        String tableName = object.getClass().getSimpleName();
        StringBuilder queryBuilder = new StringBuilder("INSERT INTO " + tableName + " (");

        Class<?> clazz = object.getClass();
        List<String> columnNames = new ArrayList<>();
        List<Object> columnValues = new ArrayList<>();

        for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            String columnName = field.getName();
            Object columnValue;
            try {
                columnValue = field.get(object);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                continue;
            }

            if (columnName.equalsIgnoreCase("id") && columnValue == null) {
                columnNames.add(columnName);
                columnValues.add(getNextSequenceValue(sequenceName));
            } else {
                columnNames.add(columnName);
                columnValues.add(columnValue);
            }
        }

        queryBuilder.append(String.join(", ", columnNames));
        queryBuilder.append(") VALUES (");
        queryBuilder.append(getValuePlaceholders(columnValues.size()));
        queryBuilder.append(")");

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(queryBuilder.toString());
            setPreparedStatementValues(preparedStatement, columnValues);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public <T> List<T> findAll(Class<T> clazz) {
        String tableName = clazz.getSimpleName();
        String query = "SELECT * FROM " + tableName;

        List<T> results = new ArrayList<>();

        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                T object = clazz.getDeclaredConstructor().newInstance();
                for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    String columnName = field.getName();
                    Object columnValue = resultSet.getObject(columnName);
                    field.set(object, columnValue);
                }
                results.add(object);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    private Long getNextSequenceValue(String sequenceName) {
        Long nextValue = null;
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT nextval('" + sequenceName + "')");
            if (resultSet.next()) {
                nextValue = resultSet.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nextValue;
    }

    private String getValuePlaceholders(int count) {
        List<String> placeholders = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            placeholders.add("?");
        }
        return String.join(", ", placeholders);
    }

    private void setPreparedStatementValues(PreparedStatement preparedStatement, List<Object> values) throws SQLException {
        for (int i = 0; i < values.size(); i++) {
            preparedStatement.setObject(i + 1, values.get(i));
        }
    }

    public <T> void save(T object) {
        String tableName = object.getClass().getSimpleName();
        StringBuilder queryBuilder = new StringBuilder("INSERT INTO " + tableName + " (");

        Class<?> clazz = object.getClass();
        List<String> columnNames = new ArrayList<>();
        List<Object> columnValues = new ArrayList<>();

        for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            String columnName = field.getName();
            Object columnValue;
            try {
                columnValue = field.get(object);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                continue;
            }

            columnNames.add(columnName);
            columnValues.add(columnValue);
        }

        for (int i = 0; i < columnNames.size(); i++) {
            queryBuilder.append(columnNames.get(i));
            if (i < columnNames.size() - 1) {
                queryBuilder.append(", ");
            }
        }

        queryBuilder.append(") VALUES (");

        for (int i = 0; i < columnValues.size(); i++) {
            queryBuilder.append("?");
            if (i < columnValues.size() - 1) {
                queryBuilder.append(", ");
            }
        }

        queryBuilder.append(")");

        try {
            PreparedStatement preparedStatement = connection.prepareStatement(queryBuilder.toString());
            for (int i = 0; i < columnValues.size(); i++) {
                preparedStatement.setObject(i + 1, columnValues.get(i));
            }
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
