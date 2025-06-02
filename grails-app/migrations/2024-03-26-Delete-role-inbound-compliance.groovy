databaseChangeLog = {
    changeSet(author: "Gunjan Sharma", id: '202403261648-1') {
        sql("DELETE FROM PVUSERS_ROLES WHERE role_id IN (SELECT id FROM role WHERE authority = 'ROLE_INBOUND_COMPLIANCE');")
        sql("DELETE FROM PVUSERGROUPS_ROLES WHERE role_id IN (SELECT id FROM role WHERE authority = 'ROLE_INBOUND_COMPLIANCE');")
        sql("DELETE FROM ROLE where authority = 'ROLE_INBOUND_COMPLIANCE';")
    }

}
