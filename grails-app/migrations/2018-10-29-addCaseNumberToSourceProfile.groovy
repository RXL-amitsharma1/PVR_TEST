databaseChangeLog = {
    changeSet(author: "jitin (generated)", id: "1960211999018-7") {

        update(tableName: "SOURCE_PROFILE", whereClause: "SOURCE_ABBREVIATION IN ('ALL', 'ARG')"){
            column(name: "CASE_NUMBER_FIELD_NAME", value: "masterCaseNum")
        }

        update(tableName: "SOURCE_PROFILE", whereClause: "SOURCE_ABBREVIATION IN ('AFF')"){
            column(name: "CASE_NUMBER_FIELD_NAME", value: "masterCaseNumLam")
        }
    }
}