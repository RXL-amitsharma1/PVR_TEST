databaseChangeLog = {

    changeSet(author: "prakriti", id: 'rename the enum date Range type Case locked Date') {
        sql("UPDATE RCONFIG SET DATE_RANGE_TYPE = 'CASE_LOCKED_DATE' where DATE_RANGE_TYPE = 'CaseLockedDate';")
    }

    changeSet(author: "prakriti", id: 'rename the enum  Case locked Date executed config') {
        sql("UPDATE Ex_RCONFIG SET DATE_RANGE_TYPE = 'CASE_LOCKED_DATE' where DATE_RANGE_TYPE = 'CaseLockedDate';")
    }

    changeSet(author: "prakriti", id: 'rename the enum  Submission Date executed config') {
        sql("UPDATE Ex_RCONFIG SET DATE_RANGE_TYPE = 'SUBMISSION_DATE' where DATE_RANGE_TYPE = 'SubmissionDate';")
    }

    changeSet(author: "prakriti", id: 'rename the enum  Submission Date config') {
        sql("UPDATE RCONFIG SET DATE_RANGE_TYPE = 'SUBMISSION_DATE' where DATE_RANGE_TYPE = 'SubmissionDate';")
    }

    changeSet(author: "prakriti", id: 'rename the enum  Creation Date config') {
        sql("UPDATE RCONFIG SET DATE_RANGE_TYPE = 'SUBMISSION_DATE' where DATE_RANGE_TYPE = 'SubmissionDate';")
    }
    changeSet(author: "prakriti", id: 'rename the enum  Creation Date  exe config') {
        sql("UPDATE EX_RCONFIG SET DATE_RANGE_TYPE = 'SUBMISSION_DATE' where DATE_RANGE_TYPE = 'SubmissionDate';")
    }

    changeSet(author: "prakriti", id: 'rename the enum  Safety Date config') {
        sql("UPDATE RCONFIG SET DATE_RANGE_TYPE = 'SUBMISSION_DATE' where DATE_RANGE_TYPE = 'SubmissionDate';")
    }

    changeSet(author: "prakriti", id: 'rename the enum  Safety Date exe config') {
        sql("UPDATE EX_RCONFIG SET DATE_RANGE_TYPE = 'SUBMISSION_DATE' where DATE_RANGE_TYPE = 'SubmissionDate';")
    }

}