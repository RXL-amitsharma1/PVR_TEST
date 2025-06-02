package com.rxlogix.reportTemplate.xml

enum ICSRSchema {
    MCCI_IN200100UV01(
            "Sends a Batch, which groups 0 or more Messages for communication purposes’ (ISO/HL7 27953-2).",
            "For ICH ICSR messages, this schema defines root element."
    ),
    MCCI_MT200100UV(
            "The Batch class functions in similar way to the Message class in an individual V3 message’ (ISO/HL7 27953-2).",
            "For ICH ICSR messages, this schema defines all of data elements in N.1 section."
    ),
    PORR_IN049016UV(
            "This schema is corresponding to individual ICSR messages in a Batch message.",
            "For ICH ICSR messages, this schema defines individual reports including initial, follow-up and nullification in a batch message, while HL7 provides separated schemas for each report."
    ),
    MCCI_MT000100UV01(
            "The “HL7 Transmission wrapper” includes information needed by a sending application or message handling service to package and route the V3 \n" +
                    "Composite Message to the designated receiving application(s) and/or message handling service(s)’ (ISO/HL7 27953-2).",
            "For ICSR messages, this schema defines most of data elements in N.2 section."
    ),
    MCAI_MT700201UV01(
            "‘The “Trigger Event Control Act” contains administrative information related to the \"controlled act\" which is being communicated as a messaging interaction’(ISO/HL7 27953-2).",
            "It specifies the intermediate wrapper structure in the HL7 version 3 composite message payload specification that is used for notification and request for action type message interactions. \n" +
                    "For ICH ICSR messages, this schema defines the Date of Creation (C.1.2)."
    )
    private String description
    private String purpose

    private ICSRSchema(String description, String purpose) {
        this.description = description
        this.purpose = purpose
    }

    String getKey() { name() }
}