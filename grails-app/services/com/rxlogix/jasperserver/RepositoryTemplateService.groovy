package com.rxlogix.jasperserver

import com.rxlogix.config.ReportTemplate
import com.rxlogix.repo.RepoFileResource
import com.rxlogix.repo.RepoResource
import grails.gorm.transactions.Transactional
import net.sf.jasperreports.engine.JasperCompileManager
import org.grails.orm.hibernate.cfg.GrailsHibernateUtil

@Transactional
class RepositoryTemplateService {
    protected static final Map<String, Object> CLIENT_CLONE_OPTIONS
    static {
        HashMap<String, Object> options = new HashMap<String, Object>()
        options.put(RepoResource.CLIENT_OPTION_FULL_DATA, null)
        options.put(RepoResource.CLIENT_OPTION_AS_NEW, null)
        CLIENT_CLONE_OPTIONS = Collections.unmodifiableMap(options)
    }
    def templateService
    def reportExecutorService

    FileResource dataAdapter

    def createDataFolder(ReportTemplate template) {
        def csvDataFile = new CSVDataFileResource(
                template: template,
                name: "data.csv",
                label: "CSV Data",
                fileType: FileResource.TYPE_CSV)
        dataAdapter = new CSVDataAdapterResource(
                template: template,
                csvDataFile: csvDataFile,
                name: "adapter.xml",
                label: "CSV Data Adapter",
                fileType: FileResource.TYPE_XML)
        Folder dataFolder = new Folder(
                name: "data",
                label: "Data")
        dataFolder.addChild(csvDataFile)
        dataFolder.addChild(dataAdapter)
        return dataFolder
    }

    def createMainReport(ReportTemplate template) {
        FileResource mainReport
        if (template.fixedTemplate == null) {
            mainReport = new FileResource(
                    name: "${template.name}.jrxml",
                    label: template.name,
                    fileType: FileResource.TYPE_JRXML)

            RepoFileResource repoResource = new RepoFileResource()
            repoResource.copyFromClient(mainReport)
            RepoFileResource.withTransaction { status ->
                try {
                    repoResource.save(failOnError: true)
                    template.fixedTemplate = repoResource
                    template.save(failOnError: true)
                    status.flush()
                } catch (Exception e) {
                    status.setRollbackOnly()
                    log.error(e.message, e)
                }
            }
        }
        // Read saved template
        mainReport = template.fixedTemplate.copyToClient(CLIENT_CLONE_OPTIONS)
        if (!mainReport.hasData()) {
            try {
                def jasperDesign = templateService.toJasperDesign(GrailsHibernateUtil.unwrapIfProxy(template))
                if (dataAdapter) {
                    jasperDesign.setProperty("net.sf.jasperreports.data.adapter", "repo:${dataAdapter.getURIString()}")
                }
                def data = JasperCompileManager.writeReportToXml(jasperDesign)
                mainReport.data = data.bytes
            } catch (e) {
                log.error(e.message, e)
            }
        }
        return mainReport
    }

    def createReportUnit(ReportTemplate template, FileResource mainReport) {
        def reportUnit = new ReportUnit(
                name: "reportUnit",
                label: template.name
        )
        reportUnit.addResource(mainReport)
        reportUnit.setMainReport(mainReport)
        return reportUnit
    }
}
