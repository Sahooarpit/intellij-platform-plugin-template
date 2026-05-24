package org.jetbrains.plugins.template.toolWindow

import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.panel
import org.jetbrains.plugins.template.api.ApiExecutor
import org.jetbrains.plugins.template.logs.SshService
import org.jetbrains.plugins.template.services.AiService
import javax.swing.*

class MainPanel(private val project: Project) {
    // 1. UI Component Definitions
    private val methodCombo = ComboBox(arrayOf("GET", "POST", "PUT", "DELETE"))
    private val urlField = JTextField("https://jsonplaceholder.typicode.com/posts/1")
    private val headersArea = JTextArea(3, 20)
    private val bodyArea = JTextArea(5, 20)
    private val responseArea = JTextArea(10, 20).apply { isEditable = false }

    private val hostField = JTextField("127.0.0.1")
    private val userField = JTextField("root")
    private val passField = JBPasswordField()
    private val logPathField = JTextField("/var/log/syslog")
    private val consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).console

    // 2. The Main Content (Tabs)
    val content: JComponent = JBTabbedPane().apply {
        addTab("API Client", createApiTab())
        addTab("Log Viewer", createLogTab())
    }

    // 3. API Tab UI
    private fun createApiTab(): JPanel = panel {
        row("Method") { cell(methodCombo) }
        row("URL") { cell(urlField).align(Align.FILL) }

        row {
            val subTabs = JBTabbedPane().apply {
                addTab("Headers", createHeadersPanel())
                addTab("Body", createBodyPanel())
            }
            cell(subTabs).align(Align.FILL)
        }

        row {
            button("Send Request") { runApiRequest() }
            button("AI Interpret") {
                val ai = service<AiService>()
                responseArea.text = ai.interpretData(responseArea.text)
            }
        }
        group("Response") {
            row { cell(JBScrollPane(responseArea)).align(Align.FILL) }
        }
    }

    private fun createHeadersPanel(): JPanel = panel {
        row { cell(JBScrollPane(headersArea)).align(Align.FILL) }
    }

    private fun createBodyPanel(): JPanel = panel {
        row { cell(JBScrollPane(bodyArea)).align(Align.FILL) }
    }

    // 4. Log Tab UI
    private fun createLogTab(): JPanel = panel {
        group("SSH Credentials") {
            row("Host") { cell(hostField).align(Align.FILL) }
            row("Username") { cell(userField).align(Align.FILL) }
            row("Password") { cell(passField).align(Align.FILL) }
            row("Log Path") { cell(logPathField).align(Align.FILL) }
        }
        row {
            button("Stream Logs") { runLogStream() }
            button("AI Analyze Logs") {
                val ai = service<AiService>()
                ai.interpretData("Analyzing logs from ${logPathField.text}...")
            }
        }
        row { cell(consoleView.component).align(Align.FILL) }
    }

    // 5. Logic Functions (Must stay inside the MainPanel class)
    private fun runApiRequest() {
        responseArea.text = "Executing request..."
        val url = urlField.text
        val method = methodCombo.selectedItem?.toString() ?: "GET"
        val headers = headersArea.text
        val body = bodyArea.text

        object : Task.Backgroundable(project, "API Request") {
            override fun run(indicator: ProgressIndicator) {
                val result = ApiExecutor.execute(url, method, headers, body)
                ApplicationManager.getApplication().invokeLater {
                    responseArea.text = result
                }
            }
        }.queue()
    }

    private fun runLogStream() {
        consoleView.clear()
        val host = hostField.text
        val user = userField.text
        val pass = String(passField.password)
        val path = logPathField.text

        object : Task.Backgroundable(project, "Streaming Logs") {
            override fun run(indicator: ProgressIndicator) {
                try {
                    SshService.streamLogs(host, user, pass, path) { line ->
                        ApplicationManager.getApplication().invokeLater {
                            consoleView.print(line + "\n", com.intellij.execution.ui.ConsoleViewContentType.NORMAL_OUTPUT)
                        }
                    }
                } catch (e: Exception) {
                    ApplicationManager.getApplication().invokeLater {
                        consoleView.print("Error: ${e.message}\n", com.intellij.execution.ui.ConsoleViewContentType.ERROR_OUTPUT)
                    }
                }
            }
        }.queue()
    }
} // <--- This final brace closes the MainPanel class