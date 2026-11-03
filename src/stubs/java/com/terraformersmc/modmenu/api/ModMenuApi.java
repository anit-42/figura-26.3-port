package com.terraformersmc.modmenu.api;
public interface ModMenuApi { default ConfigScreenFactory<?> getModConfigScreenFactory() { return s -> null; } }
