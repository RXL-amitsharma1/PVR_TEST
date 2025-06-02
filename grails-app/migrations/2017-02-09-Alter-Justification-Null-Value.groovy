databaseChangeLog = {

    changeSet(author: "emilmatevosyan", id: "1486464286483-2") {
        grailsChange {
            change {
                sql.execute("ALTER TABLE WORKFLOW_JUSTIFICATION MODIFY (DESCRIPTION NULL)")
                confirm "Successfully updated value for WORKFLOW_JUSTIFICATION."
            }
        }
    }
}