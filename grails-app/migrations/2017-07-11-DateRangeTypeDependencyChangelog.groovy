import com.rxlogix.CRUDService
import com.rxlogix.config.*
import com.rxlogix.enums.DateRangeTypeCaseEnum

databaseChangeLog = {

    changeSet(author: "prashantsahi (generated)", id: "1499690469999-888") {
        addColumn(tableName: "CASE_SERIES") {
            column(name: "DATE_RANGE_TYPE_COPY", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }

        sql("Update CASE_SERIES \n" +
                "  set DATE_RANGE_TYPE_COPY = CASE\n" +
                "   WHEN DATE_RANGE_TYPE = 'CASE_RECEIPT_DATE' THEN 1 \n" +
                "   WHEN DATE_RANGE_TYPE =  'CASE_LOCKED_DATE' THEN 2 \n" +
                " WHEN DATE_RANGE_TYPE =  'SAFTEY_RECEIPT_DATE' THEN 3 \n" +
                " WHEN DATE_RANGE_TYPE =  'CREATION_DATE' THEN 4 \n" +
                " WHEN DATE_RANGE_TYPE =  'SUBMISSION_DATE' THEN 5 \n" +
                " WHEN DATE_RANGE_TYPE =  'CASE_RECEIPT_DATE_J' THEN 6 \n" +
                " ELSE 0 \n" +
                "END;")

        dropColumn(tableName: "CASE_SERIES", columnName: "DATE_RANGE_TYPE")

        renameColumn(tableName: "CASE_SERIES", oldColumnName: "DATE_RANGE_TYPE_COPY", newColumnName: "DATE_RANGE_TYPE")
    }

    changeSet(author: "prashantsahi (generated)", id: "1499690469999-20") {
        addColumn(tableName: "EX_CASE_SERIES") {
            column(name: "DATE_RANGE_TYPE_COPY", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }

        sql("Update EX_CASE_SERIES \n" +
                "  set DATE_RANGE_TYPE_COPY = CASE\n" +
                "   WHEN DATE_RANGE_TYPE = 'CASE_RECEIPT_DATE' THEN 1 \n" +
                "   WHEN DATE_RANGE_TYPE =  'CASE_LOCKED_DATE' THEN 2 \n" +
                " WHEN DATE_RANGE_TYPE =  'SAFTEY_RECEIPT_DATE' THEN 3 \n" +
                " WHEN DATE_RANGE_TYPE =  'CREATION_DATE' THEN 4 \n" +
                " WHEN DATE_RANGE_TYPE =  'SUBMISSION_DATE' THEN 5 \n" +
                " WHEN DATE_RANGE_TYPE =  'CASE_RECEIPT_DATE_J' THEN 6 \n" +
                "  ELSE 0 \n" +
                "END;")

        dropColumn(tableName: "EX_CASE_SERIES", columnName: "DATE_RANGE_TYPE")

        renameColumn(tableName: "EX_CASE_SERIES", oldColumnName: "DATE_RANGE_TYPE_COPY", newColumnName: "DATE_RANGE_TYPE")
    }

    changeSet(author: "prashantsahi (generated)", id: "1499690469999-25") {
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "DATE_RANGE_TYPE_COPY", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }

        sql("Update EX_RCONFIG \n" +
                "  set DATE_RANGE_TYPE_COPY = CASE\n" +
                "   WHEN DATE_RANGE_TYPE = 'CASE_RECEIPT_DATE' THEN 1 \n" +
                "   WHEN DATE_RANGE_TYPE =  'CASE_LOCKED_DATE' THEN 2 \n" +
                " WHEN DATE_RANGE_TYPE =  'SAFTEY_RECEIPT_DATE' THEN 3 \n" +
                " WHEN DATE_RANGE_TYPE =  'CREATION_DATE' THEN 4 \n" +
                " WHEN DATE_RANGE_TYPE =  'SUBMISSION_DATE' THEN 5 \n" +
                " WHEN DATE_RANGE_TYPE =  'CASE_RECEIPT_DATE_J' THEN 6 \n" +
                "  ELSE 0 \n" +
                "END;")

        dropColumn(tableName: "EX_RCONFIG", columnName: "DATE_RANGE_TYPE")

        renameColumn(tableName: "EX_RCONFIG", oldColumnName: "DATE_RANGE_TYPE_COPY", newColumnName: "DATE_RANGE_TYPE")
    }

    changeSet(author: "prashantsahi (generated)", id: "1499690469999-36") {
        addColumn(tableName: "RCONFIG") {
            column(name: "DATE_RANGE_TYPE_COPY", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }

        sql("Update RCONFIG \n" +
                "  set DATE_RANGE_TYPE_COPY = CASE\n" +
                "   WHEN DATE_RANGE_TYPE = 'CASE_RECEIPT_DATE' THEN 1 \n" +
                "   WHEN DATE_RANGE_TYPE =  'CASE_LOCKED_DATE' THEN 2 \n" +
                " WHEN DATE_RANGE_TYPE =  'SAFTEY_RECEIPT_DATE' THEN 3 \n" +
                " WHEN DATE_RANGE_TYPE =  'CREATION_DATE' THEN 4 \n" +
                " WHEN DATE_RANGE_TYPE =  'SUBMISSION_DATE' THEN 5 \n" +
                " WHEN DATE_RANGE_TYPE =  'CASE_RECEIPT_DATE_J' THEN 6 \n" +
                "  ELSE 0 \n" +
                "END;")

        dropColumn(tableName: "RCONFIG", columnName: "DATE_RANGE_TYPE")

        renameColumn(tableName: "RCONFIG", oldColumnName: "DATE_RANGE_TYPE_COPY", newColumnName: "DATE_RANGE_TYPE")
    }

    changeSet(author: "prashantsahi (generated)", id: "1499690469999-37") {
        addColumn(tableName: "REPORT_REQUEST") {
            column(name: "DATE_RANGE_TYPE_COPY", type: "number(19,0)") {
                constraints(nullable: "true")
            }
        }

        sql("Update REPORT_REQUEST \n" +
                "  set DATE_RANGE_TYPE_COPY = CASE\n" +
                "   WHEN DATE_RANGE_TYPE = 'CASE_RECEIPT_DATE' THEN 1 \n" +
                "   WHEN DATE_RANGE_TYPE =  'CASE_LOCKED_DATE' THEN 2 \n" +
                " WHEN DATE_RANGE_TYPE =  'SAFTEY_RECEIPT_DATE' THEN 3 \n" +
                " WHEN DATE_RANGE_TYPE =  'CREATION_DATE' THEN 4 \n" +
                " WHEN DATE_RANGE_TYPE =  'SUBMISSION_DATE' THEN 5 \n" +
                " WHEN DATE_RANGE_TYPE =  'CASE_RECEIPT_DATE_J' THEN 6 \n" +
                "  ELSE 0 \n" +
                "END;")

        addNotNullConstraint(tableName: "REPORT_REQUEST", columnName: "DATE_RANGE_TYPE_COPY")

        dropColumn(tableName: "REPORT_REQUEST", columnName: "DATE_RANGE_TYPE")

        renameColumn(tableName: "REPORT_REQUEST", oldColumnName: "DATE_RANGE_TYPE_COPY", newColumnName: "DATE_RANGE_TYPE")
    }

    changeSet(author: "prashantsahi (generated)", id: "1499690469999-66") {
        addForeignKeyConstraint(baseColumnNames: "DATE_RANGE_TYPE", baseTableName: "EX_CASE_SERIES", constraintName: "FKFC2478DA6F5B4618", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "DATE_RANGE_TYPE", referencesUniqueColumn: "false")
    }

    changeSet(author: "prashantsahi (generated)", id: "1499690469999-67") {
        addForeignKeyConstraint(baseColumnNames: "DATE_RANGE_TYPE", baseTableName: "EX_RCONFIG", constraintName: "FKC472BE886F5B4618", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "DATE_RANGE_TYPE", referencesUniqueColumn: "false")
    }

    changeSet(author: "prashantsahi (generated)", id: "1499690469999-69") {
        addForeignKeyConstraint(baseColumnNames: "DATE_RANGE_TYPE", baseTableName: "RCONFIG", constraintName: "FK689172146F5B4618", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "DATE_RANGE_TYPE", referencesUniqueColumn: "false")
    }

    changeSet(author: "prashantsahi (generated)", id: "1499690469999-70") {
        addForeignKeyConstraint(baseColumnNames: "DATE_RANGE_TYPE", baseTableName: "REPORT_REQUEST", constraintName: "FKDB16FA646F5B4618", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "DATE_RANGE_TYPE", referencesUniqueColumn: "false")
    }

    changeSet(author: "prashantsahi (generated)", id: "1499690469999-63") {
        addForeignKeyConstraint(baseColumnNames: "DATE_RANGE_TYPE", baseTableName: "CASE_SERIES", constraintName: "FK436686666F5B4618", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "ID", referencedTableName: "DATE_RANGE_TYPE", referencesUniqueColumn: "false")
    }
}
