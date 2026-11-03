package org.figuramc.figura.ducks;

import org.figuramc.figura.model.rendering.buffers.FiguraRenderCallbacks;

public interface FiguraCallbackHolder {
    FiguraRenderCallbacks figura$getCallbacks();

    void figura$setCallbacks(FiguraRenderCallbacks callbacks);
}
