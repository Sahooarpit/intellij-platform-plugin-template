package org.jetbrains.plugins.template.logs

import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import java.io.BufferedReader
import java.io.InputStreamReader

object SshService {
    fun streamLogs(host: String, user: String, pass: String, path: String, onLine: (String) -> Unit) {
        val ssh = SSHClient()
        ssh.addHostKeyVerifier(PromiscuousVerifier())
        try {
            ssh.connect(host)
            ssh.authPassword(user, pass)
            ssh.startSession().use { session ->
                val cmd = session.exec("tail -f $path")
                val reader = BufferedReader(InputStreamReader(cmd.inputStream))
                reader.forEachLine { onLine(it) }
            }
        } catch (e: Exception) {
            onLine("SSH Error: ${e.message}")
        } finally {
            ssh.disconnect()
        }
    }
}