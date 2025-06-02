databaseChangeLog = {
    changeSet(author: "sachins", id: "191120201001") {
        sql("update localization set text='Issue Number' where code='quality.capa.capaNumber.label' and loc='*' ")
        sql("update localization set text='Issue Number' where code='quality.capa.capaNumber.label' and loc='ja' ")
    }
}
