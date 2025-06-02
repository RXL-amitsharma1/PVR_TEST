databaseChangeLog = {

    changeSet(author: "sachinverma (generated)", id: "20210307213612-01") {
        //IcsrProfile should have nextRunDate/scheduleDateJSON as null
        sql("update RCONFIG set NEXT_RUN_DATE = null, SCHEDULE_DATE = null where class='com.rxlogix.config.IcsrProfileConfiguration'")
    }

}