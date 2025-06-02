package com.rxlogix.util

import grails.util.Holders

import java.time.DayOfWeek
import java.time.LocalDate

class DbUtil {

    // Strings < 65k
    static String getStringType() {
        forDialect("text", "clob")

    }

    // String < 16MB
    static String getMediumStringType() {
        forDialect("mediumtext", "clob")
    }

    // String < 4GB
    static String getLongStringType() {
        forDialect("longtext", "clob")
    }


    // Binary data < 16k
    static String getBlobType() {
        return forDialect("blob","blob")
    }

    // Binary data < 16MB
    static String getMediumBlobType() {
        return forDialect("mediumblob","blob")
    }


    // Binary data < 4GB
    static String getLongBlobType() {
        return forDialect("longblob","blob")

    }


    static String getDialectBlobType() {
        return forDialect("longblob", "blob")
    }


    private static String forDialect(String mySqlType, String oracleType) {
        def dialect = Holders.config.getProperty('dataSource.dialect')
        switch (dialect) {
            case "org.hibernate.dialect.MySQL5InnoDBDialect":
                return mySqlType
                break

            case "org.hibernate.dialect.Oracle10gDialect":
                return oracleType
                break

            default:
                return mySqlType
        }

    }

}
