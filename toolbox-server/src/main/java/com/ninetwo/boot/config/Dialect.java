package com.ninetwo.boot.config;

import org.hibernate.dialect.PostgreSQL95Dialect;

import java.sql.Types;

public class Dialect extends PostgreSQL95Dialect {

    public Dialect(){
        registerColumnType(Types.JAVA_OBJECT, "jsonb");
    }
}
