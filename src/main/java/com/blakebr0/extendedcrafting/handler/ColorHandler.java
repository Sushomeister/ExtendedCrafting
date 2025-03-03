package com.blakebr0.extendedcrafting.handler;

import com.blakebr0.cucumber.helper.ColorHelper;
import com.blakebr0.cucumber.iface.IColored;
import com.blakebr0.extendedcrafting.init.ModItems;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ColorHandler {
    @SubscribeEvent
    public void onItemColors(ColorHandlerEvent.Item event) {
        var colors = event.getItemColors();

        colors.register(new IColored.ItemColors(), ModItems.SINGULARITY.get());
        colors.register(
                (stack, index) -> getCurrentRainbowColor(),
                ModItems.ULTIMATE_SINGULARITY.get(),
                ModItems.THE_ULTIMATE_INGOT.get(),
                ModItems.THE_ULTIMATE_NUGGET.get()
        );
        colors.register(
                (stack, index) -> index == 1 ? getCurrentRainbowColor() : -1,
                ModItems.THE_ULTIMATE_COMPONENT.get(),
                ModItems.THE_ULTIMATE_CATALYST.get()
        );
    }

    private static int getCurrentRainbowColor() {
        var hue = (System.currentTimeMillis() % 18000) / 18000F;
        return ColorHelper.hsbToRGB(hue, 1, 1);
    }
}
