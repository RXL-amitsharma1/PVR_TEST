//package com.rxlogix.security
//
//import org.springframework.security.core.session.SessionInformation
//import org.springframework.security.core.session.SessionRegistry
//
//import javax.servlet.FilterChain
//import javax.servlet.ServletException
//import javax.servlet.http.HttpServletRequest
//import javax.servlet.http.HttpServletResponse
//import javax.servlet.http.HttpSession
//import org.springframework.web.filter.OncePerRequestFilter
//
//class SessionHijackingFilter extends OncePerRequestFilter {
//
//    private final SessionRegistry sessionRegistry;
//
//    SessionHijackingFilter(SessionRegistry sessionRegistry) {
//        this.sessionRegistry = sessionRegistry
//    }
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//            throws ServletException, IOException {
//        HttpSession session = null
//        try {
//            // Get session, avoid creating a new session if none exists
//            session = request.getSession(false)
//
//            if (session && isLoggedIn(session)) {
//                // Extract stored session attributes
//                String originalIp = session.getAttribute("IP") as String
//                String originalUserAgent = session.getAttribute("USER_AGENT") as String
//
//                // Get current IP and User-Agent
//                String currentIp = getClientIpAddress(request)
//                String currentUserAgent = request.getHeader("User-Agent")
//
//                // Validate the stored IP address
//                if (originalIp && originalIp != currentIp) {
//                    println "Session hijacking detected for sessionId ${session.id}: IP mismatch. Original IP: ${originalIp}, Current IP: ${currentIp}"
//                    SessionInformation info = this.sessionRegistry.getSessionInformation(session.getId());
//                    info.expireNow()
//                    throw new SecurityException("Session hijacking detected: IP mismatch.")
//                }
//
//                // Validate the stored User-Agent
//                if (originalUserAgent && originalUserAgent != currentUserAgent) {
//                    println "Session hijacking detected for sessionId ${session.id}: User-Agent mismatch. Original User-Agent: ${originalUserAgent}, Current User-Agent: ${currentUserAgent}"
//                    SessionInformation info = this.sessionRegistry.getSessionInformation(session.getId());
//                    info.expireNow()
//                    throw new SecurityException("Session hijacking detected: User-Agent mismatch.")
//                }
//
//                // Store IP and User-Agent if not already set
//                if (!originalIp) {
//                    println "Storing new IP for sessionId ${session.id}: ${currentIp}"
//                    session.setAttribute("IP", currentIp)
//                }
//                if (!originalUserAgent) {
//                    println "Storing new User-Agent for sessionId ${session.id}: ${currentUserAgent}"
//                    session.setAttribute("USER_AGENT", currentUserAgent)
//                }
//            }
//        } catch (SecurityException e) {
//            println "Security exception occurred: ${e.message}"
//            response.status = HttpServletResponse.SC_FORBIDDEN // Set status to 403
//            response.contentType = "application/json" // Set content type to JSON
//            response.writer.write('{"error": "Session hijacking detected."}') // Write JSON response
//            response.writer.flush() // Ensure response is sent
//            return // End processing
//        } catch (Exception e) {
//            println "Unexpected error during session validation: ${e.message}"
//            response.status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR // Set status to 500
//            response.contentType = "application/json" // Set content type to JSON
//            response.writer.write('{"error": "An error occurred while validating the session."}') // Write JSON response
//            response.writer.flush() // Ensure response is sent
//            return // End processing
//        }
//
//        // Continue the filter chain
//        filterChain.doFilter(request, response)
//    }
//
//    /**
//     * Checks if the user is logged in by verifying a marker in the session.
//     *
//     * @param session The HTTP session
//     * @return true if the user is logged in; false otherwise
//     */
//    private boolean isLoggedIn(HttpSession session) {
//        try {
//            // Retrieve the security context
//            Object securityContext = session.getAttribute(org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY)
//
//            if (securityContext instanceof org.springframework.security.core.context.SecurityContext) {
//                // Check if there is an authenticated principal
//                org.springframework.security.core.context.SecurityContext context = (org.springframework.security.core.context.SecurityContext) securityContext
//                return context.getAuthentication() != null && context.getAuthentication().isAuthenticated()
//            }
//        } catch (Exception e) {
//            // Handle unexpected issues gracefully
//            println "Error checking logged-in status: ${e.message}"
//        }
//        return false
//    }
//
//    /**
//     * Safely retrieves the real client IP address, considering the X-Forwarded-For header.
//     * Falls back to getRemoteAddr() if the header is missing or invalid.
//     *
//     * @param request The HTTP request
//     * @return The client IP address or null if unavailable
//     */
//    private String getClientIpAddress(HttpServletRequest request) {
//        try {
//            String xForwardedFor = request.getHeader("X-Forwarded-For")
//            if (xForwardedFor) {
//                // The first IP in X-Forwarded-For is the client's IP
//                String[] ipList = xForwardedFor.split(",")
//                return ipList[0]?.trim()
//            }
//            // Fallback to getRemoteAddr if X-Forwarded-For is not present
//            return request.getRemoteAddr()
//        } catch (Exception e) {
//            println "Failed to retrieve client IP address: ${e.message}"
//            return null
//        }
//    }
//}
