import com.rxlogix.config.CaseLineListingTemplate
import com.rxlogix.config.ReportField
import com.rxlogix.config.ReportFieldInfo
import com.rxlogix.config.ReportFieldInfoList

databaseChangeLog = {

    changeSet(author: "gologuzov (generated)", id: "1480021200000-1") {
        addColumn(tableName: "CLL_TEMPLT") {
            column(name: "SRV_COLS_RF_INFO_LIST_ID", type: "number(19,0)")
        }
    }

    changeSet (author: "gologuzov (generated)", id: "1480021200000-2") {
        grailsChange {
            change {
                try {
                    int count = CaseLineListingTemplate.count()
                    int max = 10
                    int offset = 0
                    while (offset < count) {
                        CaseLineListingTemplate.withNewSession { session ->
                            CaseLineListingTemplate.list([max: max, offset: offset, order: 'asc', sort: 'id']).each { template ->
                                if (template.allSelectedFieldsInfo.find {"masterCaseNum".equals(it.reportField.name)} &&
                                        !template.allSelectedFieldsInfo.find {"masterVersionNum".equals(it.reportField.name)}) {
                                    ReportFieldInfoList rfList = new ReportFieldInfoList()
                                    ReportField reportField = ReportField.findByName("masterVersionNum")
                                    Locale locale = template.owner.preference?.locale ?: new Locale('en')
                                    ReportFieldInfo reportFieldInfo = new ReportFieldInfo(
                                            reportField: reportField,
                                            argusName: "${reportField.getSourceColumn(locale)?.tableName?.tableAlias}.${reportField.getSourceColumn(locale)?.columnName}",
                                            stackId: -1,
                                            sortLevel: -1
                                    )
                                    rfList.addToReportFieldInfoList(reportFieldInfo)
                                    rfList.save(failOnError: true)
                                    template.serviceColumnList = rfList
                                }
                            }
                            offset += max
                            session.flush()
                            session.clear()
                            session.close()
                            session.connection()?.close()
                        }
                    }
                } catch (Exception ex) {
                    println "##### Error Occurred while updating old records for CaseLineListingTemplate liquibase change set 1479593643682-2 ####"
                    ex.printStackTrace(System.out)
                }
            }
        }
    }
}
