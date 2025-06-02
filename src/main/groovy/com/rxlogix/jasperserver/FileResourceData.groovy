package com.rxlogix.jasperserver

public class FileResourceData {
    private final DataContainer dataContainer;

    protected FileResourceData(FileResourceData parent) {
        this.dataContainer = parent.dataContainer;
    }

    /**
     * Creates a new FileResourceData object from byte array data.
     *
     * @param data data byte array
     * @return FileResourceData object
     */
    public FileResourceData(byte[] data) {
        this.dataContainer = new MemoryDataContainer(data);
    }

    /**
     * Creates a new FileResourceData object from the input stream.
     *
     * @param is data input stream
     * @return FileResourceData object
     */
    public FileResourceData(InputStream is) {
        //this.dataContainer = new FileBufferedDataContainer();
        //DataContainerStreamUtil.pipeData(is, this.dataContainer);
    }

    public FileResourceData(DataContainer dataContainer) {
        this.dataContainer = dataContainer;
    }

    /**
     * Returns <code>true</code> if the container has some data.
     *
     * @return <code>true</code> if the container has some data.
     */
    public boolean hasData() {
        return dataContainer.hasData();
    }

    /**
     * Returns size of data contained in this file resource
     *
     * @return data size
     */
    public int dataSize() {
        return dataContainer.dataSize();
    }

    /**
     * Returns the data from the data container of resource as a byte array
     *
     * @return data
     */
    public byte[] getData() {
        return dataContainer.getData();
    }

    /**
     * Returns the data stream from the data container of resource
     *
     * @return data stream of the resource
     */
    public InputStream getDataStream() {
        return dataContainer.getInputStream();
    }

    public void dispose() {
        dataContainer.dispose();
    }
}
