package com.magnariuk.mittest.util.util

import com.github.mvysny.karibudsl.v10.onLeftClick
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.NativeLabel
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.notification.NotificationVariant
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.random.Random

fun generateRandomHex(length: Int): String {
    val hexChars = "0123456789ABCDEF"
    return (1..length)
        .map { hexChars[Random.nextInt(16)] }
        .joinToString("")
}


fun showError(message: String, dur: Int = 5000, showCloseButton: Boolean = false) {
    val notification = Notification(message)
    notification.addThemeVariants(NotificationVariant.LUMO_ERROR)
    notification.position = (Notification.Position.TOP_CENTER)
    notification.duration = dur
    if(showCloseButton){
        notification.add(
            NativeLabel(message),
            Button().apply {
            icon = Icon(VaadinIcon.CLOSE)
            onLeftClick {
                notification.close()
            }
        })
    }
    notification.open()
}

fun showWarning(message: String, dur: Int = 5000, showCloseButton: Boolean = false) {
    val notification = Notification(message)
    notification.addThemeVariants(NotificationVariant.LUMO_WARNING)
    notification.position = (Notification.Position.TOP_CENTER)
    notification.duration = dur
    if(showCloseButton){
        notification.add(
            NativeLabel(message),
            Button().apply {
                icon = Icon(VaadinIcon.CLOSE)
                onLeftClick {
                    notification.close()
                }
            })
    }
    notification.open()
}

fun unixToDate(unixTimestamp: Long): String {
    val dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(unixTimestamp), ZoneId.systemDefault())
    val formatter = DateTimeFormatter.ofPattern("dd:MM:yyyy HH:mm:ss")
    return dateTime.format(formatter)
}

fun showSuccess(message: String, dur: Int = 5000, showCloseButton: Boolean = false) {
    val notification = Notification(message)
    notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS)
    notification.position = (Notification.Position.TOP_CENTER)
    notification.duration = dur
    if(showCloseButton){
        notification.add(
            NativeLabel(message),
            Button().apply {
                icon = Icon(VaadinIcon.CLOSE)
                onLeftClick {
                    notification.close()
                }
            })
    }
    notification.open()

}