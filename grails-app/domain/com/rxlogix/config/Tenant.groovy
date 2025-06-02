package com.rxlogix.config

class Tenant implements Serializable{
    Long id
    String name
    boolean active
    //Standard fields
    Date dateCreated = new Date()
    Date lastUpdated = new Date()

    static constraints = {

    }

    static mapping = {
        table 'TENANT'
        id generator: 'assigned'
        name column: 'NAME'
        active column: 'IS_ACTIVE'
    }

    @Override
    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        Tenant that = (Tenant) o

        if (id != that.id) return false
        if (name != that.name) return false
        if (active != that.active) return false
        return true
    }

    @Override
    int hashCode() {
        int result
        result = (id ? id.hashCode() : 0)
        result = 31 * result + (name != null ? name.hashCode() : 0)
        result = 31 * result + (active ? 1 : 0)

        return result
    }

    @Override
    public String toString() {
        return name
    }

    public static void copyObj(Tenant sourceObj, Tenant targetObj) {
        targetObj.with {
            name = sourceObj.name
            active = sourceObj.active
        }
    }

    public String getDisplayName() {
        return name
    }
}
