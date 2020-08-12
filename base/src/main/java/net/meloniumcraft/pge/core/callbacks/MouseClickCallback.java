package net.meloniumcraft.pge.core.callbacks;

import net.meloniumcraft.pge.core.input.ClickType;

public interface MouseClickCallback {
    void call(int mouseButton, ClickType type);
}
