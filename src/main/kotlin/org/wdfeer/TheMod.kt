package org.wdfeer

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object TheMod : ModInitializer {
	const val MOD_ID: String = "blood_mending"
    private val logger = LoggerFactory.getLogger(MOD_ID)

	override fun onInitialize() {
		BloodMending.initialize()
		logger.info("BloodMending initialized!")
	}
}