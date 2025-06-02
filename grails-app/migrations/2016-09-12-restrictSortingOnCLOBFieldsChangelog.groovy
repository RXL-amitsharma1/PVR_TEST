databaseChangeLog = {

    changeSet(author: "Prashant (generated)", id: "1473680925599-15") {

        sql('update RPT_FIELD_INFO set sort = null, sort_level = 0 where rpt_field_id in ( select rptField.id from rpt_field rptField left join source_column_master colMaster on rptField.source_column_master_id = colMaster.REPORT_ITEM where colMaster.column_type = \'C\')')
    }

}
