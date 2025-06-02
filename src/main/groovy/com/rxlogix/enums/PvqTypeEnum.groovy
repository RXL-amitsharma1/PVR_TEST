package com.rxlogix.enums

public enum PvqTypeEnum {
    SAMPLING('Quality Sampling'),
    CASE_QUALITY('Case Quality Monitoring'),
    SUBMISSION_QUALITY('Submission Quality'),
    CASE_CORRECTIONS('Case Corrections')

    private final String val

    PvqTypeEnum(String val) {
        this.val = val
    }

    String value() { return val }

    static List<String> toStringList() {
        [SAMPLING.value(), CASE_QUALITY.value(), SUBMISSION_QUALITY.value(), CASE_CORRECTIONS.value()]
    }
    static List<String> toQualityList(){
        [SAMPLING.name(), CASE_QUALITY.name(), SUBMISSION_QUALITY.name()]
    }
}
