package com.magnariuk.mittest.util.config

import com.magnariuk.mittest.data_api.ProjectAccess
import com.magnariuk.mittest.data_api.User
import com.magnariuk.mittest.util.controllers.DbController
import com.magnariuk.mittest.util.enums.AccessLevels
import com.magnariuk.mittest.util.enums.AccessLevelsAdd
import com.magnariuk.mittest.util.enums.ActivityTypes
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class ProjectService(
    @Autowired private val dbc: DbController,
    @Autowired private val userService: UserService
) {
    val db = dbc.getDB()

    fun getProjectById(projectId: Int) = db.getProjectById(projectId)

    fun delProject(projectId: Int, user: User) {
        val project = getProjectById(projectId)

        userService.addActivity(user, ActivityTypes.PROJECT_DELETED.type, "Проєкт: ${project!!.name}")
        val accesses = db.getProjectAccessByProjectId(projectId)

        accesses.forEach { user1 ->
            if(user1.access_level <3){
                userService.addActivity(userService.getUserById(user1.user_id)!!, ActivityTypes.PROJECT_DELETED.type,
                    "Проєкт '${project.name}', до якого ви маєте доступ було видалено")
            }
        }

        db.deleteProjectById(projectId)
        db.deleteProjectAccessesByProjectId(projectId)
    }
    fun createProject(name: String, description: String, user: User){
        val currentProjectL = db.InsertProject(name, description, user.id )
        if(currentProjectL != null){
            userService.addActivity(user, ActivityTypes.PROJECT_CREATE_NEW.type, "Створено проєкт {{$name:/home}}")
            db.InsertProjectAccess(_project_id = currentProjectL.project_id, _user_id = user.id, _access_level = 3)
        }
    }
    fun addProjectAccess(projectId: Int,userId: Int, accessLevel: Int){
        userService.addActivity(userService.getUserById(getProjectOwner(projectId)!!.user_id)!!, ActivityTypes.PROJECT_ADD_MEMBER.type,
            "Надано доступ \"${AccessLevels.getDisplayName(accessLevel)}\" до {{проєкту:/project?p=${projectId}}} користувачу {{${userService.getUserById(userId)!!.display_name ?: userService.getUserById(userId)!!.username}:/user?u=${userService.getUserById(userId)!!.username}}}" )
        userService.addActivity(userService.getUserById(userId)!!, ActivityTypes.PROJECT_ADD_MEMBER_ACCESS.type, "Вам надано доступ до проєкту {{${getProjectById(projectId)!!.name}:/project?p=${projectId}}}" )
        db.InsertProjectAccess(_project_id = projectId, _user_id = userId, _access_level = accessLevel)
    }

    fun changeProject(projectId: Int,projectName: String? = null, projectDescription: String? = null, userId: Int){
        val access = getAccessByProjectAndUser(projectId, userId)
        if(access != null && access.access_level >= 3){
            if(projectName != null){
                userService.addActivity(userService.getUserById(userId)!!, ActivityTypes.PROJECT_EDIT_NAME.type, "Змінено назву проєкту {{${getProjectById(projectId)!!.name}:/project?p=$projectId}} на $projectName")
            } else if (projectDescription != null){
                userService.addActivity(userService.getUserById(userId)!!, ActivityTypes.PROJECT_EDIT_DESCRIPTION.type, "Змінено опис проєкту {{${getProjectById(projectId)!!.name}:/project?p=$projectId}} на '$projectDescription'")
            }
            db.changeProject(projectId, projectName, projectDescription)
        }
    }


    fun getUsersWithAccess(projectId: Int): List<ProjectAccess> {
        return db.getProjectAccessByProjectId(projectId)
    }
    fun getProjectsWithAccess(userID: Int): List<ProjectAccess> {
        return db.getProjectAccessByUserId(userID)
    }
    fun getAccessByProjectAndUser(projectId: Int, userID: Int): ProjectAccess? {
        return db.getProjectAccessByProjectIdAndUserId(projectId, userID)
    }
    fun getAccessById(accessId: Int): ProjectAccess? {
        return db.getProjectAccessById(accessId)
    }

    fun changeProjectOwner(projectId: Int, user: User, owner: User){
        val userToChangeAccess = getAccessByProjectAndUser(projectId, user.id)
        val ownerAccess = getAccessByProjectAndUser(projectId, owner.id)

        userService.addActivity(owner, ActivityTypes.PROJECT_OWNER_REVOKED.type,
            "Передано права власника на проєкт {{${getProjectById(projectId)!!.name}:/project?p=${projectId}}} користувачу {{${user.display_name?: user.username}/user?u=${user.username}}}")
        db.updateProjectAccess(ownerAccess!!.access_id, 2)

        userService.addActivity(user, ActivityTypes.PROJECT_OWNER_ADDED.type,
            "Отримано права власника на проєкт {{${getProjectById(projectId)!!.name}:/project?p=${projectId}}}")
        db.updateProjectAccess(userToChangeAccess!!.access_id, 3)



    }

    fun changeProjectAccess(accessId: Int, accessLevel: Int){
        val access = getAccessById(accessId)
        val owner = userService.getUserById(getProjectOwner(access!!.project_id)!!.user_id)
        val cUser = userService.getUserById(access.user_id)

        userService.addActivity(owner!!, ActivityTypes.PROJECT_EDIT_MEMBER.type, "Змінено права доступу користувача {{${cUser!!.display_name?:cUser.username}:/user?u=${cUser.username}}} в проєкті {{${getProjectById(access.project_id)!!.name}:/project?p=${access.project_id}}} на '${AccessLevelsAdd.getDisplayName(accessLevel)}'")
        userService.addActivity(cUser, ActivityTypes.PROJECT_MEMBER_ACCESS_EDIT.type, "Змінено права доступу в проєкті  {{${getProjectById(access.project_id)!!.name}:/project?p=${access.project_id}}}  на '${AccessLevelsAdd.getDisplayName(accessLevel)}'")

        db.updateProjectAccess(accessId, accessLevel)
    }
    fun deleteProjectAccess(accessId: Int){
        val access = getAccessById(accessId)
        val projectOwner = userService.getUserById(getProjectOwner(access!!.project_id)!!.user_id)
        val cUser = userService.getUserById(access.user_id)

        userService.addActivity(projectOwner!!, ActivityTypes.PROJECT_REMOVE_MEMBER.type,
            "Видалено учасника {{${cUser!!.display_name?:cUser.username}:/user?u=${cUser.username}}} з проєкту {{${getProjectById(getAccessById(accessId)!!.project_id)!!.name}/project?p=${getAccessById(accessId)!!.project_id}}}")

        userService.addActivity(cUser, ActivityTypes.PROJECT_MEMBER_ACCESS_REVOKED.type,
            "Вас позбавлено доступу до проєкту '${getProjectById(getAccessById(accessId)!!.project_id)!!.name}'")

        db.deleteProjectAccess(accessId)
    }

    fun getProjectOwner(projectId: Int): ProjectAccess?{
        val allAccesses = db.getProjectAccessByProjectId(projectId)

        return allAccesses.firstOrNull {it.access_level == 3}

    }






}