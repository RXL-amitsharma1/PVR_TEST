import com.rxlogix.config.ExecutedReportConfiguration
import com.rxlogix.config.ReportConfiguration
import grails.converters.JSON
import org.grails.web.json.JSONElement

databaseChangeLog = {

    changeSet(author: "Sachin (generated)", id: "1473781025599-4") {
        sql("update PREFERENCE set TIME_ZONE = 'Asia/Kolkata' where TIME_ZONE = 'Asia/Calcutta' ")
        sql("update PREFERENCE set TIME_ZONE = 'Asia/Kathmandu' where TIME_ZONE = 'Asia/Katmandu'")
    }

    changeSet(author: "Sachin (generated)", id: "1473781025599-3") {
        grailsChange {
            change {
                try {
                    int count = ReportConfiguration.createCriteria().get{
                        projections{
                            rowCount()
                        }
                        isNotNull("scheduleDateJSON")
                        'in'('configSelectedTimeZone', ['Asia/Calcutta', 'Asia/Katmandu'])
                    }
                    int max = 10
                    int offset = 0
                    while (offset < count) {
                        ReportConfiguration.withNewSession { session ->
                            List<ReportConfiguration> reportConfigurations = ReportConfiguration.createCriteria().list {
                                isNotNull("scheduleDateJSON")
                                'in'('configSelectedTimeZone', ['Asia/Calcutta', 'Asia/Katmandu'])
                                order('id', 'asc')
                                firstResult(offset)
                                maxResults(max)
                            }
                            reportConfigurations.each { ReportConfiguration configuration ->
                                JSONElement timeObject = JSON.parse(configuration.scheduleDateJSON)
                                if (configuration.configSelectedTimeZone == 'Asia/Calcutta') {
                                    if (timeObject.timeZone.name == "Asia/Calcutta") {
                                        timeObject.timeZone.name = "Asia/Kolkata"
                                    }
                                    sql.executeUpdate("update RCONFIG set SCHEDULE_DATE = ?, SELECTED_TIME_ZONE = ? where id = ?", [timeObject.toString(), "Asia/Kolkata", configuration.id])

                                } else if (configuration.configSelectedTimeZone == 'Asia/Katmandu') {
                                    if (timeObject.timeZone.name == "Asia/Katmandu") {
                                        timeObject.timeZone.name = "Asia/Kathmandu"
                                    }
                                    sql.executeUpdate("update RCONFIG set SCHEDULE_DATE = ?, SELECTED_TIME_ZONE = ? where id = ?", [timeObject.toString(), "Asia/Kathmandu", configuration.id])
                                }
                            }
                            offset += max
                            session.flush()
                            session.clear()
                            session.close()
                            session.connection()?.close()
                        }
                    }

                    count = ExecutedReportConfiguration.createCriteria().get{
                        projections{
                            rowCount()
                        }
                        isNotNull("scheduleDateJSON")
                        'in'('configSelectedTimeZone', ['Asia/Calcutta', 'Asia/Katmandu'])
                    }
                    max = 10
                    offset = 0

                    while (offset < count) {
                        ExecutedReportConfiguration.withNewSession { session ->
                            List<ExecutedReportConfiguration> executedReportConfigurations = ExecutedReportConfiguration.createCriteria().list {
                                isNotNull("scheduleDateJSON")
                                'in'('configSelectedTimeZone', ['Asia/Calcutta', 'Asia/Katmandu'])
                                order('id', 'asc')
                                firstResult(offset)
                                maxResults(max)
                            }
                            executedReportConfigurations.each { ExecutedReportConfiguration configuration ->
                                JSONElement timeObject = JSON.parse(configuration.scheduleDateJSON)
                                if (configuration.configSelectedTimeZone == 'Asia/Calcutta') {
                                    if (timeObject.timeZone.name == "Asia/Calcutta") {
                                        timeObject.timeZone.name = "Asia/Kolkata"
                                    }
                                    sql.executeUpdate("update EX_RCONFIG set SCHEDULE_DATE = ?, SELECTED_TIME_ZONE = ? where id = ?", [timeObject.toString(), "Asia/Kolkata", configuration.id])

                                } else if (configuration.configSelectedTimeZone == 'Asia/Katmandu') {
                                    if (timeObject.timeZone.name == "Asia/Katmandu") {
                                        timeObject.timeZone.name = "Asia/Kathmandu"
                                    }
                                    sql.executeUpdate("update EX_RCONFIG set SCHEDULE_DATE = ?, SELECTED_TIME_ZONE = ? where id = ?", [timeObject.toString(), "Asia/Kathmandu", configuration.id])
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
                    println "##### Error Occurred while updating old records for Configurations liquibase change-set 1473781025599-2 ####"
                    ex.printStackTrace(System.out)
                }
            }
        }
    }

}
