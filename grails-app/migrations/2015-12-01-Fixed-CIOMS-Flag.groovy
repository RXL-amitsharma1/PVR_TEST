databaseChangeLog = {

    changeSet(author: "Sherry (generated)", id: "fix cioms flag") {
        sql("""UPDATE RPT_TEMPLT SET CIOMS_I_TEMPLATE = '1' WHERE name ='CIOMS I Template';""")
    }

}
