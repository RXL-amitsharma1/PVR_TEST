import com.rxlogix.user.Role
import com.rxlogix.user.User
import com.rxlogix.user.UserRole

databaseChangeLog = {

	changeSet(author: "forxsv (generated)", id: "1536072712462-1") {
		grailsChange {
			change {
				try {
					Role userManager = new Role(authority: "ROLE_USER_MANAGER", description: "Can manage users rights", createdBy: "Application", modifiedBy: "Application").save()
					Set<User> admins = Role.findByAuthority("ROLE_ADMIN")?.getUsers()
					admins?.each{
						new UserRole(user: it,role: userManager).save()
					}
				} catch (Exception ex) {
					println "##### Error Occurred while creating role ROLE_USER_MANAGER changeset: 1536072712462-1 ####"
					ex.printStackTrace(System.out)
				}
			}
		}
	}

}
