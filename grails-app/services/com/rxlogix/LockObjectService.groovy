package com.rxlogix

import com.rxlogix.config.LockedObjects
import com.rxlogix.user.User

class LockObjectService {

    static final Long TIMEOUT = 1000 * 60 * 5 //lock is valid 5 min maximum

    synchronized User lock(Long id, User user) {
        return lock([id], user)
    }

    synchronized User lock(List<Long> ids, User user) {
        try {
            clear()
            List<LockedObjects> locks = LockedObjects.findAllByIdentifierInListAndUserNotEqual(ids, user)
            if (locks && locks.size() > 0) return locks[0].user
            ids.each { id ->
                LockedObjects lock = LockedObjects.findByIdentifierAndUser(id, user)
                if (lock) {
                    lock.lockTime = new Date()
                } else {
                    lock = new LockedObjects(user: user, identifier: id)
                }
                lock.save(failOnError: true, flush: true)
            }
            return user
        } catch (Exception e) {
            log.error("Exception occurred, trying to lock RCA!", e)
        }
        return null
    }

    synchronized boolean unlock(List<Long> ids, User user) {
        try {
            if (ids)
                LockedObjects.executeUpdate("delete from LockedObjects where identifier in (:ids) and user=:u", [ids: ids, u: user])
            else
                LockedObjects.executeUpdate("delete from LockedObjects where user=:u", [u: user])
            return true
        } catch (Exception e) {
            log.error(e)
        }
        return false
    }

    synchronized boolean clear() {
        LockedObjects.executeUpdate("delete from LockedObjects where lockTime<:d", [d: new Date(System.currentTimeMillis() - TIMEOUT)])
    }

}
