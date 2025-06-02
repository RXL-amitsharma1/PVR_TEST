class UrlMappings {
    static mappings = {
        "/$controller/$action?/$id?(.$format)?" {
            constraints {
                // apply constraints here
                id(matches: /\d+/)
            }
        }

        "/apidoc/$action?/$id?"(controller: "apiDoc", action: "getDocuments")

        "/health/$id"(controller: 'health', action: 'index', id: id)
        "/ping/$id"(controller: 'health', action: 'ping', id: id)
        "/keep-alive"(controller: 'userRest', action: 'keepAlive')
        "/policy"(controller: 'login', action: 'securityAndPrivacyPolicy')

        "/"(controller: "dashboard", action: "home")

        "403"(controller: "errors", action: "forbidden")
        "404"(controller: "errors", action: "notFound")
        "405"(controller: "errors", action: "notAllowed")
        "500"(controller: "errors", action: "serverError")


        "/login/$action?"(controller: "login")
        "/logout/$action?"(controller: "logout")

        group '/scim/v2', {
            '/Schemas'(controller: 'scimHome', action: 'schemas')
            "/Schemas/$id"(controller: 'scimHome', action: 'schemas')

            '/ResourceTypes'(controller: 'scimHome', action: 'resourceTypes') //TODO need to implement in plugin
            "/ResourceTypes/$id"(controller: 'scimHome', action: 'resourceTypes') //TODO need to implement in plugin

            '/ServiceProviderConfig'(controller: 'scimHome', action: 'serviceProviderConfig') //TODO need to implement in plugin

            '/ServiceConfiguration'(controller: 'scimHome', action: 'serviceProviderConfig') //TODO need to implement in plugin

            '/Users'(controller: 'scimUser') {
                action = [GET: 'index', POST: 'save']
            }
            "/Users/$id"(controller: 'scimUser') {
                action = [GET: 'show', DELETE: 'delete', PATCH: 'patch', PUT: 'update', POST: 'patch']
            }

            '/Groups'(controller: 'scimGroup') {
                action = [GET: 'index', POST: 'save']
            }
            "/Groups/$id"(controller: 'scimGroup') {
                action = [GET: 'show', DELETE: 'delete', PATCH: 'patch', PUT: 'update', POST: 'patch']
            }
        }

        "/console/$action?"(controller: "console")
        "/odata/**/**"(controller: "odata", action: "index")
        group "/wopi", {
            "/files/$file/contents"(controller: "wopi", action: "getFile", method: "GET")
            "/files/$file"(controller: "wopi", action: "getFileInfo", method: "GET")
            "/files/$file/contents"(controller: "wopi", action: "postFile", method: "POST")
            "/files/$file"(controller: "wopi", action: "postFile", method: "POST")
        }

        group "/api", {
            "/templates"(resources: "reportTemplateRest")
            "/configurations"(resources: "configurationRest")
            "/templates/columns"(controller: "reportTemplateRest", action: "columns", method: "GET")

            "/queries"(resources: "queryRest")

            "/reports"(resources: "reportResultRest", excludes: ["update", "create"])
            "/reportData"(resources: "reportResultDataRest", excludes: ["index", "create", "save", "edit", "update"])

            "/tags"(resources: "tagRest", excludes: ["delete", "show", "create", "save", "edit", "update"])
        }

        // JasperServer API
        group "/rest_v2", {
            "/serverInfo"(resources: "serverInfoRest", method: "GET")
            "/resources"(controller: "repositoryRest", action: "getResources", method: "GET")
            "/resources$uri**"(controller: "repositoryRest", action: "getResourceDetails", method: "GET")
            "/resources$uri**"(controller: "repositoryRest", action: "defaultPostHandler", method: "POST")
            "/resources$uri**"(controller: "repositoryRest", action: "defaultPutHandler", method: "PUT")
            "/resources$uri**"(controller: "repositoryRest", action: "deleteResource", method: "DELETE")
            "/users"(resources: "usersRest")
        }

        //Url's exposed for PVS integration.
        group "/public/api", {
            "/generateExecutedCaseSeries"(controller: "publicCaseSeries", action: "generateExecutedCaseSeries")
            "/importConfiguration"(controller: "publicReportsBuilderRest", action: "importConfiguration", method: "POST")
            "/exportAdhocReport"(controller: "publicReportsBuilderRest", action: "exportReportForSignal", method: "GET")
            "/createCaseForm"(controller: "publicReportsBuilderRest", action: "createCaseForm", method: "GET")
            "/getReportOutputStatus"(controller: "publicReportsBuilderRest", action: "getReportOutputStatus", method: "GET")
            "/getReportOutput"(controller: "publicReportsBuilderRest", action: "getReportOutput", method: "GET")
            "/adHocReport/templates/list"(controller: "publicTemplateRest", action: "getTemplatesDetailByUser", method: "GET")
            "/templates/list"(controller: "publicTemplateRest", action: "getTemplatesByUser", method: "GET")
            "/templates/cioms1Id"(controller: "publicTemplateRest", action: "getCioms1Id", method: "GET")
            "/templates/templateIdNameList"(controller: "publicTemplateRest", action: "templateIdNameList", method: "GET")
            "/templates/queryIdNameList"(controller: "publicQueryRest", action: "queryIdNameList", method: "GET")
            "/queries/list"(controller: "publicQueryRest", action: "getQueriesByUser", method: "GET")
            "/query/customSQLValuesForQuery"(controller: "publicQueryRest", action: "customSQLValuesForQuery", method: "GET")
            "/query/queryExpressionValuesForQuerySet"(controller: "publicQueryRest", action: "queryExpressionValuesForQuerySet", method: "GET")
            "/query/queryExpressionValuesForQuery"(controller: "publicQueryRest", action: "queryExpressionValuesForQuery", method: "GET")
            "/query/getParameterSize"(controller: "publicQueryRest", action: "getParameterSize", method: "GET")
            "/query/getQueriesIdsAsString"(controller: "publicQueryRest", action: "getQueriesIdsAsString", method: "GET")
            "/query/getQueryDetail"(controller: "publicQueryRest", action: "getQueryDetail", method: "GET")
            "/query/getQueryListDetail"(controller: "publicQueryRest", action: "getQueryListDetail", method: "GET")
            "/query/getNonValidQuery"(controller: "publicQueryRest", action: "getNonValidQuery", method: "GET")
            "/query/getQueryByName"(controller: "publicQueryRest", action: "getQueryByName", method: "GET")
            "/softDeleteConfiguration"(controller: "publicReportsBuilderRest", action: "softDeleteConfiguration", method: "GET")
            "/seedLegacyQueryGtts"(controller: "publicQueryRest", action: "seedLegacyQueryGtts", method: "GET")

            "/dictionaryGroup/fetchUserDetail"(controller: "publicDictionaryGroup", action: "fetchUserDetail", method: "GET")
            "/dictionaryGroup/fetchUserShareList"(controller: "publicDictionaryGroup", action: "fetchUserShareList", method: "GET")
            "/dictionaryGroup/fetchList"(controller: "publicDictionaryGroup", action: "fetchList", method: "GET")
            "/dictionaryGroup/save"(controller: "publicDictionaryGroup", action: "save", method: "POST")
            "/dictionaryGroup/delete"(controller: "publicDictionaryGroup", action: "delete", method: "POST")
            "/dictionaryGroup/groupDetails"(controller: "publicDictionaryGroup", action: "groupDetails", method: "GET")
            "/dictionaryGroup/saveBulkUpdate"(controller: "publicDictionaryGroup", action: "saveBulkUpdate", method: "POST")
            "/userRest/copyBulkUsers"(controller: "publicUserRest", action: "copyBulkUsers", method: "POST")
            "/userRest/copyBulkUserGroups"(controller: "publicUserRest", action: "copyBulkUserGroups", method: "POST")

            "/killExecution"(controller: "publicConfigurationKill", action: "killExecution", method: "POST")
            "/configuredCaseSeriesList"(controller: "publicCaseSeries", action: "fetchConfiguredCaseSeriesList")
            "/fetchExecutedCaseSeriesByConfigID"(controller: "publicCaseSeries", action: "fetchExecutedCaseSeriesByConfigID")
            "/executedCaseSeriesList"(controller: "publicCaseSeries", action: "fetchExecutedCaseSeriesList")
            "/fetchExecutedCaseSeriesByExID"(controller: "publicCaseSeries", action: "fetchExecutedCaseSeriesByExID")

            "/fileData"(controller: "publicIcsrAttachment", action: "fileDataOf")
            "/updateSharedWithCaseSeries"(controller: "publicCaseSeries", action: "updateSharedWithCaseSeries") // For PVS migration only

            "/fetchRecipientData"(controller: "publicUnitConfiguration", action: "fetchRecipientDetailList")
            "/updateReport"(controller: "publicReport", action: "updateSharedWith")
            "/userRest/sendTeamsNotification"(controller: "publicUserRest", action: "sendTeamsNotification", method: "POST")

            "/fetchBlindedUsers"(controller: "publicUserRest", action: "fetchBlindedUsers", method: "GET")
            "/fetchBlindedUserGroups"(controller: "publicUserRest", action: "fetchBlindedUserGroups", method: "GET")

            //For PVI

            "/userRest/fetchUserDetail"(controller: "publicUserRest", action: "fetchUserDetail", method: "GET")
            "/fetchRoutingConditions"(controller: "publicQueryRest", action: "fetchQueriesByTag", method: "GET")
            "/fetchResultForWorkflow"(controller: "publicQueryRest", action: "fetchResultForWorkflow", method: "POST")
            "/icsr/updateMdnAxway"(controller: "publicIcsrRest", action: "updateMdn", method: "POST")
            "/icsr/updateAckAxway"(controller: "publicIcsrRest", action: "updateAck", method: "POST")

            //ICSR APIS (Anuglar Integration with PVCM)
            //listing
            "/icsr/listIcsrCases"(controller: "publicIcsrRest", action: "listIcsrCaseTracking", method: "POST")
            "/icsr/statusList"(controller: "publicIcsrRest", action: "listStatus", method: "GET")
            "/icsr/listProfiles"(controller: "publicIcsrRest", action: "listProfilesFilter", method: "GET")
            "/icsr/listAuthorizationTypeFilter"(controller: "publicIcsrRest", action: "listAuthorizationFilter", method: "GET")
            //Manual Schedule
            "/icsr/manual/case"(controller: "publicIcsrRest", action: "listCase", method: "GET")
            "/icsr/profiles"(controller: "publicIcsrRest", action: "profileList", method: "GET")
            "/icsr/profile/templates"(controller: "publicIcsrRest", action: "templateQueriesFor", method: "GET")
            "/icsr/checkAvailableDevice"(controller: "publicIcsrRest", action: "checkAvailableDevice", method: "GET")
            "/icsr/listDevices"(controller: "publicIcsrRest", action: "listDevices", method: "GET")
            "/icsr/listAuthorizationType"(controller: "publicIcsrRest", action: "listAuthorizationType", method: "GET")
            "/icsr/listApprovalNumber"(controller: "publicIcsrRest", action: "listApprovalNumber", method: "GET")
            "/icsr/case/reEvaluate"(controller: "publicIcsrRest", action: "reProcess", method: "POST")
            "/icsr/scheduleCase"(controller: "publicIcsrRest", action: "manualScheduleCase", method: "POST")
            //Transmit
            "/icsr/checkPreviousVersionTransmitted"(controller: "publicIcsrRest", action: "checkPreviousVersionTransmittedIcsr", method: "POST")
            "/icsr/loadTransmitModal"(controller: "publicIcsrRest", action: "loadTransmitModal", method: "GET")
            "/icsr/transmitCase"(controller: "publicIcsrRest", action: "transmitCaseIcsr", method: "POST")
            //Submit
            "/icsr/loadSubmissionForm"(controller: "publicIcsrRest", action: "loadIcsrSubmissionForm", method: "POST")
            "/icsr/submitCase"(controller: "publicIcsrRest", action: "submitCaseIcsr", method: "POST")
            //Nullification
            "/icsr/nullifyReport"(controller: "publicIcsrRest", action: "nullifyReportIcsr", method: "POST")
            //Delete Case
            "/icsr/deleteCase"(controller: "publicIcsrRest", action: "deleteCaseIcsr", method: "POST")
            //Generate Case & Local CP
            "/icsr/saveLocalCp"(controller: "publicIcsrRest", action: "saveLocalCpIcsr", method: "POST")
            "/icsr/generateReport"(controller: "publicIcsrRest", action: "saveLocalCpIcsr", method: "POST")
            //History Modals
            "/icsr/caseHistoryDetails"(controller: "publicIcsrRest", action: "listCaseHistoryDetails", method: "GET")
            "/icsr/caseSubmissionHistoryDetails"(controller: "publicIcsrRest", action: "listCaseSubmissionHistoryDetails", method: "GET")
            "/icsr/downloadAckFile"(controller: "publicIcsrRest", action: "downloadAckFile", method: "GET")
            "/icsr/downloadAttachFile"(controller: "publicIcsrRest", action: "downloadStatusFile", method: "GET")
            //Emails
            "/icsr/listEmail"(controller: "publicIcsrRest", action: "allEmails", method: "GET")
            "/icsr/addEmailAll"(controller: "publicIcsrRest", action: "addAllEmail", method: "POST")
            "/icsr/sendEmailForIcsr"(controller: "publicIcsrRest", action: "emailIcsr", method: "POST")
            //Bulk Transmit
            //Download ICSR
            "/icsr/downloadICSR"(controller: "publicIcsrRest", action: "downloadICSR", method: "GET")
            //Regenerate
            //Bulk Regenerate
            "/icsr/regenerateCase"(controller: "publicIcsrRest", action: "regenerateCaseIcsr", method: "POST")
            //Icsr Case Tracking Status Modal
            "/icsr/loadStatusForm"(controller: "publicIcsrRest", action: "loadIcsrStatusForm", method: "POST")
            "/icsr/updateCaseStatus"(controller: "publicIcsrRest", action: "updateIcsrCaseStatus", method: "POST")
            //config
            "/icsr/config"(controller: "publicIcsrRest", action: "listConfig", method: "GET")
            //User Preference
            "/icsr/getIcsrPreference"(controller: "publicIcsrRest", action: "getUserPreferences", method: "POST")
            "/icsr/updateIcsrPreference"(controller: "publicIcsrRest", action: "updateUserPreferences", method: "POST")
            "/icsr/listJustification"(controller: "publicIcsrRest", action: "listStandardJustification", method: "GET")
            //Preview
            "/icsr/previewCaseData"(controller: "publicIcsrRest", action: "previewCaseDataScheduled", method: "POST")


            //For new admin argus environment user
            "/user/fetchUser"(controller: "publicUserRest", action: "fetchUser", method: "GET")
            "/reloadFieldDefinition"(controller: "publicUserRest", action: "getUserDefinedFields", method: "POST")
            "/rollbackFieldDefinition"(controller: "publicUserRest", action: "getRollBackFields", method: "POST")
            "/updatePrivacyFieldProfile"(controller: "publicUserRest", action: "updatePrivacyFieldProfile", method: "POST")


        }
    }
}
