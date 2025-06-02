package com.rxlogix.file

import com.rxlogix.Constants
import com.rxlogix.customException.InvalidApiResponseException
import com.rxlogix.dto.FileDTO
import grails.util.Holders
import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
import org.apache.commons.fileupload.InvalidFileNameException
import org.apache.http.HttpStatus
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClientBuilder
import groovy.json.JsonSlurper
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.client.methods.HttpGet
import org.apache.http.util.EntityUtils


@Transactional
@Slf4j
class PVCMFileAttachmentService implements FileAttachmentService {

    //Sample URL : http://10.100.21.95:8082/api/case/getAttachmentData?fileStorageId=480&caseNumber=20211000097&version=1
    final static String PVCM_ENDPOINT_URL = "/api/case/getAttachmentData"

    @Override
    FileDTO getFile(String uniqueId, Boolean isRedacted = false) {
        String url = Holders.config.getProperty('pvcm.api.attachment.url')
        log.info("Fetch PVCM Case Attachment ID : ${uniqueId} from url : " + url + PVCM_ENDPOINT_URL)
        if (!uniqueId || !uniqueId.contains('_')) {
            log.warn("Invalid request for extracting file data from PVCM for file uniqueId: ${uniqueId}")
            return null
        }
        String[] uniqueIds = uniqueId.split("_")
        Map data = [caseNumber: uniqueIds[0], version: uniqueIds[1], fileStorageId: uniqueIds[2]]
        log.info("PVCM URL to fetch attachment bytes = " + url + PVCM_ENDPOINT_URL + '?fileStorageId=' + data.fileStorageId + '&caseNumber=' + data.caseNumber + '&version=' + data.version + '&isRedacted=' + isRedacted)
        return downloadBytes(url + PVCM_ENDPOINT_URL + '?fileStorageId=' + data.fileStorageId + '&caseNumber=' + data.caseNumber + '&version=' + data.version + '&isRedacted=' + isRedacted)
    }

    private FileDTO downloadBytes(String url) {
        CloseableHttpClient client = HttpClientBuilder.create().build()
        HttpGet request = new HttpGet(url)
        request.setHeader('PVI_PUBLIC_TOKEN', Constants.PVI_PUBLIC_TOKEN)
        HttpResponse response = client.execute(request)
        if (response.statusLine?.statusCode != HttpStatus.SC_OK) {
            log.error("Invalid response code: ${response.statusLine?.statusCode} received from PVCM download url: ${url}")
            throw new InvalidApiResponseException(url, response.statusLine?.statusCode)
        }
        String filename = null
        String dispositionValue = response.getFirstHeader("Content-Disposition")?.getValue()
        Integer index = dispositionValue?.indexOf("filename=")
        if (index > 0) {
            filename = URLDecoder.decode(dispositionValue.substring(index + 10, dispositionValue.length() - 1), 'UTF-8')
        }
        if (!filename) {
            log.error("Couldn't extract valid filename from file download url: ${url}")
            throw new InvalidFileNameException("Not found fileName on : ${url}", "Couldn't extract valid filename from url: ${url}")
        }
        HttpEntity entity = response.getEntity()
        InputStream inputStream = entity.getContent()
        return new FileDTO(name: filename, data: inputStream.bytes)
    }
}