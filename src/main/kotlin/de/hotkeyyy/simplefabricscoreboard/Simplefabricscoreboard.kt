package de.hotkeyyy.simplefabricscoreboard

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

class Simplefabricscoreboard : ModInitializer {
    companion object {
        val logger = LoggerFactory.getLogger("simplefabricscoreboard")
    }

    override fun onInitialize() {
        logger.info("SimpleFabricScoreboard initialized!")
    }
}
