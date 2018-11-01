package org.maestro.agent.ext.requests.genericrequest

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.maestro.client.notes.*;

import org.maestro.agent.base.AbstractHandler


class UserCommand1Handler extends AbstractHandler {
    private static final Logger logger = LoggerFactory.getLogger(UserCommand1Handler.class);

    @Override
    Object handle() {
        String logDir = getTestLogDir().getPath()

        logger.info("Running Quiver")
        String command = "quiver --output ${logDir}"

        UserCommand1Request request = (UserCommand1Request) getNote()
        String arrow = request.getPayload()

        if (arrow != null) {
            logger.info("Using quiver arrow {}", arrow)
            command = command + " --arrow " + arrow
        }

        try {
            command = command + " " + getWorkerOptions().getBrokerURL()
            if (super.executeOnShell(command) != 0) {
                logger.warn("Unable to execute the Quiver test")
                this.getClient().notifyFailure(getCurrentTest(), "Unable to execute the Quiver test")

                return null
            }

            createTestSuccessSymlinks();

            this.getClient().notifySuccess(getCurrentTest(), "Quiver test ran successfully")
            logger.info("Quiver test ran successfully")

        }
        catch (Exception e) {
            createTestFailSymlinks();

            this.getClient().notifyFailure(getCurrentTest(), e.getMessage())

            return null
        }

        return null
    }
}