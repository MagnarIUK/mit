package com.magnariuk.mittest

import com.magnariuk.mittest.util.util.initDirs
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
@SpringBootApplication(exclude = [
    SecurityAutoConfiguration::class
])
class MitTestApplication

fun main(args: Array<String>) {
    runApplication<MitTestApplication>(*args)
    initDirs()
}