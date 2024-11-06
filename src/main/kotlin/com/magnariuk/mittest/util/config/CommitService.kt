package com.magnariuk.mittest.util.config

import com.magnariuk.mittest.data_api.Commit
import com.magnariuk.mittest.data_api.File
import com.magnariuk.mittest.data_api.User
import com.magnariuk.mittest.util.enums.ActivityTypes
import com.magnariuk.mittest.util.controllers.DbController
import com.magnariuk.mittest.util.enums.CommitStatuses
import com.magnariuk.mittest.util.util.importFile
import com.vaadin.copilot.userinfo.UserInfoRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.InputStream
import java.nio.file.Path

@Service
class CommitService(
    @Autowired private val dbc: DbController,
    @Autowired private val userService: UserService,
    @Autowired private val projectService: ProjectService,
) {
    val db = dbc.getDB()

    fun createCommit(projectId: Int, user: User, hash: String, message: String, files: MutableMap<String, InputStream>) {
        val owner = userService.getUserById(projectService.getProjectOwner(projectId)!!.user_id)!!
        if(user.id == owner.id){
            db.InsertCommit(projectId, hash, message, user.id, CommitStatuses.AUTO_ACCEPTED.id)
            val commit = db.getCommitByHash(hash)
            userService.addActivity(
                user,
                ActivityTypes.COMMIT_CREATED.type,
                "Ви додали внесок на проєкті '/project/$projectId' з описом:\n '$message'"
            )
            files.forEach { (fileName, inputStream) ->
                val fileToCreate = importFile(fileName, inputStream, commit!!)
                AddFileToCommit(
                    commit,
                    File(
                        1,
                        commit.commit_id,
                        fileToCreate.absolutePath,
                        fileName,
                        fileToCreate.length(),
                        2
                    ))
            }


        } else {
            db.InsertCommit(projectId, hash, message, user.id, CommitStatuses.PENDING.id)
            val commit = db.getCommitByHash(hash)
            userService.addActivity(
                userService.getUserById(projectService.getProjectOwner(projectId)!!.user_id)!!,
                ActivityTypes.COMMIT_CREATED.type,
                "На вашому проєкті '/project?p=$projectId' користувач '${user.username}' створив внесок з описом:\n '$message'"
            )
            userService.addActivity(
                user,
                ActivityTypes.COMMIT_MEMBER_CREATED.type,
                "Ви створили внесок '/commit?c=${commit?.commit_id}' на проєкті '/project?p=$projectId'"
            )

            files.forEach { (fileName, inputStream) ->
                val fileToCreate = importFile(fileName, inputStream, commit!!)
                AddFileToCommit(
                    commit,
                    File(
                        1,
                        commit.commit_id,
                        fileToCreate.absolutePath,
                        fileName,
                        fileToCreate.length(),
                        2
                    ))
            }

        }
    }

    fun acceptCommit(commitId: Int){
        val commit = getCommitById(commitId)!!
        val newCommit: Commit = Commit(
            commit_id = commit.commit_id,
            project_id = commit.project_id,
            commit_hash = commit.commit_hash,
            message = commit.message,
            status = CommitStatuses.ACCEPTED.id,
            commited_at = commit.commited_at,
            author_id = commit.author_id,
        )

        db.UpdateCommit(newCommit)

        val author = userService.getUserById(newCommit.author_id)!!
        val owner = userService.getUserById(projectService.getProjectOwner(newCommit.project_id)!!.user_id)!!

        userService.addActivity(
            author,
            ActivityTypes.COMMIT_MEMBER_ACCEPTED.type,
            "Ваш внесок '/commit?c=$commitId' на проєкті '/project?p=${commit.project_id}' було прийнято"
        )
        userService.addActivity(
            owner,
            ActivityTypes.COMMIT_ACCEPTED.type,
            "Ви прийняли внесок '/commit?c=$commitId' на проєкті '/project?p=${commit.project_id}'"
        )
    }

    fun rejectCommit(commitId: Int){
        val commit = getCommitById(commitId)!!
        val newCommit: Commit = Commit(
            commit_id = commit.commit_id,
            project_id = commit.project_id,
            commit_hash = commit.commit_hash,
            message = commit.message,
            status = CommitStatuses.REJECTED.id,
            commited_at = commit.commited_at,
            author_id = commit.author_id,
        )

        db.UpdateCommit(newCommit)

        val author = userService.getUserById(newCommit.author_id)!!
        val owner = userService.getUserById(projectService.getProjectOwner(newCommit.project_id)!!.user_id)!!

        userService.addActivity(
            author,
            ActivityTypes.COMMIT_MEMBER_REJECTED.type,
            "Ваш внесок '/commit?c=$commitId' на проєкті '/project?p=${commit.project_id}' було відхилено"
        )
        userService.addActivity(
            owner,
            ActivityTypes.COMMIT_REJECTED.type,
            "Ви відхилили внесок '/commit?c=$commitId' на проєкті '/project?p=${commit.project_id}'"
        )
    }




    fun getCommitsByProjectId(projectId: Int): List<Commit> = db.getCommitşByProject(projectId)


    fun getCommitsByCommitHash(hash: String): Commit? = db.getCommitşByHash(hash)

    fun getCommitById(commitId: Int): Commit? = db.getCommitById(commitId)

    fun AddFileToCommit(commit: Commit, file: File){
        db.InsertFiles(
            commit.commit_id,
            file.file_path,
            file.file_name,
            file.file_size,
        )

    }


}