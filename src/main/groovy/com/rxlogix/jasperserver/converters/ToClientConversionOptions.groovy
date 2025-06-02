package com.rxlogix.jasperserver.converters

public class ToClientConversionOptions {
    private boolean expanded
    private List<String> includes

    public static ToClientConversionOptions getDefault(){
        return new ToClientConversionOptions()
    }

    public boolean isExpanded() {
        return expanded
    }

    public ToClientConversionOptions setExpanded(boolean expanded) {
        this.expanded = expanded
        return this
    }

    public List<String> getIncludes() {
        return includes
    }

    public ToClientConversionOptions setIncludes(List<String> includes) {
        this.includes = includes
        return this
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true
        if (o == null || getClass() != o.getClass()) return false

        ToClientConversionOptions options = (ToClientConversionOptions) o

        if (expanded != options.expanded) return false
        return !(includes != null ? !includes.equals(options.includes) : options.includes != null)

    }

    @Override
    public int hashCode() {
        int result = (expanded ? 1 : 0)
        result = 31 * result + (includes != null ? includes.hashCode() : 0)
        return result
    }

    @Override
    public String toString() {
        return "ToClientConversionOptions{" +
                "expanded=" + expanded +
                ", includes='" + includes + '\'' +
                '}'
    }
}
