package org.maestro.agent.ext.requests.genericrequest

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.maestro.client.notes.*;
import org.maestro.client.exchange.MaestroTopics

import org.maestro.agent.base.AbstractHandler
import groovy.json.JsonSlurper

class UserCommand1Handler extends AbstractHandler {
    private static final Logger logger = LoggerFactory.getLogger(UserCommand1Handler.class);
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

    @Override
    Object handle() {
        logger.info("Creating directores")

        workDir.mkdirs()

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

        // docker run -it --net=host docker.io/ssorj/quiver quiver --arrow rhea q0
        String command = 'docker run -v maestro-quiver:/mnt --net=host docker.io/ssorj/quiver quiver --output /mnt ' + workerOptions.getBrokerURL()

        executeOnShell(command, quiverInstallDir)

        String logDir = System.getProperty("maestro.log.dir")
        String copyCommand = "sudo cp -Rv " + volumeInfo[0].Mountpoint + " " + logDir
        logger.info("Executing {}", copyCommand)
        executeOnShell(copyCommand)


        logger.info("Quiver run successfully")
        return null
    }
}