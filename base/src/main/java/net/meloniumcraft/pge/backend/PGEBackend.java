package net.meloniumcraft.pge.backend;

import net.meloniumcraft.pge.core.callbacks.*;
import net.meloniumcraft.pge.core.types.*;

public abstract class PGEBackend {
    public abstract int CreateLayer();
    public abstract int GetLayer();
    public abstract void SetLayer(int layer);
    public abstract void EnableLayer(int layer, boolean b);
    public abstract void SetLayerOffset(int layer, float x, float y);
    public abstract void SetLayerScale(int layer, float x, float y);
    public abstract void SetLayerTint(int layer, Pixel tint);
    
    public abstract void Clear(Pixel p);
    public abstract void Draw(int x, int y, Pixel p);
    public abstract void DrawLine(int x1, int y1, int x2, int y2, Pixel p);
    public abstract void DrawCircle(int x, int y, int radius, Pixel p);
    public abstract void FillCircle(int x, int y, int radius, Pixel p);
    public abstract void DrawRect(int x, int y, int w, int h, Pixel p);
    public abstract void FillRect(int x, int y, int w, int h, Pixel p);
    public abstract void DrawTriangle(int x1, int y1, int x2, int y2, int x3, int y3, Pixel p);
    public abstract void FillTriangle(int x1, int y1, int x2, int y2, int x3, int y3, Pixel p);
    
    public abstract void DrawSprite(int x, int y, Sprite sprite, int scale, Sprite.FLIP flip);
    public abstract void DrawPartialSprite(int x, int y, Sprite sprite, int ox, int oy, int w, int h, int scale, Sprite.FLIP flip);
    
    public abstract void DrawDecal(VF2D pos, Decal decal, VF2D scale, Pixel tint);
    public abstract void DrawPartialDecal(VF2D pos, Decal decal, VF2D source_pos, VF2D source_size, VF2D scale, Pixel tint);
    public abstract void DrawPartialDecal(VF2D pos, VF2D size, Decal decal, VF2D source_pos, VF2D source_size, Pixel tint);
    public abstract void DrawExplicitDecal(Decal decal, VF2D[] pos, VF2D[] uv, Pixel[] col);
    public abstract void DrawWarpedDecal(Decal decal, VF2D[] pos, Pixel tint);
    public abstract void DrawPartialWarpedDecal(Decal decal, VF2D[] pos, VF2D source_pos, VF2D source_size, Pixel tint);
    public abstract void DrawRotatedDecal(VF2D pos, Decal decal, float fAngle, VF2D center, VF2D scale, Pixel tint);
    public abstract void DrawPartialRotatedDecal(VF2D pos, Decal decal, float fAngle, VF2D center, VF2D source_pos, VF2D source_size, VF2D scale, Pixel tint);
    public abstract void FillRectDecal(VF2D pos, VF2D size, Pixel col);
    public abstract void GradientFillRectDecal(VF2D pos, VF2D size, Pixel colTL, Pixel colBL, Pixel colBR, Pixel colTR);
    
    public abstract VI2D GetTextSize(String text);
    public abstract void DrawString(int x, int y, String sText, Pixel col, int scale);
    public abstract void DrawStringDecal(VF2D pos, String sText, Pixel col, VF2D scale);
    
    public abstract int CreateSprite();
    public abstract void MakeSprite(Sprite sprite, int width, int height);
    public abstract void LoadSpriteTexture(Sprite sprite, String path, boolean packed);
    public abstract void DeleteSprite(Sprite sprite);
    public abstract int CreateDecal(Sprite s);
    public abstract void DeleteDecal(Decal decal);
    
    public abstract void Create(int screenW, int screenH, int pixelW, int pixelH, boolean fullScreen, boolean vSync);
    public abstract void Destroy();
    public abstract boolean ShouldClose();
    public abstract void CloseHint(boolean close);
    public abstract void OnPreUpdate();
    public abstract void OnPostUpdate();
    public abstract void SetTitle(String title);
    public abstract int GetMouseButtonCount();
    
    public abstract void RegisterKeyCallBack(KeyCallback callback);
    public abstract void RegisterMouseMoveCallback(MouseMoveCallback callback);
    public abstract void RegisterMouseClickCallback(MouseClickCallback callback);
    public abstract void RegisterScrollCallback(ScrollCallback callback);
}
