package org.wdfeer.util

import net.minecraft.entity.damage.DamageType
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.world.World

object DamageTypeHelper {
    fun getRegistryEntry(world: World, key: RegistryKey<DamageType>): RegistryEntry<DamageType>? =
        world.registryManager.get(RegistryKeys.DAMAGE_TYPE).entryOf(key)
}
