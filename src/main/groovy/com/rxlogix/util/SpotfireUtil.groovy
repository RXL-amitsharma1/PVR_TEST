package com.rxlogix.util

import groovy.xml.MarkupBuilder
import jespa.security.PasswordCredential
import jespa.security.RunAs
import org.apache.commons.codec.binary.Base64
import org.apache.commons.codec.digest.Md5Crypt
import org.apache.commons.io.output.WriterOutputStream
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.CredentialsProvider
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpPost
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.config.Registry
import org.apache.http.config.RegistryBuilder
import org.apache.http.conn.socket.ConnectionSocketFactory
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.conn.ssl.TrustStrategy
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.BasicCookieStore
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.HttpClientBuilder
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.protocol.BasicHttpContext
import org.apache.http.protocol.HttpContext
import org.apache.http.ssl.SSLContextBuilder
import org.apache.http.ssl.SSLContexts
import org.apache.http.util.EntityUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.net.ssl.SSLContext
import java.security.PrivilegedExceptionAction
import java.security.cert.CertificateException
import java.security.cert.X509Certificate

class SpotfireUtil {
    static Logger logger = LoggerFactory.getLogger(SpotfireUtil.class.getName())

    static String manifestUri = "spotfire/manifest"
    static String statusUri = "spotfire/rest/as/job/status/"
    static String startUrl = "spotfire/rest/as/job/start"

    static String triggerJob(String spotfireServer,
                             Integer port,
                             String protocol,
                             String xml,
                             String automationUser,
                             String automationPassword) {

        logger.info("protocol:[$protocol],spotfireServer:[$spotfireServer], port:[$port], username:[$automationUser]")
        HttpContext httpContext = new BasicHttpContext()
        org.apache.http.impl.client.BasicCookieStore cookieStore = new BasicCookieStore()
        httpContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore)

        CredentialsProvider credentialsProvider = new BasicCredentialsProvider()
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(automationUser, automationPassword))

        HttpClient httpClient

        if (protocol.trim().equalsIgnoreCase("http")) {
            logger.info("Using HTTP client")
            httpClient = getClientForHttp(credentialsProvider)
        } else {
            logger.info("Using HTTPs client")
            httpClient = getClientForHttps(credentialsProvider)
        }

        String url1 = "$protocol://$spotfireServer:$port/$manifestUri"
        logger.info("First step url: $url1")
        HttpGet manifestInvoker = new HttpGet(url1)
        HttpResponse response1 = httpClient.execute(manifestInvoker, httpContext)
        try {
            def firstResp = readData(response1)
            logger.info("First Step Response: [ $firstResp ]")

            String url2 = "$protocol://$spotfireServer:$port/$startUrl"
            logger.info("Second step url: $url2")

            HttpPost jobSender = new HttpPost(url2)
            jobSender.setHeader('Content-Type', 'application/xml')
            jobSender.setHeader('from-pvr', 'true')

            StringEntity xmlEntity = new StringEntity(xml)
            jobSender.setEntity(xmlEntity)

            HttpResponse response2 = httpClient.execute(jobSender, httpContext)
            def jobResp = readData(response2)
            logger.info("Second step response: [ $jobResp ]")
            return jobResp
        } catch (Throwable t) {
            logger.error("Error occurred when triggering Spotfire automation job", t)
            return null
        } finally {
            response1.close()
        }
    }

    static triggerJobOnNTML(String spotfireServer,
                            Integer port,
                            String protocol,
                            String xml,
                            String ntlmAccount,
                            String ntlmPassword) {

        logger.info("protocol:[$protocol],spotfireServer:[$spotfireServer], port:[$port], username:[$ntlmAccount]")

        URL manifestUrl = new URL("$protocol://${spotfireServer}:${port}/$manifestUri")
        logger.info("Manifest url: $manifestUrl")
        URL jobTriggerUrl = new URL("$protocol://$spotfireServer:$port/$startUrl")
        logger.info("Job Trigger url: $jobTriggerUrl")

        com.rxlogix.util.spotfire.HttpGet httpGet = new com.rxlogix.util.spotfire.HttpGet(manifestUrl)
        com.rxlogix.util.spotfire.HttpPost httpPost = new com.rxlogix.util.spotfire.HttpPost(jobTriggerUrl, xml)
        invokeNTMLUrl(httpGet, ntlmAccount, ntlmPassword)
        invokeNTMLUrl(httpPost, ntlmAccount, ntlmPassword)

        new String(httpPost.outputStream.toByteArray(), "utf-8")
    }

    private static invokeNTMLUrl(PrivilegedExceptionAction act, String acctName, String password) {
        PasswordCredential cred = new PasswordCredential(acctName, password.toCharArray())
        RunAs.runAs(act, cred)
    }

    def static composeXmlBodyForTask(parameters) {
        def writer = new StringWriter()
        def mb = new MarkupBuilder(writer)

        mb.'as:Job'('xmlns:as': 'urn:tibco:spotfire.dxp.automation') {
            'as:Tasks' {
                OpenAnalysisFromLibrary('xmlns': 'urn:tibco:spotfire.dxp.automation.tasks') {
                    'as:Title'(parameters.openTitle)
                    'AnalysisPath'(parameters.AnalysisPath)
                    'ConfigurationBlock'(parameters.ConfigurationBlock)
                }
                SaveAnalysisToLibrary('xmlns': 'urn:tibco:spotfire.dxp.automation.tasks') {
                    'as:Title'(parameters.saveTitle)
                    'LibraryPath'(parameters.LibraryPath)
                    'EmbedData'(parameters.EmbedData)
                    'DeleteExistingBookmarks'(parameters.DeleteExistingBookmarks)
                    'AnalysisDescription'('{jobid}')
                }
                OpenAnalysisFromLibrary('xmlns': 'urn:tibco:spotfire.dxp.automation.tasks') {
                    'as:Title'(parameters.openTitle)
                    'AnalysisPath'(parameters.AnalysisPath)
                    'ConfigurationBlock'(parameters.ConfigurationBlock2)
                }
                SaveAnalysisToLibrary('xmlns': 'urn:tibco:spotfire.dxp.automation.tasks') {
                    'as:Title'(parameters.saveTitle)
                    'LibraryPath'(parameters.LibraryPath2)
                    'EmbedData'(parameters.EmbedData)
                    'DeleteExistingBookmarks'(parameters.DeleteExistingBookmarks)
                    'AnalysisDescription'('{jobid}')
                }
                SendEmail('xmlns': 'urn:tibco:spotfire.dxp.automation.tasks') {
                    'as:Title'(parameters.emailTitle)
                    'Recipients' {
                        parameters.Recipients.each {
                            'string'(it)
                        }
                    }
                    'Subject'(parameters.Subject)
                    'Message'(parameters.EmailMessage)
                    'Links'()
                    'Attachments'()
                }

            }
        }
        def xmlString = writer.toString()
        writer.close()
        xmlString
    }

    static String buildAuthToken(String username, String sessionId) {
        Base64.encodeBase64(
                Md5Crypt.md5Crypt("$username . ${sessionId}".getBytes("UTF-8")).
                        getBytes("UTF-8"))
    }

    private static String readData(HttpResponse response) {
        HttpEntity entity = response.getEntity()
        EntityUtils.toString(entity, "UTF-8")
    }

    private static HttpClient getClientForHttp(CredentialsProvider credentialsProvider) {
        HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build()
    }

    private static getClientForHttps(CredentialsProvider credentialsProvider) {
        SSLContextBuilder builder = SSLContexts.custom()
        builder.loadTrustMaterial(null, new TrustStrategy() {
            @Override
            boolean isTrusted(X509Certificate[] chain, String authType)
                    throws CertificateException {
                return true
            }
        })
        SSLContext sslContext = builder.build()
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER)

        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
                .<ConnectionSocketFactory> create().register("https", sslsf).build()

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(
                socketFactoryRegistry)
        HttpClients.custom().setConnectionManager(cm).setDefaultCredentialsProvider(credentialsProvider).build()
    }

    private static getClientForNTLM(String accountName, String password, String server) {
        null
    }

    static File generateAutomationXml(File folder, String content) {
        def files = folder.listFiles()

        def nexNum = 1
        if (files)
            nexNum = files.length + 1

        File file = new File(folder, "job-${nexNum}.xml")
        file.write(content)
        logger.info("$file is generated")
        file
    }
}
