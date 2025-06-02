package com.rxlogix

import com.rxlogix.config.SuperQuery
import com.rxlogix.user.User
import org.apache.commons.lang.builder.HashCodeBuilder

class UserQuery implements Serializable {

    private static final long serialVersionUID = 1

    User user

    boolean equals(other) {
        if (!(other instanceof UserQuery)) {
            return false
        }

        other.user?.id == user?.id &&
                other.query?.id == query?.id
    }

    int hashCode() {
        def builder = new HashCodeBuilder()
        if (user) builder.append(user.id)
        if (query) builder.append(query.id)
        builder.toHashCode()
    }

    static UserQuery get(long userId, long queryId) {
        UserQuery.where {
            user == User.load(userId) &&
                    query == SuperQuery.load(queryId)
        }.get()
    }

    static boolean exists(long userId, long queryId) {
        UserQuery.where {
            user == User.load(userId) &&
                    query == SuperQuery.load(queryId)
        }.count() > 0
    }

    static UserQuery create(User user, SuperQuery query, boolean flush = false) {
        def instance = new UserQuery(user: user, query: query)
        instance.save(flush: flush, insert: true)
        instance
    }

    static boolean remove(User u, SuperQuery r, boolean flush = false) {
        if (u == null || r == null) return false

        int rowCount = UserQuery.where {
            user == User.load(u.id) &&
                    query == SuperQuery.load(r.id)
        }.deleteAll()

        if (flush) {
            UserQuery.withSession { it.flush() }
        }

        rowCount > 0
    }

    static void removeAll(User u, boolean flush = false) {
        if (u == null) return

        UserQuery.where {
            user == User.load(u.id)
        }.deleteAll()

        if (flush) {
            UserQuery.withSession { it.flush() }
        }
    }

    static void removeAll(SuperQuery r, boolean flush = false) {
        if (r == null) return

        UserQuery.where {
            query == SuperQuery.load(r.id)
        }.deleteAll()

        if (flush) {
            UserQuery.withSession { it.flush() }
        }
    }

    static belongsTo = [query: SuperQuery]

    static constraints = {
        query validator: { SuperQuery r, UserQuery ut ->
            if (ut.user == null) return
            boolean existing = false
            UserQuery.withNewSession {
                existing = UserQuery.exists(ut.user.id, r.id)
            }
            if (existing) {
                return ["userQuery.exists","${ut.user.fullNameAndUserName}"]
            }
        }
    }

    static mapping = {
        id composite: ['query', 'user']
        version false
        table name: "SUPER_QUERY_USER"
        user cascade: 'none'
        query cascade: 'none'
    }

    public String toString() {
        return user.getFullNameAndUserName()
    }
}
