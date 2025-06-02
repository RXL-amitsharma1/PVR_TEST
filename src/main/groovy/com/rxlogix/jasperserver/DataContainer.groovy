package com.rxlogix.jasperserver

public interface DataContainer extends Serializable {

    boolean hasData()

    OutputStream getOutputStream()

    int dataSize()

    InputStream getInputStream()

    byte[] getData()

    void dispose()
}
