package org.maestro.agent.ext.requests.genericrequest

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.maestro.client.notes.*;
import org.maestro.common.worker.TestLogUtils;

import org.maestro.agent.base.AbstractHandler

import java.io.File

/**
 * The process execution code here was taken from the examples provided by Joerg Mueller
 * on http://www.joergm.com/2010/09/executing-shell-commands-in-groovy. They were slightly
 * modified to adjust to the agent code
 */
class UserCommand1Handler extends AbstractHandler {
    private static final Logger logger = LoggerFactory.getLogger(UserCommand1Handler.class);

    def executeOnShell(String command) {
        return executeOnShell(command, new File(System.properties.'user.dir'))
    }

    def executeOnShell(String command, File workingDir) {
        logger.debug("Executing {}", command)

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

    @Override
    Object handle() {
        String baseLogDirStr = System.getProperty("maestro.log.dir")

        if (baseLogDirStr == null) {
            logger.error("Cannot continue without the log directory");

            this.getClient().notifyFailure(getCurrentTest(), "The log directory is not set on the agent")

            return null
        }

        File baseLogDir = new File(baseLogDirStr);
        File testLogDir = TestLogUtils.nextTestLogDir(baseLogDir);
        String logDir = testLogDir.getPath();

        logger.info("Erasing old data")
        "rm -rf ${logDir}/quiver".execute();

        logger.info("Running Quiver")
        def workerOptions = getWorkerOptions();

        String command = "quiver --output ${logDir}"

        UserCommand1Request request = (UserCommand1Request) getNote()
        String arrow = request.getPayload()

        if (arrow != null) {
            logger.info("Using quiver arrow {}", arrow)
            command = command + " --arrow " + arrow
        }

        try {
            command = command + " " + workerOptions.getBrokerURL()
            if (executeOnShell(command) != 0) {
                logger.warn("Unable to execute the Quiver test")
                this.getClient().notifyFailure(getCurrentTest(), "Unable to execute the Quiver test")

                return null
            }

            TestLogUtils.createSymlinks(baseLogDir, false);

            this.getClient().notifySuccess(getCurrentTest(), "Quiver test ran successfully")
            logger.info("Quiver test ran successfully")

        }
        catch (Exception e) {
            TestLogUtils.createSymlinks(baseLogDir, true);

            this.getClient().notifyFailure(getCurrentTest(), e.getMessage())

            return null
        }

        return null
    }
}