import java.text.DateFormat
import java.text.Format
import java.text.SimpleDateFormat
import com.rxlogix.Constants
import groovy.json.JsonSlurper
import grails.converters.JSON

databaseChangeLog = {
    changeSet(author: "riya", id: "202305241552-1") {
        grailsChange {
            change {
                try {
                    String queryString = "select a.id, dbms_lob.substr(a.metadata , dbms_lob.getlength(a.metadata), 1) as metadata" +
                            " from quality_case_data a where a.report_id is null and a.error_type = 'Manual'and isdeleted = 0 \n"

                    List list = sql.rows(queryString)
                    list.each {
                        def parser = new JsonSlurper()
                        def json = parser.parseText(it.metadata)
                        String masterCaseReceiptDateValue = json.get("masterCaseReceiptDate")
                        Format dateFormatter = new SimpleDateFormat(Constants.DateFormat.REGULAR_DATE)
                        Date date = (Date) dateFormatter.parseObject(masterCaseReceiptDateValue)
                        DateFormat dateFormat = new SimpleDateFormat(Constants.DateFormat.CSV_JASPER)
                        def formattedDate = dateFormat.format(date)
                        json.put("masterCaseReceiptDate", formattedDate);
                        String updatedJSON = json as JSON
                        sql.execute("update quality_case_data set metadata = ? where id= ?", [updatedJSON, it['id']])
                    }
                } catch (Exception ex) {
                    println "##### Error Occurred while updating quality_case_data metadata 202305241552-1 ####"
                    ex.printStackTrace(System.out)
                }
            }
        }
    }

    changeSet(author: "riya", id: "202305241552-2") {
        grailsChange {
            change {
                try {
                    String queryString = "select a.id, dbms_lob.substr(a.metadata , dbms_lob.getlength(a.metadata), 1) as metadata" +
                            " from quality_sampling a where a.report_id is null and a.error_type = 'Manual'and isdeleted = 0 \n"

                    List list = sql.rows(queryString)
                    list.each {
                        def parser = new JsonSlurper()
                        def json = parser.parseText(it.metadata)
                        String masterCaseReceiptDateValue = json.get("masterCaseReceiptDate")
                        Format dateFormatter = new SimpleDateFormat(Constants.DateFormat.REGULAR_DATE)
                        Date date = (Date) dateFormatter.parseObject(masterCaseReceiptDateValue)
                        DateFormat dateFormat = new SimpleDateFormat(Constants.DateFormat.CSV_JASPER)
                        def formattedDate = dateFormat.format(date)
                        json.put("masterCaseReceiptDate", formattedDate);
                        String updatedJSON = json as JSON
                        sql.execute("update quality_sampling set metadata = ? where id= ?", [updatedJSON, it['id']])
                    }
                } catch (Exception ex) {
                    println "##### Error Occurred while updating quality_sampling metadata 202305241552-2 ####"
                    ex.printStackTrace(System.out)
                }
            }
        }
    }

    changeSet(author: "riya", id: "202305241552-3") {
        grailsChange {
            change {
                try {
                    String queryString = "select a.id, dbms_lob.substr(a.metadata , dbms_lob.getlength(a.metadata), 1) as metadata" +
                            " from quality_submission a where a.report_id is null and a.error_type = 'Manual'and isdeleted = 0 \n"

                    List list = sql.rows(queryString)
                    list.each {
                        def parser = new JsonSlurper()
                        def json = parser.parseText(it.metadata)
                        String masterCaseReceiptDateValue = json.get("masterCaseReceiptDate")
                        Format dateFormatter = new SimpleDateFormat(Constants.DateFormat.REGULAR_DATE)
                        Date date = (Date) dateFormatter.parseObject(masterCaseReceiptDateValue)
                        DateFormat dateFormat = new SimpleDateFormat(Constants.DateFormat.CSV_JASPER)
                        def formattedDate = dateFormat.format(date)
                        json.put("masterCaseReceiptDate", formattedDate);
                        String updatedJSON = json as JSON
                        sql.execute("update quality_submission set metadata = ? where id= ?", [updatedJSON, it['id']])
                    }
                } catch (Exception ex) {
                    println "##### Error Occurred while updating quality_submission metadata 202305241552-3 ####"
                    ex.printStackTrace(System.out)
                }
            }
        }
    }

}