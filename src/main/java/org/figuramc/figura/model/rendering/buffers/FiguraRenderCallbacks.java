package org.figuramc.figura.model.rendering.buffers;

import com.mojang.blaze3d.vertex.PoseStack;
import org.figuramc.figura.ducks.FiguraSubmitCallBackExtension;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Pre/post render callbacks captured from a Model / ModelPart / ItemDisplayContext at submit time,
 * and executed when the feature renderer prepares that submit (26.x render pipeline).
 */
public final class FiguraRenderCallbacks {
    private final List<BiFunction<FiguraBufferSource, PoseStack, Boolean>> pre = new ArrayList<>();
    private final List<Runnable> post = new ArrayList<>();
    private boolean preventAnimSetup;

    /** Moves the pending callbacks out of the given objects. Returns null when there is nothing to run. */
    @Nullable
    public static FiguraRenderCallbacks drain(Object... sources) {
        FiguraRenderCallbacks callbacks = null;
        for (Object source : sources) {
            if (!(source instanceof FiguraSubmitCallBackExtension ext))
                continue;
            boolean has = !ext.figura$getPreRenderingCallbacks().isEmpty() || !ext.figura$getPostRenderingCallbacks().isEmpty() || ext.figura$getPreventAnimSetup();
            if (!has)
                continue;
            if (callbacks == null)
                callbacks = new FiguraRenderCallbacks();
            callbacks.pre.addAll(ext.figura$getPreRenderingCallbacks());
            callbacks.post.addAll(ext.figura$getPostRenderingCallbacks());
            callbacks.preventAnimSetup |= ext.figura$getPreventAnimSetup();
            ext.figura$getPreRenderingCallbacks().clear();
            ext.figura$getPostRenderingCallbacks().clear();
            ext.figura$setPreventAnimSetup(false);
        }
        return callbacks;
    }

    public boolean preventAnimSetup() {
        return preventAnimSetup;
    }

    /** @return false if vanilla rendering should be skipped */
    public boolean runPre(FiguraBufferSource bufferSource, PoseStack poseStack) {
        boolean render = true;
        for (BiFunction<FiguraBufferSource, PoseStack, Boolean> callback : pre) {
            if (!callback.apply(bufferSource, poseStack))
                render = false;
        }
        return render;
    }

    public void runPost() {
        for (Runnable runnable : post)
            runnable.run();
    }
}
