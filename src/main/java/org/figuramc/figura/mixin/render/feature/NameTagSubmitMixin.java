package org.figuramc.figura.mixin.render.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.Vec3;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.config.Configs;
import org.figuramc.figura.ducks.CameraRenderStateExtension;
import org.figuramc.figura.lua.api.nameplate.EntityNameplateCustomization;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.model.rendering.buffers.FontRenderHelper;
import org.figuramc.figura.model.rendering.buffers.RecordingBufferSource;
import org.figuramc.figura.permissions.Permissions;
import org.figuramc.figura.utils.TextUtils;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Custom entity nameplates (pivot, position, scale, light, background, outline, shadow, multi-line).
 * 26.x: nameplates are text submits; when an avatar customizes its nameplate, vanilla's submit is replaced
 * by Figura-built text geometry.
 */
@Mixin(SubmitNodeCollection.class)
public class NameTagSubmitMixin {

    @Inject(method = "submitNameTag", at = @At("HEAD"), cancellable = true)
    private void figura$customNameTag(PoseStack poseStack, Vec3 attachment, int yOffset, Component text, boolean seeThrough, int lightCoords, CameraRenderState camera, CallbackInfo ci) {
        if (camera == null || attachment == null)
            return;

        CameraRenderStateExtension ext = (CameraRenderStateExtension) camera;
        Avatar avatar = ext.figura$getAvatar();
        boolean isName = ext.figura$isRenderingNameTag();
        ext.figura$setAvatar(null);
        ext.figura$setRenderingNameTag(false);

        if (avatar == null || avatar.luaRuntime == null || AvatarManager.panic || Configs.ENTITY_NAMEPLATE.value <= 0)
            return;

        EntityNameplateCustomization custom = avatar.luaRuntime.nameplate.ENTITY;
        if (custom == null || avatar.permissions.get(Permissions.NAMEPLATE_EDIT) != 1)
            return;

        ci.cancel();
        FiguraMod.pushProfiler(FiguraMod.MOD_ID);
        FiguraMod.pushProfiler("nameplate");
        try {
            poseStack.pushPose();

            FiguraVec3 pivot = custom.getPivot() != null ? custom.getPivot() : FiguraVec3.of(attachment.x, attachment.y + 0.5, attachment.z);
            poseStack.translate(pivot.x, pivot.y, pivot.z);
            poseStack.rotate(camera.orientation);

            if (custom.getPos() != null) {
                FiguraVec3 pos = custom.getPos();
                poseStack.translate(pos.x, pos.y, pos.z);
            }

            FiguraVec3 scale = FiguraVec3.of(0.025, -0.025, 0.025);
            if (custom.getScale() != null)
                scale.multiply(custom.getScale());
            poseStack.scale((float) scale.x, (float) scale.y, (float) scale.z);

            Matrix4f matrix = new Matrix4f(poseStack.last().pose());
            Minecraft minecraft = Minecraft.getInstance();
            Font font = minecraft.font;

            int light = custom.light != null ? custom.light : lightCoords;
            int background = custom.background != null ? custom.background
                    : ARGB.color(minecraft.gameRenderer.gameRenderState().optionsRenderState.getBackgroundOpacity(0.25f), 0xFF000000);
            int outlineColor = custom.outlineColor != null ? custom.outlineColor : 0x202020;
            boolean deadmau = text.getString().equals("deadmau5");

            List<Component> lines = isName ? TextUtils.splitText(text, "\\r?\\n") : List.of(text);
            RecordingBufferSource buffer = new RecordingBufferSource((SubmitNodeCollection) (Object) this);

            for (int i = 0; i < lines.size(); i++) {
                Component line = lines.get(i);
                if (line.getString().isEmpty())
                    continue;

                int lineIndex = i - lines.size() + 1;
                float x = -font.width(line) / 2f;
                float y = yOffset + (deadmau ? -10f : 0f) + (font.lineHeight + 1) * lineIndex;

                if (seeThrough)
                    FontRenderHelper.drawInBatch(font, line, x, y, 0x20FFFFFF, false, matrix, buffer, Font.DisplayMode.SEE_THROUGH, background, light);

                if (custom.outline) {
                    FontRenderHelper.drawInBatch8xOutline(font, line.getVisualOrderText(), x, y, -1, outlineColor, matrix, buffer, light);
                } else {
                    FontRenderHelper.drawInBatch(font, line, x, y, -1, custom.shadow, matrix, buffer, Font.DisplayMode.NORMAL, seeThrough ? 0 : background, light);
                }
            }

            buffer.flush();
            poseStack.popPose();
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to render Figura nameplate", e);
        }
        FiguraMod.popProfiler(2);
    }
}
