package com.rxlogix.jasperserver

public class SelfCleaningFileResourceDataWrapper extends FileResourceData {
    public SelfCleaningFileResourceDataWrapper(FileResourceData object){
        super(object);
    }

    @Override
    public InputStream getDataStream() {
        return new SelfCleaningStreamWrapper(super.getDataStream());
    }

    private class SelfCleaningStreamWrapper extends InputStream {
        InputStream stream;

        SelfCleaningStreamWrapper (InputStream stream){
            this.stream = stream;
        }

        @Override
        public int read() throws IOException {
            return stream.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            int read = stream.read(b);
            if (read == -1) {
                dispose();
            }
            return read;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int read = stream.read(b, off, len);
            if (read == -1) {
                dispose();
            }
            return read;
        }

        @Override
        public long skip(long n) throws IOException {
            return stream.skip(n);
        }

        @Override
        public int available() throws IOException {
            return stream.available();
        }

        @Override
        public void close() throws IOException {
            stream.close();
        }

        @Override
        public void mark(int readlimit) {
            stream.mark(readlimit);
        }

        @Override
        public void reset() throws IOException {
            stream.reset();
        }

        @Override
        public boolean markSupported() {
            return stream.markSupported();
        }
    }
}
