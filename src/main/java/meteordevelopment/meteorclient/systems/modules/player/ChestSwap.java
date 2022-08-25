/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.player;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket.Flag;



public class ChestSwap extends Module {
    public enum Chestplate {
        Diamond,
        Netherite,
        PreferDiamond,
        PreferNetherite
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();


    public ChestSwap() {
        super(Categories.Player, "chest-swap", "Automatically swaps between a chestplate and an elytra.");
    }

    public boolean usingElytra;

    @Override
    public void onActivate() {
        swap();
        toggle();

    }

    public void swap() {

        if(myCheckFallFlying()) return;



        Item currentItem = mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem();

        if (currentItem instanceof ArmorItem && ((ArmorItem) currentItem).getSlotType() == EquipmentSlot.CHEST) {
            equipElytra();
            usingElytra = false;

        }

    }



    public boolean myCheckFallFlying() {
        return mc.player.isOnGround() || mc.player.isFallFlying() || mc.player.isInsideWaterOrBubbleColumn();
    }


    private void equipElytra() {
        for (int i = 0; i < mc.player.getInventory().main.size(); i++) {
            Item item = mc.player.getInventory().main.get(i).getItem();
            ItemStack itemStack = mc.player.getInventory().getStack(i);
            if (item == Items.ELYTRA && (itemStack.getMaxDamage() - itemStack.getDamage()) > 20) {
                equip(i);
                break;
            }
        }
    }

    private void equip(int slot) {
        InvUtils.move().from(slot).toArmor(2);
    }

    // @Override
    // public void sendToggledMsg() {
    //     if (stayOn.get()) super.sendToggledMsg();
    //     else if (Config.get().chatFeedback.get()) info("Triggered (highlight)%s(default).", title);
    // }
}
