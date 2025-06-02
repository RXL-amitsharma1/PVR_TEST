import com.rxlogix.config.CustomSQLTemplate
import com.rxlogix.config.ReportTemplate

databaseChangeLog = {

    changeSet(author: "prashantsahi (generated)", id: "1555886666666-7") {
          preConditions(onFail: 'MARK_RAN', onError: 'MARK_RAN') {
            sqlCheck(expectedResult: '1', 'select COUNT(*) from  RPT_TEMPLT\n' +
                    'where CIOMS_I_TEMPLATE =1 and ORIG_TEMPLT_ID=0 and name=\'CIOMS I Template\';')
        }
        grailsChange {
            change {
                try {
                    CustomSQLTemplate.withNewSession { session ->
                        List<CustomSQLTemplate> csts = CustomSQLTemplate.findAllByNameAndCiomsIAndOriginalTemplateId(ReportTemplate.CIOMS_I_TEMPLATE_NAME, true,0L)
                        csts.each { CustomSQLTemplate cst ->
                            cst.editable = true
                            cst.customSQLTemplateWhere = " and cci.flag_blinded = ':BLINDED_CIOMS_CHECKBOX_VALUE:' and cci.flag_protect_privacy = ':PRIVACY_CIOMS_CHECKBOX_VALUE:' \n\r order by CCI.MFR_CONTROL_NO_24B asc"
                            cst.save(flush: true)
                        }
                        session.flush()
                        session.clear()
                        session.close()
                        session.connection()?.close()
                    }
                }
                catch (Exception ex) {
                    println "##### Error Occurred while updating the sorting of CIOMS I Template ####"
                    ex.printStackTrace(System.out)
                }
            }
        }
    }

}
