package com.magnariuk.mittest.util.util

import com.magnariuk.mittest.data_api.Commit
import com.magnariuk.mittest.data_api.Project
import com.vaadin.flow.server.InputStreamFactory
import com.vaadin.flow.server.StreamResource
import org.apache.tika.Tika
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

val FILES_DIR = System.getenv("FILES_DIR") ?: "./FILES/COMMIT_DIR"
fun initDirs() {
    try {
        val commitDirPath: Path = Paths.get(FILES_DIR)
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

fun getFileExtensionM(fileName: String): String {
    return fileName.substringAfterLast('.', "")
}

fun processFile(file: File): String? {
    val tika = Tika()
    val mimeType = tika.detect(file)
    println("MIME-тип файлу: $mimeType")

    when {
        mimeType.startsWith("image/") -> {
            return null
        }
        mimeType.startsWith("text/") -> {
            return "text"
        }
        mimeType.startsWith("video/") -> {
            return null
        }
        mimeType in listOf(
            "application/zip",
            "application/x-rar-compressed",
            "application/x-7z-compressed",
            "application/octet-stream",
            "application/x-msdownload"
        ) -> {
            return null
        }
        else -> {
            return null
        }
    }
}

fun createCommitDir(commitDir: String){
    try {
        val commitDirPath: Path = Paths.get("$FILES_DIR/$commitDir")
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

fun zipFolderToStream(commit: Commit, project: Project): StreamResource {
    val sourceFolder = File("$FILES_DIR/${commit.commit_hash}")
    if (!sourceFolder.exists() || !sourceFolder.isDirectory) {
        println("Provided path is not a valid folder.")
        throw IllegalArgumentException("Invalid folder path")
    }

    val byteArrayOutputStream = ByteArrayOutputStream()
    val zipOutputStream = ZipOutputStream(byteArrayOutputStream)

    try {
        sourceFolder.walkTopDown().forEach { file ->
            val relativePath = sourceFolder.toPath().relativize(file.toPath()).toString()
            val zipEntry = if (file.isDirectory) {
                ZipEntry("$relativePath/")
            } else {
                ZipEntry(relativePath)
            }

            zipOutputStream.putNextEntry(zipEntry)

            if (file.isFile) {
                file.inputStream().use { fis ->
                    fis.copyTo(zipOutputStream)
                }
            }

            zipOutputStream.closeEntry()
        }

        zipOutputStream.close()

    } catch (e: IOException) {
        e.printStackTrace()
        throw RuntimeException("Error zipping folder: ${e.message}")
    }

    val zipByteArray = byteArrayOutputStream.toByteArray()
    return StreamResource("${project.name}_${commit.commit_hash}.zip", InputStreamFactory { ByteArrayInputStream(zipByteArray) })
}


fun importFile(name: String, stream: InputStream, commit: Commit): File  {
    createCommitDir(commit.commit_hash)
    val file = File("$FILES_DIR/${commit.commit_hash}/$name")
    file.parentFile.mkdirs()

    FileOutputStream(file).use { outputStream ->
        stream.copyTo(outputStream)
        }
    println("Файл '$name' успішно створено за шляхом ${file.absolutePath}")
    return file
}

fun unzipArchive(zipInputStream: InputStream, commit: Commit): MutableMap<String, File> {
    createCommitDir(commit.commit_hash)
    val files: MutableMap<String, File> = mutableMapOf()
    val zipStream = ZipInputStream(zipInputStream)
    var entry: ZipEntry? = zipStream.nextEntry

    while (entry != null) {
        val filePath = Path.of("$FILES_DIR/${commit.commit_hash}", entry.name)
        val file = filePath.toFile()

        if (entry.isDirectory) {

            Files.createDirectories(filePath)
        } else {

            Files.createDirectories(filePath.parent)

            Files.newOutputStream(filePath).use { outputStream ->
                zipStream.copyTo(outputStream)
            }
            files[entry.name] = file
        }

        zipStream.closeEntry()
        entry = zipStream.nextEntry
    }

    zipStream.close()
    println("Архів успішно розпаковано до: ${commit.commit_hash}")
    return files
}