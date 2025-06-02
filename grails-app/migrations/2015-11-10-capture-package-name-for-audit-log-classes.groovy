databaseChangeLog = {

    changeSet(author: "Michael Morett", id:'capture-package-name-for-audit-log-classes') {
        sql("""UPDATE audit_log
                SET PARENT_OBJECT = CASE
                WHEN PARENT_OBJECT= 'Configuration' THEN
                'com.rxlogix.config.Configuration'

                WHEN PARENT_OBJECT= 'Query' THEN
                'com.rxlogix.config.Query'
                WHEN PARENT_OBJECT= 'QuerySet' THEN
                'com.rxlogix.config.QuerySet'
                WHEN PARENT_OBJECT= 'CustomSQLQuery' THEN
                'com.rxlogix.config.CustomSQLQuery'

                WHEN PARENT_OBJECT= 'CaseLineListingTemplate' THEN
                'com.rxlogix.config.CaseLineListingTemplate'
                WHEN PARENT_OBJECT= 'DataTabulationTemplate' THEN
                'com.rxlogix.config.DataTabulationTemplate'
                WHEN PARENT_OBJECT= 'CustomSQLTemplate' THEN
                'com.rxlogix.config.CustomSQLTemplate'
                WHEN PARENT_OBJECT= 'NonCaseTemplate' THEN
                'com.rxlogix.config.NonCaseTemplate'
                WHEN PARENT_OBJECT= 'TemplateSet' THEN
                'com.rxlogix.config.TemplateSet'

                WHEN PARENT_OBJECT= 'EtlSchedule' THEN
                'com.rxlogix.config.EtlSchedule'

                WHEN PARENT_OBJECT= 'CognosReport' THEN
                'com.rxlogix.config.CognosReport'

                WHEN PARENT_OBJECT= 'Preference' THEN
                'com.rxlogix.user.Preference'
                WHEN PARENT_OBJECT= 'User' THEN
                'com.rxlogix.user.User'

                ELSE
                PARENT_OBJECT

                END;""".stripIndent())
    }


}