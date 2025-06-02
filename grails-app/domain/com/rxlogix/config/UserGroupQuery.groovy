package com.rxlogix.config

import com.rxlogix.user.UserGroup
import org.apache.commons.lang.builder.HashCodeBuilder

class UserGroupQuery implements Serializable {

    private static final long serialVersionUID = 1

    UserGroup userGroup

    boolean equals(other) {
        if (!(other instanceof UserGroupQuery)) {
            return false
        }

        other.userGroup?.id == userGroup?.id &&
                other.query?.id == query?.id
    }

    int hashCode() {
        def builder = new HashCodeBuilder()
        if (userGroup) builder.append(userGroup.id)
        if (query) builder.append(query.id)
        builder.toHashCode()
    }

    static UserGroupQuery get(long userGroupId, long queryId) {
        UserGroupQuery.where {
            userGroup == UserGroup.load(userGroupId) &&
                    query == SuperQuery.load(queryId)
        }.get()
    }

    static boolean exists(long userGroupId, long queryId) {
        UserGroupQuery.where {
            userGroup == UserGroup.load(userGroupId) &&
                    query == SuperQuery.load(queryId)
        }.count() > 0
    }

    static UserGroupQuery create(UserGroup userGroup, SuperQuery query, boolean flush = false) {
        def instance = new UserGroupQuery(userGroup: userGroup, query: query)
        instance.save(flush: flush, insert: true)
        instance
    }

    static boolean remove(UserGroup u, SuperQuery r, boolean flush = false) {
        if (u == null || r == null) return false

        int rowCount = UserGroupQuery.where {
            userGroup == UserGroup.load(u.id) &&
                    query == SuperQuery.load(r.id)
        }.deleteAll()

        if (flush) {
            UserGroupQuery.withSession { it.flush() }
        }

        rowCount > 0
    }

    static void removeAll(UserGroup u, boolean flush = false) {
        if (u == null) return

        UserGroupQuery.where {
            userGroup == UserGroup.load(u.id)
        }.deleteAll()

        if (flush) {
            UserGroupQuery.withSession { it.flush() }
        }
    }

    static void removeAll(SuperQuery r, boolean flush = false) {
        if (r == null) return

        UserGroupQuery.where {
            query == SuperQuery.load(r.id)
        }.deleteAll()

        if (flush) {
            UserGroupQuery.withSession { it.flush() }
        }
    }

    static belongsTo = [query: SuperQuery]

    static constraints = {
        query validator: { SuperQuery r, UserGroupQuery ugt ->
            if (ugt.userGroup == null) return
            boolean existing = false
            UserGroupQuery.withNewSession {
                existing = UserGroupQuery.exists(ugt.userGroup.id, r.id)
            }
            if (existing) {
                return ["userGroupQuery.exists", "${ugt.userGroup.name}"]
            }
        }
    }

    static mapping = {
        id composite: ['query', 'userGroup']
        version false
        table name: "SUPER_QUERY_USER_GROUP"
        userGroup cascade: 'none'
        query cascade: 'none'
    }

    public String toString() {
        return userGroup.name
    }
}
