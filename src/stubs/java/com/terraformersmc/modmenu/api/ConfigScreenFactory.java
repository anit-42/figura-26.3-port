package com.terraformersmc.modmenu.api;
@FunctionalInterface
public interface ConfigScreenFactory<S extends net.minecraft.client.gui.screens.Screen> { S create(net.minecraft.client.gui.screens.Screen parent); }
