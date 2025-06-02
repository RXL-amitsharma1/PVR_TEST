import groovy.sql.Sql

databaseChangeLog = {

    changeSet(author: "RishabhJ", id: "202111291852") {
        modifyDataType(columnName: "VALUE", tableName: "PUBLISHER_TPL_PRM", newDataType: "VARCHAR2(32000 BYTE)")
    }

    changeSet(author: "Sergey", id: "202112271852") {
        modifyDataType(columnName: "NAME", tableName: "PUBLISHER_CFG_SECT", newDataType: "VARCHAR2(400)")
    }
    changeSet(author: "sergey (generated)", id: "202112271852-4") {
        preConditions(onFail: "MARK_RAN") {
           tableExists(tableName: "PUBLISHER_CFG_SECT_PARAM_VAL")
        }
        addColumn(tableName: "PUBLISHER_CFG_SECT") {
            column(name: "parameter_values_json", type: "clob")
        }
        grailsChange {
            change {
                Sql sql = null
                try {
                    sql = new Sql(grails.util.Holders.applicationContext.getBean("dataSource"))
                    Map data=[:]

                    sql.rows("select * from PUBLISHER_CFG_SECT_PARAM_VAL")?.each{
                        Map current = data.get(it['PUBLISHER_CFG_SECT_ID'])
                        if(!current){
                            current = [:]
                            data.put(it['PUBLISHER_CFG_SECT_ID'], current)
                        }
                        Object val = it['PARAMETER_VALUES_ELT']

                        current.put(it['PARAMETER_VALUES_IDX'],val?val.getSubString(1, (int) val.length()):null)
                    }
                    data?.each{ k,v->
                        sql.execute("update PUBLISHER_CFG_SECT set parameter_values_json='${(v as grails.converters.JSON).toString().replaceAll("'","''")}' where id=${k}")
                    }
                    sql.execute("drop table PUBLISHER_CFG_SECT_PARAM_VAL cascade constraints")
                }
                catch (Exception ex) {
                    println "##### Error Occurred while updating the publisher parameter values ####"
                    ex.printStackTrace(System.out)
                } finally {
                    sql?.close()
                }
            }
        }
    }
}