package com.rxlogix.jasperserver.converters

public class ToServerConversionOptions {
    private boolean allowReferencesOnly, resetVersion, suppressValidation
    private String ownersUri
    private Map<String, InputStream> attachments

    public static ToServerConversionOptions getDefault(){
        return new ToServerConversionOptions()
    }

    public Map<String, InputStream> getAttachments() {
        return attachments
    }

    public ToServerConversionOptions setAttachments(Map<String, InputStream> attachments) {
        this.attachments = attachments
        return this
    }

    public boolean isAllowReferencesOnly() {
        return allowReferencesOnly
    }

    public ToServerConversionOptions setAllowReferencesOnly(boolean allowReferencesOnly) {
        this.allowReferencesOnly = allowReferencesOnly
        return this
    }

    public String getOwnersUri() {
        return ownersUri
    }

    public ToServerConversionOptions setOwnersUri(String ownersUri) {
        this.ownersUri = ownersUri
        return this
    }

    public boolean isResetVersion() {
        return resetVersion
    }

    public ToServerConversionOptions setResetVersion(boolean resetVersion) {
        this.resetVersion = resetVersion
        return this
    }

    public boolean isSuppressValidation() {
        return suppressValidation
    }

    public ToServerConversionOptions setSuppressValidation(boolean suppressValidation) {
        this.suppressValidation = suppressValidation
        return this
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true
        if (!(o instanceof ToServerConversionOptions)) return false

        ToServerConversionOptions that = (ToServerConversionOptions) o

        if (allowReferencesOnly != that.allowReferencesOnly) return false
        if (resetVersion != that.resetVersion) return false
        if (suppressValidation != that.suppressValidation) return false
        if (attachments != null ? !attachments.equals(that.attachments) : that.attachments != null) return false
        if (ownersUri != null ? !ownersUri.equals(that.ownersUri) : that.ownersUri != null) return false

        return true
    }

    @Override
    public int hashCode() {
        int result = (allowReferencesOnly ? 1 : 0)
        result = 31 * result + (resetVersion ? 1 : 0)
        result = 31 * result + (suppressValidation ? 1 : 0)
        result = 31 * result + (ownersUri != null ? ownersUri.hashCode() : 0)
        result = 31 * result + (attachments != null ? attachments.hashCode() : 0)
        return result
    }

    @Override
    public String toString() {
        return "ToServerConversionOptions{" +
                "allowReferencesOnly=" + allowReferencesOnly +
                ", resetVersion=" + resetVersion +
                ", suppressValidation=" + suppressValidation +
                ", ownersUri='" + ownersUri + '\'' +
                ", attachments=" + attachments +
                '}'
    }
}
