package com.magnariuk.mittest.views.commit

import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.magnariuk.mittest.data_api.Commit
import com.magnariuk.mittest.data_api.File
import com.magnariuk.mittest.util.config.AuthService
import com.magnariuk.mittest.util.config.CommitService
import com.magnariuk.mittest.util.config.ProjectService
import com.magnariuk.mittest.util.config.UserService
import com.magnariuk.mittest.util.enums.CommitStatuses
import com.magnariuk.mittest.util.util.*
import com.magnariuk.mittest.views.MainLayout
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.combobox.ComboBox
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
import com.vaadin.flow.data.provider.ListDataProvider
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.router.*
import com.vaadin.flow.server.InputStreamFactory
import com.vaadin.flow.server.StreamResource
import org.springframework.beans.factory.annotation.Autowired
import org.vaadin.olli.FileDownloadWrapper
import java.io.ByteArrayInputStream

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@PageTitle("Внесок")
@Route("/commit", layout = MainLayout::class)
class CommitView(
    @Autowired private val authService: AuthService,
    @Autowired private val userService: UserService,
    @Autowired private val projectService: ProjectService,
    @Autowired private val commitService: CommitService
): KComposite(), BeforeEnterObserver {
    var user = authService.getLoggedInUser()
    private var currentCommit: Commit? = null

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

        val commitProjectAccess = projectService.getAccessByProjectAndUser(currentCommit!!.project_id, user!!.id)
        val commitAuthor = userService.getUserById(currentCommit!!.author_id)
        val commitProject = projectService.getProjectById(currentCommit!!.project_id)
        val commitAuthorHolder = Anchor("/user?u=${commitAuthor!!.id}",
            commitAuthor.display_name ?: commitAuthor.username
        ).apply {
            style.set(CSS.OVERFLOW_WRAP, OVERFLOW_WRAP.BREAK_WORD)
            style.set(CSS.MARGIN_BOTTOM, 10.px)
        }
        val commitProjectHolder = Anchor("/user?u=${currentCommit!!.project_id}",
            commitProject!!.name
        ).apply {
            style.set(CSS.OVERFLOW_WRAP, OVERFLOW_WRAP.BREAK_WORD)
            style.set(CSS.MARGIN_BOTTOM, 10.px)
        }

        val statusHolder = if (commitProjectAccess!!.access_level >= 3) {
            ComboBox<CommitStatuses>().apply {
                val items: MutableList<CommitStatuses> = CommitStatuses.entries.toMutableList()
                items.remove(CommitStatuses.PENDING)
                if(currentCommit!!.status != CommitStatuses.AUTO_ACCEPTED.id){
                    items.remove(CommitStatuses.AUTO_ACCEPTED)
                }
                setItems(items)
                setItemLabelGenerator { item ->
                    item.label
                }
                value = CommitStatuses.getById(currentCommit!!.status)!!
                addValueChangeListener { event ->
                    val newSelectedCommitStats = event.value
                    if (newSelectedCommitStats == CommitStatuses.ACCEPTED && newSelectedCommitStats != event.oldValue) {
                        commitService.acceptCommit(currentCommit!!.commit_id)
                        updateUI()
                    } else if(newSelectedCommitStats == CommitStatuses.REJECTED && newSelectedCommitStats != event.oldValue){
                        commitService.rejectCommit(currentCommit!!.commit_id)
                        updateUI()

                    }
                }

            }
        } else{
            ComboBox<CommitStatuses>().apply {
                val items: MutableList<CommitStatuses> = CommitStatuses.entries.toMutableList()
                setItems(items)
                setItemLabelGenerator { item ->
                    item.label
                }
                value = CommitStatuses.getById(currentCommit!!.status)!!
                isEnabled = false
            }
        }

        val leftBarHolder = VerticalLayout(
            HorizontalLayout(NativeLabel("Автор: "), commitAuthorHolder).apply {
                style.set(CSS.MARGIN_BOTTOM, 5.px)
            },
            HorizontalLayout(NativeLabel("Проєкт: "), commitProjectHolder).apply {
                style.set(CSS.MARGIN_BOTTOM, 5.px)
            },
            NativeLabel(currentCommit!!.message).apply {
                style.set(CSS.OVERFLOW_WRAP, OVERFLOW_WRAP.BREAK_WORD)
                style.set(CSS.OVERFLOW, OVERFLOW.AUTO)
                width = 290.px
                maxWidth = 290.px
                style.set(CSS.MARGIN_BOTTOM, 5.px)
            },
            HorizontalLayout(NativeLabel("Статус: "), statusHolder).apply {
                style.set(CSS.MARGIN_BOTTOM, 5.px)
            }
        ).apply {
            alignItems = Alignment.CENTER
            justifyContentMode = JustifyContentMode.CENTER
            width = 400.px
            style.set(CSS.BACKGROUND_COLOR, "f0f0f0".hex)
            style.set(CSS.BORDER_RADIUS, 10.px)
            style.set(CSS.PADDING, 10.px)
            style.set(CSS.MARGIN, 5.px )
            style.set(CSS.BORDER, ELEMENT().add(1.px).add(CSS.SOLID).add("d3d3d3".hex).css())
        }


        val files = commitService.getFilesByCommit(currentCommit!!.commit_id)
        val filesDataProvider = ListDataProvider(files)
        val searchField = TextField().apply {
            placeholder = "Пошук..."
            width = 300.px

            addValueChangeListener { event ->
                val filterText = event.value ?: ""
                filesDataProvider.setFilter { file ->
                    file.file_name.contains(filterText, ignoreCase = true) ||
                            file.file_path.contains(filterText, ignoreCase = true)

                }
            }
        }

        val downloadCommitButton = Button().apply {
            icon = Icon(VaadinIcon.DOWNLOAD)
            addThemeVariants(ButtonVariant.LUMO_ICON)
        }
        val downloadCommitButtonWrapper = FileDownloadWrapper(
            zipFolderToStream(currentCommit!!, projectService.getProjectById(currentCommit!!.project_id)!!)
        )
        downloadCommitButtonWrapper.wrapComponent(downloadCommitButton)

        val filesGrid = Grid<File>().apply {
            dataProvider = filesDataProvider
            addColumn { file ->
                file.file_name
            }.setHeader("Назва файлу")

            addColumn { file ->
                file.file_size.mb
            }.setHeader("Розмір")

            addColumn( ComponentRenderer { fileX ->
                val b =Button().apply {
                    icon = Icon(VaadinIcon.DOWNLOAD)
                    addThemeVariants(ButtonVariant.LUMO_ICON)
                }
                val fileToDownload = java.io.File(fileX.file_path)
                val buttonWrapper = FileDownloadWrapper(
                    StreamResource(fileToDownload.name, InputStreamFactory {ByteArrayInputStream(fileToDownload.readBytes())})
                )
                buttonWrapper.wrapComponent(b)

                buttonWrapper
            })

            addItemClickListener { event ->
                val fileT = java.io.File(event.item.file_path)
                val fileData = String(fileT.readBytes())
                val dialog = com.vaadin.flow.component.dialog.Dialog()
                dialog.headerTitle = event.item.file_name
                dialog.width = 1000.px
                dialog.height = 800.px
                dialog.add(TextArea().apply {
                    value = fileData
                    setSizeFull()
                })
                dialog.open()
            }
        }

        val rightBarHolder = VerticalLayout(
            HorizontalLayout(searchField,downloadCommitButtonWrapper),
            filesGrid
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


    }



}