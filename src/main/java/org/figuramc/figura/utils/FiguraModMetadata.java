package org.figuramc.figura.utils;


public abstract class FiguraModMetadata {
    private final String modId;
    protected FiguraModMetadata(String modID) {
        this.modId = modID;
    }

    public abstract String getCustomValueAsString(String key);
    public abstract Number getCustomValueAsNumber(String key);
    public abstract Boolean getCustomValueAsBoolean(String key);
    public abstract Object getCustomValueAsObject(String key);

    public abstract Version getModVersion();

    public String getModId() {
        return this.modId;
    }
    public static FiguraModMetadata getMetadataForMod(String modID) {
        return org.figuramc.figura.utils.fabric.FiguraModMetadataImpl.getMetadataForMod(modID);
    }
}
