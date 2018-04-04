package org.maestro.agent.ext.requests.genericrequest

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.maestro.client.notes.*;
import org.maestro.client.exchange.MaestroTopics

import org.maestro.agent.base.AbstractHandler
import groovy.json.JsonSlurper

class UserCommand1Handler extends AbstractHandler {
    private static final Logger logger = LoggerFactory.getLogger(UserCommand1Handler.class);

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

    @Override
    Object handle() {
        logger.info("Creating directores")

        logger.info("Obtaining quiver image")
        executeOnShell("docker pull docker.io/ssorj/quiver")


        logger.info("Creating temporary docker volume")
        executeOnShell("docker volume create maestro-quiver")

        logger.info("Obtaining the volume directory")
        def volumeProc = "docker volume inspect maestro-quiver".execute()
        volumeProc.waitFor()

        def slurper = new JsonSlurper()
        def volumeInfo = slurper.parseText(volumeProc.text)

        logger.info("Docker volume directory is {}", volumeInfo[0].Mountpoint)


        logger.info("Running quiver via docker")
        def workerOptions = getWorkerOptions();

        String command = 'docker run -v maestro-quiver:/mnt --net=host docker.io/ssorj/quiver quiver --output /mnt '

        UserCommand1Request request = (UserCommand1Request) getNote()
        String arrow = request.getPayload()

        if (arrow != null) {
            logger.info("Using quiver arrow {}", arrow)
            command = command + " --arrow " + arrow
        }

        command = command + " " + workerOptions.getBrokerURL()

        executeOnShell(command)

        String logDir = System.getProperty("maestro.log.dir")
        String copyCommand = "sudo cp -Rv " + volumeInfo[0].Mountpoint + " " + logDir
        logger.debug("Executing {}", copyCommand)
        executeOnShell(copyCommand)

        logger.info("Fixing log file permissions")
        executeOnShell("chown -Rv maestro:maestro " + logDir)

        logger.info("Quiver test ran successfully")
        return null
    }
}