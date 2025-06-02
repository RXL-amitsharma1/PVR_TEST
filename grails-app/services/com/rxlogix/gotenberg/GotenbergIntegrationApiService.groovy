package com.rxlogix.gotenberg

import grails.gorm.transactions.Transactional
import groovy.util.logging.Slf4j
import groovyx.net.http.Method
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.client.methods.HttpPost
import org.apache.http.HttpEntity
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.entity.ContentType
import com.rxlogix.dto.FileDTO
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.HttpResponse

@Transactional
@Slf4j
class GotenbergIntegrationApiService {

    FileDTO postData(String baseUrl, String path, def inputFilePath, Method method = Method.POST) {
        Map ret = [:]
        try {
            CloseableHttpClient client = HttpClientBuilder.create().build()
            HttpPost request = new HttpPost(baseUrl + path)
            MultipartEntityBuilder builder = MultipartEntityBuilder.create()   //  builder.add("field1", "yes", ContentType.TEXT_PLAIN);
            File file = new File(inputFilePath)
            builder.addBinaryBody("files", new FileInputStream(file), ContentType.APPLICATION_OCTET_STREAM, file.getName())
            HttpEntity multipart = builder.build()
            request.setEntity(multipart);

            HttpResponse response = client.execute(request)

            String filename = null
            String dispositionValue = response.getFirstHeader("Content-Disposition")?.getValue()
            Integer index = dispositionValue?.indexOf("filename=")
            if (index > 0) {
                filename = URLDecoder.decode(dispositionValue.substring(index + 10, dispositionValue.length() - 1), 'UTF-8')
            }
            HttpEntity entity = response.getEntity()
            InputStream inputStream = entity.getContent()
            return new FileDTO(name: filename, data: inputStream.bytes)
        } catch (ConnectException ct) {
            log.error("Unable to connect with the server")
            log.error(ct.getMessage())
        } catch (Throwable t) {
            log.error(t.getMessage(), t)
        }
        return ret
    }
}