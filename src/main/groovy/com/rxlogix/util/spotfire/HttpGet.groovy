package com.rxlogix.util.spotfire

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.security.PrivilegedExceptionAction

class HttpGet implements PrivilegedExceptionAction {
    URL url
    ByteArrayOutputStream outputStream

    HttpGet(url) {
        this.url = url
        this.outputStream = new ByteArrayOutputStream(1024)
    }

    Logger logger = LoggerFactory.getLogger(HttpGet.class.getName())

    Object run() throws Exception {
        InputStream input
        byte [] buf = new byte[1024]
        int n

        HttpURLConnection conn = new jespa.http.HttpURLConnection(url)
        try {
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible MSIE 7.0 Windows NT 6.0 SLCC1 .NET CLR 2.0.50727 InfoPath.2  .NET CLR 3.5.30729 .NET CLR 3.0.30618)")
            conn.setRequestProperty("jespa.http.tls.sslsocketfactory.classname", Base64.encoder.encodeToString("jespa.http.DummySSLSocketFactory".getBytes('utf-8')))

            input = conn.getInputStream()
            while ((n = input.read(buf, 0, buf.length)) > 0) {
                outputStream.write(buf, 0, n)
            }
        } catch (Throwable ioe) {
            logger.error("Jespa HttpGet had an error", ioe)

            /* Print the body of the error response
             */
            input = conn.getInputStream()
            while ((n = input.read(buf, 0, buf.length)) > 0) {
                outputStream.write(buf, 0, n)
            }
        } finally {
            conn.disconnect()
        }
    }
}
