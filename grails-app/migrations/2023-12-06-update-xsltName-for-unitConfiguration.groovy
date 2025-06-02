databaseChangeLog = {

    changeSet(author: "meenal (generated)", id: "202312061152-1") {
        sql("UPDATE UNIT_CONFIGURATION set XSLT_NAME = 'FDAv2_1' where XSLT_NAME = 'FDA';")
        sql("UPDATE UNIT_CONFIGURATION set XSLT_NAME = 'FDAv2_1' where XSLT_NAME = 'FDA_2.1';")
        sql("UPDATE UNIT_CONFIGURATION set XSLT_NAME = 'FDAv2_2' where XSLT_NAME = 'FDA_2.2';")
    }

    changeSet(author: "meenal (generated)", id: "202312061152-2") {
        sql("UPDATE  EX_RCONFIG set XSLT_NAME = 'FDAv2_1' where XSLT_NAME = 'FDA';")
        sql("UPDATE EX_RCONFIG set XSLT_NAME = 'FDAv2_1' where XSLT_NAME = 'FDA_2.1';")
        sql("UPDATE EX_RCONFIG set XSLT_NAME = 'FDAv2_2' where XSLT_NAME = 'FDA_2.2';")
    }
}
