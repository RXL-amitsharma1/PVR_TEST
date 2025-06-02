package com.rxlogix.signal

import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.apache.http.client.config.RequestConfig
import org.apache.http.config.SocketConfig
import org.apache.http.impl.client.HttpClients

@Transactional
class SignalIntegrationApiService {

    GrailsApplication grailsApplication

    Map postData(String baseUrl, String path, String data, method = Method.POST) {

        def returnMap = [:]
        def timeout = grailsApplication.config.pvsignal.callback.timeout.limit // millis
        SocketConfig sc = SocketConfig.custom().setSoTimeout(timeout).build()
        RequestConfig rc = RequestConfig.custom().setConnectTimeout(timeout).setSocketTimeout(timeout).build()
        def hc = HttpClients.custom().setDefaultSocketConfig(sc).setDefaultRequestConfig(rc).build()
        def http = new HTTPBuilder(baseUrl)
        http.client = hc

        // perform a POST request, expecting JSON response
        http.request(method, ContentType.JSON) {
            uri.path = path

            body = data
            // response handlers
            response.success = { resp, reader ->
                returnMap = [status: resp.status, result: reader]
            }
            response.failure = { resp ->
                returnMap = [status: resp.status]
            }
        }
        return returnMap

    }

    Map postCallback(String url, Map data, method = Method.POST) {

        def returnMap = [:]
        def http = new HTTPBuilder(url)

        // perform a POST request, expecting JSON response
        http.request(method, ContentType.JSON) {
            if(method == Method.POST){
                body = data
            }else {
                uri.query = data
            }

            // response handlers
            response.success = { resp, reader ->
                returnMap = [status: resp.status, result: reader]
            }
            response.failure = { resp ->
                returnMap = [status: resp.status]
            }
        }
        return returnMap

    }
}
