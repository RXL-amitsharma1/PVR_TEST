package com.rxlogix.enums

enum IcsrAttachmentExtEnum {

    XLS("xls"),
    XLSX("xlsx"),
    DOC("doc"),
    DOCX("docx"),
    TXT("txt"),
    VSD("vsd"),
    TIF("tif"),
    TIFF("tiff"),
    RTF("rtf"),
    PSD("psd"),
    PS("ps"),
    PNG("png"),
    PDF("pdf"),
    MDB("mdb"),
    JPG("jpg"),
    BMP("bmp"),
    XML("xml"),
    SGML("sgml"),
    SGM("sgm"),
    MSG("msg"),
    GIF("gif"),
    JPEG("jpeg"),
    WPD("wpd"),
    DICOM("dicom"),
    DCM("dcm"),
    EML("eml"),
    HTML("html"),
    CSV("csv")

    final String value

    IcsrAttachmentExtEnum(String value){
        this.value = value
    }

    //Used to get to key for dropdown lists
    String getKey() {
        name()
    }

}
