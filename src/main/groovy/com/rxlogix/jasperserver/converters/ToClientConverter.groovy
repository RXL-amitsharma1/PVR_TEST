package com.rxlogix.jasperserver.converters

public interface ToClientConverter<T, ResultType, OptionsType> {
    /**
     * Conversion of server object to client representation.
     * @param serverObject - the server object ot convert
     * @param options - conversion options. Use null for default
     * @return the client representation of a server object
     */
    ResultType toClient(T serverObject, OptionsType options)
    String getClientResourceType()
}
