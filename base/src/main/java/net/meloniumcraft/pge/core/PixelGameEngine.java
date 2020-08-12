package net.meloniumcraft.pge.core;

import net.meloniumcraft.pge.backend.PGEBackend;
import net.meloniumcraft.pge.core.callbacks.KeyCallback;
import net.meloniumcraft.pge.core.callbacks.MouseClickCallback;
import net.meloniumcraft.pge.core.callbacks.MouseMoveCallback;
import net.meloniumcraft.pge.core.callbacks.ScrollCallback;
import net.meloniumcraft.pge.core.input.HWButton;
import net.meloniumcraft.pge.core.input.Key;
import net.meloniumcraft.pge.core.types.*;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

public abstract class PixelGameEngine {
    protected String sAppName = "";
    
    private final PGEBackend backend;
    
    private int screenW;
    private int screenH;
    private int pixelW;
    private int pixelH;
    private boolean fullScreen;
    private boolean vSync;
    
    private Map<Key, HWButton> keyStates;
    private HWButton[] mouseStates;
    private int mouseX;
    private int mouseY;
    private int wheelDelta;
    
    public PixelGameEngine() {
        backend = ServiceLoader.load(PGEBackend.class).iterator().next();
        Sprite.setBackend(backend);
    }
    
    protected boolean OnUserCreate() { return true; }
    protected abstract boolean OnUserUpdate(float fElapsedTime);
    protected boolean OnUserDestroy() { return true; }
    
    public boolean Construct(int screenW, int screenH, int pixelW, int pixelH) { return Construct(screenW, screenH, pixelW, pixelH, false); }
    public boolean Construct(int screenW, int screenH, int pixelW, int pixelH, boolean fullScreen) { return Construct(screenW, screenH, pixelW, pixelH, fullScreen, false); }
    public boolean Construct(int screenW, int screenH, int pixelW, int pixelH, boolean fullScreen, boolean vSync) {
        if (screenW <= 0 || screenH <= 0 || pixelW <= 0 || pixelH <= 0)
            return false;
        
        this.screenW = screenW;
        this.screenH = screenH;
        this.pixelW = pixelW;
        this.pixelH = pixelH;
        this.fullScreen = fullScreen;
        this.vSync = vSync;
        
        keyStates = new HashMap<>();
        for (Key k : Key.values())
            keyStates.put(k, new HWButton());
        
        mouseStates = new HWButton[backend.GetMouseButtonCount()];
        for (int i = 0; i < mouseStates.length; i++) {
            mouseStates[i] = new HWButton();
        }
        
        return true;
    }
    
    public final void Start() {
        backend.Create(screenW, screenH, pixelW, pixelH, fullScreen, vSync);
        
        long lastTime = System.currentTimeMillis();
        long time, elapsedTime;
        
        float timeCount = 0;
        int fps = 0;
        
        String baseName = "Pixel Game Engine - " + sAppName + " - FPS: ";
        
        backend.RegisterKeyCallBack((key, type) -> {
            HWButton button = keyStates.get(key);
            switch (type) {
                case PRESSED:
                    button.bPressed = true;
                    button.bHeld = true;
                    break;
                case RELEASED:
                    button.bReleased = true;
                    button.bHeld = false;
                    break;
            }
        });
        
        backend.RegisterMouseClickCallback((mouseButton, type) -> {
            HWButton button = mouseStates[mouseButton];
            switch(type) {
                case PRESSED:
                    button.bPressed = true;
                    button.bHeld = true;
                    break;
                case RELEASED:
                    button.bReleased = true;
                    button.bHeld = false;
                    break;
            }
        });
        
        backend.RegisterMouseMoveCallback((x, y) -> {
            mouseX = x;
            mouseY = y;
        });
        
        backend.RegisterScrollCallback(delta -> wheelDelta += delta);
        
        if (!OnUserCreate())
            backend.CloseHint(true);
        
        backend.SetTitle(baseName + 0);
        
        while (!backend.ShouldClose()) {
            while (!backend.ShouldClose()) {
                time = System.currentTimeMillis();
                elapsedTime = time - lastTime;
                lastTime = time;
                timeCount += elapsedTime;
                
                backend.OnPreUpdate();
                if (!OnUserUpdate(elapsedTime / 1000.f))
                    backend.CloseHint(true);
                
                keyStates.values().forEach(state -> {
                    state.bPressed = false;
                    state.bReleased = false;
                });
                
                for (HWButton state : mouseStates) {
                    state.bPressed = false;
                    state.bReleased = false;
                }
                
                backend.OnPostUpdate();
                
                fps++;
                while(timeCount >= 1000) {
                    backend.SetTitle(baseName + fps);
                    fps = 0;
                    timeCount -= 1000;
                }
            }
            if (!OnUserDestroy())
                backend.CloseHint(false);
        }
        backend.Destroy();
    }
    
    public final int  CreateLayer()                               { return backend.CreateLayer();              }
    public final int  GetLayer()                                  { return backend.GetLayer();                 }
    public final void SetLayer(int layer)                         { backend.SetLayer(layer);                   }
    public final void EnableLayer(int layer, boolean b)           { backend.EnableLayer(layer, b);             }
    public final void SetLayerOffset(int layer, VF2D offset)      { SetLayerOffset(layer, offset.x, offset.y); }
    public final void SetLayerOffset(int layer, float x, float y) { backend.SetLayerOffset(layer, x, y);       }
    public final void SetLayerScale(int layer, VF2D offset)       { SetLayerScale(layer, offset.x, offset.y);  }
    public final void SetLayerScale(int layer, float x, float y)  { backend.SetLayerScale(layer, x, y);        }
    public final void SetLayerTint(int layer, Pixel tint)         { backend.SetLayerTint(layer, tint);         }
    
    public final void Clear(Pixel p)                                                        { backend.Clear(p);                                                }
    public final void Draw(VI2D pos)                                                        { Draw(pos.x, pos.y);                                              }
    public final void Draw(VI2D pos, Pixel p)                                               { Draw(pos.x, pos.y, p);                                           }
    public final void Draw(int x, int y)                                                    { Draw(x, y, Pixel.WHITE);                                         }
    public final void Draw(int x, int y, Pixel p)                                           { backend.Draw(x, y, p);                                           }
    public final void DrawLine(VI2D pos1, VI2D pos2)                                        { DrawLine(pos1.x, pos2.y, pos2.x, pos2.y);                        }
    public final void DrawLine(VI2D pos1, VI2D pos2, Pixel p)                               { DrawLine(pos1.x, pos1.y, pos2.x, pos2.y, p);                     }
    public final void DrawLine(int x1, int y1, int x2, int y2)                              { DrawLine(x1, y1, x2, y2, Pixel.WHITE);                           }
    public final void DrawLine(int x1, int y1, int x2, int y2, Pixel p)                     { backend.DrawLine(x1, y1, x2, y2, p);                             }
    public final void DrawCircle(VI2D pos, int radius)                                      { DrawCircle(pos.x, pos.y, radius);                                }
    public final void DrawCircle(VI2D pos, int radius, Pixel p)                             { DrawCircle(pos.x, pos.y, radius, p);                             }
    public final void DrawCircle(int x, int y, int radius)                                  { DrawCircle(x, y, radius, Pixel.WHITE);                           }
    public final void DrawCircle(int x, int y, int radius, Pixel p)                         { backend.DrawCircle(x, y, radius, p);                             }
    public final void FillCircle(VI2D pos, int radius)                                      { FillCircle(pos.x, pos.y, radius);                                }
    public final void FillCircle(VI2D pos, int radius, Pixel p)                             { FillCircle(pos.x, pos.y, radius, p);                             }
    public final void FillCircle(int x, int y, int radius)                                  { FillCircle(x, y, radius, Pixel.WHITE);                           }
    public final void FillCircle(int x, int y, int radius, Pixel p)                         { backend.FillCircle(x, y, radius, p);                             }
    public final void DrawRect(VI2D pos, VI2D size)                                         { DrawRect(pos.x, pos.y, size.x, size.y);                          }
    public final void DrawRect(VI2D pos, VI2D size, Pixel p)                                { DrawRect(pos.x, pos.y, size.x, size.y, p);                       }
    public final void DrawRect(int x, int y, int w, int h)                                  { DrawRect(x, y, w, h, Pixel.WHITE);                               }
    public final void DrawRect(int x, int y, int w, int h, Pixel p)                         { backend.DrawRect(x, y, w, h, p);                                 }
    public final void FillRect(VI2D pos, VI2D size)                                         { FillRect(pos.x, pos.y, size.x, size.y);                          }
    public final void FillRect(VI2D pos, VI2D size, Pixel p)                                { FillRect(pos.x, pos.y, size.x, size.y, p);                       }
    public final void FillRect(int x, int y, int w, int h)                                  { FillRect(x, y, w, h, Pixel.WHITE);                               }
    public final void FillRect(int x, int y, int w, int h, Pixel p)                         { backend.FillRect(x, y, w, h, p);                                 }
    public final void DrawTriangle(VI2D pos1, VI2D pos2, VI2D pos3)                         { DrawTriangle(pos1.x, pos1.y, pos2.x, pos2.y, pos3.x, pos3.y);    }
    public final void DrawTriangle(VI2D pos1, VI2D pos2, VI2D pos3, Pixel p)                { DrawTriangle(pos1.x, pos1.y, pos2.x, pos2.y, pos3.x, pos3.y, p); }
    public final void DrawTriangle(int x1, int y1, int x2, int y2, int x3, int y3)          { DrawTriangle(x1, y1, x2, y2, x3, y3, Pixel.WHITE);               }
    public final void DrawTriangle(int x1, int y1, int x2, int y2, int x3, int y3, Pixel p) { backend.DrawTriangle(x1, y1, x2, y2, x3, y3, p);                 }
    public final void FillTriangle(VI2D pos1, VI2D pos2, VI2D pos3)                         { FillTriangle(pos1.x, pos1.y, pos2.x, pos2.y, pos3.x, pos3.y);    }
    public final void FillTriangle(VI2D pos1, VI2D pos2, VI2D pos3, Pixel p)                { FillTriangle(pos1.x, pos1.y, pos2.x, pos2.y, pos3.x, pos3.y, p); }
    public final void FillTriangle(int x1, int y1, int x2, int y2, int x3, int y3)          { FillTriangle(x1, y1, x2, y2, x3, y3, Pixel.WHITE);               }
    public final void FillTriangle(int x1, int y1, int x2, int y2, int x3, int y3, Pixel p) { backend.FillTriangle(x1, y1, x2, y2, x3, y3, p);                 }
    
    public final void DrawSprite(VI2D pos, Sprite sprite)                                                                       { DrawSprite(pos.x, pos.y, sprite);                                                               }
    public final void DrawSprite(VI2D pos, Sprite sprite, int scale)                                                            { DrawSprite(pos.x, pos.y, sprite, scale);                                                        }
    public final void DrawSprite(VI2D pos, Sprite sprite, int scale, Sprite.FLIP flip)                                          { DrawSprite(pos.x, pos.y, sprite, scale, flip);                                                  }
    public final void DrawSprite(int x, int y, Sprite sprite)                                                                   { DrawSprite(x, y, sprite, 1);                                                                    }
    public final void DrawSprite(int x, int y, Sprite sprite, int scale)                                                        { DrawSprite(x, y, sprite, scale, Sprite.FLIP.NONE);                                              }
    public final void DrawSprite(int x, int y, Sprite sprite, int scale, Sprite.FLIP flip)                                      { backend.DrawSprite(x, y, sprite, scale, flip);                                                  }
    public final void DrawPartialSprite(VI2D pos, Sprite sprite, VI2D sourcepos, VI2D size)                                     { DrawPartialSprite(pos.x, pos.y, sprite, sourcepos.x, sourcepos.y, size.x, size.y);              }
    public final void DrawPartialSprite(VI2D pos, Sprite sprite, VI2D sourcepos, VI2D size, int scale)                          { DrawPartialSprite(pos.x, pos.y, sprite, sourcepos.x, sourcepos.y, size.x, size.y, scale);       }
    public final void DrawPartialSprite(VI2D pos, Sprite sprite, VI2D sourcepos, VI2D size, int scale, Sprite.FLIP flip)        { DrawPartialSprite(pos.x, pos.y, sprite, sourcepos.x, sourcepos.y, size.x, size.y, scale, flip); }
    public final void DrawPartialSprite(int x, int y, Sprite sprite, int ox, int oy, int w, int h)                              { DrawPartialSprite(x, y, sprite, ox, oy, w, h, 1);                                               }
    public final void DrawPartialSprite(int x, int y, Sprite sprite, int ox, int oy, int w, int h, int scale)                   { DrawPartialSprite(x, y, sprite, ox, oy, w, h, scale, Sprite.FLIP.NONE);                         }
    public final void DrawPartialSprite(int x, int y, Sprite sprite, int ox, int oy, int w, int h, int scale, Sprite.FLIP flip) { backend.DrawPartialSprite(x, y, sprite, ox, oy, w, h, scale, flip);                             }
    
    public final void DrawDecal(VF2D pos, Decal decal)                                                                                                     { DrawDecal(pos, decal, new VF2D(1, 1));                                                             }
    public final void DrawDecal(VF2D pos, Decal decal, VF2D scale)                                                                                         { DrawDecal(pos, decal, scale, Pixel.WHITE);                                                         }
    public final void DrawDecal(VF2D pos, Decal decal, VF2D scale, Pixel tint)                                                                             { backend.DrawDecal(pos, decal, scale, tint);                                                        }
    public final void DrawPartialDecal(VF2D pos, Decal decal, VF2D source_pos, VF2D source_size)                                                           { DrawPartialDecal(pos, decal, source_pos, source_size, new VF2D(1, 1));                             }
    public final void DrawPartialDecal(VF2D pos, Decal decal, VF2D source_pos, VF2D source_size, VF2D scale)                                               { DrawPartialDecal(pos, decal, source_pos, source_size, scale, Pixel.WHITE);                         }
    public final void DrawPartialDecal(VF2D pos, Decal decal, VF2D source_pos, VF2D source_size, VF2D scale, Pixel tint)                                   { backend.DrawPartialDecal(pos, decal, source_pos, source_size, scale, tint);                        }
    public final void DrawPartialDecal(VF2D pos, VF2D size, Decal decal, VF2D source_pos, VF2D source_size)                                                { DrawPartialDecal(pos, size, decal, source_pos, source_size, Pixel.WHITE);                          }
    public final void DrawPartialDecal(VF2D pos, VF2D size, Decal decal, VF2D source_pos, VF2D source_size, Pixel tint)                                    { backend.DrawPartialDecal(pos, size, decal, source_pos, source_size, tint);                         }
    public final void DrawExplicitDecal(Decal decal, VF2D[] pos, VF2D[] uv, Pixel[] col)                                                                   { backend.DrawExplicitDecal(decal, pos, uv, col);                                                    }
    public final void DrawWarpedDecal(Decal decal, VF2D[] pos)                                                                                             { DrawWarpedDecal(decal, pos, Pixel.WHITE);                                                          }
    public final void DrawWarpedDecal(Decal decal, VF2D[] pos, Pixel tint)                                                                                 { backend.DrawWarpedDecal(decal, pos, tint);                                                         }
    public final void DrawPartialWarpedDecal(Decal decal, VF2D[] pos, VF2D source_pos, VF2D source_size)                                                   { DrawPartialWarpedDecal(decal, pos, source_pos, source_size, Pixel.WHITE);                          }
    public final void DrawPartialWarpedDecal(Decal decal, VF2D[] pos, VF2D source_pos, VF2D source_size, Pixel tint)                                       { backend.DrawPartialWarpedDecal(decal, pos, source_pos, source_size, tint);                         }
    public final void DrawRotatedDecal(VF2D pos, Decal decal, float fAngle, VF2D center)                                                                   { DrawRotatedDecal(pos, decal, fAngle, center, new VF2D(1, 1));                                      }
    public final void DrawRotatedDecal(VF2D pos, Decal decal, float fAngle, VF2D center, VF2D scale)                                                       { DrawRotatedDecal(pos, decal, fAngle, center, scale, Pixel.WHITE);                                  }
    public final void DrawRotatedDecal(VF2D pos, Decal decal, float fAngle, VF2D center, VF2D scale, Pixel tint)                                           { backend.DrawRotatedDecal(pos, decal, fAngle, center, scale, tint);                                 }
    public final void DrawPartialRotatedDecal(VF2D pos, Decal decal, float fAngle, VF2D center, VF2D source_pos, VF2D source_size)                         { DrawPartialRotatedDecal(pos, decal, fAngle, center, source_pos, source_size, new VF2D(1, 1));      }
    public final void DrawPartialRotatedDecal(VF2D pos, Decal decal, float fAngle, VF2D center, VF2D source_pos, VF2D source_size, VF2D scale)             { DrawPartialRotatedDecal(pos, decal, fAngle, center, source_pos, source_size, scale, Pixel.WHITE);  }
    public final void DrawPartialRotatedDecal(VF2D pos, Decal decal, float fAngle, VF2D center, VF2D source_pos, VF2D source_size, VF2D scale, Pixel tint) { backend.DrawPartialRotatedDecal(pos, decal, fAngle, center, source_pos, source_size, scale, tint); }
    public final void FillRectDecal(VF2D pos, VF2D size, Pixel col)                                                                                        { backend.FillRectDecal(pos, size, col);                                                             }
    public final void GradientFillRectDecal(VF2D pos, VF2D size, Pixel colTL, Pixel colBL, Pixel colBR, Pixel colTR)                                       { backend.GradientFillRectDecal(pos, size, colTL, colBL, colBR, colTR);                              }
    
    public final VI2D GetTextSize(String text)                                       { return backend.GetTextSize(text);                 }
    public final void DrawString(int x, int y, String sText)                         { DrawString(x, y, sText, Pixel.WHITE);             }
    public final void DrawString(int x, int y, String sText, Pixel col)              { DrawString(x, y, sText, col, 1);                  }
    public final void DrawString(int x, int y, String sText, Pixel col, int scale)   { backend.DrawString(x, y, sText, col, scale);      }
    public final void DrawStringDecal(VF2D pos, String sText)                        { DrawStringDecal(pos, sText, Pixel.WHITE);         }
    public final void DrawStringDecal(VF2D pos, String sText, Pixel col)             { DrawStringDecal(pos, sText, col, new VF2D(1, 1)); }
    public final void DrawStringDecal(VF2D pos, String sText, Pixel col, VF2D scale) { backend.DrawStringDecal(pos, sText, col, scale);  }
    
    public final HWButton GetKey(Key k)                                       { return keyStates.get(k);                      }
    public final void RegisterKeyCallback(KeyCallback callback)               { backend.RegisterKeyCallBack(callback);        }
    public final HWButton GetMouse(int mouse)                                 { return mouseStates[mouse];                    }
    public final void RegisterMouseClickCallback(MouseClickCallback callback) { backend.RegisterMouseClickCallback(callback); }
    public final int GetMouseX()                                              { return mouseX;                                }
    public final int GetMouseY()                                              { return mouseY;                                }
    public final void RegisterMouseMoveCallback(MouseMoveCallback callback)   { backend.RegisterMouseMoveCallback(callback);  }
    public final int GetMouseWheel()                                          { return wheelDelta;                            }
    public final void RegisterScrollCallback(ScrollCallback callback)         { backend.RegisterScrollCallback(callback);     }
}
