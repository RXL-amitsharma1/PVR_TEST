databaseChangeLog = {

    changeSet(author: "shubham", id: "1536656112623-1") {
        sql("update localization set text='Clinical Reference Number' where code='app.studyDictionary.studyCompound.label' ")
    }

    changeSet(author: "shubham", id: "1536656112623-4") {
        sql("update localization set text='At least one submittable section should be included in the report' where code='com.rxlogix.config.templateQueries.atleast.one.without.draft' and loc='*' ")
    }

    changeSet(author: "shubham", id: "1536656112623-5") {
        sql("update localization set text='Refresh Case Series' where code='app.refresh.case.btn.label' and loc='*' ")
    }
}





