package com.magnariuk.mittest.views.project
import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.onLeftClick
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.github.mvysny.kaributools.setPrimary
import com.magnariuk.mittest.data_api.Commit
import com.magnariuk.mittest.data_api.Project
import com.magnariuk.mittest.data_api.ProjectAccess
import com.magnariuk.mittest.data_api.User
import com.magnariuk.mittest.util.config.AuthService
import com.magnariuk.mittest.util.config.CommitService
import com.magnariuk.mittest.util.config.ProjectService
import com.magnariuk.mittest.util.config.UserService
import com.magnariuk.mittest.util.enums.AccessLevels
import com.magnariuk.mittest.util.enums.AccessLevelsAdd
import com.magnariuk.mittest.util.enums.CommitStatuses
import com.magnariuk.mittest.util.util.*
import com.magnariuk.mittest.views.MainLayout
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.Anchor
import com.vaadin.flow.component.html.NativeLabel
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.component.upload.Upload
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer
import com.vaadin.flow.data.provider.ListDataProvider
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.router.*
import org.springframework.beans.factory.annotation.Autowired
import java.io.InputStream

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@PageTitle("Проєкт")
@Route("/project", layout = MainLayout::class)
class ProjectView(
    @Autowired private val authService: AuthService,
    @Autowired private val userService: UserService,
    @Autowired private val projectService: ProjectService,
    @Autowired private val commitService: CommitService
): KComposite(), BeforeEnterObserver {
    private var user = authService.getLoggedInUser()
    private var currentProject: Project? = null
    private var currentAccess: ProjectAccess? = null
    private var authenticated: Boolean = authService.isUserLoggedIn();
    private var estimatedUser: User? = null

    private lateinit var dynamicLayout: VerticalLayout

    override fun beforeEnter(event: BeforeEnterEvent) {
        val queryParameters: QueryParameters = event.location.queryParameters

        val projectParam = queryParameters.parameters["p"]?.firstOrNull()
        if (projectParam != null && user != null) {
            currentProject = projectService.getProjectById(projectParam.toInt())
            if (currentProject != null) currentAccess = projectService.getAccessByProjectAndUser(currentProject!!.project_id, user!!.id)

        }



        val userParam = queryParameters.parameters["u"]?.firstOrNull()
        if (userParam != null) estimatedUser = userService.getUserByUsername(userParam)


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
        }

    }

    private fun updateUI() {
        dynamicLayout.removeAll()

        if(currentProject != null && currentAccess != null){
            if(authenticated){
                val projectAuthor = userService.getUserById(currentProject!!.author_id)

                val projectUsers = projectService.getUsersWithAccess(currentProject!!.project_id)
                println(projectUsers)
                val users: List<User> = projectUsers.map { userService.getUserById(it.user_id)!! }.sortedWith { user1, user2 ->
                    val access1 = projectService.getAccessByProjectAndUser(currentProject!!.project_id, user1.id)
                    val access2 = projectService.getAccessByProjectAndUser(currentProject!!.project_id, user2.id)

                    val level1 = access1?.access_level ?: 0
                    val level2 = access2?.access_level ?: 0


                    level2.compareTo(level1) }


                val addUserButton = Button("Додати користувача").apply {
                    onLeftClick {
                        val dialog = Dialog()
                        dialog.headerTitle = "Додати користувача"
                        val usernameField = TextField().apply {
                            placeholder = "Введіть юзернейм користувача, якого хочете додати"
                            isRequiredIndicatorVisible = true
                        }
                        val accessLevelSelector = ComboBox<AccessLevelsAdd>().apply {
                            label = "Рівень доступу"
                            setItems(AccessLevelsAdd.entries)
                            setItemLabelGenerator { it.displayName }
                            isRequiredIndicatorVisible = true
                        }

                        dialog.add(
                            VerticalLayout(
                                usernameField, accessLevelSelector,
                                HorizontalLayout(
                                    Button("Скасувати").apply {
                                        onLeftClick { dialog.close() }
                                    },
                                    Button("Додати").apply {
                                        onLeftClick {
                                            val userToAdd = userService.getUserByUsername(usernameField.value)
                                            if(userToAdd != null) {
                                                if(!accessLevelSelector.isEmpty){
                                                    setPrimary()
                                                    projectService.addProjectAccess(currentProject!!.project_id, userToAdd.id, accessLevelSelector.value.level)
                                                    showSuccess("Додано користувача")
                                                    updateUI()
                                                    dialog.close()
                                                }else{
                                                    accessLevelSelector.isInvalid = true
                                                    accessLevelSelector.errorMessage = "Оберіть рівень доступу"
                                                }
                                            } else{
                                                usernameField.isInvalid = true
                                                usernameField.errorMessage = "Невідомий користувач"
                                            }
                                        }
                                    }
                                )
                            )
                        )
                        dialog.open()
                    }

                }
                val delProjectButton = Button().apply {
                    addThemeVariants(ButtonVariant.LUMO_ERROR)
                    icon = Icon(VaadinIcon.TRASH)
                    onLeftClick {
                        val dialog = Dialog()
                        dialog.headerTitle = "Видалити проєкт?"
                        dialog.add(
                            HorizontalLayout(

                                Button("Скасувати").apply {
                                    onLeftClick { dialog.close() }
                                },
                                Button("Видалити").apply {
                                    setPrimary()
                                    addThemeVariants(ButtonVariant.LUMO_WARNING)
                                    onLeftClick {
                                        dialog.close()
                                        UI.getCurrent().navigate("home")
                                        projectService.delProject(currentProject!!.project_id, user!!)
                                        showSuccess("Проєкт видалено")
                                    }
                                }
                            )
                        )
                        dialog.open()
                    }

                }
                val nameHolder = HorizontalLayout(
                    NativeLabel(currentProject!!.name).apply {
                        style.set("overflow-wrap", "break-word")
                        width = 200.px
                        style.set("margin-bottom", 5.px)
                    }
                )

                val descriptionHolder = HorizontalLayout(
                    NativeLabel(currentProject!!.description).apply {
                        style.set("overflow-wrap", "break-word")
                        style.set("overflow", "auto")
                        width = 290.px
                        maxWidth = 290.px
                        style.set("margin-bottom", 5.px)
                    }
                )




                if(currentAccess!!.access_level >= 3){
                    nameHolder.add(
                        Button().apply {
                            icon = Icon(VaadinIcon.EDIT)
                            addThemeVariants(ButtonVariant.LUMO_SUCCESS)
                            onLeftClick {
                                val dialog = Dialog()
                                dialog.headerTitle = "Зміна назви проєкту"
                                val newName = TextField().apply {
                                    placeholder = "Введіть нову назву"
                                }
                                dialog.add(
                                    VerticalLayout(
                                        newName,
                                        HorizontalLayout(
                                            Button("Скасувати").apply {
                                                addThemeVariants(ButtonVariant.LUMO_TERTIARY)
                                                onLeftClick { dialog.close() }
                                            },
                                            Button("Змінити").apply {
                                                setPrimary()
                                                addThemeVariants(ButtonVariant.LUMO_SUCCESS)
                                                onLeftClick {
                                                    projectService.changeProject(currentProject!!.project_id, projectName =  newName.value, userId = user!!.id)
                                                    dialog.close()
                                                    updateUI()
                                                }
                                            }
                                        )
                                        )
                                )

                                dialog.open()
                            }
                        }
                    )
                    descriptionHolder.add(
                        Button().apply {
                            icon = Icon(VaadinIcon.EDIT)
                            addThemeVariants(ButtonVariant.LUMO_SUCCESS)
                            onLeftClick {
                                val dialog = Dialog()
                                dialog.headerTitle = "Зміна опису проєкту"
                                val newDescription = TextArea().apply {
                                    placeholder = "Введіть новий опис"
                                    maxLength = 1000
                                }
                                dialog.add(
                                    VerticalLayout(
                                        newDescription,
                                        HorizontalLayout(
                                            Button("Скасувати").apply {
                                                addThemeVariants(ButtonVariant.LUMO_TERTIARY)
                                                onLeftClick { dialog.close() }
                                            },
                                            Button("Змінити").apply {
                                                setPrimary()
                                                addThemeVariants(ButtonVariant.LUMO_SUCCESS)
                                                onLeftClick {
                                                    projectService.changeProject(currentProject!!.project_id, projectDescription = newDescription.value, userId = user!!.id)
                                                    dialog.close()
                                                    updateUI()
                                                }
                                            }
                                        )
                                    )
                                )

                                dialog.open()
                            }
                        }
                    )
                }
                val leftBarHolder = VerticalLayout(
                    nameHolder,
                    descriptionHolder,
                    HorizontalLayout(
                        NativeLabel("Автор:"),
                        Anchor("/user?u=${currentProject!!.author_id}",
                            projectAuthor!!.display_name ?: projectAuthor.username
                        ).apply {
                            style.set("overflow-wrap", "break-word")

                            style.set("margin-bottom", 10.px)
                        }
                    ),
                    Grid<User>().apply {
                        if(currentAccess!!.access_level != 3){
                            addColumn {it.display_name?: it.username}.setHeader("Користувачі:")

                            addColumn {
                                val ac: ProjectAccess? = projectService.getAccessByProjectAndUser(currentProject!!.project_id, it.id)
                                AccessLevels.getDisplayName(ac!!.access_level)
                            }.setSortable(false)
                            addItemClickListener { event ->
                                UI.getCurrent().navigate("/user?u=${event.item.username}")
                            }


                        } else{
                            addColumn {it.display_name?: it.username}.setHeader("Користувачі:")
                            addColumn(ComponentRenderer { user1 ->
                                    val user1AccessToThisProject: ProjectAccess? = projectService.getAccessByProjectAndUser(currentProject!!.project_id, user1.id)
                                    val projectOwnerAccess: ProjectAccess? = projectService.getProjectOwner(currentProject!!.project_id)
                                    val delButton = Button().apply {
                                        icon = Icon(VaadinIcon.TRASH)
                                        addThemeVariants(ButtonVariant.LUMO_TERTIARY)
                                        onLeftClick {
                                            val dialog = Dialog()
                                            dialog.headerTitle = "Видалити користувача?"
                                            dialog.add(
                                                HorizontalLayout(

                                                    Button("Скасувати").apply {
                                                        onLeftClick { dialog.close() }
                                                    },
                                                    Button("Видалити").apply {
                                                        setPrimary()
                                                        addThemeVariants(ButtonVariant.LUMO_WARNING)
                                                        onLeftClick {
                                                            projectService.deleteProjectAccess(user1AccessToThisProject!!.access_id)
                                                            dialog.close()
                                                            updateUI()
                                                            showSuccess("Доступ користувача прибрано")
                                                        }
                                                    }
                                                )
                                            )
                                            dialog.open()
                                        }
                                    }

                                    val editButton = Button().apply {
                                        icon = Icon(VaadinIcon.EDIT)
                                        addThemeVariants(ButtonVariant.LUMO_TERTIARY)
                                        onLeftClick {
                                            val dialog = Dialog()
                                            dialog.headerTitle = "Змінити права користувача ${user1.display_name?: user1.username}"
                                            val accessLevelSelector = ComboBox<AccessLevels>().apply {
                                                label = "Рівень доступу"
                                                setItems(AccessLevels.entries)
                                                setItemLabelGenerator { it.displayName }
                                                isRequiredIndicatorVisible = true
                                            }

                                            dialog.add(
                                                VerticalLayout(
                                                    accessLevelSelector,
                                                    HorizontalLayout(
                                                        Button("Скасувати").apply {
                                                            onLeftClick { dialog.close() }
                                                        },
                                                        Button("Змінити").apply {
                                                            setPrimary()
                                                            onLeftClick {
                                                                if(!accessLevelSelector.isEmpty){
                                                                    setPrimary()
                                                                    if(accessLevelSelector.value.level == 3){

                                                                        val dialog2 = Dialog()
                                                                        dialog2.headerTitle = "Ви впевнені, що хочете передати права на даний проєкт ${user1.display_name ?: user1.username}"
                                                                        dialog2.add(
                                                                            HorizontalLayout(
                                                                                Button("Скасувати").apply {
                                                                                    onLeftClick {
                                                                                        dialog2.close()
                                                                                    }
                                                                                },
                                                                                Button("Підтвердити").apply {
                                                                                    addThemeVariants(ButtonVariant.LUMO_WARNING)
                                                                                    onLeftClick {
                                                                                        projectService.changeProjectOwner(currentProject!!.project_id, user1, user!!)
                                                                                        showSuccess("Права власника передано")
                                                                                        updateUI()
                                                                                        dialog2.close()
                                                                                        dialog.close()
                                                                                    }

                                                                                }
                                                                            )
                                                                        )

                                                                        dialog2.open()

                                                                    } else{
                                                                        projectService.changeProjectAccess(user1AccessToThisProject!!.access_id, accessLevelSelector.value.level)
                                                                        showSuccess("Оновлено доступ користувача")
                                                                        updateUI()
                                                                        dialog.close()
                                                                    }
                                                                }else{
                                                                    accessLevelSelector.isInvalid = true
                                                                    accessLevelSelector.errorMessage = "Оберіть рівень доступу"
                                                                }
                                                            }
                                                        }

                                                    )
                                                )
                                            )
                                            dialog.open()
                                        }
                                    }

                                    val buttonsHolder = HorizontalLayout(

                                    ).apply {
                                        isSpacing = false
                                        alignItems = Alignment.END
                                    }

                                if(projectOwnerAccess!!.user_id != user1.id){
                                    buttonsHolder.add(editButton, delButton)
                                }



                                    HorizontalLayout(
                                        NativeLabel(AccessLevels.getDisplayName(user1AccessToThisProject!!.access_level)),
                                        buttonsHolder,


                                    ).apply {

                                    }

                            }).setSortable(false)

                            addItemClickListener {event ->
                                val clickedItem = event.item
                                UI.getCurrent().navigate("/user?u=${clickedItem.username}")
                            }
                        }
                        height = 200.px
                        dataProvider = ListDataProvider(users)

                    },


                    ).apply {
                    alignItems = Alignment.CENTER
                    justifyContentMode = JustifyContentMode.CENTER
                    width = 400.px
                    style.set("background-color", "#f0f0f0")
                    style.set("border-radius", "10px")
                    style.set("padding", "10px")
                    style.set("margin", "5px")
                    style.set("border", "1px solid #d3d3d3")
                }

                if(currentAccess!!.access_level >= 3){
                    leftBarHolder.add(HorizontalLayout(
                        addUserButton, delProjectButton
                    ))
                }


                val commits = commitService.getCommitsByProjectId(currentProject!!.project_id)
                val commitDataProvider = ListDataProvider(commits)

                var searchField = TextField().apply {
                    placeholder = "Пошук..."
                    width = 300.px

                    addValueChangeListener { event ->
                        val filterText = event.value ?: ""
                        commitDataProvider.setFilter { commit ->
                            commit.message.contains(filterText, ignoreCase = true) ||
                            userService.getUserById(commit.author_id)!!.username.contains(filterText, ignoreCase = true)
                        }
                    }
                }

                var commitsGrid = Grid<Commit>().apply {
                    style.set(CSS.BORDER_RADIUS, 15.px)
                    dataProvider = commitDataProvider

                    addColumn { unixToDate(it.commited_at) }.setHeader("Час внеску").apply {
                        isSortable = true
                        key = "commited_at"
                    }
                    addColumn { CommitStatuses.getLabel(it.status) }.setHeader("Статус").apply {
                        isSortable = true
                        key = "status"
                    }

                    addColumn ( ComponentRenderer { commit ->
                        val user = userService.getUserById(commit.author_id)!!

                        Button(user.username).apply {
                            onLeftClick {
                                UI.getCurrent().navigate("/user?u=${user.username}")
                            }
                        }

                    })

                    addItemClickListener { event ->
                        UI.getCurrent().navigate("/commit?c=${event.item.commit_hash}")
                    }

                }


                val rightBarHolder = VerticalLayout(
                    HorizontalLayout(
                        Button("Створити внесок").apply {
                            onLeftClick {
                                val dialog = Dialog()
                                dialog.headerTitle = "Створення внеску"
                                val message = TextArea().apply {
                                    placeholder = "Введіть повідомлення внеску"
                                    maxLength = 500
                                }

                                var files: MutableMap<String, InputStream> = mutableMapOf()
                                val buffer = MultiFileMemoryBuffer()

                                val uploader = Upload(buffer).apply {
                                    addSucceededListener { event ->

                                        val fileName = event.fileName
                                        val inputStream = buffer.getInputStream(fileName)
                                        println(fileName)
                                        files[fileName] = inputStream
                                    }
                                }



                                dialog.add(
                                    VerticalLayout(
                                        message,
                                        uploader,
                                        HorizontalLayout(
                                            Button("Скасувати").apply {
                                            onLeftClick {
                                                dialog.close()
                                            }
                                        },
                                            Button("Створити").apply {
                                                addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                                                onLeftClick {
                                                    var hashCode = generateRandomHex(33)

                                                    while (commitService.getCommitsByCommitHash(hashCode)!= null){
                                                        hashCode = generateRandomHex(33)
                                                    }

                                                        commitService.createCommit(
                                                            currentProject!!.project_id,
                                                            user!!,
                                                            hashCode,
                                                            message.value,
                                                            files
                                                        )

                                                        showSuccess("Створено внесок")
                                                        dialog.close()
                                                        updateUI()


                                                }
                                            })
                                    )
                                )
                                dialog.open()
                            }
                        },
                        searchField
                    ),
                    commitsGrid
                ).apply {
                    alignItems = Alignment.CENTER
                    justifyContentMode = JustifyContentMode.CENTER
                    width = 800.px
                    style.set(CSS.BACKGROUND_COLOR, "f0f0f0".hex)
                    style.set(CSS.BORDER_RADIUS, 10.px)
                    style.set(CSS.PADDING, 10.px)
                    style.set(CSS.MARGIN, 5.px)
                    style.set(CSS.BORDER, ELEMENT().add(1.px).add(CSS.SOLID).add("d3d3d3".hex).css())
                }


                dynamicLayout.add(
                    HorizontalLayout(
                        leftBarHolder,
                        rightBarHolder
                    ).apply {}
                )


            }else{
                showError("Нема доступу", 0, true)
            }
        }else{
            showError("Нема доступу", 0, true)
        }


    }




}