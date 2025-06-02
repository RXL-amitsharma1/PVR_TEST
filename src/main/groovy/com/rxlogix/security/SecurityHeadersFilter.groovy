package com.rxlogix.security

import grails.config.Config
import grails.core.support.GrailsConfigurationAware
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.security.SecureRandom

@Slf4j
@CompileStatic
class SecurityHeadersFilter implements Filter, GrailsConfigurationAware {

    Map<String, String> additionalSecurityHeaders
    Boolean strictTransportEnabled
    List<String> allowedFrameAncestors
    Set<String> allowedHostnames = [] as Set

    void setConfiguration(Config cfg) {
        additionalSecurityHeaders = cfg.getProperty('pv.app.headers.additional', Map, [:])
        strictTransportEnabled = cfg.getProperty('pvr.strict.transport.security.enabled', Boolean, true)
        allowedFrameAncestors = cfg.getProperty('pvr.allowed.frame.ancestors', List, [])

        // Extract and normalize allowed hostnames
        allowedHostnames = allowedFrameAncestors.findAll { it }  // Filter out null/empty
                .collect { ancestor ->
                    try {
                        return new URL(ancestor).host?.toLowerCase()
                    } catch (MalformedURLException e) {
                        log.warn("Invalid URL in allowedFrameAncestors config: $ancestor. Using raw value.")
                        return ancestor?.toLowerCase()
                    }
                }.findAll { it } // Remove nulls after parsing
                .toSet()
    }

    @Override
    void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        if (request.getClass() != HttpServletRequest.class || response.getClass() != HttpServletResponse.class) {
            chain.doFilter(request, response)
            return
        }

        HttpServletRequest httpRequest = request as HttpServletRequest
        HttpServletResponse httpResponse = response as HttpServletResponse

        // Host header validation (early exit)
        String requestHost = httpRequest.getHeader('Host')?.toLowerCase()?.trim()
        if (requestHost && !allowedHostnames.contains(requestHost)) {
            log.warn("Blocked request with invalid Host header: $requestHost")
            if (!httpResponse.isCommitted()) {
                httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Host header")
            }
            return
        }

        // Continue normal filter processing
        filterHeader(httpRequest, httpResponse, chain)
        chain.doFilter(request, response)
    }

    @Override
    void destroy() {

    }

    protected void filterHeader(final HttpServletRequest request, final HttpServletResponse response, final FilterChain chain) {
        String nonce = generateNonce()
        log.debug("URL for session filter" +request.getRequestURI())

        /**
         * Prevent Cacheable HTTPS Response
         */
        response.setHeader("Cache-Control", "no-cache, no-store")
        response.setHeader("Pragma", "no-cache")
        response.setHeader('X-XSS-Protection', '1; mode=block')
        response.setHeader('X-Content-Type-Options', 'nosniff')
        /*
           By default, Grails sets X-Frame-Options to DENY, preventing content embedding in iframes.
           To allow embedding from any origin and restrict script sources, using Content-Security-Policy (CSP) with frame-ancestors.
           Note: The 'allowFrom = *' directive is ineffective due to the DENY setting, necessitating the use of CSP headers to override Grails properties.
           To customize this configuration, add it to additionalSecurityHeaders, e.g., pv.app.headers.additional.
        */
        response.setHeader("Content-Security-Policy", "frame-ancestors 'self' ${allowedFrameAncestors.join(" ")}; script-src 'self' 'strict-dynamic' 'nonce-${nonce}' data: blob: ;img-src 'self' data:;")
        if (strictTransportEnabled && (request.isSecure() || request.getHeader('X-Forwarded-Proto')?.toLowerCase() == 'https')) {
            log.trace("===========Strict-Transport-Security=============")
            response.setHeader('Strict-Transport-Security', "max-age=31536000;includeSubDomains")
        }
        additionalSecurityHeaders?.each {
            String value = it.value
            String headerName = it.key
            if (headerName.toLowerCase() == 'access-control-allow-origin' && value?.contains(',')) {
                def origin = request.getHeader('Origin')
                if (origin && value.split(",")*.trim().contains(origin)) {
                    value = origin
                }
            }
            response.setHeader(headerName, value)
        }

        // Pass through the filter chain
        CachedHttpResponseWrapper wrappedResponse = new CachedHttpResponseWrapper(response)
        chain.doFilter(request, wrappedResponse)

        //Ensure everything is flushed
        wrappedResponse.getWriter().flush()

        String originalContent = wrappedResponse.getCapturedContent()
        String contentType = wrappedResponse.getContentType()?.toLowerCase()
        log.debug("============ Content Type ===============" +contentType)
        if (originalContent?.trim() && contentType?.contains('text/html')) {
            log.debug("==== Modifying HTML Content ====")
            String modifiedContent = originalContent.replaceAll(
                    ~/<script(.*?)>/,
                    '<script$1 nonce="' + nonce + '">'
            )
//                    .replaceAll(
//                    ~/<button([^>]*?)onclick="([^"]*?)"([^>]*?)>/,
//                    '<button$1 onclick="$2" nonce="' + nonce + '" $3>'
//            );
            response.contentType = contentType ?: "text/html; charset=UTF-8"
            response.writer.write(modifiedContent)
            response.writer.flush()
        } else {
            log.debug("==== Skipping Modification ====")
            //response.contentType = contentType
            response.outputStream.write(wrappedResponse.getOutputStream().getCapturedBytes())
            response.getOutputStream().flush()
        }

    }

    private static String generateNonce() {
        byte[] nonceBytes = new byte[16]
        new SecureRandom().nextBytes(nonceBytes)
        return Base64.getEncoder().encodeToString(nonceBytes)
    }

}