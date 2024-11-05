package com.magnariuk.mittest.views.login

import com.github.mvysny.karibudsl.v10.*
import com.vaadin.flow.component.Key
import com.github.mvysny.kaributools.setPrimary
import com.magnariuk.mittest.util.config.AuthService
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import org.springframework.beans.factory.annotation.Autowired
import com.magnariuk.mittest.util.util.CSS
import com.magnariuk.mittest.util.util.px
import com.magnariuk.mittest.util.util.showSuccess


@PageTitle("Вхід")
@Route(value = "login")
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
                    val _username = usernameField.value
                    val _password = passwordField.value

                    val isAuthenticated = authService.login(_username, _password)
                    if (isAuthenticated== "true") {
                        showSuccess("Вхід успішний")
                        UI.getCurrent().navigate("home")

                    } else if(isAuthenticated == "false:unf"){
                        usernameField.isInvalid = true
                        usernameField.errorMessage  ="Користувача не знайдено"
                    } else if(isAuthenticated == "false:pnv"){
                        passwordField.isInvalid = true
                        passwordField.errorMessage = "Пароль невірний"
                    }
                }
            }

            button("Реєстрація") {
                width = 300.px
                onLeftClick {
                    UI.getCurrent().navigate("registration")
                }
            }
        }


        }

}

