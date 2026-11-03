package org.figuramc.figura.mixin.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import org.figuramc.figura.avatar.AvatarManager;
import org.figuramc.figura.gui.screens.WardrobeScreen;
import org.figuramc.figura.gui.widgets.Button;
import org.figuramc.figura.utils.FiguraIdentifier;
import org.figuramc.figura.utils.FiguraText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Adds a Figura icon button to the 26.x title screen icon row
 * (friends / language / accessibility, plus whatever other mods put there),
 * then re-centres the whole row so it stays tidy.
 */
@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

    @Unique private static final Identifier FIGURA_ICON = new FiguraIdentifier("textures/gui/icon.png");
    @Unique private static final int ICON = 20, GAP = 4;

    protected TitleScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void figura$addTitleButton(CallbackInfo ci) {
        // find the vanilla icon row via the language / accessibility buttons
        AbstractWidget anchor = null;
        for (GuiEventListener l : this.children()) {
            if (l instanceof AbstractWidget w && w.getWidth() == ICON && w.getHeight() == ICON
                    && w.getMessage().getContents() instanceof TranslatableContents tc
                    && (tc.getKey().equals("options.language") || tc.getKey().equals("options.accessibility"))) {
                anchor = w;
                break;
            }
        }

        int y = anchor != null ? anchor.getY() : this.height / 4 + 48 + 72 + 12 - 24;
        Button figura = new Button(0, y, ICON, ICON, 0, 0, ICON, FIGURA_ICON, 60, 20, FiguraText.of(), btn -> this.minecraft.gui.setScreen(new WardrobeScreen(this))) {
            @Override
            public void extractContents(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta) {
                renderVanillaBackground(gui, mouseX, mouseY, delta);
                super.extractContents(gui, mouseX, mouseY, delta);
            }

            @Override
            protected int getU() {
                int u = super.getU();
                if (u == 1 && AvatarManager.panic)
                    return 0;
                return u;
            }
        };

        if (anchor == null) { // unknown layout: sit left of the Options button row
            figura$button = null;
            figura.setX(this.width / 2 - 100 - GAP - ICON);
            this.addRenderableWidget(figura);
            return;
        }

        figura$button = figura;
        this.addRenderableWidget(figura);
        figura$layoutRow();
    }

    // other mods may add row buttons after us (e.g. Mod Menu), so re-centre lazily too
    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void figura$relayout(GuiGraphicsExtractor gui, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (figura$button != null)
            figura$layoutRow();
    }

    @Unique private Button figura$button;

    @Unique
    private void figura$layoutRow() {
        int y = figura$button.getY();
        // every 20x20 widget on that row near the centre belongs to the row
        List<AbstractWidget> row = new ArrayList<>();
        for (GuiEventListener l : this.children())
            if (l instanceof AbstractWidget w && w != figura$button && w.getWidth() == ICON && w.getHeight() == ICON
                    && w.getY() == y && Math.abs(w.getX() + ICON / 2 - this.width / 2) < 160)
                row.add(w);
        row.sort(Comparator.comparingInt(AbstractWidget::getX));
        row.add(figura$button);

        int total = row.size() * ICON + (row.size() - 1) * GAP;
        int x = this.width / 2 - total / 2;
        for (AbstractWidget w : row) {
            if (w.getX() != x) w.setX(x);
            x += ICON + GAP;
        }
    }
}
