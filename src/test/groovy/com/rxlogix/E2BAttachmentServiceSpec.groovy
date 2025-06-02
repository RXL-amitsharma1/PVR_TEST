package com.rxlogix


import com.rxlogix.dto.FileDTO
import com.rxlogix.file.ArgusFileAttachmentService
import com.rxlogix.file.FileAttachmentLocator
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import groovy.mock.interceptor.MockFor
import spock.lang.Specification

class E2BAttachmentServiceSpec extends Specification implements DataTest, ServiceUnitTest<E2BAttachmentService> {

    void setup(){
        grailsApplication.config.safety.source = 'Argus'
        service.grailsApplication = grailsApplication
    }

    void "Check xml nodes not modified when no matching content found"() {
        when:
        String xml = """<?xml version="1.0" encoding="UTF-8"?><person>
  <name>John</name>
</person>
""";
        String xsltName = "EMA";
        service.fileAttachmentLocator = new MockFor(FileAttachmentLocator).proxyInstance()
        then:
        xml == service.addMissingAttachmentBytesToXMl(xml, xsltName,null, null, null)
    }

    void "Check xml nodes modified when matching content found and attachment not exists exception thrown"() {
        given:
        def fileAttachmentLocator = new MockFor(FileAttachmentLocator)
        fileAttachmentLocator.demand.getServiceFor(1){ String type ->
            def argusService = new MockFor(ArgusFileAttachmentService)
            argusService.demand.getFile(1){ String id ->
                return null

            }
            return argusService.proxyInstance()
        }
        service.fileAttachmentLocator = fileAttachmentLocator.proxyInstance()
        when:
        String xml = """<?xml version="1.0" encoding="UTF-8"?><person>
  <name>John</name>
  <attachment mediaType="text/plain">SampleFile.txt(###121_1###)</attachment>
</person>
""";
        String xsltName = "EMA"
        service.addMissingAttachmentBytesToXMl(xml, xsltName,null, null, null).trim()
        then:
        final RuntimeException exception = thrown()
        exception.message == 'Attachment data not found for 121_1'
    }


    void "Check xml nodes modified when matching content found and attachment exists"() {
        given:
        def fileAttachmentLocator = new MockFor(FileAttachmentLocator)
        fileAttachmentLocator.demand.getServiceFor(2){ String type ->
            def argusService = new MockFor(ArgusFileAttachmentService)
            argusService.demand.getFile(2){ String id ->
                return new FileDTO(name: "SampleFile.txt",data: "Sample File data".bytes)

            }
            return argusService.proxyInstance()
        }
        service.fileAttachmentLocator = fileAttachmentLocator.proxyInstance()
        when:
        String xml = """<?xml version="1.0" encoding="UTF-8"?><person>
<!-- test comment -->
  <name>John</name>
  <attachment mediaType="text/plain">SampleFile.txt(###121_1###)</attachment>
  <attachment mediaType="pdf">SampleFile.pdf(###121_1###)</attachment>
</person>
""";
        String xsltName = "EMA"
        then:
        """<?xml version="1.0" encoding="UTF-8"?><person>
<!-- test comment -->
  <name>John</name>
  <attachment mediaType="text/plain">Sample File data</attachment>
  <attachment mediaType="pdf">U2FtcGxlIEZpbGUgZGF0YQ==</attachment>
</person>""".trim() == service.addMissingAttachmentBytesToXMl(xml, xsltName, null, null, null).trim()
    }

    void "Check xml nodes modified when matching content found and attachment exists with Compression"() {
        given:
        def fileAttachmentLocator = new MockFor(FileAttachmentLocator)
        fileAttachmentLocator.demand.getServiceFor(2){ String type ->
            def argusService = new MockFor(ArgusFileAttachmentService)
            argusService.demand.getFile(2){ String id ->
                return new FileDTO(name: "SampleFile.txt",data: "Sample File data".bytes)

            }
            return argusService.proxyInstance()
        }
        service.fileAttachmentLocator = fileAttachmentLocator.proxyInstance()
        when:
        String xml = """<?xml version="1.0" encoding="UTF-8"?><person>
<!-- test comment -->
  <name>John</name>
  <attachment mediaType="text/plain">SampleFile.txt(###121_1###)</attachment>
  <attachment mediaType="pdf" compression="DF">SampleFile.pdf(###121_1###)</attachment>
</person>
""";
        String xsltName = "EMA"
        then:
        """<?xml version="1.0" encoding="UTF-8"?><person>
<!-- test comment -->
  <name>John</name>
  <attachment mediaType="text/plain">Sample File data</attachment>
  <attachment compression="DF" mediaType="pdf">C07MLchJVXDLBBIpiSWJAA==</attachment>
</person>""".trim() == service.addMissingAttachmentBytesToXMl(xml, xsltName, null, null, null).trim()
    }
}
