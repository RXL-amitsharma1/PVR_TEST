databaseChangeLog = {
    changeSet(author: "Pragyat", id: "123120221805-1") {
        modifyDataType(columnName: "DESCRIPTION", tableName: "ROLE", newDataType: "VARCHAR2(2000 char)")
        sql("update role set description='Users/Groups with Administrator role can perform all operations except User/Group Management, Quality Check, Share with Users/Groups and other system administrative Operations like Application/Jobs Monitoring and custom field management' where authority='ROLE_ADMIN'")
        sql("update role set description='Users/Groups with Super administrator role can perform system administrator operations like Application/Jobs Monitoring, Custom Field Management, Extract JSONs and other business operations except Quality Check and  Share with Users/Groups operations' where authority='ROLE_DEV'")
        sql("update role set description='Users/Groups with this role have access to Settings menu to general business configurations like Workflow State, Rules, Dashboards and Email configuration etc. Users/Groups with only System configuration role restricted to perform System Administrative and User/Group Management operations' where authority='ROLE_SYSTEM_CONFIGURATION'")
    }
}