package com.rxlogix.admin

import grails.gorm.transactions.Transactional
import grails.util.Holders
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import groovyx.net.http.RESTClient
import com.rxlogix.Constants

@Transactional
class AdminIntegrationApiService {

    Map get(String url, String path, Map query) {
        Map ret = [:]
        try {
            RESTClient endpoint = new RESTClient(url)
            Map publicToken = fetchPublicToken()
            endpoint.handler.failure = { resp -> ret = [status: resp.status] }
            if (publicToken)
                endpoint.setHeaders(publicToken)
            def resp = endpoint.get(
                    path: path,
                    query: query
            )

            if (resp.status == 200) {
                ret = [status: resp.status, data: resp.data]
            } else {
                log.info("Response from admin :" + resp)
            }
        } catch (ConnectException ct) {
            log.error(ct.getMessage())
        } catch (Throwable t) {
            log.error(t.getMessage(), t)
        }
        return ret
    }

    Map fetchPublicToken() {
        return Holders.config.getProperty('pvadmin.publicApi.token', Map, [:])
    }

    Map fetchPvcmPublicToken() {
        return ['PVI_PUBLIC_TOKEN': Constants.PVI_PUBLIC_TOKEN]
    }

    Map postData(String baseUrl, String path, def data, Method method = Method.POST, boolean needPvcmToken = false) {

        Map ret = [:]
        HTTPBuilder http = new HTTPBuilder(baseUrl)

        Map publicToken = [:]

        if(needPvcmToken) {
            publicToken = fetchPvcmPublicToken()
        } else {
            publicToken = fetchPublicToken()
        }

        try {
            // perform a POST request, expecting JSON response
            http.request(method, ContentType.JSON) {
                /** If we have a parameter in url and a method type as a POST
                 * E.g., http://10.100.21.216:8085/pvadmin/refresh-cache-data?moduleId=ROUTING_CONDITION and method type POST,
                 * For this we have to use the baseUrl and need to pass a value in path as null otherwise it is throwing a exception
                 * To handle this exception we have used a null checker
                 */
                if(path) {
                    uri.path = path
                }
                body = data

                if (publicToken)
                    headers = publicToken

                // response handlers
                response.success = { resp, reader ->
                    ret = [status: 200, result: reader]
                }
                response.failure = { resp, reader ->
                    log.error(resp)
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
}