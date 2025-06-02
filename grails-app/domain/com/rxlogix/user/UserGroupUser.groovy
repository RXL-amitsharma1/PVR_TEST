package com.rxlogix.user

import com.rxlogix.hibernate.EscapedILikeExpression
import org.apache.commons.lang.builder.HashCodeBuilder
import org.hibernate.criterion.CriteriaSpecification

class UserGroupUser implements Serializable {

	private static final long serialVersionUID = 1

	UserGroup userGroup
	User user
	Boolean manager

	boolean equals(other) {
		if (!(other instanceof UserGroupUser)) {
			return false
		}

		other.userGroup?.id == userGroup?.id &&
			other.user?.id == user?.id
	}

	int hashCode() {
		def builder = new HashCodeBuilder()
		if (userGroup) builder.append(userGroup.id)
		if (user) builder.append(user.id)
		builder.toHashCode()
	}

	static UserGroupUser get(long userGroupId, long userId) {
		UserGroupUser.where {
			userGroup == UserGroup.load(userGroupId) &&
					user == User.load(userId)
		}.get()
	}

	static boolean exists(long userGroupId, long userId) {
		UserGroupUser.where {
			userGroup == UserGroup.load(userGroupId) &&
					user == User.load(userId)
		}.count() > 0
	}

	static boolean isManager(long userGroupId, long userId){
		UserGroupUser.where {
			userGroup == UserGroup.load(userGroupId) &&
					user == User.load(userId)
		}.get().manager
	}

	static UserGroupUser create(UserGroup userGroup, User user, Boolean manager, boolean flush = false) {
		def instance = new UserGroupUser(userGroup: userGroup, user: user, manager: manager)
		instance.save(flush: flush, insert: true)
		instance
	}

	static boolean remove(UserGroup ug, User u, boolean flush = false) {
		if (ug == null || u == null) return false

		int rowCount = UserGroupUser.where {
			userGroup == UserGroup.load(ug.id) &&
			user == User.load(u.id)
		}.deleteAll()

		if (flush) { UserGroupUser.withSession { it.flush() } }

		rowCount > 0
	}

	static void removeAll(UserGroup u, boolean flush = false) {
		if (u == null) return

		UserGroupUser.where {
			userGroup == UserGroup.load(u.id)
		}.deleteAll()

		if (flush) { UserGroupUser.withSession { it.flush() } }
	}

	static void removeAll(User u, boolean flush = false) {
		if (u == null) return

		UserGroupUser.where {
			user == User.load(u.id)
		}.deleteAll()

		if (flush) { UserGroupUser.withSession { it.flush() } }
	}

	static constraints = {
		user validator: { User u, UserGroupUser ur ->
			if (ur.userGroup == null) return
			boolean existing = false
			UserGroupUser.withNewSession {
				existing = UserGroupUser.exists(ur.userGroup.id, u.id)
			}
			if (existing) {
				return 'userGroupUser.exists'
			}
		}
		manager nullable: true, blank: true
	}

	static mapping = {
		id composite: ['user', 'userGroup']
		version false
		table name: "PVUSERGROUPS_USERS"
		manager column: "MANAGER"
		user cascade: 'none'
		userGroup cascade: 'none'
	}

	static namedQueries = {
		fetchAllByUserGroupAndSearchString {UserGroup ug, String search = null ->
			projections {
				distinct("user")
				user{
					property("fullName")
					eq('enabled', true)
				}
				property("manager")
			}
			allByUserGroupAndSearchString(ug, search)
		}
		countAllByUserGroupAndSearchString {UserGroup ug, String search = null ->
			projections {
				countDistinct('user')
			}
			allByUserGroupAndSearchString(ug, search)
		}
		allByUserGroupAndSearchString {UserGroup ug, String search ->
			eq('userGroup', ug)
			userGroup {
				eq('isDeleted', false)
			}
			if(search){
				user{
					iLikeWithEscape('fullName', "%${EscapedILikeExpression.escapeString(search)}%")
				}
			}
		}
	}

	public String toString() {
		return "$userGroup - $user"
	}
}
