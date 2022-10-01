/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Fullbright;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import meteordevelopment.meteorclient.systems.modules.render.Xray;
import meteordevelopment.meteorclient.utils.render.color.Color;
import net.minecraft.client.render.LightmapTextureManager;

import org.checkerframework.checker.units.qual.min;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(LightmapTextureManager.class)
public class LightmapTextureManagerMixin {

    int light;
    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/texture/NativeImage;setColor(III)V"))
    private void update(Args args) {
        if (Modules.get().get(Fullbright.class).getGamma() || Modules.get().isActive(Xray.class)) {

            light = getIntFromColor(Modules.get().get(Fullbright.class).getGammaA());
            // light = new Color(
            //     Modules.get().get(Fullbright.class).getGammaA(),
            //     Modules.get().get(Fullbright.class).getGammaA(),
            //     Modules.get().get(Fullbright.class).getGammaA());


            // light = colorAdd(light, getColorFromHex(args.get(2).toString()));
            light += Integer.decode(args.get(2).toString());
            light = light < 0x00000000 ? 0xFFFFFFFF : light;

            args.set(2, getIntFromColor(light));
        }
    }

    @Inject(method = "getDarknessFactor(F)F", at = @At("HEAD"), cancellable = true)
	private void getDarknessFactor(float tickDelta, CallbackInfoReturnable<Float> info) {
		if (Modules.get().get(NoRender.class).noDarkness()) info.setReturnValue(0.0f);
	}

    public int getIntFromColor(int Red, int Green, int Blue){
        Red = (Red << 16) & 0x00FF0000; //Shift red 16-bits and mask out other stuff
        Green = (Green << 8) & 0x0000FF00; //Shift Green 8-bits and mask out other stuff
        Blue = Blue & 0x000000FF; //Mask out anything not blue.
        return 0xFF000000 | Red | Green | Blue; //0xFF000000 for 100% Alpha. Bitwise OR everything together.
    }
    public int getIntFromColor(Integer brigtness){
        int Red = (brigtness << 16) & 0x00FF0000; //Shift red 16-bits and mask out other stuff
        int Green = (brigtness << 8) & 0x0000FF00; //Shift Green 8-bits and mask out other stuff
        int Blue = brigtness & 0x000000FF; //Mask out anything not blue.
        return 0xFF000000 | Red | Green | Blue; //0xFF000000 for 100% Alpha. Bitwise OR everything together.
    }
    public int getIntFromColor(Color color){
        int Red = (color.r << 16) & 0x00FF0000; //Shift red 16-bits and mask out other stuff
        int Green = (color.g << 8) & 0x0000FF00; //Shift Green 8-bits and mask out other stuff
        int Blue = color.b & 0x000000FF; //Mask out anything not blue.
        return 0xFF000000 | Red | Green | Blue; //0xFF000000 for 100% Alpha. Bitwise OR everything together.
    }



    public Color getColorFromHex(String color){
        return new Color(
            Integer.valueOf(color.substring( 1, 3 ), 16 ),
            Integer.valueOf(color.substring( 3, 5 ), 16 ),
            Integer.valueOf(color.substring( 5, 7 ), 16 ) );
    }

    public Color colorAdd(Color c1, Color c2){
        return new Color(
            clamp(c1.r + c2.r, 0, 255),
            clamp(c1.g + c2.g, 0, 255),
            clamp(c1.b + c2.b, 0, 255));
    }

    public int clamp(int v, int min, int max){
        return Math.max(Math.min(v, max), min);
    }

    public Color fromB(int b){
        return new Color(b, b, b);
    }





}
