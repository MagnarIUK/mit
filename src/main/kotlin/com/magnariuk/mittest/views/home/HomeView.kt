package com.magnariuk.mittest.views.home
import com.github.mvysny.karibudsl.v10.*
import com.magnariuk.mittest.data_api.Project
import com.magnariuk.mittest.util.config.AuthService
import com.magnariuk.mittest.util.config.ProjectService
import com.magnariuk.mittest.util.config.UserService
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.NativeLabel
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent.*
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.provider.ListDataProvider
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.router.*
import org.springframework.beans.factory.annotation.Autowired
import com.magnariuk.mittest.util.util.*
import com.magnariuk.mittest.views.MainLayout


@PageTitle("Проєкти")
@Route(value = "home", layout = MainLayout::class)
class HomeView(
    @Autowired private val authService: AuthService,
    @Autowired private val projectService: ProjectService,
    private val userService: UserService
): KComposite(), BeforeEnterObserver {
    private var authenticated: Boolean = authService.isUserLoggedIn()
    private var user = authService.getLoggedInUser()
    private lateinit var dynamicLayout: VerticalLayout


    override fun beforeEnter(event: BeforeEnterEvent) {
        updateUI()
    }

    private val root = ui {
        verticalLayout {
            alignItems = Alignment.CENTER
            justifyContentMode = JustifyContentMode.CENTER


            dynamicLayout = verticalLayout {
                alignItems = Alignment.CENTER
                justifyContentMode = JustifyContentMode.CENTER

            }


        }
    }
    private fun updateUI() {
        dynamicLayout.removeAll()

        if(authenticated) {
            val projects = projectService.getProjectsWithAccess(user!!.id).map { projectService.getProjectById(it.project_id)!! }

            val dataProvider = ListDataProvider(projects)
            val searchField = TextField().apply {
                placeholder = "Пошук..."
                width = 300.px
                addValueChangeListener { event ->
                    val filterText = event.value ?: ""
                    dataProvider.setFilter { project ->
                        project.name.contains(filterText, ignoreCase = true) ||
                        project.description.contains(filterText, ignoreCase = true) ||
                        userService.getUserById(project.author_id)!!.display_name!!.contains(filterText, ignoreCase = true) ||
                        userService.getUserById(project.author_id)!!.username.contains(filterText, ignoreCase = true)
                    }
                }

            }


            val projectsGrid = Grid<Project>().apply {
                style.set(CSS.BORDER_RADIUS, 15.px)
                setDataProvider(dataProvider)
                addColumn { it.name }.setHeader("Назва").setSortable(true)

                addColumn(ComponentRenderer { project ->
                    val shortDescription = if (project.description.length >10) project.description.take(10) + "..." else project.description
                    val descriptionLabel = NativeLabel(shortDescription).apply {
                        style.set(CSS.OVERFLOW_WRAP, OVERFLOW_WRAP.BREAK_WORD)
                        style.set(CSS.MAX_WIDTH, 300.px)
                        style.set(CSS.TRANSITION, ELEMENT().add(TRANSITION_D.ALL).add(0.3.s).add(TRANSITION_D.EASE).css())
                    }

                    val toggleButton = Button().apply {
                        icon = Icon(VaadinIcon.ANGLE_DOWN)
                        addClickListener {
                            val dialog = Dialog()
                            dialog.add(NativeLabel(project.description).apply {
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
                }).setHeader("Опис")

                addColumn(ComponentRenderer { project ->
                    val author = userService.getUserById(project.author_id)
                    Button(author!!.username).apply {
                        style.set(CSS.COLOR, COLORS.BLUE)
                        addThemeVariants(ButtonVariant.LUMO_TERTIARY)
                        addClickListener {
                            UI.getCurrent().navigate("user?u=${author.username}")
                        }
                    }
                }).setHeader("Автор").setSortable(true)

                /*addColumn(ComponentRenderer { project ->
                    Button("Деталі").apply {
                        addThemeVariants(ButtonVariant.LUMO_TERTIARY)
                        addClickListener {
                                UI.getCurrent().navigate("project?p=${project.project_id}")
                        }
                    }
                }).setHeader("Деталі")*/
                addItemClickListener { event ->
                    UI.getCurrent().navigate("project?p=${event.item.project_id}")

                }


                height = 400.px
            }


            dynamicLayout.add(
                searchField,
                projectsGrid,
                Button("Створити проєкт").apply {
                    addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                    onLeftClick {
                        val dialog = Dialog()
                        dialog.headerTitle = "Створення проєкту"
                        val name = TextField().apply {
                            placeholder = "Введіть назву проєкту"
                            maxLength = 255
                        }
                        val desc = TextArea().apply {
                            placeholder = "Введіть опис проєкту"
                            maxLength = 1000
                        }
                        dialog.add(
                            VerticalLayout(

                                name, desc,
                                HorizontalLayout(
                                    Button("Скасувати").apply {
                                        onLeftClick {
                                            dialog.close()
                                        }
                                    },
                                    Button("Створити").apply {
                                        addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                                        onLeftClick {
                                            if(name.value.length <= 20){
                                                if(desc.value.length <= 1000){
                                                    projectService.createProject(
                                                        name = name.value,
                                                        description = desc.value,
                                                        user = user!!
                                                    )
                                                    showSuccess("Створено проєкт")
                                                    dialog.close()
                                                    updateUI()
                                                } else{
                                                    showError("Завеликий опис (максимально - 1000 символів")
                                                }

                                            } else{
                                                showError("Завелика назва (максимально - 20 символів)")
                                            }

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



    }



}