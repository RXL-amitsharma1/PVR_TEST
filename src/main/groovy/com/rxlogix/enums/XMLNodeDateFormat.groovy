package com.rxlogix.enums

enum XMLNodeDateFormat {

    CCYY('yyyy'),
    CCYYMM('yyyyMM'),
    CCYYMMDD('yyyyMMdd'),
    CCYYMMDDhhmm('yyyyMMddhhmm'),
    CCYYMMDDhhmmss('yyyyMMddhhmmss')

    private final String format

    private XMLNodeDateFormat(String format) {
        this.format = format
    }

    String getFormat() {
        return format
    }
}