package org.maestro.agent.ext.requests.ping

import org.maestro.agent.base.AbstractHandler
import org.maestro.client.exchange.MaestroTopics
import org.maestro.client.notes.OkResponse

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PingHandler extends AbstractHandler {
    private static final Logger logger = LoggerFactory.getLogger(PingHandler.class);

    @Override
    Object handle() {
        logger.info("Hello from a sample Maestro Extension point");

        this.getClient().publish(MaestroTopics.MAESTRO_TOPIC, new OkResponse())
        return null
    }
}