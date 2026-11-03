package org.figuramc.figura.compat;

public class IrisCompat {
    public static void assignPipelinesToIrisPrograms() {
        // 26.x: Figura no longer registers a custom pipeline (the solid render type uses vanilla debug quads),
        // so there is nothing to assign to Iris programs.
    }
}
