package ru.itmo.models;


import ru.itmo.utilsCommon.PasswordHash;

import java.io.Serializable;

public class UserDB implements Serializable {


    private Integer id;
    private String username;
    private String password;


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = PasswordHash.getPasswordHash(password);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }


}
