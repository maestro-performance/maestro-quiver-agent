package org.maestro.agent.ext.requests.genericrequest

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.maestro.client.notes.*;

import org.maestro.agent.base.AbstractHandler

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
        String logDir = System.getProperty("maestro.log.dir")

        logger.info("Erasing old data")
        "rm -rf ${logDir}/quiver".execute();

//        logger.info("Obtaining Quiver image")
//        if (executeOnShell("docker pull docker.io/ssorj/quiver") != 0) {
//            logger.error("Unable to pull the Quiver image")
//            this.getClient().publish(MaestroTopics.MAESTRO_TOPIC, new InternalError())
//
//            return null
//        }
//
//        logger.info("Creating temporary docker volume")
//        if (executeOnShell("docker volume create maestro-quiver") != 0) {
//            logger.error("Unable to create the quiver volume")
//            this.getClient().publish(MaestroTopics.MAESTRO_TOPIC, new InternalError())
//
//            return null
//        }
//
//        logger.info("Obtaining the volume directory")
//        def volumeProc = "docker volume inspect maestro-quiver".execute()
//        volumeProc.waitFor()
//
//        if (volumeProc.exitValue() != 0) {
//            logger.error("Unable to query the temporary volume")
//            this.getClient().publish(MaestroTopics.MAESTRO_TOPIC, new InternalError())
//
//            return null
//        }
//
//        def slurper = new JsonSlurper()
//        def volumeInfo = slurper.parseText(volumeProc.text)
//
//        def dockerVolumePath =  volumeInfo[0].Mountpoint
//        logger.info("Docker volume directory is {}", dockerVolumePath)
//

        logger.info("Running Quiver")
        def workerOptions = getWorkerOptions();

        String command = 'quiver --output /mnt/quiver '

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
                this.getClient().notifyFailure("Unable to execute the Quiver test")

                return null
            }
            this.getClient().notifySuccess("Quiver test ran successfully")
            logger.info("Quiver test ran successfully")
        }
        finally {
//            String username = System.getProperty("user.name")
//            logger.info("Fixing log file permissions")
//            if (executeOnShell("sudo chown -Rv ${username} ${logDir}") != 0) {
//                logger.error("Unable to fix the permissions of the report files")
//                this.getClient().publish(MaestroTopics.MAESTRO_TOPIC, new InternalError())
//            }
        }

        return null
    }
}