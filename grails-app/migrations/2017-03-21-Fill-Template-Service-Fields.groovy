import com.rxlogix.TemplateService
import com.rxlogix.config.CaseLineListingTemplate

databaseChangeLog = {
    changeSet(author: "gologuzov", id: "1490091867667-1") {
        grailsChange {
            change {
                try {
                    TemplateService templateService = ctx.getBean("templateService")
                    int count = CaseLineListingTemplate.count()
                    int max = 10
                    int offset = 0
                    while (offset < count) {
                        CaseLineListingTemplate.withNewSession { session ->
                            CaseLineListingTemplate.list([max: max, offset: offset, order: 'asc', sort: 'id']).each { template ->
                                templateService.fillCLLTemplateServiceFields(template)
                            }
                            offset += max
                            session.flush()
                            session.clear()
                            session.close()
                            session.connection()?.close()
                        }
                    }
                } catch (Exception ex) {
                    println "##### Error Occurred while updating old records for CaseLineListingTemplate liquibase change set 1490091867667-1 ####"
                    ex.printStackTrace(System.out)
                }
            }
        }
    }
}