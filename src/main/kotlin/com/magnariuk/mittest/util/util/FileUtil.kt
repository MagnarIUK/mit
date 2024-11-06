package com.magnariuk.mittest.util.util

import com.magnariuk.mittest.data_api.Commit
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

fun initDirs() {
    try {
        val commitDirPath: Path = Paths.get("./FILES/COMMIT_DIR")
        if (Files.notExists(commitDirPath)) {
            Files.createDirectories(commitDirPath)
            println("Директорію створено: $commitDirPath")
        } else {
            println("Директорія вже існує: $commitDirPath")
        }
    } catch (e: IOException) {
        println("Помилка створення директорії: ${e.message}")
    }
}


fun createCommitDir(commitDir: String){
    try {
        val commitDirPath: Path = Paths.get("./FILES/COMMIT_DIR/$commitDir")
        if (Files.notExists(commitDirPath)) {
            Files.createDirectories(commitDirPath)
            println("Директорію створено: $commitDirPath")
        } else {
            println("Директорія вже існує: $commitDirPath")

        }
    } catch (e: IOException) {
        println("Помилка створення директорії: ${e.message}")
    }
}

fun importFile(name: String, stream: InputStream, commit: Commit): File  {
    createCommitDir(commit.commit_hash)
    val file = File("./FILES/COMMIT_DIR/${commit.commit_hash}/$name")
    file.parentFile.mkdirs()

    FileOutputStream(file).use { outputStream ->
        stream.copyTo(outputStream)
        }
    println("Файл '$name' успішно створено за шляхом ${file.absolutePath}")
    return file
}