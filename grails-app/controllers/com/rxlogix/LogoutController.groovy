/* Copyright 2013-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rxlogix

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.util.Holders
import org.springframework.security.access.annotation.Secured
import org.springframework.security.web.RedirectStrategy

import javax.servlet.http.HttpServletResponse

@Secured('permitAll')
class LogoutController {

	/** Dependency injection for RedirectStrategy. */
	RedirectStrategy redirectStrategy

	/**
	 * Index action. Redirects to the Spring security logout uri.
	 */
	def index() {

		if (!request.post && SpringSecurityUtils.getSecurityConfig().logout.postOnly) {
			response.sendError HttpServletResponse.SC_METHOD_NOT_ALLOWED // 405
			return
		}

		// TODO put any pre-logout code here
		redirectStrategy.sendRedirect request, response, SpringSecurityUtils.securityConfig.logout.filterProcessesUrl // '/j_spring_security_logout'
		response.flushBuffer()
	}

	//Added to have landing page if user tries to logout even after already logged out.
	def saml2() {
		forward(action: 'local')
	}

	def local() {
		if (!Holders.config.getProperty('grails.plugin.springsecurity.saml.active', Boolean)) {
			redirect(url: '/')
			return
		}
		render(view: 'local')
	}
}
