package com.magnariuk.mittest.views.login

import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.p
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.magnariuk.mittest.util.config.AuthService
import com.vaadin.flow.component.UI
import com.vaadin.flow.router.Route
import org.springframework.beans.factory.annotation.Autowired
import com.magnariuk.mittest.util.util.showSuccess

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@Route(value = "logout")
class LogoutView(@Autowired private val authService: AuthService): KComposite() {
    val user = authService.getLoggedInUser()

    private val root = ui {
        verticalLayout {
            if(!authService.isUserLoggedIn()){
                UI.getCurrent().navigate("login")
            } else{
                p(
                    "user: ${user?.username}"
                )
                authService.logout()
                showSuccess("Ви вийшли з акаунту")
                UI.getCurrent().navigate("login")
            }
            UI.getCurrent().navigate("login")

        }
    }


}