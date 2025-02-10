package org.wdfeer

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.damage.DamageTypes
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import net.minecraft.world.Difficulty
import org.wdfeer.TheMod.MOD_ID
import kotlin.math.min

private val id = Identifier.of(MOD_ID, "blood_mending")

object BloodMending {
    init {
        ServerTickEvents.END_WORLD_TICK.register(::onWorldTick)
    }

    private fun onWorldTick(world: ServerWorld) {
        for (player in world.players) {
            if (!canBloodMend(player)) continue

            val inventory = player.inventory
            for (i in 0 until inventory.size()) {
                val stack = inventory.getStack(i)
                val bloodMending: Int = getBloodMending(stack)
                if (bloodMending > 0) {
                    tickBloodMending(world, player, stack, bloodMending)
                }
            }
        }
    }

    private fun canBloodMend(player: ServerPlayerEntity): Boolean = player.hurtTime == 0
            && (player.world.difficulty == Difficulty.HARD || safeToBloodMend(player))

    private fun safeToBloodMend(player: ServerPlayerEntity): Boolean =
        player.health > 2 && player.health > player.maxHealth / 2f

    private fun getBloodMending(stack: ItemStack): Int {
        if (!stack.hasEnchantments()) return 0

        return stack.enchantments.enchantmentEntries.find { it.key.matchesId(id) }?.intValue ?: 0
    }

    private fun tickBloodMending(world: ServerWorld, player: ServerPlayerEntity, stack: ItemStack, level: Int) {
        if (!stack.isDamaged) return

        var repair = 0
        when (level) {
            1 -> repair = 20
            2 -> repair = 40
            3 -> repair = 80
        }

        // Account for items with max durability < repair
        repair = min(repair, stack.maxDamage - 1)

        if (stack.damage >= repair) {
            player.damage(
                DamageSource(DamageTypeHelper.getRegistryEntry(world, DamageTypes.MAGIC)),
                getDamageAmount(world)
            )
            stack.damage -= repair
        }
    }

    private fun getDamageAmount(world: ServerWorld): Float {
        return when (world.difficulty) {
            null -> 1f
            Difficulty.PEACEFUL -> 1f
            Difficulty.EASY -> 1f
            Difficulty.NORMAL -> 2f
            Difficulty.HARD -> 3f
        }
    }
}