databaseChangeLog = {

	changeSet(author: "Sherry (generated)", id: "1447970844603-1") {
		addColumn(tableName: "RPT_TEMPLT") {
			column(name: "EDITABLE", type: "number(1,0)")
		}
        grailsChange {
            change {
                sql.execute("UPDATE RPT_TEMPLT SET EDITABLE = '1'")
                confirm "Successfully set default value for RPT_TEMPLT."
            }
        }
        addNotNullConstraint(tableName: "RPT_TEMPLT", columnName: "EDITABLE", columnDataType: "number(1,0)")
	}

	changeSet(author: "Sherry (generated)", id: "1447970844603-2") {
        preConditions(onFail:'MARK_RAN',onError:'MARK_RAN', onFailMessage:'table is empty.',onErrorMessage:'table is empty.'){
            sqlCheck(expectedResult:'>0', 'SELECT COUNT(*) FROM RPT_TEMPLT;')
        }
        sql("""INSERT INTO RPT_TEMPLT(ID, VERSION, CATEGORY_ID, CREATED_BY, DATE_CREATED, DESCRIPTION, FACTORY_DEFAULT, EDITABLE, HASBLANKS, IS_DELETED, IS_PUBLIC, LAST_UPDATED, MODIFIED_BY, NAME, ORIG_TEMPLT_ID, PV_USER_ID, REASSESS_LISTEDNESS, TEMPLATE_TYPE)
                VALUES (HIBERNATE_SEQUENCE.nextval, 0, NULL, 'APPLICATION', CURRENT_TIMESTAMP, 'Cannot be modified.', 0, 0, 0, 0, 1, CURRENT_TIMESTAMP, 'Application', 'CIOMS I Template', 0, (select id from PVUSER where rownum = 1), NULL, 'CUSTOM_SQL');""")
	}

    changeSet(author: "Sherry (generated)", id: "1447970844603-3") {
        preConditions(onFail:'MARK_RAN',onError:'MARK_RAN', onFailMessage:'table is empty.',onErrorMessage:'table is empty.'){
            sqlCheck(expectedResult:'>0', 'SELECT COUNT(*) FROM SQL_TEMPLT;')
        }
        sql("""INSERT INTO SQL_TEMPLT(ID, COLUMN_NAMES, SELECT_FROM_STMT, WHERE_STMT)
                VALUES ((select id from RPT_TEMPLT where name ='CIOMS I Template'), '[CASE_ID, VERSION_NUM]', 'select case_id, version_num from case_master cm', NULL);""")
    }

    changeSet(author: "Chetan (generated)", id: "1456361883134-1") {
        addColumn(tableName: "RPT_TEMPLT") {
            column(name: "TEMPLATE_FOOTER", type: "varchar2(255 char)")
        }
    }
}
