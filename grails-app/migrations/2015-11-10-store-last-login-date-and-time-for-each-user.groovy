databaseChangeLog = {
    changeSet(author: "Michael Morett", id:'store-last-login-date-and-time-for-each-user') {
        sql("update audit_log set category = 'LOGIN_FAILURE' where category = 'LOGIN';")
    }
}