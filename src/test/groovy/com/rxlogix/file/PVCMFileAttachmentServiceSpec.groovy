package com.rxlogix.file

import com.rxlogix.Constants
import com.rxlogix.customException.InvalidApiResponseException
import com.stehno.ersatz.ErsatzServer
import com.stehno.ersatz.cfg.Expectations
import com.stehno.ersatz.cfg.Request
import com.stehno.ersatz.cfg.Response
import grails.testing.gorm.DataTest
import grails.util.Holders
import groovy.json.JsonOutput
import org.grails.config.PropertySourcesConfig
import spock.lang.Shared
import spock.lang.Specification
import com.rxlogix.dto.FileDTO


class PVCMFileAttachmentServiceSpec extends Specification implements DataTest {

    @Shared
    FileAttachmentService service

    def setupSpec() {
        service = new PVCMFileAttachmentService()
    }

    void "test getFile for valid status"() {
        given:
        Long fileStorageId = 123
        String caseNumber = '1223232'
        int version = 12

        ErsatzServer ersatz = new ErsatzServer()
        ersatz.expectations { Expectations expectations ->
            expectations.GET('/api/case/getAttachmentData', { Request request ->
                request.query('fileStorageId', "${fileStorageId}")
                        .query('caseNumber', caseNumber)
                        .query('version', "${version}")
                request.responder { Response response ->
                    response.code(200)
                            .body(JsonOutput.toJson([result: [fileName: 'file.png', file: 'test'.getBytes(), contentType: 'application/text']]), 'application/json')
                }

            }).called(1)
        }
        Holders.setConfig(new PropertySourcesConfig())
        Holders.config.pvcm.api.attachment.url = ersatz.httpUrl
        when:
        FileDTO result = service.getFile("${caseNumber}_${version}_${fileStorageId}")
        then:
        result.name == 'file.png'
        result.data == 'test'.bytes
        and:
        ersatz.verify()
        cleanup:
        ersatz.stop()

    }

    void "test getFile for invalid staus frpm api end"() {
        given:
        Long fileStorageId = 123
        String caseNumber = '1223232'
        int version = 12

        ErsatzServer ersatz = new ErsatzServer()
        ersatz.expectations { Expectations expectations ->
            expectations.GET('/api/case/getAttachmentData', { Request request ->
                request.query('fileStorageId', "${fileStorageId}")
                        .query('caseNumber', caseNumber)
                        .query('version', "${version}")
                        .header('PVI_PUBLIC_TOKEN', Constants.PVI_PUBLIC_TOKEN)
                request.responder { Response response ->
                    response.code(500)
                            .body('Error at the service end')
                }

            }).called(1)
        }
        Holders.setConfig(new PropertySourcesConfig())
        Holders.config.pvcm.api.attachment.url = ersatz.httpUrl
        when:
        service.getFile("${caseNumber}_${version}_${fileStorageId}")
        then:
        thrown(InvalidApiResponseException)
        and:
        ersatz.verify()
        cleanup:
        ersatz.stop()

    }

    void "test getFile for invalid input"() {
        given:
        String caseNumber = '1223232'
        Holders.setConfig(new PropertySourcesConfig())
        Holders.config.pvcm.api.attachment.url = 'http://pvr.rxlogix.com/reports'
        when:
        FileDTO result = service.getFile("${caseNumber}")
        then:
        result == null
    }

}
