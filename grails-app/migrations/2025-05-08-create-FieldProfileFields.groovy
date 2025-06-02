import grails.util.Holders

databaseChangeLog = {
    changeSet(author: "rxl-shivamg1", id: "202505081627-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                tableExists(tableName: 'FIELD_PROFILE_FIELDS')
            }
        }
        createTable(tableName: "FIELD_PROFILE_FIELDS") {
            column(name: "FIELD_PROFILE_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "REPORT_FIELD_ID", type: "number(19,0)") {
                constraints(nullable: "false")
            }

            column(name: "IS_BLINDED", type: "number(1)", defaultValue: 0) {
                constraints(nullable: "false")
            }

            column(name: "IS_PROTECTED", type: "number(1)", defaultValue: 0) {
                constraints(nullable: "false")
            }

            column(name: "IS_HIDDEN", type: "number(1)", defaultValue: 0) {
                constraints(nullable: "false")
            }
        }

        addPrimaryKey(columnNames: "FIELD_PROFILE_ID,REPORT_FIELD_ID", constraintName: "FIELD_PROFILE_FIELD_PK", tableName: "FIELD_PROFILE_FIELDS")
    }

    changeSet(author: "rxl-shivamg1", id: "202505151122-3") {
        preConditions(onFail: 'MARK_RAN') {
            sqlCheck(expectedResult: '0', "SELECT COUNT(1) FROM field_profile_fields;")
        }

        grailsChange {
            change {
                sql.execute("""
                BEGIN
                  FOR rec IN (
                    SELECT DISTINCT field_profile_id FROM field_profile_rpt_field
                  ) LOOP
                    -- Insert protected fields
                    INSERT INTO field_profile_fields (
                      field_profile_id, report_field_id, is_blinded, is_protected, is_hidden
                    )
                    SELECT
                      field_profile_id,
                      protected_field_id,
                      0, 1, 0
                    FROM field_profile_protected_field
                    WHERE field_profile_id = rec.field_profile_id;

                    -- Insert blinded fields (excluding ones already inserted as protected)
                    INSERT INTO field_profile_fields (
                      field_profile_id, report_field_id, is_blinded, is_protected, is_hidden
                    )
                    SELECT
                      field_profile_id,
                      blinded_field_id,
                      1, 0, 0
                    FROM field_profile_blinded_field
                    WHERE field_profile_id = rec.field_profile_id
                      AND blinded_field_id NOT IN (
                        SELECT protected_field_id
                        FROM field_profile_protected_field
                        WHERE field_profile_id = rec.field_profile_id
                      );

                    -- Insert hidden fields (those not present in FIELD_PROFILE_RPT_FIELD)
                    INSERT INTO field_profile_fields (
                      field_profile_id, report_field_id, is_blinded, is_protected, is_hidden
                    )
                    SELECT
                      rec.field_profile_id,
                      rf.id,
                      0, 0, 1
                    FROM rpt_field rf
                    WHERE rf.id NOT IN (
                      SELECT report_field_id
                      FROM field_profile_rpt_field
                      WHERE field_profile_id = rec.field_profile_id
                    );

                    COMMIT;
                  END LOOP;
                END;
            """)
            }
        }
    }

    changeSet(author: "rxl-shivamg1", id: "202505151122-5") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "FIELD_PROFILE_RPT_FIELD_BKP")
        }

        renameTable(oldTableName: "FIELD_PROFILE_RPT_FIELD_BKP", newTableName: "FLD_PROF_RPTFLD_BKPINIT")
    }

    changeSet(author: "rxl-shivamg1", id: "202505151122-6") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "FIELD_PROFILE_RPT_FIELD")
        }

        renameTable(oldTableName: "FIELD_PROFILE_RPT_FIELD", newTableName: "FP_RPT_FIELD_BKP_FINAL")
    }

    changeSet(author: "rxl-shivamg1", id: "202505151122-7") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "FIELD_PROFILE_BLINDED_FIELD")
        }

        renameTable(oldTableName: "FIELD_PROFILE_BLINDED_FIELD", newTableName: "FP_BLIND_FIELD_BKP_FINAL")
    }

    changeSet(author: "rxl-shivamg1", id: "202505151122-8") {
        preConditions(onFail: "MARK_RAN") {
            tableExists(tableName: "FIELD_PROFILE_PROTECTED_FIELD")
        }
        
        renameTable(oldTableName: "FIELD_PROFILE_PROTECTED_FIELD", newTableName: "FP_PROTECT_FIELD_BKP_FINAL")
    }
}
