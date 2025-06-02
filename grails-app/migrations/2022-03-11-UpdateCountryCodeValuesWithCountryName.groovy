import com.rxlogix.config.UnitConfiguration
import groovy.sql.Sql;
databaseChangeLog = {
    changeSet(author: "Shubham Sharma (generated)", id: "202203110509-1") {
        grailsChange {
            change {
                Sql sql = null
                try {
                    sql = new Sql(ctx.getBean('utilService').getReportConnection())
                    if (UnitConfiguration.count()) {
                        UnitConfiguration.list().each {
                            String query = """Select c.country from VW_LCO_A3 a3, VW_LCO_COUNTRY c where a3.country_id=c.country_id and a3=upper('${
                                it.organizationCountry
                            }')""";
                            def countryName = sql.firstRow(query)
                            if (countryName) {
                                it.organizationCountry = countryName['COUNTRY']
                                it.save()
                            }
                        }

                        UnitConfiguration.withSession {
                            it.flush()
                        }
                    }
                } catch (Exception ex) {
                    println "##### Error Occurred while updating County Code, liquibase change-set 202203110509-1 ####"
                    ex.printStackTrace(System.out)
                } finally {
                    sql?.close()
                }
            }
        }
    }
}