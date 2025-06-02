package com.rxlogix.security

import javax.servlet.ServletOutputStream
import javax.servlet.WriteListener
import java.nio.charset.StandardCharsets

class ServletOutputStreamWrapper extends ServletOutputStream {

    private final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    private final PrintWriter printWriter = new PrintWriter(new OutputStreamWriter(byteStream, StandardCharsets.UTF_8));
    @Override
    public void write(int b) throws IOException {
        byteStream.write(b);
    }

    public byte[] getCapturedBytes() {
        try{
            printWriter.flush(); //Ensured all buffered data is written
            byteStream.flush();
        } catch (IOException e){
            e.printStackTrace()
        }
        return byteStream.toByteArray()
    }

    public PrintWriter getWriter(){
        return printWriter;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    void setWriteListener(WriteListener writeListener) {

    }
}
