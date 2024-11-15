package com.magnariuk.mittest.views.login

import com.github.mvysny.karibudsl.v10.*
import com.vaadin.flow.component.Key
import com.github.mvysny.kaributools.setPrimary
import com.magnariuk.mittest.util.config.AuthService
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode
import org.springframework.beans.factory.annotation.Autowired
import com.magnariuk.mittest.util.util.CSS
import com.magnariuk.mittest.util.util.px
import com.magnariuk.mittest.util.util.showError
import com.magnariuk.mittest.util.util.showSuccess
import com.magnariuk.mittest.views.home.HomeView
import com.magnariuk.mittest.views.registration.RegistrationView
import com.vaadin.flow.router.*

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@PageTitle("Вхід")
@Route("login")
@RouteAlias("auth")
@RouteAlias("log")
class LoginView(@Autowired private val authService: AuthService) : KComposite() {

    private val root = ui {


        verticalLayout {
            alignItems = Alignment.CENTER
            justifyContentMode = JustifyContentMode.CENTER
            h1("Авторизація") {
                width = CSS.MAX_CONTENT
            }

            val usernameField = textField("Ім'я користувача") {
                width = 300.px
                isRequiredIndicatorVisible = true
            }

            val passwordField = passwordField("Пароль") {
                width = 300.px
                isRequiredIndicatorVisible = true

            }



            button("Увійти") {
                width = 300.px
                setPrimary()
                addClickShortcut(Key.ENTER)
                onLeftClick {
                    if (authService.isUserLoggedIn()){
                        showError("Ви уже автентифіковані")
                        ui.ifPresent {ui -> ui.navigate(HomeView::class.java) }
                    } else {
                        val _username = usernameField.value
                        val _password = passwordField.value

                        val isAuthenticated = authService.login(_username, _password)
                        when (isAuthenticated) {
                            "true" -> {
                                showSuccess("Вхід успішний")
                                ui.ifPresent {ui -> ui.navigate(HomeView::class.java) }

                            }
                            "false:unf" -> {
                                usernameField.isInvalid = true
                                usernameField.errorMessage  ="Користувача не знайдено"
                            }
                            "false:pnv" -> {
                                passwordField.isInvalid = true
                                passwordField.errorMessage = "Пароль невірний"
                            }
                        }
                    }
                }
            }

            button("Реєстрація") {
                width = 300.px
                onLeftClick {
                    ui.ifPresent {ui -> ui.navigate(RegistrationView::class.java) }
                }
            }
        }



        }



}

