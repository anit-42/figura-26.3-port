package org.figuramc.figura.mixin.gui;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.figuramc.figura.gui.ActionWheel;
import org.figuramc.figura.gui.PopupMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// 26.x: screens are owned by Gui instead of Minecraft
@Mixin(Gui.class)
public class GuiSetScreenMixin {
    @Inject(at = @At("HEAD"), method = "setScreen")
    private void figura$setScreen(Screen screen, CallbackInfo ci) {
        if (ActionWheel.isEnabled())
            ActionWheel.setEnabled(false);

        if (PopupMenu.isEnabled())
            PopupMenu.run();
    }
}
