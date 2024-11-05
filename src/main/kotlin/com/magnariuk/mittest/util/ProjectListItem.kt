package com.magnariuk.mittest.util

import com.github.mvysny.karibudsl.v10.onLeftClick
import com.magnariuk.mittest.data_api.Project
import com.magnariuk.mittest.util.config.UserService
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.html.NativeLabel
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.FlexComponent.*
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import org.hibernate.loader.ast.spi.NaturalIdLoadOptions
import org.springframework.beans.factory.annotation.Autowired

class ProjectListItem(val project: Project, @Autowired private val userService: UserService): HorizontalLayout() {
    init {
        addClassName("projectListItem")
        isSpacing = true
        isPadding = true
        alignItems = Alignment.CENTER
        justifyContentMode = JustifyContentMode.CENTER
        val projectName = NativeLabel(project.name)
        projectName.className = "projectName"

        val author = userService.getUserById(project.author_id)
        val authorShow = Button(author!!.display_name ?: author.username).apply {
            style.set("color", "blue")
            addThemeVariants(ButtonVariant.LUMO_TERTIARY)
            onLeftClick {
                UI.getCurrent().navigate("user?u=${author.username}")
            }
        }
        authorShow.className = "projectAuthor"

        val shortDescription = project.description.take(10) + "..."
        val fullDescription = project.description
        val descriptionLabel = NativeLabel(shortDescription).apply {
            style.set("overflow-wrap", "break-word")
            style.set("margin-left", "10px")
            style.set("transition", "height 0.5s ease")
            maxWidth = "200px"
            style.set("overflow", "auto")
            maxHeight = "80px"
        }
        val toggleButton = Button().apply {
            style.set("margin-left", "5px")

            icon = Icon(VaadinIcon.ANGLE_DOWN)
            addClickListener {
                if (descriptionLabel.text == shortDescription) {
                    descriptionLabel.text = fullDescription
                    icon = Icon(VaadinIcon.ANGLE_UP)
                } else {
                    descriptionLabel.text = shortDescription
                    icon = Icon(VaadinIcon.ANGLE_DOWN)
                }
            }
        }


        val detailsButon= Button("Деталі").apply {
            addThemeVariants(ButtonVariant.LUMO_TERTIARY)
            onLeftClick {
                UI.getCurrent().navigate("project?p=${project.project_id}")
            }
        }
        if(project.description.length < 10){
            descriptionLabel.text = fullDescription
            add(
                projectName, authorShow,
                HorizontalLayout(descriptionLabel).apply { isSpacing = false
                    alignItems = Alignment.CENTER
                    justifyContentMode = JustifyContentMode.CENTER
                    style.set("margin-left", "10px")
                    style.set("margin-right", "10px")},
                detailsButon
            )
        } else{
            add(
                projectName, authorShow,
                HorizontalLayout(descriptionLabel, toggleButton).apply { isSpacing = false
                    alignItems = Alignment.CENTER
                    justifyContentMode = JustifyContentMode.CENTER
                    style.set("margin-left", "10px")
                    style.set("margin-right", "10px")},
                detailsButon
            )
        }


        style.set("border", "1px solid #ccc")
        style.set("border-radius", "10px")
        style.set("padding", "10px")
        style.set("margin", "5px")
        width = "100%"
        height = "100px"

    }
}