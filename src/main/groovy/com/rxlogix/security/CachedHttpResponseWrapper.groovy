package com.rxlogix.security

import javax.servlet.ServletOutputStream
import javax.servlet.WriteListener
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponseWrapper
import java.nio.charset.StandardCharsets

public class CachedHttpResponseWrapper extends HttpServletResponseWrapper {

    private final ServletOutputStreamWrapper outputStream = new ServletOutputStreamWrapper();
    private PrintWriter writer;

    public CachedHttpResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            writer = new PrintWriter(outputStream.getWriter())
        }
        return writer;
    }

    @Override
    public ServletOutputStreamWrapper getOutputStream() {
        return outputStream;
    }

    public String getCapturedContent() {
        return new String(outputStream.getCapturedBytes(), StandardCharsets.UTF_8);
    }
}
