package net.meloniumcraft.pge.core.types;

import net.meloniumcraft.pge.backend.PGEBackend;

public final class Sprite {
    private static PGEBackend backend;
    
    
    private final int id;
    
    private int width;
    private int height;
    
    public Sprite() { this.id = backend.CreateSprite(); }
    public Sprite(String path) { this(path, true); }
    public Sprite(String path, boolean packed) { this(); LoadFromFile(path, packed); }
    public Sprite(int width, int height) { this(); this.width = width; this.height = height; }
    
    public enum FLIP {
        NONE, HORIZONTAL, VERTICAL, BOTH
    }
    
    public int getId() {
        return id;
    }
    
    public static void setBackend(PGEBackend backend) {
        if (Sprite.backend != null)
            throw new IllegalStateException("Sprite: Tried to reassign the backend!");
        Sprite.backend = backend;
    }
    
    public void LoadFromFile(String path, boolean packed) {
        backend.LoadSpriteTexture(this, path, packed);
    }
    
    public int getWidth() {
        return width;
    }
    
    public int getHeight() {
        return height;
    }
    
    public void setWidth(int width) {
        this.width = width;
    }
    
    public void setHeight(int height) {
        this.height = height;
    }
}
