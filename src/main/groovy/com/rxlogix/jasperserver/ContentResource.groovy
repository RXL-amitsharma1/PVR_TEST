package com.rxlogix.jasperserver

public class ContentResource extends Resource {
    public static final String TYPE_PDF = "pdf"
    public static final String TYPE_HTML = "html"
    public static final String TYPE_XLS = "xls"
    public static final String TYPE_RTF = "rtf"
    public static final String TYPE_CSV = "csv"
    public static final String TYPE_ODT = "odt"
    public static final String TYPE_TXT = "txt"
    public static final String TYPE_DOCX = "docx"
    public static final String TYPE_ODS = "ods"
    public static final String TYPE_XLSX = "xlsx"
    public static final String TYPE_IMAGE = "img"//unspecified image type
    public static final String TYPE_PPTX = "pptx"
    public static final String TYPE_JSON = "json"
    public static final String TYPE_UNSPECIFIED = "contentResource"
    private static final int READ_STREAM_BUFFER_SIZE = 10000

    private String fileType
    private DataContainer dataContainer
    private String referenceURI
    private List resources

    public ContentResource() {
        super()
        resources = new ArrayList()
    }

    public byte[] getData() {
        return dataContainer == null ? null : dataContainer.getData()
    }

    public void setData(byte[] data) {
        setDataContainer(new MemoryDataContainer(data))
    }

    public int getSize() {
        return dataContainer == null || !hasData() ? 0 : dataContainer.dataSize()
    }

    public void setDataContainer(DataContainer dataContainer) {
        this.dataContainer = dataContainer
    }

    public void readData(InputStream is) {
        if (is == null) {
            return
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream()

        byte[] bytes = new byte[READ_STREAM_BUFFER_SIZE]
        int ln = 0
        try {
            while ((ln = is.read(bytes)) > 0) {
                baos.write(bytes, 0, ln)
            }
        } catch (IOException e) {
            throw new RuntimeException(e)
        }
        setData(baos.toByteArray())
    }

    public InputStream getDataStream() {
        return dataContainer == null ? null : dataContainer.getInputStream()
    }

    public String getFileType() {
        return fileType
    }

    public void setFileType(String fileType) {
        this.fileType = fileType
    }

    public boolean isReference() {
        return referenceURI != null && referenceURI.length() > 0
    }

    public String getReferenceURI() {
        return referenceURI
    }

    public void setReferenceURI(String referenceURI) {
        this.referenceURI = referenceURI
    }

    public boolean hasData() {
        return !isReference() && dataContainer != null && dataContainer.hasData()
    }

    public List getResources() {
        return resources
    }

    public void setResources(List resources) {
        this.resources = resources
    }

    public void addChildResource(ContentResource child) {
        resources.add(child)
    }

    @Override
    protected Class getClientItf() {
        return ContentResource.class
    }

    public FileResourceData copyData() {
        //if (isFileReference()) {
        //    String quotedResourceURI = "\"" + getResourceURI() + "\"";
        //throw new JSException("jsexception.file.resource.is.reference", new Object[] { quotedResourceURI });
        //}
        return new FileResourceData(getData())
    }
}
