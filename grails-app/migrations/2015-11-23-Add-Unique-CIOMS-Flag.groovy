databaseChangeLog = {

	changeSet(author: "Sherry (generated)", id: "1448325156559-1") {
		addColumn(tableName: "RPT_TEMPLT") {
			column(name: "CIOMS_I_TEMPLATE", type: "number(1,0)")
		}
        grailsChange {
            change {
                sql.execute("UPDATE RPT_TEMPLT SET CIOMS_I_TEMPLATE = '0'")
                confirm "Successfully set default value for RPT_TEMPLT."
            }
        }
        addNotNullConstraint(tableName: "RPT_TEMPLT", columnName: "CIOMS_I_TEMPLATE", columnDataType: "number(1,0)")
	}

    changeSet(author: "Sherry (generated)", id: "1448325156559-2") {
        preConditions(onFail:'MARK_RAN',onError:'MARK_RAN', onFailMessage:'should have only one CIOMS I Template.',onErrorMessage:'should have only one CIOMS I Template.'){
            sqlCheck(expectedResult:'=1', 'SELECT COUNT(*) FROM RPT_TEMPLT WHERE NAME =  \'CIOMS I Template\';')
        }
        sql("""UPDATE RPT_TEMPLT SET CIOMS_I_TEMPLATE = '1' WHERE name ='CIOMS I Template';""")
    }

}
