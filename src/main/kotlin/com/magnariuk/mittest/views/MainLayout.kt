package com.magnariuk.mittest.views

import com.magnariuk.mittest.util.config.AuthService
import com.magnariuk.mittest.util.util.CSS
import com.magnariuk.mittest.util.util.p
import com.magnariuk.mittest.views.home.HomeView
import com.magnariuk.mittest.views.login.LoginView
import com.magnariuk.mittest.views.user.UserView
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.router.RouterLink
import org.springframework.beans.factory.annotation.Autowired
@Suppress("SpringJavaInjectionPointsAutowiringInspection")
class MainLayout(@Autowired private val authService: AuthService): AppLayout() {

    init {
        val title = H1("MIT").apply {
            style.set("margin-left", "20")
        }

        val menu = HorizontalLayout().apply {
            add(createNavLink("Головна", HomeView::class.java))
            if(authService.isUserLoggedIn()){
                add(createNavLink("Користувач", UserView::class.java))
                add(createNavLink("Вийти", LoginView::class.java))
            } else{
                add(createNavLink("Увійти", LoginView::class.java))

            }
            isSpacing = true
            alignItems = FlexComponent.Alignment.CENTER
        }

        val titleWrapper = HorizontalLayout().apply {
            add(title)
            justifyContentMode = FlexComponent.JustifyContentMode.CENTER
            alignItems = FlexComponent.Alignment.CENTER
            expand(title)
            setWidthFull()
        }

        val header = HorizontalLayout().apply {
            add(titleWrapper, menu)
            setWidthFull()
            width = "100%"
            justifyContentMode = FlexComponent.JustifyContentMode.BETWEEN
            alignItems = FlexComponent.Alignment.CENTER
            style.set(CSS.MARGIN_LEFT, 30.p)
                    .set(CSS.MARGIN_RIGHT, 30.p)
        }
        addToNavbar(header)

    }

    private fun createNavLink(text: String, navigationTarget: Class<out Component>, logout: Boolean = false): RouterLink {
        return if( logout){
            authService.logout()
            RouterLink(text, navigationTarget).apply {
                style.set("margin-right", "10px")
            }
        } else{
            RouterLink(text, navigationTarget).apply {
                style.set("margin-right", "10px")
            }
        }
    }


}