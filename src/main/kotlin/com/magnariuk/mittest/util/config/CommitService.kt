package com.magnariuk.mittest.util.config

import com.magnariuk.mittest.data_api.Commit
import com.magnariuk.mittest.data_api.File
import com.magnariuk.mittest.data_api.User
import com.magnariuk.mittest.util.enums.ActivityTypes
import com.magnariuk.mittest.util.controllers.DbController
import com.magnariuk.mittest.util.enums.CommitStatuses
import com.magnariuk.mittest.util.util.importFile
import com.magnariuk.mittest.util.util.unzipArchive
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.InputStream
import com.magnariuk.mittest.util.util.getFileExtensionM
@Service
class CommitService(
    @Autowired private val dbc: DbController,
    @Autowired private val userService: UserService,
    @Autowired private val projectService: ProjectService,
) {
    val db = dbc.getDB()

    fun createCommit(projectId: Int, user: User, hash: String, message: String, files: MutableMap<String, InputStream>) {
        val owner = userService.getUserById(projectService.getProjectOwner(projectId)!!.user_id)!!
        val project = projectService.getProjectById(projectId)!!
        if(user.id == owner.id){
            db.InsertCommit(projectId, hash, message, user.id, CommitStatuses.AUTO_ACCEPTED.id)
            val commit = db.getCommitByHash(hash)
            userService.addActivity(
                user,
                ActivityTypes.COMMIT_CREATED.type,
                "Ви додали внесок на проєкті {{${project.name}:/project?p=$projectId}} з описом:\n '$message'"
            )
            importFiles(files, commit!!)


        } else {
            db.InsertCommit(projectId, hash, message, user.id, CommitStatuses.PENDING.id)
            val commit = db.getCommitByHash(hash)
            userService.addActivity(
                userService.getUserById(projectService.getProjectOwner(projectId)!!.user_id)!!,
                ActivityTypes.COMMIT_CREATED.type,
                "На вашому проєкті {{${project.name}:/project?p=$projectId}} користувач ${user.username} створив {{внесок:/commit?c=${commit?.commit_hash}}} з описом:\n '$message'"
            )
            userService.addActivity(
                user,
                ActivityTypes.COMMIT_MEMBER_CREATED.type,
                "Ви створили {{внесок:/commit?c=${commit?.commit_hash}}} на проєкті {{${project.name}:/project?p=$projectId}}"
            )
            importFiles(files, commit!!)

        }
    }

    fun getFilesByCommit(commitId: Int): List<File> = db.getFilesByCommitId(commitId)

    fun importFiles(files: MutableMap<String, InputStream>, commit: Commit){
        files.forEach { (fileName, inputStream) ->
            val ex = getFileExtensionM(fileName)
            if (ex == "zip"){
                val unpackedFiles = unzipArchive(inputStream, commit)
                unpackedFiles.forEach { (fileName1, fileUn ) ->
                    addFileToCommit(
                        commit,
                        File(
                            1,
                            commit.commit_id,
                            fileUn.absolutePath,
                            fileName1,
                            fileUn.length(),
                            2
                        ))
                }
            } else{
                val fileToCreate = importFile(fileName, inputStream, commit)
                addFileToCommit(
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
        val project = projectService.getProjectById(commit.project_id)!!
        val newCommit = Commit(
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

        if(author.id == owner.id){
            userService.addActivity(
                owner,
                ActivityTypes.COMMIT_ACCEPTED.type,
                "Ви прийняли {{внесок:/commit?c=${commit.commit_hash}}} на проєкті {{${project.name}:/project?p=${commit.project_id}}}"
            )
        } else{
            userService.addActivity(
                author,
                ActivityTypes.COMMIT_MEMBER_ACCEPTED.type,
                "Ваш {{внесок:/commit?c=${commit.commit_hash}}} на проєкті {{${project.name}:/project?p=${commit.project_id}}} було прийнято"
            )
            userService.addActivity(
                owner,
                ActivityTypes.COMMIT_ACCEPTED.type,
                "Ви прийняли {{внесок:/commit?c=${commit.commit_hash}}} на проєкті {{${project.name}:/project?p=${commit.project_id}}}"
            )
        }
    }

    fun rejectCommit(commitId: Int){
        val commit = getCommitById(commitId)!!
        val project = projectService.getProjectById(commit.project_id)!!
        val newCommit = Commit(
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

        if(author.id == owner.id){
            userService.addActivity(
                owner,
                ActivityTypes.COMMIT_REJECTED.type,
                "Ви відхилили {{внесок:/commit?c=${commit.commit_hash}}} на проєкті {{${project.name}:/project?p=${commit.project_id}}}"
            )
        } else{
            userService.addActivity(
                author,
                ActivityTypes.COMMIT_MEMBER_REJECTED.type,
                "Ваш {{внесок:/commit?c=${commit.commit_hash}}} на проєкті {{${project.name}:/project?p=${commit.project_id}}} було відхилено"
            )
            userService.addActivity(
                owner,
                ActivityTypes.COMMIT_REJECTED.type,
                "Ви відхилили внесок {{внесок:/commit?c=${commit.commit_hash}}} на проєкті {{${project.name}:/project?p=${commit.project_id}}}"
            )
        }


    }




    fun getCommitsByProjectId(projectId: Int): List<Commit> = db.getCommitsByProject(projectId)


    fun getCommitsByCommitHash(hash: String): Commit? = db.getCommitsByHash(hash)

    fun getCommitById(commitId: Int): Commit? = db.getCommitById(commitId)

    fun addFileToCommit(commit: Commit, file: File){
        db.InsertFiles(
            commit.commit_id,
            file.file_path,
            file.file_name,
            file.file_size,
        )

    }


}