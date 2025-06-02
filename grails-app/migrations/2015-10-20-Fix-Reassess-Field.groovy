databaseChangeLog = {

    changeSet(author: "Sherry (generated)", id: "change mapping of reassess listedness") {
        grailsChange {
            change {
                sql.execute("UPDATE RPT_FIELD SET LIST_DOMAIN_CLASS = 'com.rxlogix.mapping.LmListedness' WHERE ARGUS_COLUMN_MASTER_ID = 'DCEAL_REASSESS_LISTEDNESS'")
                confirm "Successfully set default value for RPT_FIELD."
            }
        }
    }
}
