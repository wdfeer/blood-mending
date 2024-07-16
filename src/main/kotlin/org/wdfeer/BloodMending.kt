package org.wdfeer

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.EnchantmentTarget
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.damage.DamageTypes
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier
import org.wdfeer.TheMod.MOD_ID
import org.wdfeer.util.DamageTypeHelper
import kotlin.math.min

class BloodMending() : Enchantment(Rarity.UNCOMMON, EnchantmentTarget.BREAKABLE, EquipmentSlot.entries.toTypedArray()) {
    companion object {
        private val instance: BloodMending = BloodMending()

        fun initialize() {
            Registry.register(Registries.ENCHANTMENT, Identifier(MOD_ID, "blood_mending"), instance)
            ServerTickEvents.END_WORLD_TICK.register(::onWorldTick)
        }

        private fun onWorldTick(world: ServerWorld) {
            for (player in world.players) {
                if (!canBloodMend(player)) continue

                val inventory = player.inventory
                for (i in 0..inventory.size()){
                    val stack = inventory.getStack(i)
                    val bloodMending: Int = getBloodMending(stack)
                    if (bloodMending > 0){
                        tickBloodMending(world, player, stack, bloodMending)
                    }
                }
            }
        }

        private fun canBloodMend(player: ServerPlayerEntity): Boolean = player.health > 1 && player.health >= player.maxHealth / 2f && player.canTakeDamage()

        private fun getBloodMending(stack: ItemStack): Int {
            if (!stack.hasEnchantments()) return 0

            val enchantments: MutableMap<Enchantment, Int> = EnchantmentHelper.fromNbt(stack.enchantments)
            return enchantments[instance] ?: 0
        }

        private fun tickBloodMending(world: ServerWorld, player: ServerPlayerEntity, stack: ItemStack, level: Int) {
            if (!stack.isDamaged) return

            var repair = 0
            when (level) {
                1 -> repair = 10
                2 -> repair = 30
                3 -> repair = 50
            }

            // Account for items with max durability < repair
            repair = min(repair, stack.maxDamage - 1)

            if (stack.damage >= repair) {
                player.damage(DamageSource(DamageTypeHelper.getRegistryEntry(world, DamageTypes.MAGIC), player), 1f)
                player.yaw = player.headYaw
                stack.damage -= repair
            }
        }
    }

    override fun getMinPower(level: Int): Int {
        return 1 + level * 6
    }

    override fun getMaxPower(level: Int): Int {
        return getMinPower(level) + 10
    }

    override fun getMaxLevel(): Int {
        return 3
    }
}