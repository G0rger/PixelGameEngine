package net.meloniumcraft.pge.core.callbacks;

import net.meloniumcraft.pge.core.input.ClickType;
import net.meloniumcraft.pge.core.input.Key;

public interface KeyCallback {
    void call(Key key, ClickType type);
}
