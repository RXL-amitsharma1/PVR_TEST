package com.rxlogix.jasperserver

import com.rxlogix.ReportExecutorService
import com.rxlogix.config.ReportTemplate
import grails.util.Holders
import groovy.util.logging.Slf4j

/**
 * Created by gologuzov on 28.02.17.
 */
@Slf4j
class CSVDataFileResource extends FileResource {
    ReportExecutorService reportExecutorService = Holders.applicationContext.getBean("reportExecutorService")
    ReportTemplate template

    @Override
    public FileResourceData copyData() {
        log.debug("[CSVDataFileResource] copyData")
        if (!this.data) {
            this.data = reportExecutorService.generatePreviewDataSource(template)
        }
        return super.copyData()
    }
}
