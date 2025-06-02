package com.rxlogix

import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder


@CompileStatic
@Transactional(readOnly = true)
class CustomMessageService {
    MessageSource messageSource

    String getMessage(String code, Object... args = null) {
        messageSource.getMessage(code,  args, '', LocaleContextHolder.getLocale())
    }

    String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
        messageSource.getMessage(code,  args, defaultMessage, locale)
    }
}
