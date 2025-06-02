package com.rxlogix.enums

import groovy.transform.CompileStatic

@CompileStatic
enum PVGatewayStatusEnum {
    MDN_VALIDATED,
    MDN_NRR,
    FILE_UPLOAD_FAILED,
    MESSAGE_GENERATION_FAILED,
    TRANSMISSION_FAILED,
    PROCESSING_FAILED,
    MDN_NOT_VALIDATED,
    MESSAGE_INVALID,
    MDN_PROCESSING_FAILED,
    MESSAGE_PROCESSED_SIGNATURE_INVALID

    static List<PVGatewayStatusEnum> fetchSuccessfulTransmissionStatus() {
        return [MDN_VALIDATED, MDN_NRR]
    }

    static List<PVGatewayStatusEnum> fetchFailedTransmissionStatus() {
        return [FILE_UPLOAD_FAILED, MESSAGE_GENERATION_FAILED, TRANSMISSION_FAILED, PROCESSING_FAILED, MDN_NOT_VALIDATED, MESSAGE_INVALID, MDN_PROCESSING_FAILED,MESSAGE_PROCESSED_SIGNATURE_INVALID]
    }
}