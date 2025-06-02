package com.rxlogix.util.spotfire

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.security.PrivilegedExceptionAction

class HttpPost implements PrivilegedExceptionAction {
    ByteArrayOutputStream outputStream
    URL url
    String payload
    Logger logger = LoggerFactory.getLogger(HttpPost.class.getName())

    HttpPost(url, payload) {
        this.url = url
        this.payload = payload
        this.outputStream = new ByteArrayOutputStream(1024)
    }

    Object run() throws Exception {
        InputStream inputStream
        byte[] buf = new byte[1024]
        int n

        HttpURLConnection conn = new jespa.http.HttpURLConnection(url)
        try {
            conn.setRequestProperty('Content-Type', 'application/xml')
            conn.setRequestProperty('from-pvr', 'true')
            conn.setRequestProperty("jespa.http.tls.sslsocketfactory.classname", Base64.encoder.encodeToString("jespa.http.DummySSLSocketFactory".getBytes('utf-8')))

            conn.setDoOutput(true)
            OutputStreamWriter out = new OutputStreamWriter(conn.getOutputStream())
            out.write(payload)
            out.flush() // this triggers the POST

            inputStream = conn.getInputStream()
            while ((n = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, n)
            }
        } catch (Throwable ioe) {
            logger.error("Error occurred when post to NTLM URL [${url.toString()}]", ioe)
            inputStream = conn.getInputStream()
            while ((n = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, n)
            }
        } finally {
            conn.disconnect()
        }
    }
}
