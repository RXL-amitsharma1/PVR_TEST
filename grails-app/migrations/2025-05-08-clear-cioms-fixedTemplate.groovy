databaseChangeLog = {

    changeSet(author: "Siddharth", id: "20250508160027-07") {
        sql("UPDATE RPT_TEMPLT SET FIXED_TEMPLT_ID = NULL WHERE NAME = 'CIOMS I Template' AND CIOMS_I_TEMPLATE = 1 AND IS_DELETED =0;")
    }

    changeSet(author: "Siddharth", id: "20250509143530-07") {
        sql("UPDATE RPT_TEMPLT SET FIXED_TEMPLT_ID = NULL WHERE NAME = 'Medwatch Template' AND MEDWATCH_TEMPLATE = 1 AND IS_DELETED =0;")
    }
}