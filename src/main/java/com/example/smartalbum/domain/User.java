package com.example.smartalbum.domain;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Data
@ToString
public class User implements Serializable {
    private Integer id;

    private String username;

    private String password;

    private String mail;

    private Integer depositoryId;

    private Date registerDate;

    private Depository depository;

    private static final long serialVersionUID = 1L;
}