package com.rxlogix.dynamicReports.charts

import org.htmlunit.*
import org.htmlunit.html.parser.HTMLParserListener
import org.htmlunit.html.HtmlDivision
import org.htmlunit.html.HtmlPage
import org.htmlunit.util.NameValuePair
import com.rxlogix.ChartOptionsUtils
import com.rxlogix.htmlunit.RxJavaScriptErrorListener
import com.rxlogix.json.JsonOutput
import grails.util.Holders
import groovy.text.SimpleTemplateEngine
import groovy.text.TemplateEngine
import groovy.util.logging.Slf4j
import net.sf.jasperreports.engine.JRDefaultStyleProvider
import net.sf.jasperreports.engine.JRGenericPrintElement
import net.sf.jasperreports.engine.base.JRBasePrintImage
import net.sf.jasperreports.engine.type.OnErrorTypeEnum
import net.sf.jasperreports.renderers.util.RendererUtil
import org.apache.commons.io.IOUtils
import org.springframework.http.HttpStatus

abstract class ImageChartGenerator {
    private static final int DEFAULT_BACKGROUND_JS_TIMEOUT = 30000


    private static final int CHART_WIDTH = 1200
    private static final int CHART_HEIGHT = 640
    private static final String CHART_TEMPLATE_PATH = "http://localhost/templates/PdfChartHandlerTemplate.html"
    private static final String WORLDMAP_TEMPLATE_PATH = "http://localhost/templates/PdfWorldMapHandlerTemplate.html"

    private static TemplateEngine templateEngine
    private static int backgroundJsTimeout

    static {
        templateEngine = new SimpleTemplateEngine()
        backgroundJsTimeout = Holders.applicationContext.getBean("grailsApplication").config.pvreports.pdf.background.js.timeout ?: DEFAULT_BACKGROUND_JS_TIMEOUT
    }

    static JRBasePrintImage  getChartImage(JRDefaultStyleProvider defaultStyleProvider, JRGenericPrintElement element, Integer rows=null, Integer cols=null) {
        WebClient webClient = new WebClient(BrowserVersion.BEST_SUPPORTED)
        webClient.getOptions().setThrowExceptionOnScriptError(false)
        webClient.setWebConnection(new ResourceWebConnection(webClient))
        def chartData = ((ChartGenerator) element.getParameterValue(ChartGenerator.PARAMETER_CHART_GENERATOR)).generateChart(true)
        boolean worldMap = false
        if (chartData.chart?.map == "custom/world") {
            worldMap = true
            ((Map) chartData).remove("plotOptions")
            ((Map) chartData).remove("legend")
            ((Map) chartData).remove("subtitle")
            ((Map) chartData).remove("xAxis")
            ((Map) chartData).remove("yAxis")
            ((Map) chartData).series.remove(((Map) chartData).series.findIndexOf {it.isPercentageColumn!=null})
        } else {
            chartData.plotOptions.series.animation = false
        }
        chartData.chart.width = cols && cols > 2 ? (674 + (cols - 2) * 81) : CHART_WIDTH
        chartData.chart.height = rows && rows > 2 ? (545 + (rows - 1) * 20) : CHART_HEIGHT
        String latestComment = ((ChartGenerator) element.getParameterValue(ChartGenerator.PARAMETER_CHART_GENERATOR)).getLatestComment() ?: ""
        try {
            WebRequest request = new WebRequest(new URL(worldMap?WORLDMAP_TEMPLATE_PATH:CHART_TEMPLATE_PATH))
            request.requestParameters = [new NameValuePair("chartData", ChartOptionsUtils.serializeToHtml(chartData)), new NameValuePair("latestComment", JsonOutput.toJson(latestComment))]
            final HtmlPage page = webClient.getPage(request);
            webClient.waitForBackgroundJavaScript(backgroundJsTimeout);
            webClient.setJavaScriptErrorListener(new RxJavaScriptErrorListener())
            webClient.setHTMLParserListener(HTMLParserListener.LOG_REPORTER);
            final HtmlDivision div = page.getHtmlElementById("container")
            String svg = div.getFirstChild().getFirstChild().asXml()
            JRBasePrintImage image = new JRBasePrintImage(defaultStyleProvider)
            image.renderer = RendererUtil.getInstance().getRenderable(new ByteArrayInputStream(svg.getBytes("UTF8")), OnErrorTypeEnum.ERROR)
            image.x = element.x
            image.y = element.y
            image.width = element.width
            image.height = element.height
            return image
        } finally {
            webClient.close()
        }
    }
    @Slf4j
    private static class ResourceWebConnection extends HttpWebConnection {

        ResourceWebConnection(WebClient webClient) {
            super(webClient)
        }

        @Override
        WebResponse getResponse(WebRequest webRequest) throws IOException {
            URL url = webRequest.url
            long startTime = System.currentTimeMillis()
            if ("localhost".equals(url.host)) {
                WebResponseData data = new WebResponseData(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.name(), [])
                byte[] content
                if (url.path.startsWith("/templates")) {
                    def reader
                    if (url.path.contains("PdfWorldMapHandlerTemplate"))
                        reader = new InputStreamReader(new ByteArrayInputStream(worldmap_html.getBytes("UTF8")))
                    else
                        reader = new InputStreamReader(getClass().getResourceAsStream("/com/rxlogix/dynamicReports/rc" + url.path))
                    def template = templateEngine.createTemplate(reader)
                    def writable = template.make(webRequest.requestParameters.collectEntries{[(it.name): it.value]})
                   content = writable.toString().getBytes("UTF8")
                } else {
                    // Trim the leading slash
                    def path = url.path.startsWith("/") ? url.path.substring(1) : url.path
                    ByteArrayOutputStream baos = new ByteArrayOutputStream()
                    IOUtils.copy(Holders.applicationContext.getBean("assetResourceLocator").findAssetForURI(path)?.inputStream, baos)
                    content = baos.toByteArray()
                }
                if (content != null) {
                    data = new WebResponseData(content, HttpStatus.OK.value(), HttpStatus.OK.name(), [])
                }
                long loadTime = System.currentTimeMillis() - startTime
                log.debug(webRequest.url.toString() + " is loaded in " + loadTime + " ms. and status " + data?.statusCode)
                return new WebResponse(data, webRequest, loadTime)
            } else {
                return super.getResponse(webRequest)
            }
        }

        @Override
        void close() throws Exception {
        }
    }

    static final String worldmap_html ="""<html>
<head>
    <script src="http://localhost/vendorUi/jquery/jquery-3.7.1.min.js"></script>
    <script src="http://localhost/vendorUi/highcharts/highcharts-10.3.3/highstock.js"></script>
    <script src="http://localhost/vendorUi/highcharts/highcharts-10.3.3/highcharts-more.js"></script>
    <script src="http://localhost/vendorUi/highcharts/highcharts-10.3.3/highcharts-3d.js"></script>
    <script src="http://localhost/vendorUi/highcharts/modules/no-data-to-display.js"></script>
    <script src="http://localhost/vendorUi/highcharts/modules/map.js"></script>
    <script src="http://localhost/vendorUi/highcharts/modules/world.js"></script>
    <script src="http://localhost/vendorUi/highcharts/modules/annotations.js"></script>
    <script src="http://localhost/vendorUi/highcharts/highcharts-10.3.3/solid-gauge.js"></script>
    <script src="http://localhost/vendorUi/highcharts/plugins/grouped-categories-1.3.2.js"></script>
</head>

<body>
<div id="container" style="width:100%; height:100%;"></div>
    <script>

    jQuery(function() {
        var data = \${chartData};
       
        jQuery('#container').highcharts('Map',data);
    });
    </script>
</body>

    </html>
"""
}
