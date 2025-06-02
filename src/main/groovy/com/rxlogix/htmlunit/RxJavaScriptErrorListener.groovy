package com.rxlogix.htmlunit

import org.htmlunit.ScriptException
import org.htmlunit.html.HtmlPage
import org.htmlunit.javascript.JavaScriptErrorListener
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

@Slf4j
@CompileStatic
class RxJavaScriptErrorListener implements JavaScriptErrorListener {

    RxJavaScriptErrorListener() {

    }

    @Override
    void scriptException(HtmlPage htmlPage, ScriptException e) {
        log.error("Backend HTML render has an issue due to JavaScript exception on ${htmlPage} error: ${e.message}", e)
    }

    @Override
    void timeoutError(HtmlPage htmlPage, long l, long l1) {
        log.error("Backend HTML render has an issue due to JavaScript timeout on ${htmlPage}")
    }

    @Override
    public void malformedScriptURL(HtmlPage page, String url, MalformedURLException malformedURLException) {
        log.error("Unable to build URL for script src tag [" + url + "]", malformedURLException);
    }

    @Override
    public void loadScriptError(HtmlPage page, URL scriptUrl, Exception exception) {
        log.error("Error loading JavaScript from [" + scriptUrl + "].", exception);
    }

    @Override
    void warn(String message, String sourceName, int line, String lineSource, int lineOffset) {
        log.error("Warning for Javascript [" + sourceName + "].", message);
    }
}
