databaseChangeLog = {
    changeSet(author: "pragyatiwari", id: "202407311238-1"){
        sql("CREATE INDEX FIELD_PROFILE_RPT_FIELD_IDX ON FIELD_PROFILE_RPT_FIELD (FIELD_PROFILE_REPORT_FIELDS_ID);")
    }
}