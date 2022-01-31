/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.player;

import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.server.integrated.IntegratedServer;

import java.net.InetAddress;
import java.net.UnknownHostException;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;



public class sendip extends Module {
    String ipText;
    public enum Chestplate {
        Diamond,
        Netherite,
        PreferDiamond,
        PreferNetherite
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Chestplate> chestplate = sgGeneral.add(new EnumSetting.Builder<Chestplate>()
            .name("chestplate")
            .description("Which type of chestplate to swap to.")
            .defaultValue(Chestplate.PreferNetherite)
            .build()
    );

    private final Setting<Boolean> stayOn = sgGeneral.add(new BoolSetting.Builder()
            .name("stay-on")
            .description("Stays on and activates when you turn it off.")
            .defaultValue(false)
            .build()
    );

    public sendip() {
        super(Categories.Player, "sendip", "sends ip in chat");
    }

    @Override
    public void onActivate() {
        swap();
        if (!stayOn.get()) toggle();
    }

    @Override
    public void onDeactivate() {
        if (stayOn.get()) swap();
    }

    public void swap() {
        
        ServerInfo server = mc.getCurrentServerEntry();

        if (server == null) {
            info("Couldn't obtain any server information.");
            return;
        }

        String ipv4 = "";
        try {
            ipv4 = InetAddress.getByName(server.address).getHostAddress();
        } catch (UnknownHostException ignored) {}


        if (ipv4.isEmpty()) {
            ipText = server.address;
        }
        else{
            ipText = ipv4;
        }
        
        mc.player.sendChatMessage(ipText);
        
        
    }
    

    // private boolean equipChestplate() {
    //     int bestSlot = -1;
    //     boolean breakLoop = false;

    //     for (int i = 0; i < mc.player.getInventory().main.size(); i++) {
    //         Item item = mc.player.getInventory().main.get(i).getItem();

    //         switch (chestplate.get()) {
    //             case Diamond:
    //                 if (item == Items.DIAMOND_CHESTPLATE) {
    //                     bestSlot = i;
    //                     breakLoop = true;
    //                 }
    //                 break;
    //             case Netherite:
    //                 if (item == Items.NETHERITE_CHESTPLATE) {
    //                     bestSlot = i;
    //                     breakLoop = true;
    //                 }
    //                 break;
    //             case PreferDiamond:
    //                 if (item == Items.DIAMOND_CHESTPLATE) {
    //                     bestSlot = i;
    //                     breakLoop = true;
    //                 } else if (item == Items.NETHERITE_CHESTPLATE) {
    //                     bestSlot = i;
    //                 }
    //                 break;
    //             case PreferNetherite:
    //                 if (item == Items.DIAMOND_CHESTPLATE) {
    //                     bestSlot = i;
    //                 } else if (item == Items.NETHERITE_CHESTPLATE) {
    //                     bestSlot = i;
    //                     breakLoop = true;
    //                 }
    //                 break;
    //         }

    //         if (breakLoop) break;
    //     }

    //     if (bestSlot != -1) equip(bestSlot);
    //     return bestSlot != -1;
    // }

    private void equipElytra() {
        for (int i = 0; i < mc.player.getInventory().main.size(); i++) {
            Item item = mc.player.getInventory().main.get(i).getItem();

            if (item == Items.ELYTRA) {
                equip(i);
                break;
            }
        }
    }

    private void equip(int slot) {
        InvUtils.move().from(slot).toArmor(2);
    }

    @Override
    public void sendToggledMsg() {
        if (stayOn.get()) super.sendToggledMsg();
        else if (Config.get().chatFeedback.get()) info("Triggered (highlight)%s(default).", title);
    }
}
