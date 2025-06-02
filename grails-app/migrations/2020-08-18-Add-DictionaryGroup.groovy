import com.rxlogix.dictionary.DictionaryGroup
import com.rxlogix.user.User
import com.rxlogix.user.UserGroup
import groovy.sql.Sql

databaseChangeLog = {

    changeSet(author: "Sachin (generated)", id: "202008182306001-1") {

        preConditions(onFail: "MARK_RAN", onFailMessage: "Table already exists") {
            not {
                tableExists(tableName: "DICTIONARY_GROUP")
            }
        }

        createTable(tableName: "DICTIONARY_GROUP") {
            column(name: "ID", type: "number(19,0)") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "DICTIONARY_GROUP_PK")
            }

            column(name: "GROUP_NAME", type: "varchar2(255 char)") {
                constraints(nullable: "false")
            }

            column(name: "GROUP_TYPE", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "TENANT_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "DESCRIPTION", type: "varchar2(4000 char)") {
                constraints(nullable: "false")
            }

            column(name: "IS_DELETED", type: "number(1,0)") {
                constraints(nullable: "false")
            }

            column(name: "PVUSER_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "VERSION", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "CREATED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

            column(name: "DATE_CREATED", type: "timestamp") {
                constraints(nullable: "true")
            }

            column(name: "LAST_UPDATED", type: "timestamp") {
                constraints(nullable: "true")
            }

            column(name: "MODIFIED_BY", type: "varchar2(255 char)") {
                constraints(nullable: "true")
            }

        }
    }

    changeSet(author: "Sachin (generated)", id: "202008182306001-2") {

        preConditions(onFail: "MARK_RAN", onFailMessage: "Table already exists") {
            not {
                tableExists(tableName: "DICT_GRP_DATA_SRC")
            }
        }

        createTable(tableName: "DICT_GRP_DATA_SRC") {
            column(name: "DICTIONARY_GROUP_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }
            column(name: "DATA_SRC_NAME", type: "varchar2(255 char)")
        }
    }

    changeSet(author: "Sachin (generated)", id: "202008182306001-3") {

        preConditions(onFail: "MARK_RAN", onFailMessage: "Table already exists") {
            not {
                tableExists(tableName: "DICT_GRP_SHARED_WITHS")
            }
        }

        createTable(tableName: "DICT_GRP_SHARED_WITHS") {
            column(name: "DICTIONARY_GROUP_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }
            column(name: "SHARED_WITH_ID", type: "number(19,0)")
        }
    }


    changeSet(author: "Sachin (generated)", id: "202008182306001-4") {

        preConditions(onFail: "MARK_RAN", onFailMessage: "Table already exists") {
            not {
                tableExists(tableName: "DICT_GRP_SHARED_WITH_GRPS")
            }
        }

        createTable(tableName: "DICT_GRP_SHARED_WITH_GRPS") {
            column(name: "DICTIONARY_GROUP_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }
            column(name: "SHARED_WITH_GROUP_ID", type: "number(19,0)")
        }
    }

    changeSet(author: "Sachin (generated)", id: "202008182306001-6") {
        dropNotNullConstraint(columnDataType: "varchar2(4000 char)", columnName: "DESCRIPTION", tableName: "DICTIONARY_GROUP")
    }

    changeSet(author: "Sachin (generated)", id: "202008182306001-7") {
        grailsChange {
            change {
                Sql sqlPVA = null
                Sql sql = null
                try {
                    if(DictionaryGroup.count()){
                        return
                    }
                    sqlPVA = new Sql(ctx.getBean("dataSource_pva"))
                    try {
                        sqlPVA.execute("select count(1) from DICT_GRP_MSTR")
                    } catch (e) {
                        println "No DICT_GRP_MSTR table exists"
                        return
                    }
                    sql = new Sql(ctx.getBean("dataSource"))
                    User seedingUser = ctx.getBean('seedDataService').getApplicationUserForSeeding()
                    sqlPVA.rows("SELECT * from DICT_GRP_MSTR").each {
                        User user = User.findByUsername(it.OWNER) ?: seedingUser
                        sql.executeInsert('INSERT INTO DICTIONARY_GROUP (ID, GROUP_NAME,GROUP_TYPE,TENANT_ID,DESCRIPTION,IS_DELETED,PVUSER_ID,VERSION,CREATED_BY,DATE_CREATED,MODIFIED_BY,LAST_UPDATED) VALUES (:ID,:GROUP_NAME,:GROUP_TYPE,:TENANT_ID,:DESCRIPTION,:IS_DELETED,:PVUSER_ID,0,:CREATED_BY,sysdate,:MODIFIED_BY,sysdate)', [ID: it.DICT_GRP_ID, GROUP_NAME: it.DICT_GRP_NAME, GROUP_TYPE: it.DICT_GRP_TYPE, TENANT_ID: it.TENANT_ID, DESCRIPTION: it.DICT_GRP_DESC, IS_DELETED: it.IS_DELETED, PVUSER_ID: user.id, CREATED_BY: it.CREATED_BY, MODIFIED_BY: it.UPDATED_BY])

                    }
                    sqlPVA.rows("SELECT * from DICT_GRP_DATA_SRC").each {
                        sql.executeInsert('INSERT INTO DICT_GRP_DATA_SRC (DICTIONARY_GROUP_ID,DATA_SRC_NAME) VALUES (?,?)', [it.DICT_GRP_ID, it.DATA_SRC_NAME])
                    }

                    sqlPVA.rows("SELECT * from DICT_GRP_SHARE_USR").each {
                        User user = User.findByUsername(it.USER_NAME)
                        if (user) {
                            sql.executeInsert('INSERT INTO DICT_GRP_SHARED_WITHS (DICTIONARY_GROUP_ID,SHARED_WITH_ID) VALUES (?,?)', [it.DICT_GRP_ID, user.id])
                        }
                    }
                    sqlPVA.rows("SELECT * from DICT_GRP_SHARE_USRGRP").each {
                        UserGroup userGroup = UserGroup.findByName(it.USER_GRP_NAME)
                        if (userGroup) {
                            sql.executeInsert('INSERT INTO DICT_GRP_SHARED_WITH_GRPS (DICTIONARY_GROUP_ID,SHARED_WITH_GROUP_ID) VALUES (?,?)', [it.DICT_GRP_ID, userGroup.id])
                        }
                    }
                }
                catch (Exception ex) {
                    println "##### Error Occurred while updating the DictionaryGroup Data ####"
                    ex.printStackTrace(System.out)
                } finally {
                    sql?.close()
                    sqlPVA?.close()
                }
            }
        }
    }

}