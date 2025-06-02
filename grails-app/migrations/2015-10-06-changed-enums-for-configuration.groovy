
databaseChangeLog = {

    changeSet(author: "prakriti", id:'rename the enum date Range type Case') {
        sql("UPDATE RCONFIG SET DATE_RANGE_TYPE = 'CASE_RECEIPT_DATE' where DATE_RANGE_TYPE = 'CaseReceiptDate';")
    }


    changeSet(author: "prakriti", id:'rename the enum date Range type Case executed Config') {
        sql("UPDATE EX_RCONFIG SET DATE_RANGE_TYPE = 'CASE_RECEIPT_DATE' where DATE_RANGE_TYPE = 'CaseReceiptDate';")
    }


    changeSet(author: "prakriti", id:'rename the enum  for latestVersion') {
        sql("UPDATE RCONFIG SET EVALUATE_DATE_AS = 'LATEST_VERSION' where EVALUATE_DATE_AS = 'LatestVersion';")
    }

    changeSet(author: "prakriti", id:'rename the enum  for versionAs  Config') {
        sql("UPDATE RCONFIG SET EVALUATE_DATE_AS = 'VERSION_ASOF' where EVALUATE_DATE_AS = 'VersionAsOf';")
    }
    changeSet(author: "prakriti", id:'rename the enum evaluate case date for latestVersion') {
        sql("UPDATE EX_RCONFIG SET EVALUATE_DATE_AS = 'LATEST_VERSION' where EVALUATE_DATE_AS = 'LatestVersion';")
    }

    changeSet(author: "prakriti", id:'rename the enum for versionAs of executed Config') {
        sql("UPDATE EX_RCONFIG SET EVALUATE_DATE_AS = 'VERSION_ASOF' where EVALUATE_DATE_AS = 'VersionAsOf';")
    }

}
