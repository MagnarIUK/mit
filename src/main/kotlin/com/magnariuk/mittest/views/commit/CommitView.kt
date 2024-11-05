package com.magnariuk.mittest.views.commit

import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.magnariuk.mittest.data_api.Commit
import com.magnariuk.mittest.util.config.AuthService
import com.magnariuk.mittest.util.config.CommitService
import com.magnariuk.mittest.util.config.ProjectService
import com.magnariuk.mittest.util.config.UserService
import com.magnariuk.mittest.util.util.px
import com.magnariuk.mittest.util.util.showError
import com.magnariuk.mittest.views.MainLayout
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterObserver
import com.vaadin.flow.router.QueryParameters
import com.vaadin.flow.router.Route
import org.springframework.beans.factory.annotation.Autowired

@Route("/commit", layout = MainLayout::class)
class CommitView(
    @Autowired private val authService: AuthService,
    @Autowired private val userService: UserService,
    @Autowired private val projectService: ProjectService,
    @Autowired private val commitService: CommitService
): KComposite(), BeforeEnterObserver {
    var user = authService.getLoggedInUser()
    var currentCommit: Commit? = null

    private lateinit var dynamicLayout: VerticalLayout


    override fun beforeEnter(event: BeforeEnterEvent) {
        val queryParameters: QueryParameters = event.location.queryParameters

        val commitParam = queryParameters.parameters["c"]?.firstOrNull()

        if (commitParam != null && user != null) {
            currentCommit = commitService.getCommitsByCommitHash(commitParam)
        }

        if(currentCommit == null) {
            showError("Внесок не знайдено")
        } else{
            updateUI()
        }
    }

    private val root = ui {
        verticalLayout {
            alignItems = FlexComponent.Alignment.CENTER
            justifyContentMode = FlexComponent.JustifyContentMode.CENTER

            dynamicLayout = verticalLayout {
                alignItems = FlexComponent.Alignment.CENTER
                justifyContentMode = FlexComponent.JustifyContentMode.CENTER
            }
        }
    }


    fun updateUI() {
        dynamicLayout.removeAll()


        val leftBarHolder = VerticalLayout().apply {
            alignItems = Alignment.CENTER
            justifyContentMode = JustifyContentMode.CENTER
            width = 400.px
            style.set("background-color", "#f0f0f0")
            style.set("border-radius", "10px")
            style.set("padding", "10px")
            style.set("margin", "5px")
            style.set("border", "1px solid #d3d3d3")
        }




        var rightBarHolder = VerticalLayout().apply {
            alignItems = Alignment.CENTER
            justifyContentMode = JustifyContentMode.CENTER
            width = 800.px
            style.set("background-color", "#f0f0f0")
            style.set("border-radius", "10px")
            style.set("padding", "10px")
            style.set("margin", "5px")
            style.set("border", "1px solid #d3d3d3")
        }


        dynamicLayout.add(
            HorizontalLayout(
                leftBarHolder,
                rightBarHolder
            ).apply {}
        )


    }

}