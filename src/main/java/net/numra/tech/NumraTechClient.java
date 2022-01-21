package net.numra.tech;

import net.fabricmc.api.ClientModInitializer;
import net.numra.tech.blocks.ConveyorBasic;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NumraTechClient implements ClientModInitializer {
    public static final Logger logger_client = LogManager.getLogger("NumraTech - Client");
    
    @Override
    public void onInitializeClient() {
        logger_client.debug("Beginning EntityRenderer init");
        ConveyorBasic.initEntityRenderers();
        logger_client.debug("Client init finished");
    }
}
