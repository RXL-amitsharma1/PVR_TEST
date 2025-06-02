databaseChangeLog = {

    changeSet(author: "shikhars", id: "202108161238-1") {
        sql("update localization set text='Assigned To' where code='app.pvc.assignedTo.filter.label' ")
    }
}