package org.maestro.agent.ext.requests.genericrequest

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.maestro.client.notes.*;
import org.maestro.common.worker.TestLogUtils;

import org.maestro.agent.base.AbstractHandler

import java.io.File

class UserCommand1Handler extends AbstractHandler {
    private static final Logger logger = LoggerFactory.getLogger(UserCommand1Handler.class);

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
            if (super.executeOnShell(command) != 0) {
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