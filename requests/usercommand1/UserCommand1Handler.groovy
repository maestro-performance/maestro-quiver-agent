package org.maestro.agent.ext.requests.genericrequest

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.maestro.client.notes.*;
import org.maestro.client.exchange.MaestroTopics

import org.maestro.agent.base.AbstractHandler

class UserCommand1Handler extends AbstractHandler {
    private static final Logger logger = LoggerFactory.getLogger(UserCommand1Handler.class);
    private File workDir = new File("/tmp/maestro/agent");
    private File quiverSourceDir = new File(workDir, "quiver");
    private File quiverInstallDir = new File("/tmp/maestro/quiver");

    def executeOnShell(String command) {
        return executeOnShell(command, new File(System.properties.'user.dir'))
    }

    def executeOnShell(String command, File workingDir) {
        println command
        def process = new ProcessBuilder(addShellPrefix(command))
                                        .directory(workingDir)
                                        .redirectErrorStream(true)
                                        .start()
        process.inputStream.eachLine { logger.debug("Subprocess output: {}", it) }
        process.waitFor();

        return process.exitValue()
    }

    def addShellPrefix(String command) {
        String[] commandArray = new String[3]

        commandArray[0] = "sh"
        commandArray[1] = "-c"
        commandArray[2] = command
        return commandArray
    }

    void quiverInstall() {
        logger.info("Creating directores")

        workDir.mkdirs()

        if (!quiverSourceDir.exists()) {

            logger.info("Cloning the project")
            def cloneProcess = "git clone https://github.com/ssorj/quiver.git".execute(null, workDir)
            cloneProcess.waitFor()

            if (cloneProcess.exitValue() != 0) {
                logger.error("Unable to clone quiver repository")
                cloneProcess.text.eachLine { logger.error("Subprocess output: {}", it) }
                this.getClient().publish(MaestroTopics.MAESTRO_TOPIC, new InternalError())

                return
            }
        }


        logger.info("Building the project")

        def buildProcess = executeOnShell("make build PREFIX=/tmp/maestro/quiver", quiverSourceDir)

        if (buildProcess != 0) {
            logger.error("Unable to build quiver")
            this.getClient().publish(MaestroTopics.MAESTRO_TOPIC, new InternalError())

            return
        }

        logger.info("Installing the project")
        def installProcess = executeOnShell("make install", quiverSourceDir)

        if (installProcess != 0) {
            logger.error("Unable to install quiver")

            this.getClient().publish(MaestroTopics.MAESTRO_TOPIC, new InternalError())

            return
        }

        logger.info("Quiver installed successfully")
        return
    }

    @Override
    Object handle() {
        if (!quiverInstallDir.exists()) {
            quiverInstall()
        }


        logger.info("Running quiver")
        def workerOptions = getWorkerOptions();

        String command = './quiver ' + workerOptions.getBrokerURL()

        executeOnShell(command, quiverInstallDir)

        logger.info("Quiver installed successfully")
        return null
    }
}