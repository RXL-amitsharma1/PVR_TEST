package com.rxlogix

class ParamsUtils {
    public static Map parseSharedWithParam(List sharedWith, Long userId) {
        def sharedWithMap = [:]
        if (sharedWith && sharedWith.size() > 0) {
            if (sharedWith) {
                sharedWithMap.team = sharedWith.find { it.startsWith(Constants.TEAM_SELECT_VALUE) } ? true : false
                sharedWithMap.ownerId = sharedWith.find { it.startsWith(Constants.OWNER_SELECT_VALUE) } ? userId : null
                sharedWithMap.groupsId = sharedWith.findAll { it.startsWith(Constants.USER_GROUP_TOKEN) }?.collect { Long.valueOf(it.replaceAll(Constants.USER_GROUP_TOKEN, '')) }
                sharedWithMap.usersId = sharedWith.findAll { it.startsWith(Constants.USER_TOKEN) }?.collect { Long.valueOf(it.replaceAll(Constants.USER_TOKEN, '')) }
                if (sharedWith.find { it.startsWith(Constants.SHARED_WITH_ME_SELECT_VALUE) }) {
                    sharedWithMap.usersId << userId
                }
            }
        }
        sharedWithMap
    }

}
