package com.magnariuk.mittest.data_api

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

open class DatabaseController{



    fun databaseExists(dbPath: String): Boolean {
        val file = File(dbPath)
        return file.exists() && !file.isDirectory
    }


    fun init() {
        val dbPath = System.getenv("DB_PATH") ?: "mit.sqlite"
        val dbExists = databaseExists(dbPath)
        Database.connect("jdbc:sqlite:$dbPath", driver = "org.sqlite.JDBC")
        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(Projects, Commits, Users, Files, ProjectAccesses, UserActivities)
        }
        when {
            dbExists -> {
                println("База даних існує і підключена")
            }
            !dbExists -> {
                println("База даних створена і таблиці ініціалізовані")
            }
        }
    }

    fun <T> dbQuery(block: () -> T): T {
        return transaction {
            block()
        }
    }
}

open class DB(private val dbController: DatabaseController) {

    fun getUNIXtime(): Long{
        return System.currentTimeMillis()
    }

    fun UpdateUser(user: User){
        dbController.dbQuery {
            Users.update({Users.user_id eq user.id}){
                it[username] = user.username
                it[password] = user.password
                it[display_name] = user.display_name
                it[email] = user.email
            }
        }
    }
    fun InsertUser(_username: String, _password: String, _display_name: String?, _email: String?) {
        dbController.dbQuery {

            Users.insert {
                it[username] = _username
                it[password] = _password
                it[display_name] = _display_name
                it[email] = _email
                it[created_at] = getUNIXtime()
            }


        }


    }

    fun getUserByUsername(_username: String): User? {
        return dbController.dbQuery {
            Users.selectAll().where { Users.username eq _username }.firstOrNull()?.let { Converter().toUser(it)}
        }
    }
    fun getUserById(_id: Int): User? {
        return dbController.dbQuery {
            Users.selectAll().where { Users.user_id eq _id }.firstOrNull()?.let { Converter().toUser(it)}
        }
    }



    fun InsertProject(_name: String, _description: String, _author_id: Int): Project?{
        return dbController.dbQuery {

            Projects.insert {
                it[name] = _name
                it[description] = _description
                it[created_at] = getUNIXtime()
                it[author_id] = _author_id
            }

            getProjectByName(_name)



        }
    }

    fun getProjectByName(_name: String): Project?{
        return dbController.dbQuery {
            Projects.selectAll().where {Projects.name eq _name}.firstOrNull()?.let { Converter().toProject(it) }

        }
    }



    fun deleteProjectById(id: Int){
        dbController.dbQuery {
            Projects.deleteWhere { project_id eq id }
        }
    }

    fun getProjectById(_id: Int): Project?{
        return dbController.dbQuery {
            Projects.selectAll().where {Projects.project_id eq _id}.firstOrNull()?.let { Converter().toProject(it) }
        }
    }



    fun InsertCommit(_project_id: Int, _commit_hash: String, _message: String, _author_id: Int, _status: String ){
        dbController.dbQuery {
            Commits.insert {
                it[project_id] = _project_id
                it[commit_hash] = _commit_hash
                it[message] = _message
                it[status] = _status
                it[author_id] = _author_id
                it[commited_at] = getUNIXtime()
            }

        }
    }

    fun UpdateCommit(commit: Commit){
        dbController.dbQuery {
            Commits.update({ Commits.commit_id eq commit.commit_id }) {
                it[message] = commit.message
                it[status] = commit.status
            }
        }
    }

    fun getCommitByHash(_commit_hash: String): Commit? {
        return dbController.dbQuery {
            Commits.selectAll().where { Commits.commit_hash eq _commit_hash }.firstOrNull()?.let { Converter().toCommit(it) }
        }
    }


    fun getCommitById(_commit_id: Int): Commit? {
        return dbController.dbQuery {
            Commits.selectAll().where { Commits.commit_id eq _commit_id }.firstOrNull()?.let { Converter().toCommit(it) }
        }
    }

    fun getCommitsByProject(_project_id: Int): List<Commit>{
        return dbController.dbQuery {
            Commits.selectAll().where { Commits.project_id eq  _project_id}.toList().map { Converter().toCommit(it) }
        }
    }

    fun getCommitsByHash(_hash: String): Commit? {
        return dbController.dbQuery {
            Commits.selectAll().where { Commits.commit_hash eq _hash }.firstOrNull()?.let { Converter().toCommit(it) }
        }
    }


    fun InsertFiles(_commit_id: Int, _file_path: String, _file_name: String, _file_size: Long){
        dbController.dbQuery {

            Files.insert {
                it[commit_id] = _commit_id
                it[file_path] = _file_path
                it[file_name] = _file_name
                it[file_size] = _file_size
                it[added_at] = getUNIXtime()
            }

        }
    }


    fun getFilesByCommitId(_id: Int): List<com.magnariuk.mittest.data_api.File>{
        return dbController.dbQuery {
            Files.selectAll().where { Files.commit_id eq _id}.map { Converter().toFile(it) }
        }
    }


    fun InsertProjectAccess(_project_id: Int, _user_id: Int, _access_level: Int) {
        dbController.dbQuery {
            ProjectAccesses.insert {
                it[project_id] = _project_id
                it[user_id] = _user_id
                it[access_level] = _access_level
            }
        }
    }

    fun changeProject(_project_id: Int, _projectName: String?, _projectDescription: String?){
        dbController.dbQuery {
            if(_projectName!= null){
                Projects.update(where = { Projects.project_id eq _project_id }) {
                    it[name] = _projectName
                }
            }
            if(_projectDescription != null){
                Projects.update(where = { Projects.project_id eq _project_id }) {
                    it[description] = _projectDescription
                }
            }
        }
    }

    fun deleteProjectAccessesByProjectId(_project_id: Int){
        dbController.dbQuery {
            ProjectAccesses.deleteWhere { project_id eq _project_id }
        }
    }



    fun getProjectAccessByUserId(_user_id: Int): List<ProjectAccess>{
        return dbController.dbQuery {
            ProjectAccesses.selectAll().where { ProjectAccesses.user_id eq _user_id}.map { Converter().toProjectAccess(it) }
        }
    }
    fun getProjectAccessByProjectId(_project_id: Int): List<ProjectAccess>{
        return dbController.dbQuery {
            ProjectAccesses.selectAll().where { ProjectAccesses.project_id eq _project_id}.map { access -> Converter().toProjectAccess(access) }
        }
    }
    fun updateProjectAccess(_access_id: Int, _access_level: Int){
        dbController.dbQuery {
            ProjectAccesses.update(where = { ProjectAccesses.access_id eq _access_id }) {
                it[access_level] = _access_level
            }
        }
    }
    fun deleteProjectAccess(_access_id: Int){
        dbController.dbQuery {
            ProjectAccesses.deleteWhere { access_id eq _access_id }
        }
    }

    fun getProjectAccessById(_access_id: Int): ProjectAccess?{
        return dbController.dbQuery {
            ProjectAccesses.selectAll().where { ProjectAccesses.access_id eq _access_id }.firstOrNull()?.let { Converter().toProjectAccess(it) }

        }
    }

    fun getProjectAccessByProjectIdAndUserId(_project_id: Int, _user_id: Int): ProjectAccess?{
        return dbController.dbQuery {
            ProjectAccesses.selectAll().where { ProjectAccesses.project_id eq _project_id and (ProjectAccesses.user_id eq _user_id)}.firstOrNull()?.let { Converter().toProjectAccess(it) }
        }
    }



    fun InsertUserActivity(_user_id: Int, _activity_type: String, _description: String){
        dbController.dbQuery {
            UserActivities.insert {
                it[user_id] = _user_id
                it[activity_type] = _activity_type
                it[description] = _description
                it[created_at] = getUNIXtime()
            }
        }


    }

    fun getUserActivityByUserId(_user_id: Int): List<UserActivity>{
        return dbController.dbQuery {
            UserActivities.selectAll().where{ UserActivities.user_id eq _user_id }.map { Converter().toActivity(it) }
        }
    }

}

open class Converter {
    fun toUser(row: ResultRow): User {
        return User(
            id = row[Users.user_id],
            username = row[Users.username],
            password = row[Users.password],
            display_name = row[Users.display_name],
            email = row[Users.email],
            created_at = row[Users.created_at],
        )
    }

    fun toProject(row: ResultRow): Project {
        return Project(
            project_id = row[Projects.project_id],
            name = row[Projects.name],
            description = row[Projects.description],
            created_at = row[Projects.created_at],
            author_id = row[Projects.author_id],
        )
    }

    fun toCommit(row: ResultRow): Commit {
        return Commit(
            commit_id = row[Commits.commit_id],
            project_id = row[Commits.project_id],
            commit_hash = row[Commits.commit_hash],
            message = row[Commits.message],
            status = row[Commits.status],
            author_id = row[Commits.author_id],
            commited_at = row[Commits.commited_at],
        )
    }

    fun toFile(row: ResultRow): com.magnariuk.mittest.data_api.File {
        return File(
            file_id = row[Files.file_id],
            commit_id = row[Files.commit_id],
            file_path = row[Files.file_path],
            file_name = row[Files.file_name],
            file_size = row[Files.file_size],
            added_at = row[Files.added_at],
        )
    }

    fun toActivity(row: ResultRow): UserActivity {
        return UserActivity(
            activity_id = row[UserActivities.activity_id],
            user_id = row[UserActivities.user_id],
            activity_type = row[UserActivities.activity_type],
            description = row[UserActivities.description],
            created_at = row[UserActivities.created_at],
        )
    }

    fun toProjectAccess(row: ResultRow): ProjectAccess {
        return ProjectAccess(
            access_id = row[ProjectAccesses.access_id],
            project_id = row[ProjectAccesses.project_id],
            user_id = row[ProjectAccesses.user_id],
            access_level = row[ProjectAccesses.access_level],
        )
    }


}