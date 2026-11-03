package org.figuramc.figura.mixin.gui;

import net.minecraft.client.gui.components.debug.DebugScreenEntryList;
import net.minecraft.client.gui.components.debug.DebugScreenEntryStatus;
import net.minecraft.client.gui.components.debug.DebugScreenProfile;
import net.minecraft.resources.Identifier;
import org.figuramc.figura.FiguraMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(DebugScreenEntryList.class)
public class DebugScreenEntryListMixin {
    @Shadow @org.spongepowered.asm.mixin.Final
    private Map<Identifier, DebugScreenEntryStatus> allStatuses;

    private void setFullDebugStatuses() {
        this.allStatuses.put(FiguraMod.FIGURA_DEBUG_KEY, DebugScreenEntryStatus.IN_OVERLAY);
    }

    // 26.x: profiles are applied in resetToProfile, saved statuses in load
    @Inject(method = "resetToProfile", at = @At("RETURN"))
    private void injectResetToProfile(CallbackInfo ci) {
        this.setFullDebugStatuses();
    }

    @Inject(method = "load", at = @At("RETURN"))
    private void injectLoad(CallbackInfo ci) {
        this.setFullDebugStatuses();
    }

    @Inject(method = "rebuildCurrentList", at = @At("HEAD"))
    private void injectSodiumSettings(CallbackInfo ci) {
        if (!this.allStatuses.containsKey(FiguraMod.FIGURA_DEBUG_KEY)) {
            this.allStatuses.put(FiguraMod.FIGURA_DEBUG_KEY, DebugScreenEntryStatus.IN_OVERLAY);
        }
    }
}
