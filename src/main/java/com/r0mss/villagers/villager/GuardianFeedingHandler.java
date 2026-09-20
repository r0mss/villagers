package com.r0mss.villagers.villager;

import com.r0mss.villagers.registry.ModProfessions;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Le permite al jugador darle pan o una botella de agua al Guardian de
 * Tierras con clic derecho, como un gesto de roleplay: el item se consume
 * y el aldeano responde con un sonido de agradecimiento, sin dar nada a
 * cambio (no es un trade, es solo una interaccion directa).
 */
public final class GuardianFeedingHandler {

    private GuardianFeedingHandler() {
    }

    @SubscribeEvent
    public static void onInteract(PlayerInteractEvent.EntityInteract event) {
        if (!(event.getTarget() instanceof Villager villager)) {
            return;
        }
        if (villager.level().isClientSide()) {
            return;
        }
        if (villager.getVillagerData().getProfession() != ModProfessions.LAND_GUARDIAN.get()) {
            return;
        }

        InteractionHand hand = event.getHand();
        ItemStack stack = event.getEntity().getItemInHand(hand);

        boolean isBread = stack.is(Items.BREAD);
        boolean isWaterBottle = stack.is(Items.POTION)
                && stack.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).is(Potions.WATER);

        if (!isBread && !isWaterBottle) {
            return;
        }

        if (!event.getEntity().getAbilities().instabuild) {
            stack.shrink(1);
        }

        villager.level().playSound(null, villager.blockPosition(),
                isWaterBottle ? SoundEvents.GENERIC_DRINK : SoundEvents.GENERIC_EAT,
                SoundSource.NEUTRAL, 1.0f, 1.0f);
        villager.level().playSound(null, villager.blockPosition(),
                SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0f, 1.0f);

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
}
