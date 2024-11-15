package com.magnariuk.mittest.views.user

import com.github.mvysny.karibudsl.v10.*
import com.magnariuk.mittest.data_api.User
import com.magnariuk.mittest.data_api.UserActivity
import com.magnariuk.mittest.util.config.AuthService
import com.magnariuk.mittest.util.config.UserService
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.NativeLabel
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.PasswordField
import com.vaadin.flow.component.textfield.TextField
import org.springframework.beans.factory.annotation.Autowired
import com.magnariuk.mittest.util.enums.ActivityTypes
import com.magnariuk.mittest.util.util.*
import com.magnariuk.mittest.views.MainLayout
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.combobox.MultiSelectComboBox
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.data.provider.ListDataProvider
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.router.*

@PageTitle("Користувач")
@Route("/user", layout = MainLayout::class)
class UserView(
    @Autowired private val authService: AuthService,
    @Autowired private val userService: UserService,
): KComposite(), BeforeEnterObserver {
    var user = authService.getLoggedInUser()
    var authenticated: Boolean = authService.isUserLoggedIn();
    var currentUser: User? = null

    private lateinit var dynamicLayout: VerticalLayout

    override fun beforeEnter(event: BeforeEnterEvent) {
        val queryParameters: QueryParameters = event.location.queryParameters

        val usernameParam = queryParameters.parameters["u"]?.firstOrNull()
        if (usernameParam != null) {
            currentUser = userService.getUserByUsername(usernameParam)
        }

        updateUI()
    }

    private val root = ui {

        verticalLayout {
            alignItems = Alignment.CENTER
            justifyContentMode = JustifyContentMode.CENTER
            dynamicLayout = verticalLayout {
                justifyContentMode = JustifyContentMode.CENTER
                alignItems = Alignment.CENTER
            }
            /*horizontalLayout {
                alignItems = Alignment.CENTER
                justifyContentMode = JustifyContentMode.CENTER
                width = "600px"

                button("Назад") {
                    width = "30%"
                    setPrimary()
                    onLeftClick {
                        UI.getCurrent().navigate("home")
                    }
                }
                if(user != null){
                    button("Вийти") {
                        width = "10%"
                        addThemeVariants(ButtonVariant.LUMO_TERTIARY)
                        onLeftClick {
                            if(user != null){
                                UI.getCurrent().navigate("logout")
                            }
                        }
                    }
                }



            }*/


        }


    }

    private fun updateUI() {
        dynamicLayout.removeAll()

        if (currentUser == null || currentUser?.id == user?.id) {
            if (user == null) {
                showError("Невідомий користувач")
            }
            else {
                val userProfileLeft = VerticalLayout(
                    HorizontalLayout(NativeLabel("Ім'я користувача: ${user?.username}"), Button("Змінити").apply {
                        onLeftClick {
                            val dialog = Dialog()
                            dialog.headerTitle = "Зміна імені користувача"
                            val field = TextField().apply {
                                placeholder = "Введіть нове ім'я"
                                value = user!!.username
                            }
                            dialog.add(
                                VerticalLayout(
                                    NativeLabel("Ця дія викине вас з акаунту").apply {
                                        style.set(CSS.COLOR, COLORS.RED)
                                    },
                                    field,
                                    HorizontalLayout(
                                        Button("Скасувати").apply {
                                            onLeftClick {
                                                dialog.close()
                                            }
                                        },
                                        Button("Зберегти").apply {
                                            addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                                            onLeftClick {
                                                if(field.value.length >= 5){
                                                    val user_check = userService.getUserByUsername(field.value)
                                                    if(user_check == null || user_check.id == user!!.id) {
                                                        userService.updateUser(user!!.copy(), username = field.value)
                                                        showSuccess("Видиме ім'я змінено на ${field.value}")
                                                        dialog.close()
                                                        UI.getCurrent().navigate("logout")
                                                    } else{
                                                        showError("Користувач з даним ім'ям користувача вже існує")
                                                    }

                                                } else{
                                                    showError("Ім'я користувача має бути не коротшим за 5 символів")
                                                }

                                            }
                                        }
                                    )

                                )
                            )
                            dialog.open()
                        }
                    },
                        Button("Змінити пароль").apply{
                            onLeftClick {
                                val dialog = Dialog()
                                dialog.headerTitle = "Зміна паролю"
                                dialog.width = 500.px
                                val field = PasswordField("Старий пароль").apply {
                                    placeholder = "Введіть старий пароль"
                                    width = 300.px
                                }
                                val field2 = PasswordField("Новий пароль").apply {
                                    placeholder = "Введіть новий пароль"
                                    width = 300.px
                                }
                                dialog.add(
                                    VerticalLayout(
                                        NativeLabel("Ця дія викине вас з акаунту").apply {
                                            style.set(CSS.COLOR, COLORS.RED)
                                        },
                                        field,
                                        field2,
                                        HorizontalLayout(
                                            Button("Скасувати").apply {
                                                onLeftClick {
                                                    dialog.close()
                                                }
                                            },
                                            Button("Зберегти").apply {
                                                addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                                                onLeftClick {
                                                    if (userService.isPasswordValid(user!!, field.value)){
                                                        if(field2.value.length >= 8){
                                                            userService.updateUser(user!!.copy(), rawPassword = field2.value)
                                                            showSuccess("Пароль змінено, увійдіть знову")
                                                            dialog.close()
                                                            UI.getCurrent().navigate("logout")
                                                        } else{
                                                            showError("Пароль має бути не коротшим за 8 символів")
                                                        }

                                                    } else{
                                                        showError("Пароль невірний")
                                                    }

                                                }
                                            }
                                        )

                                    ).apply {
                                        alignItems = Alignment.CENTER
                                        justifyContentMode = JustifyContentMode.CENTER
                                    }
                                )
                                dialog.open()
                            }
                        }
                    ).apply {
                        width = 600.px

                        alignItems = Alignment.CENTER
                        justifyContentMode = JustifyContentMode.CENTER
                        style.set(CSS.BACKGROUND_COLOR, "f0f0f0".hex)
                        style.set(CSS.BORDER_RADIUS, 15.px)
                        style.set(CSS.PADDING, 10.px)
                        style.set(CSS.MARGIN, 5.px)
                        style.set(CSS.BORDER, ELEMENT().add(1.px).add(CSS.SOLID).add("d3d3d3".hex).css())
                    },
                    HorizontalLayout(NativeLabel("Видиме ім'я: ${user?.display_name}"),Button("Змінити").apply {
                        onLeftClick {
                            val dialog = Dialog()
                            dialog.headerTitle = "Зміна видимого імені"
                            val field = TextField().apply {
                                placeholder = "Введіть нове ім'я"
                                value = user!!.display_name
                            }
                            dialog.add(
                                VerticalLayout(
                                    field,
                                    HorizontalLayout(
                                        Button("Скасувати").apply {
                                            onLeftClick {
                                                dialog.close()
                                            }
                                        },
                                        Button("Зберегти").apply {
                                            addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                                            onLeftClick {
                                                userService.updateUser(user!!.copy(), displayName = field.value)
                                                showSuccess("Видиме ім'я змінено на ${field.value}")
                                                dialog.close()
                                                user = authService.updateLoggedUser()
                                                updateUI()
                                            }
                                        }
                                    )

                                )
                            )
                            dialog.open()
                        }


                    } )
                        .apply {
                        width = 600.px

                        alignItems = Alignment.CENTER
                        justifyContentMode = JustifyContentMode.CENTER
                        style.set(CSS.BORDER_RADIUS, 15.px)

                        style.set(CSS.BACKGROUND_COLOR, "f0f0f0".hex)
                        style.set(CSS.BORDER_COLOR, 10.px)
                        style.set(CSS.PADDING, 10.px)
                        style.set(CSS.MARGIN, 5.px)
                        style.set(CSS.BORDER, ELEMENT().add(1.px).add(CSS.SOLID).add("d3d3d3".hex).css())
                    },
                    HorizontalLayout(NativeLabel("Електронна пошта: ${user!!.email}"),Button("Змінити").apply {

                        onLeftClick {
                            val dialog = Dialog()
                            dialog.headerTitle = "Зміна електронної пошти"
                            val emailField = TextField().apply {
                                placeholder = "Введіть нову електронну пошту"
                                value = user!!.email
                            }
                            dialog.add(
                                VerticalLayout(
                                    emailField,
                                    HorizontalLayout(
                                        Button("Скасувати").apply {
                                            onLeftClick {
                                                dialog.close()
                                            }
                                        },
                                        Button("Зберегти").apply {
                                            addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                                            onLeftClick {
                                                val cu = user!!.copy()
                                                userService.updateUser(cu, email = emailField.value)
                                                showSuccess("Електронна пошта змінена на ${emailField.value}")
                                                dialog.close()
                                                user = authService.updateLoggedUser()
                                                updateUI()
                                            }
                                        }
                                    )

                                )
                            )
                            dialog.open()


                        } } )
                        .apply {
                        width = 600.px
                            style.set(CSS.BORDER_RADIUS, 15.px)
                        alignItems = Alignment.CENTER
                        justifyContentMode = JustifyContentMode.CENTER
                        style.set(CSS.BACKGROUND_COLOR, "f0f0f0".hex)
                        style.set(CSS.BORDER_COLOR  , 10.px)
                        style.set(CSS.PADDING, 10.px)
                        style.set(CSS.MARGIN, 5.px)
                        style.set(CSS.BORDER, ELEMENT().add(1.px).add(CSS.SOLID).add("d3d3d3".hex).css())
                    },
                    HorizontalLayout(NativeLabel("Аккаунт створено: ${unixToDate(user!!.created_at)}")).apply {
                        width = 600.px
                        alignItems = Alignment.CENTER
                        justifyContentMode = JustifyContentMode.CENTER
                        style.set(CSS.BORDER_RADIUS, 15.px)
                        style.set(CSS.BACKGROUND_COLOR, "f0f0f0".hex)
                        style.set(CSS.BORDER_COLOR  , 10.px)
                        style.set(CSS.PADDING, 10.px)
                        style.set(CSS.MARGIN, 5.px)
                        style.set(CSS.BORDER, ELEMENT().add(1.px).add(CSS.SOLID).add("d3d3d3".hex).css())
                    })

                val dataProvider = ListDataProvider(userService.getActivities(user!!.id))
                val filterComboBox = MultiSelectComboBox<ActivityTypes>().apply {
                    label = "Типи активностей"
                    setItems(ActivityTypes.entries)
                    setItemLabelGenerator { it.label }
                    width = 600.px
                }

                val searchField = TextField().apply {
                    placeholder = "Пошук..."
                    width = 600.px
                }

                val ignoreAuthFilter = Checkbox().apply {
                    label = "Ігнорувати приватні"
                    value = true
                }





                val applyFilters = {
                    val selectedTypes = filterComboBox.selectedItems.map { it.type }
                    val filterText  = searchField.value ?: ""
                    dataProvider.setFilter { activity ->
                        val isPrivateIgnored = ignoreAuthFilter.value

                        (selectedTypes.isEmpty() || selectedTypes.contains(activity.activity_type)) &&
                                activity.description.contains(filterText, ignoreCase = true) &&
                                (!isPrivateIgnored || !ActivityTypes.isPrivate(activity.activity_type))
                    }
                }
                filterComboBox.addValueChangeListener { applyFilters() }
                searchField.addValueChangeListener { applyFilters() }
                ignoreAuthFilter.addValueChangeListener { applyFilters() }
                applyFilters()

                val activityGrid = Grid<UserActivity>().apply {
                    height = 600.px
                    width = 900.px
                    style.set(CSS.BORDER_RADIUS, 15.px)
                    setDataProvider(dataProvider)
                    addColumn { unixToDate(it.created_at) }.setHeader("Час активності").apply {
                        isSortable = true
                        key = "created_at"
                    }
                    addColumn { ActivityTypes.getDisplayName(it.activity_type)}.setHeader("Тип активності").apply {
                        isSortable = true
                        key = "activity_type"
                    }
                    addColumn( ComponentRenderer { activity ->
                        val shortDescription = if (activity.description.length >10 ) activity.description.take(10) + "..." else activity.description
                        val descriptionLabel = NativeLabel(shortDescription).apply {
                            style.set(CSS.OVERFLOW_WRAP, OVERFLOW_WRAP.BREAK_WORD)
                            style.set(CSS.MAX_WIDTH, 300.px)
                            style.set(CSS.TRANSITION, ELEMENT().add(TRANSITION_D.ALL).add(0.3.s).add(TRANSITION_D.EASE).css())
                        }

                        val toggleButton = Button().apply {
                            icon = Icon(VaadinIcon.ANGLE_DOWN)
                            addClickListener {
                                val dialog = Dialog()
                                dialog.add(NativeLabel(activity.description).apply {
                                    style.set(CSS.OVERFLOW_WRAP, OVERFLOW_WRAP.BREAK_WORD)
                                    style.set(CSS.TRANSITION, ELEMENT().add(CSS.HEIGHT).add(0.5.s).add(TRANSITION_D.EASE).css())
                                    maxWidth = 500.px
                                    style.set(CSS.OVERFLOW, OVERFLOW.AUTO)
                                    style.set(CSS.OVERFLOW, OVERFLOW.AUTO)
                                    maxHeight = 200.px
                                })
                                dialog.open()
                            }
                        }

                        HorizontalLayout(descriptionLabel, toggleButton).apply {
                            isSpacing = true
                            alignItems = Alignment.CENTER
                        }
                    } )

                }




                val userActivities = VerticalLayout(
                    HorizontalLayout(searchField, ignoreAuthFilter).apply {
                        width = 1000.px
                        alignItems = Alignment.CENTER
                        justifyContentMode = JustifyContentMode.CENTER
                    },
                    filterComboBox,
                    activityGrid
                ).apply {
                    width = 1000.px
                    alignItems = Alignment.CENTER
                    justifyContentMode = JustifyContentMode.CENTER
                    style.set(CSS.BORDER_RADIUS, 15.px)
                    style.set(CSS.BACKGROUND_COLOR, "f0f0f0".hex)
                    style.set(CSS.BORDER_COLOR  , 10.px)
                    style.set(CSS.PADDING, 10.px)
                    style.set(CSS.MARGIN, 5.px)
                    style.set(CSS.BORDER, ELEMENT().add(1.px).add(CSS.SOLID).add("d3d3d3".hex).css())
                }




                val userHolder = HorizontalLayout(userProfileLeft, userActivities)
                dynamicLayout.add(userHolder)

            }
        } else {
            val userProfileLeft = VerticalLayout(
                NativeLabel("Ім'я користувача: ${currentUser!!.username}"),
                NativeLabel("Видиме ім'я: ${currentUser!!.display_name}"),
                NativeLabel("Електронна пошта: ${currentUser!!.email}"),
                NativeLabel("Аккаунт створено: ${unixToDate(currentUser!!.created_at)}")
            )

            val dataProvider = ListDataProvider(userService.getActivities(currentUser!!.id)).apply {
                setFilter { activity ->
                    ActivityTypes.isPrivate(activity.activity_type)
                }
            }
            val filterComboBox = MultiSelectComboBox<ActivityTypes>().apply {
                label = "Типи активностей"
                setItems(ActivityTypes.entries)
                setItemLabelGenerator { it.label }
                width = 600.px
            }

            val searchField = TextField().apply {
                placeholder = "Пошук..."
                width = 600.px
            }

            val applyFilters = {
                val selectedTypes = filterComboBox.selectedItems ?: setOf(ActivityTypes.entries)
                val filterText = searchField.value ?: ""

                dataProvider.setFilter { activity ->
                    ( selectedTypes.contains(activity.activity_type) ||
                    activity.description.contains(filterText, ignoreCase = true) ) &&
                    ActivityTypes.isPrivate(activity.activity_type)
                }
            }
            filterComboBox.addValueChangeListener { applyFilters() }
            searchField.addValueChangeListener { applyFilters() }

            val activityGrid = Grid<UserActivity>().apply {
                height = 600.px
                width = 900.px
                style.set(CSS.BORDER_RADIUS, 15.px)
                setDataProvider(dataProvider)
                addColumn { unixToDate(it.created_at) }.setHeader("Час активності").apply {
                    isSortable = true
                    key = "created_at"
                }
                addColumn { ActivityTypes.getDisplayName(it.activity_type)}.setHeader("Тип активності").apply {
                    isSortable = true
                    key = "activity_type"
                }
                addColumn( ComponentRenderer { activity ->
                    val shortDescription = if (activity.description.length >10) activity.description.take(10) + "..." else activity.description
                    val descriptionLabel = NativeLabel(shortDescription).apply {
                    }

                    val toggleButton = Button().apply {
                        icon = Icon(VaadinIcon.ANGLE_DOWN)
                        addClickListener {
                            val dialog = Dialog()
                            dialog.add(NativeLabel(activity.description).apply {
                                style.set(CSS.OVERFLOW_WRAP, OVERFLOW_WRAP.BREAK_WORD)
                                style.set(CSS.TRANSITION, ELEMENT().add(CSS.HEIGHT).add(0.5.s).add(TRANSITION_D.EASE).css())
                                maxWidth = 500.px
                                style.set(CSS.OVERFLOW, OVERFLOW.AUTO)
                                maxHeight = 500.px
                            })
                            dialog.open()
                        }
                    }

                    HorizontalLayout(descriptionLabel, toggleButton).apply {
                        isSpacing = true
                        alignItems = Alignment.CENTER
                    }
                } )
            }
            val userActivities = VerticalLayout(
                HorizontalLayout(searchField).apply {
                    alignItems = Alignment.CENTER
                    justifyContentMode = JustifyContentMode.CENTER
                    width = 1000.px
                },
                filterComboBox,
                activityGrid
            ).apply {
                width = 1000.px
                alignItems = Alignment.CENTER
                justifyContentMode = JustifyContentMode.CENTER
                style.set(CSS.BORDER_RADIUS, 15.px)
                style.set(CSS.BACKGROUND_COLOR, "f0f0f0".hex)
                style.set(CSS.BORDER_COLOR  , 10.px)
                style.set(CSS.PADDING, 10.px)
                style.set(CSS.MARGIN, 5.px)
                style.set(CSS.BORDER, ELEMENT().add(1.px).add(CSS.SOLID).add("d3d3d3".hex).css())
            }


            val userHolder = HorizontalLayout(userProfileLeft, userActivities)

            dynamicLayout.add(userHolder)
        }
    }





}