package org.figuramc.figura.mixin.render.renderers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.lua.api.nameplate.EntityNameplateCustomization;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.TextUtils;
import org.figuramc.figura.avatar.Badges;
import org.spongepowered.asm.mixin.Unique;
import java.util.regex.Pattern;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.ducks.CameraRenderStateExtension;
import org.figuramc.figura.ducks.EntityRendererAccessor;
import org.figuramc.figura.ducks.FiguraEntityRenderStateExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity, S extends EntityRenderState> implements EntityRendererAccessor {

    @Inject(at = @At("HEAD"), method = "shouldRender", cancellable = true)
    private void shouldRender(T entity, Frustum frustum, double d, double e, double f, float partialTick, CallbackInfoReturnable<Boolean> cir) {
        Avatar avatar = AvatarManager.getAvatar(entity);
        if (avatar != null && avatar.permissions.get(Permissions.OFFSCREEN_RENDERING) == 1)
            cir.setReturnValue(true);
    }

    @Inject(at = @At("HEAD"), method = "extractRenderState")
    private void extractRenderState(T entity, S entityRenderState, float f, CallbackInfo ci) {
        ((FiguraEntityRenderStateExtension)entityRenderState).figura$setEntityId(entity.getId());
        ((FiguraEntityRenderStateExtension)entityRenderState).figura$setTickDelta(f);
    }


    @Unique
    private static CameraRenderState figura$cameraWithAvatar(CameraRenderState cameraRenderState, EntityRenderState state, boolean renderingName) {
        Avatar figura$avatar = AvatarManager.getAvatar(state);
        if (figura$avatar == null)
            return cameraRenderState;

        CameraRenderState replacement = new CameraRenderState();
        replacement.pos = cameraRenderState.pos;
        replacement.blockPos = cameraRenderState.blockPos;
        replacement.initialized = cameraRenderState.initialized;
        replacement.orientation = cameraRenderState.orientation;

        // i literally cannot believe we have to do this, but here we are
        ((CameraRenderStateExtension) replacement).figura$setAvatar(figura$avatar);
        ((CameraRenderStateExtension) replacement).figura$setRenderingNameTag(renderingName);
        return replacement;
    }

    // 26.x: score line and name are both submitted from EntityRenderer#submitNameDisplay (ordinal 0 = score, 1 = name)
    @ModifyArg(method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitNameTag(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZILnet/minecraft/client/renderer/state/level/CameraRenderState;)V", ordinal = 0), index = 6)
    private CameraRenderState figura$scoreCamera(CameraRenderState cameraRenderState, @Local(argsOnly = true) S entityRenderState) {
        return figura$cameraWithAvatar(cameraRenderState, entityRenderState, false);
    }

    @ModifyArg(method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitNameTag(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZILnet/minecraft/client/renderer/state/level/CameraRenderState;)V", ordinal = 1), index = 6)
    private CameraRenderState figura$nameCamera(CameraRenderState cameraRenderState, @Local(argsOnly = true) S entityRenderState) {
        return figura$cameraWithAvatar(cameraRenderState, entityRenderState, entityRenderState instanceof AvatarRenderState);
    }

    @ModifyArg(method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitNameTag(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZILnet/minecraft/client/renderer/state/level/CameraRenderState;)V", ordinal = 1), index = 3)
    private Component figura$modifyPlayerNameText(Component text, @Local(argsOnly = true) S entityRenderState) {
        if (!(entityRenderState instanceof AvatarRenderState player))
            return text;

        int config = Configs.ENTITY_NAMEPLATE.value;
        if (config == 0 || AvatarManager.panic)
            return text;

        Avatar avatar = AvatarManager.getAvatar(player);
        EntityNameplateCustomization custom = avatar == null || avatar.luaRuntime == null ? null : avatar.luaRuntime.nameplate.ENTITY;

        // customization boolean, which also is the permission check
        boolean hasCustom = custom != null && avatar.permissions.get(Permissions.NAMEPLATE_EDIT) == 1;

        Component name = player.nameTag;
        if (name == null)
            return text;
        FiguraMod.popPushProfiler("text");

        Component replacement = hasCustom && custom.getJson() != null ? custom.getJson().copy() : name;

        // name
        replacement = TextUtils.replaceInText(replacement, "\\$\\{name\\}", name);

        // badges
        FiguraMod.popPushProfiler("badges");
        Entity entity = Minecraft.getInstance().level == null ? null : Minecraft.getInstance().level.getEntity(player.id);
        if (entity != null) // null while dead
            replacement = Badges.appendBadges(replacement, entity.getUUID(), config > 1);

        FiguraMod.popPushProfiler("applyName");
        return TextUtils.replaceInText(text, "\\b" + Pattern.quote(name.getString()) + "\\b", replacement);
    }
}
