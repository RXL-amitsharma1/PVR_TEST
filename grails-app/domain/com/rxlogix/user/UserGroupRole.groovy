package com.rxlogix.user

import org.apache.commons.lang.builder.HashCodeBuilder

class UserGroupRole implements Serializable {

	private static final long serialVersionUID = 1

	UserGroup userGroup
	Role role

	boolean equals(other) {
		if (!(other instanceof UserGroupRole)) {
			return false
		}

		other.userGroup?.id == userGroup?.id &&
			other.role?.id == role?.id
	}

	int hashCode() {
		def builder = new HashCodeBuilder()
		if (userGroup) builder.append(userGroup.id)
		if (role) builder.append(role.id)
		builder.toHashCode()
	}

	static UserGroupRole get(long userGroupId, long roleId) {
		UserGroupRole.where {
			userGroup == UserGroup.load(userGroupId) &&
			role == Role.load(roleId)
		}.get()
	}

	static boolean exists(long userGroupId, long roleId) {
		UserGroupRole.where {
			userGroup == UserGroup.load(userGroupId) &&
			role == Role.load(roleId)
		}.count() > 0
	}

	static UserGroupRole create(UserGroup userGroup, Role role, boolean flush = false) {
		def instance = new UserGroupRole(userGroup: userGroup, role: role)
		instance.save(flush: flush, insert: true)
		instance
	}

	static boolean remove(UserGroup u, Role r, boolean flush = false) {
		if (u == null || r == null) return false

		int rowCount = UserGroupRole.where {
			userGroup == UserGroup.load(u.id) &&
			role == Role.load(r.id)
		}.deleteAll()

		if (flush) { UserGroupRole.withSession { it.flush() } }

		rowCount > 0
	}

	static void removeAll(UserGroup u, boolean flush = false) {
		if (u == null) return

		UserGroupRole.where {
			userGroup == UserGroup.load(u.id)
		}.deleteAll()

		if (flush) { UserGroupRole.withSession { it.flush() } }
	}

	static void removeAll(Role r, boolean flush = false) {
		if (r == null) return

		UserGroupRole.where {
			role == Role.load(r.id)
		}.deleteAll()

		if (flush) { UserGroupRole.withSession { it.flush() } }
	}

	static constraints = {
		role validator: { Role r, UserGroupRole ur ->
			if (ur.userGroup == null) return
			boolean existing = false
			UserGroupRole.withNewSession {
				existing = UserGroupRole.exists(ur.userGroup.id, r.id)
			}
			if (existing) {
				return 'userGroupRole.exists'
			}
		}
	}

	static mapping = {
		id composite: ['role', 'userGroup']
		version false
		table name: "PVUSERGROUPS_ROLES"
		userGroup cascade: 'none'
		role cascade: 'none'
	}

	public String toString() {
		return "$userGroup - $role"
	}
}
