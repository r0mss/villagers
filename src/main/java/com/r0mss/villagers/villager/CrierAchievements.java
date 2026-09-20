package com.r0mss.villagers.villager;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * Rastrea logros/hitos del jugador (viajar al Nether, conseguir armadura
 * completa de hierro, matar 10 creepers) y los deja "pendientes" en el
 * jugador. Cuando este se acerca a cualquier Pregonero, el mensaje se
 * anuncia (ver {@link VillagerBehaviorHandler}) y se borra de la lista.
 * <p>
 * Es un test con pocos logros a proposito; se pueden agregar mas despues
 * siguiendo el mismo patron (llamar a {@link #queue}).
 */
public final class CrierAchievements {

    private static final String TAG_PENDING = "villagers_pending_announcements";
    private static final String TAG_NETHER_ANNOUNCED = "villagers_achv_nether";
    private static final String TAG_IRON_ARMOR_ANNOUNCED = "villagers_achv_iron_armor";
    private static final String TAG_CREEPER_KILLS = "villagers_creeper_kills";

    private static final int CREEPER_MILESTONE = 10;

    private CrierAchievements() {
    }

    /**
     * Agrega un mensaje a la lista de anuncios pendientes de un jugador.
     */
    public static void queue(Player player, String message) {
        CompoundTag data = player.getPersistentData();
        ListTag pending = data.getList(TAG_PENDING, Tag.TAG_STRING);
        pending.add(StringTag.valueOf(message));
        data.put(TAG_PENDING, pending);
    }

    /**
     * Saca (y elimina) el primer mensaje pendiente de un jugador, o null si
     * no tiene ninguno.
     */
    public static String popPending(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        ListTag pending = data.getList(TAG_PENDING, Tag.TAG_STRING);
        if (pending.isEmpty()) {
            return null;
        }
        Tag first = pending.remove(0);
        data.put(TAG_PENDING, pending);
        return first.getAsString();
    }

    @SubscribeEvent
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getTo() != Level.NETHER) {
            return;
        }
        Player player = event.getEntity();
        CompoundTag data = player.getPersistentData();
        if (data.getBoolean(TAG_NETHER_ANNOUNCED)) {
            return;
        }
        data.putBoolean(TAG_NETHER_ANNOUNCED, true);
        queue(player, "¡Atencion, ciudadanos! Se dice que " + player.getName().getString()
                + " ha cruzado hacia el Nether!");
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }
        if (event.getSlot() != EquipmentSlot.HEAD
                && event.getSlot() != EquipmentSlot.CHEST
                && event.getSlot() != EquipmentSlot.LEGS
                && event.getSlot() != EquipmentSlot.FEET) {
            return;
        }

        CompoundTag data = player.getPersistentData();
        if (data.getBoolean(TAG_IRON_ARMOR_ANNOUNCED)) {
            return;
        }
        if (!hasFullIronArmor(player)) {
            return;
        }

        data.putBoolean(TAG_IRON_ARMOR_ANNOUNCED, true);
        queue(player, "¡Escuchen bien! " + player.getName().getString()
                + " ahora porta una armadura completa de hierro!");
    }

    private static boolean hasFullIronArmor(ServerPlayer player) {
        return player.getItemBySlot(EquipmentSlot.HEAD).is(Items.IRON_HELMET)
                && player.getItemBySlot(EquipmentSlot.CHEST).is(Items.IRON_CHESTPLATE)
                && player.getItemBySlot(EquipmentSlot.LEGS).is(Items.IRON_LEGGINGS)
                && player.getItemBySlot(EquipmentSlot.FEET).is(Items.IRON_BOOTS);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Creeper)) {
            return;
        }
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }

        CompoundTag data = player.getPersistentData();
        int kills = data.getInt(TAG_CREEPER_KILLS) + 1;
        data.putInt(TAG_CREEPER_KILLS, kills);

        if (kills == CREEPER_MILESTONE) {
            queue(player, "¡Que se sepa! " + player.getName().getString()
                    + " ha acabado con diez creepers!");
        }
    }
}
