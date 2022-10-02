/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.utils.player;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.entity.player.SendMovementPacketsEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.utils.PreInit;
import meteordevelopment.meteorclient.utils.entity.Target;
import meteordevelopment.meteorclient.utils.misc.Pool;
import meteordevelopment.orbit.EventHandler;
// import meteordevelopment.starscript.compiler.Expr.Null;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

import com.mojang.authlib.minecraft.client.MinecraftClient;

// import me.jellysquid.mods.sodium.client.render.chunk.shader.ChunkShaderFogComponent.None;

import static meteordevelopment.meteorclient.MeteorClient.mc;

import java.lang.Math;



public class Rotations {
    private static final Pool<Rotation> rotationPool = new Pool<>(Rotation::new);
    private static final List<Rotation> rotations = new ArrayList<>();
    public static Rotation rotation = rotationPool.get();
    public static float serverYaw;
    public static float serverPitch;
    public static int rotationTimer;
    private static float preYaw, prePitch;
    private static int i = 0;


    private static Rotation lastRotation;
    private static int lastRotationTimer;
    private static boolean sentLastRotation;
    public static boolean rotating = false;

    public static double pYaw = 0;
    public static double pPitch = 0;

    public static double rYaw;
    public static double rPitch;

    public static int priority;
    public static boolean clientSide;
    public static Runnable callback;
    public static boolean shouldmoveback;


    public static Double dis = 180.0;

    static double yawdis;
    static double pitchdis;

    public static boolean rendering;

    static float speed;





    @PreInit
    public static void init() {
        MeteorClient.EVENT_BUS.subscribe(Rotations.class);
    }

    public static void rotate(double yaw, double pitch, int vpriority, boolean vclientSide, Runnable vcallback) {


        rYaw = yaw;  //the yaw that we should be rotating to
        rPitch = pitch;
        priority = vpriority;
        clientSide = vclientSide;
        callback = vcallback;
        runCallback();
        callback = null;
<<<<<<< Updated upstream
=======

>>>>>>> Stashed changes
        rotating = true;

        resetLastRotation();
    }
    public static void rotatetorotation(){
        rotationTimer++;
        if (mc.world != null){

            if (!rotating){
                pYaw = mc.player.getYaw();
                pPitch = mc.player.getPitch();
            }
            else if (lastRotationTimer <= Config.get().rotationHoldTicks.get()){//if (serverYaw != rYaw && serverPitch != rPitch){

                shouldmoveback = true;

                rotateto();

            }
            else if (shouldmoveback){
                rYaw = mc.player.getYaw();
                rPitch = mc.player.getPitch();

                rotateto();

                if (serverYaw == mc.player.getYaw() && serverPitch == mc.player.getPitch()) {shouldmoveback = false; rotating = false;}
                if (mc.player.getYaw() == pYaw && mc.player.getPitch() == pPitch) rotating = false;

            }



        }
    }


    @EventHandler
    private static void onTick(TickEvent.Pre event) {
        runCallback();
        speed = Config.get().Speed.get();
        if(!Config.get().OnFrame.get()) rotatetorotation();
        lastRotationTimer++;
        if(rotating) rotations.add(0, rotation);



        // if(mc.world != null && mc.player != null) mc.player.sendChatMessage(""+mc.getLastFrameDuration(), null);
    }

    @EventHandler
    private static void onRender3D(Render3DEvent event) {
        // rendering = true;
        speed = Config.get().Speed.get() * (mc.getLastFrameDuration());

        if(Config.get().OnFrame.get()) rotatetorotation();
        // setCamRotation(rotation.yaw, rotation.yaw);



    }

    public static void calcDis()
    {

        yawdis = rYaw - pYaw;
        pitchdis = rPitch - pPitch;

        if (yawdis < -180) {yawdis = yawdis + 360;}
        if (yawdis > 180) {yawdis =  yawdis - 360;}

        dis = Math.sqrt(yawdis * yawdis + pitchdis * pitchdis);


    }

    public static void rotateto(){
        if (Config.get().Smooth.get()){
            Double relativespeed = 1.0;

            calcDis();

            if(Config.get().rotmode.get() == Config.RotationMode.smoothstep)
            {
                relativespeed = Math.min(dis * Config.get().p.get() / 100 + Config.get().Add.get(), Config.get().cap.get());
            }


            if (yawdis != 0)
            {
                pYaw = pYaw + closestToZero((yawdis / dis) * speed * relativespeed, yawdis);
            }
            if(pitchdis != 0)
            {
                pPitch = pPitch + closestToZero((pitchdis / dis) * speed * relativespeed, pitchdis);
            }

            // pYaw = rYaw;
            // pPitch = rPitch;

            setCamRotation(pYaw, pPitch);
        }


        rotation.set(pYaw, pPitch, priority, clientSide, callback);


        // int i = 0;
        // for (; i < rotations.size(); i++) {
        //     if (priority > rotations.get(i).priority) break;
        }




    public static double closestToZero(int num, Double num2){
        return (int) Math.signum(num2) * (Math.min(Math.abs(num), Math.abs(num2)));

    }
    public static double closestToZero(Double num, Double num2){
        return (int) Math.signum(num2) * (Math.min(Math.abs(num), Math.abs(num2)));

    }


    public static void rotate(double yaw, double pitch, int priority, Runnable callback) {
        rotate(yaw, pitch, priority, false, callback);
    }

    public static void rotate(double yaw, double pitch, Runnable callback) {
        rotate(yaw, pitch, 0, callback);
    }

    public static void rotate(double yaw, double pitch) {
        rotate(yaw, pitch, 0, null);
    }

    private static void resetLastRotation() {
        if (lastRotation != null) {
            rotationPool.free(lastRotation);

            lastRotation = null;
            lastRotationTimer = 0;
        }
    }

    @EventHandler
    private static void onSendMovementPacketsPre(SendMovementPacketsEvent.Pre event) {
        if (mc.cameraEntity != mc.player) return;
        sentLastRotation = false;

        if (!rotations.isEmpty()) {
            // rotating = true;


            Rotation rotation = rotations.get(i);
            setupMovementPacketRotation(rotation);

            if (rotations.size() > 1) rotationPool.free(rotation);

            i++;
        } else if (lastRotation != null) {
            if (lastRotationTimer >= 1) {
                resetLastRotation();
                rotating = false;
            } else {
                setupMovementPacketRotation(lastRotation);
                sentLastRotation = true;

                // lastRotationTimer++;
            }
        }
    }

    private static void setupMovementPacketRotation(Rotation rotation) {
        setClientRotation(rotation);
        setCamRotation(rotation.yaw, rotation.pitch);
    }

    private static void setClientRotation(Rotation rotation) {
        preYaw = mc.player.getYaw();
        prePitch = mc.player.getPitch();

        mc.player.setYaw((float) rotation.yaw);
        mc.player.setPitch((float) rotation.pitch);
    }

    @EventHandler
    private static void onSendMovementPacketsPost(SendMovementPacketsEvent.Post event) {
        if (!rotations.isEmpty()) {
            if (mc.cameraEntity == mc.player) {

                // rotations.get(i - 1).runCallback();



                if (rotations.size() == 1) lastRotation = rotations.get(i - 1);

                resetPreRotation();
            }

            for (; i < rotations.size(); i++) {
                Rotation rotation = rotations.get(i);

                setCamRotation(rotation.yaw, rotation.pitch);
                if (rotation.clientSide) setClientRotation(rotation);
                rotation.sendPacket();

                // runCallback();
                if (rotation.clientSide) resetPreRotation();

                if (i == rotations.size() - 1) lastRotation = rotation;
                else rotationPool.free(rotation);
            }

            rotations.clear();
            i = 0;
        } else if (sentLastRotation) {
            resetPreRotation();
        }
    }

    private static void resetPreRotation() {
        mc.player.setYaw(preYaw);
        mc.player.setPitch(prePitch);
    }

    public static double getYaw(Entity entity) {
        return mc.player.getYaw() + MathHelper.wrapDegrees((float) Math.toDegrees(Math.atan2(entity.getZ() - mc.player.getZ(), entity.getX() - mc.player.getX())) - 90f - mc.player.getYaw());
    }

    public static double getYaw(Vec3d pos) {
        return mc.player.getYaw() + MathHelper.wrapDegrees((float) Math.toDegrees(Math.atan2(pos.getZ() - mc.player.getZ(), pos.getX() - mc.player.getX())) - 90f - mc.player.getYaw());
    }

    public static double getPitch(Vec3d pos) {
        double diffX = pos.getX() - mc.player.getX();
        double diffY = pos.getY() - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        double diffZ = pos.getZ() - mc.player.getZ();

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        return mc.player.getPitch() + MathHelper.wrapDegrees((float) -Math.toDegrees(Math.atan2(diffY, diffXZ)) - mc.player.getPitch());
    }

    public static double getPitch(Entity entity, Target target) {
        double y;
        if (target == Target.Head) y = entity.getEyeY();
        else if (target == Target.Body) y = entity.getY() + entity.getHeight() / 2;
        else y = entity.getY();

        double diffX = entity.getX() - mc.player.getX();
        double diffY = y - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        double diffZ = entity.getZ() - mc.player.getZ();

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        return mc.player.getPitch() + MathHelper.wrapDegrees((float) -Math.toDegrees(Math.atan2(diffY, diffXZ)) - mc.player.getPitch());
    }

    public static double getPitch(Entity entity) {
        return getPitch(entity, Target.Body);
    }

    public static double getYaw(BlockPos pos) {
        return mc.player.getYaw() + MathHelper.wrapDegrees((float) Math.toDegrees(Math.atan2(pos.getZ() + 0.5 - mc.player.getZ(), pos.getX() + 0.5 - mc.player.getX())) - 90f - mc.player.getYaw());
    }

    public static double getPitch(BlockPos pos) {
        double diffX = pos.getX() + 0.5 - mc.player.getX();
        double diffY = pos.getY() + 0.5 - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        double diffZ = pos.getZ() + 0.5 - mc.player.getZ();

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        return mc.player.getPitch() + MathHelper.wrapDegrees((float) -Math.toDegrees(Math.atan2(diffY, diffXZ)) - mc.player.getPitch());
    }

    public static void setCamRotation(double yaw, double pitch) {
        serverYaw = (float) yaw;
        serverPitch = (float) pitch;
        rotationTimer = 0;
    }

    private static class Rotation {
        public double yaw, pitch;
        public int priority;
        public boolean clientSide;
        public Runnable callback;

        public void set(double yaw, double pitch, int priority, boolean clientSide, Runnable callback) {
            this.yaw = yaw;
            this.pitch = pitch;
            this.priority = priority;
            this.clientSide = clientSide;
            this.callback = callback;
        }

        public void sendPacket() {
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.LookAndOnGround((float) yaw, (float) pitch, mc.player.isOnGround()));

            // runCallback();
        }

        public void runCallback() {
            if (dis < Config.get().maxdis.get()){

                if (callback != null) callback.run();
                // mc.player.sendChatMessage("" +( callback), null);
            }
        }
    }

    public static void runCallback() {
        calcDis();
        if (dis < Config.get().maxdis.get()){

            if (callback != null) callback.run();

        }
    }
}
