package com.rxlogix.jasperserver.converters

import com.rxlogix.jasperserver.exception.IllegalParameterValueException
import com.rxlogix.jasperserver.exception.MandatoryParameterNotFoundException

public interface ToServerConverter<T, ResultType, OptionsType> {
    ResultType toServer(T clientObject, OptionsType options) throws IllegalParameterValueException, MandatoryParameterNotFoundException
    ResultType toServer(T clientObject, ResultType resultToUpdate, OptionsType options) throws IllegalParameterValueException, MandatoryParameterNotFoundException
    String getServerResourceType()
}
