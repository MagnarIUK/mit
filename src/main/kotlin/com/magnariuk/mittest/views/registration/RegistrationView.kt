package com.magnariuk.mittest.views.registration

import com.github.mvysny.karibudsl.v10.*
import com.vaadin.flow.component.Key
import com.github.mvysny.kaributools.setPrimary
import com.magnariuk.mittest.util.config.AuthService
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode
import com.vaadin.flow.router.Menu
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import com.vaadin.flow.server.auth.AnonymousAllowed
import org.springframework.beans.factory.annotation.Autowired
import com.magnariuk.mittest.util.util.CSS
import com.magnariuk.mittest.util.util.px
import com.magnariuk.mittest.util.util.showSuccess
import com.magnariuk.mittest.views.home.HomeView
import com.magnariuk.mittest.views.login.LoginView
import com.vaadin.flow.router.RouteAlias

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@PageTitle("Регістрація")
@Route(value = "registration")
@RouteAlias("reg")
@AnonymousAllowed
class RegistrationView(
    @Autowired private val authService: AuthService) : KComposite() {
    private val root = ui {
        verticalLayout {
            alignItems = Alignment.CENTER
            justifyContentMode = JustifyContentMode.CENTER
            h1("Реєстрація"){
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

            val visibleNameField = textField("Відображуване ім'я") {
                width = 300.px
            }
            val emailField = textField("Email") {
                width = 300.px
            }


            button("Створити акаунт"){
                width = 300.px
                addClickShortcut(Key.ENTER)
                setPrimary()
                onLeftClick {
                    val _username = usernameField.value
                    val _password = passwordField.value
                    val _email = emailField.value
                    val _visibleName = visibleNameField.value

                    usernameField.style.remove(CSS.BORDER_COLOR)
                    passwordField.style.remove(CSS.BORDER_COLOR)

                    var valid = true


                    if (_username.length < 5){
                        println("Ім'я користувача має бути не коротшим за 5 символів")
                        usernameField.isInvalid =true
                        usernameField.errorMessage = "Ім'я користувача має бути не коротшим за 5 символів"
                        valid = false
                    } else if(_password.length < 8){
                        println("Пароль має бути не коротшим за 8 символів")
                        passwordField.isInvalid = true
                        passwordField.errorMessage = "Пароль має бути не коротшим за 8 символів"
                        valid = false
                    } else if(_username.containsWhitespace()){
                        println("Ім'я користувача має містити пробілів")
                        usernameField.isInvalid =true
                        usernameField.errorMessage = "Ім'я користувача має містити пробілів"
                        valid = false
                    } else if(_password.containsWhitespace()){
                        println("Пароль має містити пробілів")
                        passwordField.isInvalid =true
                        passwordField.errorMessage = "Пароль має містити пробілів"
                        valid = false
                    }


                    if(valid){
                        val isRegistrated = authService.register(_username, _password, _visibleName, _email)

                        if (isRegistrated) {
                            showSuccess("Зареєстровано, увійдіть")
                            ui.ifPresent {ui -> ui.navigate(LoginView::class.java) }
                        } else{
                            println("Даний користувач вже існує")
                            usernameField.isInvalid =true
                            usernameField.errorMessage = "Даний користувач вже існує"
                        }
                    }


                }


            }

            button("Увійти"){
                width = 300.px
                onLeftClick {
                    ui.ifPresent {ui -> ui.navigate(LoginView::class.java) }
                }
            }

        }
    }

}