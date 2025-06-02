databaseChangeLog = {
    
    changeSet(author: "Nitin Nepalia", id: "202110121228-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'DUE_DATE_ADJUSTMENT_OPTIONS')
            }
        }
        
        addColumn(tableName: "RCONFIG") {
            column(name: "DUE_DATE_ADJUSTMENT_OPTIONS", type: "varchar2(255 char)")
        }
        
    }
    changeSet(author: "Nitin Nepalia", id: "202110121228-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'DUE_DATE_ADJUSTMENT')
            }
        }
        
        addColumn(tableName: "RCONFIG") {
            column(name: "DUE_DATE_ADJUSTMENT", type: "varchar2(255 char)")
        }
        
    }
    changeSet(author: "Nitin Nepalia", id: "202110121228-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'RCONFIG', columnName: 'ADJUST_DUE_DATE')
            }
        }
        
        addColumn(tableName: "RCONFIG") {
            column(name: "ADJUST_DUE_DATE", type: "varchar2(255 char)")
        }
        
    }
    
    changeSet(author: "Nitin Nepalia", id: "202110191451-1") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'ADJUST_DUE_DATE')
            }
        }
        
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "ADJUST_DUE_DATE", type: "varchar2(255 char)")
        }
        
    }
    changeSet(author: "Nitin Nepalia", id: "202110191550-2") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'DUE_DATE_ADJUSTMENT_OPTIONS')
            }
        }
        
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "DUE_DATE_ADJUSTMENT_OPTIONS", type: "varchar2(255 char)")
        }
        
    }
    changeSet(author: "Nitin Nepalia", id: "202110191551-3") {
        preConditions(onFail: 'MARK_RAN') {
            not {
                columnExists(tableName: 'EX_RCONFIG', columnName: 'DUE_DATE_ADJUSTMENT')
            }
        }
        
        addColumn(tableName: "EX_RCONFIG") {
            column(name: "DUE_DATE_ADJUSTMENT", type: "varchar2(255 char)")
        }
        
    }
}
