package com.rxlogix.config


import grails.testing.gorm.DomainUnitTest
import spock.lang.Specification

class CustomSQLValueSpec extends Specification implements DomainUnitTest<CustomSQLValue>{

    void "Test custom validator value, when customSQLValue contains reserved words"() {

        given: "A Custom SQL Value instance"
        CustomSQLValue customSQLValue = new CustomSQLValue(key: 'key1', isFromCopyPaste: false, value: value)

        when: "Validating the Custom SQL Value instance"
        Boolean result = customSQLValue.validate()

        then: "validation fails because SQL may not contain reserved words."
        !result
        customSQLValue.errors.getFieldError('value').code == 'com.rxlogix.config.query.customSQLQuery.invalid'

        where:
        sno | value
        1   | 'where id=20;'
        2   | 'use C_AE_IDENTIFICATION'
        3   | 'alter table table1'
        4   | 'desc case_num'
        5   | 'create tabale tablename'
        6   | 'insert into C_AE_IDENTIFICATION'
        7   | 'drop table table1'
        8   | 'delete from table1'
        9   | 'update tablename'
    }
}
