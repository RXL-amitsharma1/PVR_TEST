package com.rxlogix.odata

import javax.servlet.AsyncContext
import javax.servlet.DispatcherType
import javax.servlet.RequestDispatcher
import javax.servlet.ServletContext
import javax.servlet.ServletInputStream
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession
import javax.servlet.http.HttpUpgradeHandler
import javax.servlet.http.Part
import java.security.Principal

class OdataRequestProxy implements HttpServletRequest {
    HttpServletRequest req

    public OdataRequestProxy(HttpServletRequest req) {
        this.req = req
    }

    @Override
    String getAuthType() {
        return req.getAuthType()
    }

    @Override
    Cookie[] getCookies() {
        return req.getCookies()
    }

    @Override
    long getDateHeader(String name) {
        return req.getDateHeader(name)
    }

    @Override
    String getHeader(String name) {
        return req.getHeader(name)
    }

    @Override
    Enumeration getHeaders(String name) {
        return req.getHeaders(name)
    }

    @Override
    Enumeration getHeaderNames() {
        return req.getHeaderNames()
    }

    @Override
    int getIntHeader(String name) {
        return req.getIntHeader(name)
    }

    @Override
    String getMethod() {
        return req.getMethod()
    }

    @Override
    String getPathInfo() {
        return req.getPathInfo()
    }

    @Override
    String getPathTranslated() {
        return req.getPathTranslated()
    }

    @Override
    String getContextPath() {
        return req.getContextPath()
    }

    @Override
    String getQueryString() {
        return req.getQueryString()
    }

    @Override
    String getRemoteUser() {
        return req.getRemoteUser()
    }

    @Override
    boolean isUserInRole(String role) {
        return req.isUserInRole(role)
    }

    @Override
    Principal getUserPrincipal() {
        return req.getUserPrincipal()
    }

    @Override
    String getRequestedSessionId() {
        return req.getRequestedSessionId()
    }

    @Override
    String getRequestURI() {
        return req.forwardURI
    }

    @Override
    StringBuffer getRequestURL() {
        return new StringBuffer(req.getScheme() + "://" + req.getServerName() + ":" + req.getServerPort() + req.forwardURI)
    }

    @Override
    String getServletPath() {
        int beginIndex, endIndex;
        String rawRequestUrl = getRequestURL()
        beginIndex = rawRequestUrl.indexOf("/odata/")
        endIndex = rawRequestUrl.indexOf("/", beginIndex + 7)
        return rawRequestUrl.substring(beginIndex, endIndex + 1)

    }

    String getDsName() {
        int beginIndex, endIndex;
        String rawRequestUrl = getRequestURL()
        beginIndex = rawRequestUrl.indexOf("/odata/")
        endIndex = rawRequestUrl.indexOf("/", beginIndex + 7)
        return rawRequestUrl.substring(beginIndex + 7, endIndex)

    }

    @Override
    HttpSession getSession(boolean create) {
        return req.getSession(create)
    }

    @Override
    HttpSession getSession() {
        return req.getSession()
    }

    @Override
    boolean isRequestedSessionIdValid() {
        return req.isRequestedSessionIdValid()
    }

    @Override
    boolean isRequestedSessionIdFromCookie() {
        return req.isRequestedSessionIdFromCookie()
    }

    @Override
    boolean isRequestedSessionIdFromURL() {
        return req.isRequestedSessionIdFromURL()
    }

    @Override
    boolean isRequestedSessionIdFromUrl() {
        return req.isRequestedSessionIdFromUrl()
    }

    @Override
    Object getAttribute(String name) {
        return req.getAttribute(name)
    }

    @Override
    Enumeration getAttributeNames() {
        return req.getAttributeNames()
    }

    @Override
    String getCharacterEncoding() {
        return req.getCharacterEncoding()
    }

    @Override
    void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        req.setCharacterEncoding(env)
    }

    @Override
    int getContentLength() {
        return req.getContentLength()
    }

    @Override
    String getContentType() {
        return req.getContentType()
    }

    @Override
    ServletInputStream getInputStream() throws IOException {
        return req.getInputStream()
    }

    @Override
    String getParameter(String name) {
        return req.getParameter(name)
    }

    @Override
    Enumeration getParameterNames() {
        return req.getParameterNames()
    }

    @Override
    String[] getParameterValues(String name) {
        return req.getParameterValues(name)
    }

    @Override
    Map getParameterMap() {
        return req.getParameterMap()
    }

    @Override
    String getProtocol() {
        return req.getProtocol()
    }

    @Override
    String getScheme() {
        return req.getScheme()
    }

    @Override
    String getServerName() {
        return req.getServerName()
    }

    @Override
    int getServerPort() {
        return req.getServerPort()
    }

    @Override
    BufferedReader getReader() throws IOException {
        return req.getReader()
    }

    @Override
    String getRemoteAddr() {
        return req.getRemoteAddr()
    }

    @Override
    String getRemoteHost() {
        return req.getRemoteHost()
    }

    @Override
    void setAttribute(String name, Object o) {
        req.setAttribute(name, o)
    }

    @Override
    void removeAttribute(String name) {
        req.removeAttribute(name)
    }

    @Override
    Locale getLocale() {
        return req.getLocale()
    }

    @Override
    Enumeration getLocales() {
        return req.getLocales()
    }

    @Override
    boolean isSecure() {
        return req.isSecure()
    }

    @Override
    RequestDispatcher getRequestDispatcher(String path) {
        return req.getRequestDispatcher(path)
    }

    @Override
    String getRealPath(String path) {
        return req.getRealPath(path)
    }

    @Override
    int getRemotePort() {
        return req.getLocalName()
    }

    @Override
    String getLocalName() {
        return req.getLocalName()
    }

    @Override
    String getLocalAddr() {
        return req.getLocalAddr()
    }

    @Override
    int getLocalPort() {
        return req.getLocalPort()
    }

    DispatcherType getDispatcherType() {
        return null
    }

    boolean isAsyncSupported() { return false }

    AsyncContext startAsync() { return null }

    Collection getParts() { return null }

    long getContentLengthLong() { return getContentLength() as Long }

    void logout() {}

    String changeSessionId() { return null }

    void login(String a, String b) {}

    AsyncContext getAsyncContext() { return null }

    boolean isAsyncStarted() { return false }

    AsyncContext startAsync(ServletRequest rq, ServletResponse rs) { return null }

    HttpUpgradeHandler upgrade(Class c) { return null }

    boolean authenticate(HttpServletResponse r) { return false }

    ServletContext getServletContext() { return null }

    Part getPart(String a) { return null }

}
