import com.rxlogix.config.ExecutedConfiguration
import com.rxlogix.config.Configuration
import grails.converters.JSON
import org.grails.web.json.JSONElement

databaseChangeLog = {

    changeSet(author: "Sachin (generated)", id: "1473781025598-4") {
        sql("update PREFERENCE set TIME_ZONE = 'Asia/Kolkata' where TIME_ZONE = 'Asia/Calcutta' ")
        sql("update PREFERENCE set TIME_ZONE = 'Asia/Kathmandu' where TIME_ZONE = 'Asia/Katmandu'")
    }

    changeSet(author: "Sachin (generated)", id: "1473781025598-3") {
        grailsChange {
            change {
                try {
                    int count = Configuration.createCriteria().get {
                        projections {
                            rowCount()
                        }
                        isNotNull("scheduleDateJSON")
                        'in'('configSelectedTimeZone', ['Asia/Calcutta', 'Asia/Katmandu'])
                    }
                    int max = 10
                    int offset = 0
                    while (offset < count) {
                        Configuration.withNewSession { session ->
                            List<Configuration> configurations = Configuration.createCriteria().list {
                                isNotNull("scheduleDateJSON")
                                'in'('configSelectedTimeZone', ['Asia/Calcutta', 'Asia/Katmandu'])
                                order('id', 'asc')
                                firstResult(offset)
                                maxResults(max)
                            }
                            configurations.each { Configuration configuration ->
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

                    count = ExecutedConfiguration.createCriteria().get{
                        projections{
                            rowCount()
                        }
                        isNotNull("scheduleDateJSON")
                        'in'('configSelectedTimeZone', ['Asia/Calcutta', 'Asia/Katmandu'])
                    }
                    max = 10
                    offset = 0

                    while (offset < count) {
                        ExecutedConfiguration.withNewSession { session ->
                            List<ExecutedConfiguration> executedConfigurations = ExecutedConfiguration.createCriteria().list {
                                isNotNull("scheduleDateJSON")
                                'in'('configSelectedTimeZone', ['Asia/Calcutta', 'Asia/Katmandu'])
                                order('id', 'asc')
                                firstResult(offset)
                                maxResults(max)
                            }
                            executedConfigurations.each { ExecutedConfiguration configuration ->
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
