package org.figuramc.figura.mixin.render.feature;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.feature.FlameFeatureRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.utils.RenderUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

// lets avatars replace the fire textures shown on their entity
@Mixin(FlameFeatureRenderer.class)
public class FlameFeatureRendererMixin {
    @WrapOperation(method = "buildGroup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/FlameFeatureRenderer;prepare(Lnet/minecraft/client/renderer/feature/FlameFeatureRenderer$Submit;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;Lnet/minecraft/client/renderer/texture/TextureAtlasSprite;)V"))
    private void figura$customFire(FlameFeatureRenderer instance, FlameFeatureRenderer.Submit submit, VertexConsumer consumer, TextureAtlasSprite first, TextureAtlasSprite second, Operation<Void> original) {
        Avatar avatar = AvatarManager.getAvatar(submit.entityRenderState());
        if (avatar != null) {
            TextureAtlasSprite s1 = RenderUtils.firstFireLayer(avatar);
            TextureAtlasSprite s2 = RenderUtils.secondFireLayer(avatar);
            if (s1 != null) first = s1;
            if (s2 != null) second = s2;
        }
        original.call(instance, submit, consumer, first, second);
    }
}
