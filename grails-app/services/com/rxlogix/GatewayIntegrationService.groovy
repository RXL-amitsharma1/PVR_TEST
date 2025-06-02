package com.rxlogix

import com.rxlogix.config.IcsrCaseTracking
import grails.core.GrailsApplication
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import groovyx.net.http.RESTClient
import org.apache.commons.io.FileUtils
import org.apache.http.HttpEntity
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntityBuilder

class GatewayIntegrationService {

    static transactional = false

    GrailsApplication grailsApplication

    def transmitFile(String senderId, String receiverId, String caseNumber, Integer versionNumber, File file){
        log.info("Transmitting File for CaseNumber : ${caseNumber} with Version : ${versionNumber}")
        def data = "{\"senderId\": \"${senderId}\", \"receiverId\": \"${receiverId}\"}"
        log.info(data)
        Map response = multiPartData(grailsApplication.config.getProperty('pvgateway.url'), grailsApplication.config.getProperty('pvgateway.transmit.file.uri'), file, data)
        if (response.status == 200) {
            log.info("File successfully transmitted for CaseNumber : ${caseNumber} with Version : ${versionNumber} and Filename : ${file.name}")
        } else {
            log.error("Error Transmitting File for CaseNumber : ${caseNumber} with Version : ${versionNumber} and Filename : ${file.name}, ${response.error}")
            throw new Exception(response.error ?: "Error Transmitting Case, Something Unexpected happened !")
        }
    }

    void searchForAckFiles(Boolean includedDownloadedFiles) {
        int transmittedCaseCount = IcsrCaseTracking.getAllTransmittedCases().count()
        def response = getData(grailsApplication.config.getProperty('pvgateway.url'), grailsApplication.config.getProperty('pvgateway.search.ack.files.uri'), [includedDownloadedFiles: includedDownloadedFiles, size: transmittedCaseCount, transactionType: "RECEIVE"])
        if (response.status == 200) {
            List data = response.data
            if (data.size()) {
                data.each {
                    String senderName = it.sender
                    def transactionList = it.transactionList
                    if (transactionList) {
                        transactionList.each {
                            String receiverName = it.receiver
                            if (it.ack) {
                                log.info("Found ${it.ack.size()} Files to download for Sender: ${senderName} and Receiver: ${receiverName}")
                                it.ack.each {
                                    Long ackId = it
                                    try {
                                        def resp = downloadReceivedFile(ackId)
                                        if (resp && resp.status == 200) {
                                            InputStream is = resp.data
                                            String filename = resp.filename
                                            if (!filename.toLowerCase().endsWith('.xml') && !filename.toLowerCase().endsWith('.ack')) {
                                                filename = filename + ".ack"
                                            }
                                            List<String> paths = grailsApplication.config.getProperty('pv.app.e2b.incoming.folders.path', List)
                                            //Added logic to push ack files into the folder where senderName is there.
                                            String filePath = paths.find { String p -> p.endsWith(File.separator + receiverName) } ?: paths.first()
                                            File file = new File("${filePath}/${filename}")
                                            FileUtils.copyInputStreamToFile(is, file)
                                            log.info("Successfully downloaded ack file for transaction id : ${ackId} with filename : ${filename}")
                                            notifyDownloadedFilesToGateway([ackId])
                                        } else {
                                            log.error("Error While downloading ACK from Gateway for ACK ID : ${ackId}!")
                                        }
                                    } catch (Exception e) {
                                        log.error("Error while Downloading ack file with id ${ackId} : ${e.message}", e)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    void notifyDownloadedFilesToGateway(List downloadedIdsList) {
        Map response = postData(grailsApplication.config.getProperty('pvgateway.url'), grailsApplication.config.getProperty('pvgateway.notify.downloadedFiles.uri'), downloadedIdsList)
        if (response.status == 200) {
            log.info("Successful processing acknowledgement sent for ${downloadedIdsList.first()}")
        }
    }

    def downloadReceivedFile(Long id) {
        log.info("Downloading Ack for transaction ID : "+id)
        def response = getData(grailsApplication.config.pvgateway.url, "${grailsApplication.config.pvgateway.download.receivedFile}/${id}", [asyncDownload: true], true)
        return response
    }

    def getTransactions(String filename) {
        def response = getData(grailsApplication.config.pvgateway.url, "${grailsApplication.config.pvgateway.get.transactions}", [filename: filename])
        if (response.data && (response.data instanceof List)) {
            return response.data.last()
        }
        return null
    }

    Map postData(String baseUrl, String path, def data, method = Method.POST) {
        Map ret = [:]
        HTTPBuilder http = new HTTPBuilder(baseUrl)
        http.getClient().getParams().setParameter("http.connection.timeout", grailsApplication.config.getProperty('gateway.endpoint.connection.timeout', Integer, 10 * 1000))
        http.getClient().getParams().setParameter("http.socket.timeout", grailsApplication.config.getProperty('gateway.endpoint.read.timeout', Integer, 30 * 1000))
        Map publicToken = fetchClientAgentCode()
        try {
            // perform a POST request, expecting JSON response
            http.request(method, ContentType.JSON) {
                uri.path = path
                body = data
                if (publicToken)
                    headers = publicToken
                // response handlers
                response.success = { resp, reader ->
                    ret = [status: 200, result: reader]
                }
                response.failure = { resp, reader ->
                    log.error(reader.message)
                    ret = [status: 500, error: reader.message?:""]
                }
            }
        } catch (ConnectException ct) {
            log.error(ct.getMessage())
            ret = [status: 500, error: ct.getMessage()]
        } catch (Throwable t) {
            log.error(t.getMessage(), t)
            ret = [status: 500, error: t.getMessage()]
        }
        return ret
    }

    Map multiPartData(String baseUrl, String path, File file, def request) {
        Map ret = [:]
        HTTPBuilder httpBuilder = new HTTPBuilder(baseUrl)
        httpBuilder.getClient().getParams().setParameter("http.connection.timeout", grailsApplication.config.getProperty('gateway.endpoint.connection.timeout', Integer, 10 * 1000))
        httpBuilder.getClient().getParams().setParameter("http.socket.timeout", grailsApplication.config.getProperty('gateway.endpoint.read.timeout', Integer, 30 * 1000))
        Map publicToken = fetchClientAgentCode()
        try {
            httpBuilder.request(Method.POST) { req ->
                uri.path = path
                if (publicToken)
                    headers = publicToken
                requestContentType = "multipart/form-data"
                MultipartEntityBuilder builder = MultipartEntityBuilder.create()
                builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
                builder.addBinaryBody("file", file)
                builder.addTextBody("request", request)
                HttpEntity httpEntity = builder.build()
                req.setEntity(httpEntity)

                response.success = { resp, reader ->
                    log.info(reader?.toString())
                    ret = [status: 200, result: reader]
                }
                response.failure = { resp, reader ->
                    log.error(reader?.toString())
                    ret = [status: 500, error: reader.errorMessage ?: ""]
                }
            }
        } catch (ConnectException ct) {
            ret = [status: 500, error: ct.getMessage()]
        } catch (Throwable t) {
            ret = [status: 500, error: t.getMessage()]
        }
        return ret
    }

    Map getData(String url, String path, Map query, boolean isFile=false) {
        Map ret = [:]
        try {
            RESTClient endpoint = new RESTClient(url)
            endpoint.getClient().getParams().setParameter("http.connection.timeout", grailsApplication.config.getProperty('gateway.endpoint.connection.timeout', Integer, 10 * 1000))
            endpoint.getClient().getParams().setParameter("http.socket.timeout", grailsApplication.config.getProperty('gateway.endpoint.read.timeout', Integer, 30 * 1000))
            Map publicToken = fetchClientAgentCode()
            String filename = null
            endpoint.handler.failure = { resp -> ret = [status: resp.status] }
            if (publicToken)
                endpoint.setHeaders(publicToken)
            def resp = endpoint.get(
                    path: path,
                    query: query
            )
            if (resp.status == 200) {
                if(isFile) {
                    //todo: find a better solution to extract filename from response headers
                    filename = resp.getHeaders('Content-Disposition')?.first()?.toString()?.replaceAll("Content-Disposition: attachment; filename=",'')
                    filename = filename.replaceAll("\"",'')
                }
                ret = [status: resp.status, data: resp.data, filename: filename]
            }
        } catch (ConnectException ct) {
            log.error(ct.getMessage())
        } catch (Throwable t) {
            log.error(t.getMessage(), t)
        }
        return ret
    }

    Map fetchClientAgentCode() {
        return ["client-agent-code": "${grailsApplication.config.getProperty('pvgateway.clientAgentCode')}"]
    }

}
