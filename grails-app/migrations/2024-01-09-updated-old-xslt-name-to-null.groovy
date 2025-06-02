databaseChangeLog = {
    changeSet(author: "vivekkumar", id: "202401091750-1") {
        sql("UPDATE UNIT_CONFIGURATION SET XSLT_NAME = NULL WHERE XSLT_NAME IN ('EVHUMAN', 'EVCTM', 'CDER', 'CBER', 'OCP', 'CDRH', 'PMDA', 'PMDA_PMS', 'ICH')")
    }
}
