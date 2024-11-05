package com.magnariuk.mittest

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.transaction.annotation.EnableTransactionManagement
@SpringBootApplication(exclude = [
    SecurityAutoConfiguration::class
])
class MitTestApplication

fun main(args: Array<String>) {
    runApplication<MitTestApplication>(*args)
}
