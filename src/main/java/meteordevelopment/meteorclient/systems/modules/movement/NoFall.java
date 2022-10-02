/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.movement;

import baritone.api.BaritoneAPI;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.PlayerMoveC2SPacketAccessor;
import meteordevelopment.meteorclient.mixininterface.IPlayerMoveC2SPacket;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.function.Predicate;

import org.apache.commons.io.filefilter.TrueFileFilter;

import com.ibm.icu.util.BytesTrie.Result;

public class NoFall extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("The way you are saved from fall damage.")
        .defaultValue(Mode.Packet)
        .build()
    );

    private final Setting<PlaceMode> airPlaceMode = sgGeneral.add(new EnumSetting.Builder<PlaceMode>()
        .name("place-mode")
        .description("Whether place mode places before you die or before you take damage.")
        .defaultValue(PlaceMode.BeforeDeath)
        .visible(() -> mode.get() == Mode.AirPlace)
        .build()
    );

    private final Setting<Boolean> anchor = sgGeneral.add(new BoolSetting.Builder()
        .name("anchor")
        .description("Centers the player and reduces movement when using bucket or air place mode.")
        .defaultValue(true)
        .visible(() -> mode.get() != Mode.Packet)
        .build()
    );

    private boolean placedWater;
    private int preBaritoneFallHeight;

    public NoFall() {
        super(Categories.Movement, "no-fall", "Attempts to prevent you from taking fall damage.");
    }

    @Override
    public void onActivate() {
        preBaritoneFallHeight = BaritoneAPI.getSettings().maxFallHeightNoWater.value;
        if (mode.get() == Mode.Packet) BaritoneAPI.getSettings().maxFallHeightNoWater.value = 255;
        placedWater = false;
    }

    @Override
    public void onDeactivate() {
        BaritoneAPI.getSettings().maxFallHeightNoWater.value = preBaritoneFallHeight;
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (mc.player.getAbilities().creativeMode
            || !(event.packet instanceof PlayerMoveC2SPacket)
            || mode.get() != Mode.Packet
            || ((IPlayerMoveC2SPacket) event.packet).getTag() == 1337) return;


        if (!Modules.get().isActive(Flight.class)) {
            if (mc.player.isFallFlying()) return;
            if (mc.player.getVelocity().y > -0.5) return;
            ((PlayerMoveC2SPacketAccessor) event.packet).setOnGround(true);
        } else {
            ((PlayerMoveC2SPacketAccessor) event.packet).setOnGround(true);
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player.getAbilities().creativeMode) return;

        // Airplace mode
        if (mode.get() == Mode.AirPlace) {
            // Test if fall damage setting is valid
            if (!airPlaceMode.get().test(mc.player.fallDistance)) return;

            // Center and place block
            if (anchor.get()) PlayerUtils.centerPlayer();

            Rotations.rotate(mc.player.getYaw(), 90, Integer.MAX_VALUE, () -> {
                double preY = mc.player.getVelocity().y;
                ((IVec3d) mc.player.getVelocity()).setY(0);

                BlockUtils.place(mc.player.getBlockPos().down(), InvUtils.findInHotbar(itemStack -> itemStack.getItem() instanceof BlockItem), false, 0, true);

                ((IVec3d) mc.player.getVelocity()).setY(preY);
            });
        }

        // Bucket mode
        if (mode.get() == Mode.Bucket) {
            BlockHitResult result = null; // = mc.world.raycast(new RaycastContext(mc.player.getPos(), mc.player.getPos().subtract(0, 5, 0), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player));
            if (mc.player.fallDistance > 3 && !EntityUtils.isAboveWater(mc.player)) {
                // Place water
                FindItemResult waterBucket = InvUtils.findInHotbar(Items.WATER_BUCKET);

                if (!waterBucket.found()) return;

                BlockHitResult mresult = null; // = mc.world.raycast(new RaycastContext(mc.player.getPos(), mc.player.getPos().subtract(0, 5, 0), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player));

                // Center player

                if (anchor.get()) PlayerUtils.centerPlayer();
                for (int x = 0; x < 2; x++) {
                    for (int y = 0; y < 2; y++) {
                        mresult = mc.world.raycast(new RaycastContext(mc.player.getPos().subtract(x * 0.8 - 0.4, 0, y * 0.8 - 0.4), mc.player.getPos().subtract(x * 0.8 - 0.4, 5, y * 0.8 - 0.4), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mc.player));
                        // if (mresult != null && mresult.getType() == HitResult.Type.BLOCK){


                        //     if (result == null && mresult.getType() == HitResult.Type.BLOCK){
                        //         result = mresult;
                        //     }
                        //     else if (mresult.getBlockPos().getY() > result.getBlockPos().getY()){
                        //         result = mresult;
                        //     }
                        // }


                        mc.player.sendChatMessage(String.valueOf(x));
                        if (mresult != null && mresult.getType() == HitResult.Type.BLOCK) {
                            if (result == null){
                                result = mresult;
                            }
                            else if (mresult.getBlockPos().getY() > result.getBlockPos().getY()) {
                                result = mresult;


                            }
                        }


                    }
                  }


                // Check if there is a block within 5 blocks

                // Place water
                if (result != null && result.getType() == HitResult.Type.BLOCK) {
                    useBucket(tovec3d(result),
                    waterBucket,
                    true);
                }

            }
            // Remove water
            if (placedWater && mc.player.getBlockStateAtPos().getFluidState().getFluid() == Fluids.WATER) {
                // useBucket(tovec3d(result), InvUtils.findInHotbar(Items.BUCKET), false);
            }
        }
    }

    private Vec3d tovec3d(BlockHitResult r) {
        return new Vec3d(
            r.getBlockPos().getX(),
            r.getBlockPos().getY(),
            r.getBlockPos().getZ());
    }

    private void useBucket(Vec3d placepos, FindItemResult bucket, boolean placedWater) {
        if (!bucket.found()) return;

        Rotations.rotate(Rotations.getYaw(placepos), Rotations.getPitch(placepos), 100, true, () -> {
            mc.player.sendChatMessage(placepos.toString());

            if (bucket.isOffhand()) {
                mc.interactionManager.interactItem(mc.player, Hand.OFF_HAND);
            } else {
                int preSlot = mc.player.getInventory().selectedSlot;
                InvUtils.swap(bucket.slot(), true);
                mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                InvUtils.swapBack();
            }

            this.placedWater = placedWater;
        });
    }



    @Override
    public String getInfoString() {
        return mode.get().toString();
    }

    public enum Mode {
        Packet,
        AirPlace,
        Bucket
    }

    public enum PlaceMode {
        BeforeDamage(height -> height > 2),
        BeforeDeath(height -> height > Math.max(PlayerUtils.getTotalHealth(), 2));

        private final Predicate<Float> fallHeight;

        PlaceMode(Predicate<Float> fallHeight) {
            this.fallHeight = fallHeight;
        }

        public boolean test(float fallheight) {
            return fallHeight.test(fallheight);
        }
    }
}
