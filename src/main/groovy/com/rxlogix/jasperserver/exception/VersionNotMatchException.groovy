package com.rxlogix.jasperserver.exception

public class VersionNotMatchException extends UpdateConflictException {
    public final String ERROR_VERSION_NOT_MATCH = "version.not.match"

    public VersionNotMatchException() {
        this.getErrorDescriptor().setErrorCode(ERROR_VERSION_NOT_MATCH)
    }
}
