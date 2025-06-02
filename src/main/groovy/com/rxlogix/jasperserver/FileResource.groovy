package com.rxlogix.jasperserver

public class FileResource extends Resource {

    static String TYPE_IMAGE = "img"
    static String TYPE_FONT = "font"
    static String TYPE_JRXML = "jrxml"
    static String TYPE_JAR = "jar"
    static String TYPE_RESOURCE_BUNDLE = "prop"
    static String TYPE_STYLE_TEMPLATE = "jrtx"
    static String TYPE_XML = "xml"
    static String TYPE_JSON = "json"
    static String TYPE_CSS = "css"
    static String TYPE_ACCESS_GRANT_SCHEMA = "accessGrantSchema"
    static String TYPE_MONGODB_JDBC_CONFIG = "config"
    static String TYPE_AZURE_CERTIFICATE = "cer"
    static String TYPE_SECURE_FILE = "secureFile"
    static String TYPE_DASHBOARD_COMPONENTS_SCHEMA = "dashboardComponent"
    static String TYPE_CSV = "csv"

    private static final Set<String> XML_BASED_FORMATS = new HashSet<String>([
            TYPE_XML,
            TYPE_JRXML,
            TYPE_STYLE_TEMPLATE,
            "olapMondrianSchema",
            TYPE_ACCESS_GRANT_SCHEMA])


    private String fileType
    private byte[] data
    private FileResource reference

    public FileResource() {
        super()
    }

    public byte[] getData() {
        return data
    }

    public void setData(byte[] data) {
        this.data = data
    }

    public void readData(InputStream is) {
        setData(is.getBytes())
    }

    public boolean hasData() {
        //empty array is considered no data
        return !isReference() && data != null && data.length > 0
    }

    public String getFileType() {
        return fileType
    }

    public void setFileType(String type) {
        this.fileType = type
    }

    public String getFinalFileType() {
        FileResource res = this
        while (res.isFileReference()) {
            res = res.getReference()
        }
        return res.getFileType()
    }

    public boolean isFileReference() {
        return getReference() != null
    }

    public FileResource getReference() {
        return reference
    }

    public void setReference(FileResource reference) {
        this.reference = reference
    }

    public String getReferenceURI() {
        return reference?.getURIString()
    }

    public void setReferenceURI(String referenceURI) {
        if (referenceURI) {
            this.reference = new ResourceReference(referenceURI)
        } else {
            this.reference = null
        }
    }

    protected Class getClientItf() {
        return FileResource.class
    }

    public boolean isReference() {
        return reference != null
    }

/*
    protected void copyTo(Resource clientRes, ResourceFactory resourceFactory) {
        super.copyTo(clientRes, resourceFactory)

        FileResource fileRes = (FileResource) clientRes

        if (isFileReference()) {
            FileResource ref = getReference()
            fileRes.setFileType(null)
            fileRes.setData(null)
            fileRes.setReferenceURI(ref.getResourceURI())
        } else {
            fileRes.setFileType(getFileType())
            if (hasClientOption(CLIENT_OPTION_FULL_DATA)) {
                copyDataTo(fileRes)
            } else {
                fileRes.setData(null)
            }
            fileRes.setReferenceURI(null)
        }
    }

    protected void copyDataTo(FileResource fileRes) {
        Blob blob = getData()
        if (blob == null) {
            fileRes.setData(null)
        } else {
            try {
                // check for XXE vulnerability
                if (XML_BASED_FORMATS.contains(fileRes.getFileType())) {
                    fileRes.readData(XMLUtil.checkForXXE(blob.getBinaryStream()))
                } else {
                    fileRes.readData(blob.getBinaryStream())
                }
            } catch (SQLException e) {
                log.error("Error while reading data blob of \"" + getResourceURI() + "\"", e)
                throw new JSExceptionWrapper(e)
            } catch (SAXParseException ex) {
                log.error("Insecure XML resource!", ex)
            } catch (Exception ex) {
                log.error(ex)
            }
        }
    }
*/

    public FileResourceData copyData() {
        //if (isFileReference()) {
        //    String quotedResourceURI = "\"" + getResourceURI() + "\""
        //throw new JSException("jsexception.file.resource.is.reference", new Object[] { quotedResourceURI })
        //}
        return new FileResourceData(getData())
    }
/*
    protected void copyDataFrom(FileResource dataRes) {
        if (dataRes.isReference()) {
            setData(null)
        } else {
            //only update when the client has set some data
            if (dataRes.hasData()) {
                byte[] clientData = dataRes.getData()
                // check for XXE vulnerability
                try {
                    if (XML_BASED_FORMATS.contains(dataRes.getFileType())) {
                        XMLUtil.checkForXXE(clientData)
                    }
                } catch (Exception e) {
                    log.error(e)
                    throw new JSException(e)
                }
                ComparableBlob blob = new ComparableBlob(clientData)
                setData(blob)
            }
        }
    }

    protected void copyFrom(Resource clientRes,
                            ReferenceResolver referenceResolver) {
        super.copyFrom(clientRes, referenceResolver)

        FileResource dataRes = (FileResource) clientRes
        if (dataRes.isReference()) {
            setFileType(null)
            setData(null)

            FileResource externalReference = (FileResource) referenceResolver
                    .getExternalReference(dataRes.getReferenceURI(), FileResource.class)

            if (dataRes.getFileType() != null
                    && !dataRes.getFileType().equals(externalReference.getFinalFileType())) {
                throw new JSException("jsexception.file.resource.no.match.type",
                        new Object[] { dataRes.getFileType(), externalReference.getFileType() }
                )
            }
            setReference(externalReference)
        } else {
            setFileType(dataRes.getFileType())
            copyDataFrom(dataRes)
            setReference(null)
        }
    }

    protected Class getImplementingItf() {
        return FileResourceBase.class
    }
    */
}
