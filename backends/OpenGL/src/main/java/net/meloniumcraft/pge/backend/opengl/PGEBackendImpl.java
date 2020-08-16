package net.meloniumcraft.pge.backend.opengl;

import net.meloniumcraft.pge.backend.PGEBackend;
import net.meloniumcraft.pge.core.callbacks.KeyCallback;
import net.meloniumcraft.pge.core.callbacks.MouseClickCallback;
import net.meloniumcraft.pge.core.callbacks.MouseMoveCallback;
import net.meloniumcraft.pge.core.callbacks.ScrollCallback;
import net.meloniumcraft.pge.core.input.ClickType;
import net.meloniumcraft.pge.core.input.Key;
import net.meloniumcraft.pge.core.types.*;
import net.meloniumcraft.utils.list.ReverseListIterator;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL32;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.lwjgl.system.MemoryUtil.NULL;

public final class PGEBackendImpl extends PGEBackend {
    private static class LayerDesc {
        public int texID;
        public int frambufferID;
        public VF2D offset;
        public VF2D scale;
        public final LinkedList<DecalInstance> decals;
        public Pixel tint;
        public boolean bShow;
        public final int id;
        
        public LayerDesc(int texID, int frambufferID, int id) {
            this.texID = texID;
            this.frambufferID = frambufferID;
            offset = new VF2D();
            scale = new VF2D(1, 1);
            decals = new LinkedList<>();
            tint = Pixel.WHITE;
            bShow = true;
            this.id = id;
        }
    }
    private static class DecalInstance {
        public Decal decal;
        public VF2D[] pos = new VF2D[] {new VF2D(), new VF2D(), new VF2D(), new VF2D()};
        public VF2D[] uv = new VF2D[] {new VF2D(0, 0), new VF2D(1, 0), new VF2D(1, 1), new VF2D(0, 1)};
        public float[] w = new float[4];
        public Pixel[] tint = new Pixel[] {Pixel.WHITE, Pixel.WHITE, Pixel.WHITE, Pixel.WHITE};
    }
    
    private VI2D screenSize;
    private VI2D windowSize;
    private VI2D windowOffset;
    private long window;
    
    private Sprite fontSprite;
    private Decal fontDecal;
    
    private final GLFWErrorCallback errorCallback = GLFWErrorCallback.createPrint(System.err);
    private GLFWKeyCallback keyCallback;
    private GLFWMouseButtonCallback mouseButtonCallback;
    private GLFWCursorPosCallback cursorPosCallback;
    private GLFWScrollCallback scrollCallback;
    
    private List<KeyCallback> keyCallbacks;
    private List<MouseMoveCallback> mouseMoveCallbacks;
    private List<MouseClickCallback> mouseClickCallbacks;
    private List<ScrollCallback> scrollCallbacks;
    
    private static final Map<Integer, Key> keyMap = Stream.of(new Object[][] {
            {GLFW.GLFW_KEY_A, Key.A}, {GLFW.GLFW_KEY_B, Key.B}, {GLFW.GLFW_KEY_C, Key.C}, {GLFW.GLFW_KEY_D, Key.D},
            {GLFW.GLFW_KEY_E, Key.E}, {GLFW.GLFW_KEY_F, Key.F}, {GLFW.GLFW_KEY_G, Key.G}, {GLFW.GLFW_KEY_H, Key.H},
            {GLFW.GLFW_KEY_I, Key.I}, {GLFW.GLFW_KEY_J, Key.J}, {GLFW.GLFW_KEY_K, Key.K}, {GLFW.GLFW_KEY_L, Key.L},
            {GLFW.GLFW_KEY_M, Key.M}, {GLFW.GLFW_KEY_N, Key.N}, {GLFW.GLFW_KEY_O, Key.O}, {GLFW.GLFW_KEY_P, Key.P},
            {GLFW.GLFW_KEY_Q, Key.Q}, {GLFW.GLFW_KEY_R, Key.R}, {GLFW.GLFW_KEY_S, Key.S}, {GLFW.GLFW_KEY_T, Key.T},
            {GLFW.GLFW_KEY_U, Key.U}, {GLFW.GLFW_KEY_V, Key.V}, {GLFW.GLFW_KEY_W, Key.W}, {GLFW.GLFW_KEY_X, Key.X},
            {GLFW.GLFW_KEY_Y, Key.Y}, {GLFW.GLFW_KEY_Z, Key.Z}, {GLFW.GLFW_KEY_0, Key.K0}, {GLFW.GLFW_KEY_1, Key.K1},
            {GLFW.GLFW_KEY_2, Key.K2}, {GLFW.GLFW_KEY_3, Key.K3}, {GLFW.GLFW_KEY_4, Key.K4}, {GLFW.GLFW_KEY_5, Key.K5},
            {GLFW.GLFW_KEY_6, Key.K6}, {GLFW.GLFW_KEY_7, Key.K7}, {GLFW.GLFW_KEY_8, Key.K8}, {GLFW.GLFW_KEY_9, Key.K9},
            {GLFW.GLFW_KEY_F1, Key.F1}, {GLFW.GLFW_KEY_F2, Key.F2}, {GLFW.GLFW_KEY_F3, Key.F3},
            {GLFW.GLFW_KEY_F4, Key.F4}, {GLFW.GLFW_KEY_F5, Key.F5}, {GLFW.GLFW_KEY_F6, Key.F6},
            {GLFW.GLFW_KEY_F7, Key.F7}, {GLFW.GLFW_KEY_F8, Key.F8}, {GLFW.GLFW_KEY_F9, Key.F9},
            {GLFW.GLFW_KEY_F10, Key.F10}, {GLFW.GLFW_KEY_F11, Key.F11}, {GLFW.GLFW_KEY_F12, Key.F12},
            {GLFW.GLFW_KEY_UP, Key.UP}, {GLFW.GLFW_KEY_DOWN, Key.DOWN}, {GLFW.GLFW_KEY_LEFT, Key.LEFT},
            {GLFW.GLFW_KEY_RIGHT, Key.RIGHT}, {GLFW.GLFW_KEY_SPACE, Key.SPACE}, {GLFW.GLFW_KEY_TAB, Key.TAB},
            {GLFW.GLFW_KEY_LEFT_SHIFT, Key.SHIFT}, {GLFW.GLFW_KEY_RIGHT_SHIFT, Key.SHIFT},
            {GLFW.GLFW_KEY_LEFT_CONTROL, Key.CTRL}, {GLFW.GLFW_KEY_RIGHT_CONTROL, Key.CTRL},
            {GLFW.GLFW_KEY_INSERT, Key.INS}, {GLFW.GLFW_KEY_DELETE, Key.DEL}, {GLFW.GLFW_KEY_HOME, Key.HOME},
            {GLFW.GLFW_KEY_END, Key.END}, {GLFW.GLFW_KEY_PAGE_UP, Key.PGUP}, {GLFW.GLFW_KEY_PAGE_DOWN, Key.PGDN},
            {GLFW.GLFW_KEY_BACKSPACE, Key.BACK}, {GLFW.GLFW_KEY_ESCAPE, Key.ESCAPE}, {GLFW.GLFW_KEY_ENTER, Key.RETURN},
            {GLFW.GLFW_KEY_KP_ENTER, Key.ENTER}, {GLFW.GLFW_KEY_PAUSE, Key.PAUSE},
            {GLFW.GLFW_KEY_SCROLL_LOCK, Key.SCROLL}, {GLFW.GLFW_KEY_KP_0, Key.NP0}, {GLFW.GLFW_KEY_KP_1, Key.NP1},
            {GLFW.GLFW_KEY_KP_2, Key.NP2}, {GLFW.GLFW_KEY_KP_3, Key.NP3}, {GLFW.GLFW_KEY_KP_4, Key.NP4},
            {GLFW.GLFW_KEY_KP_5, Key.NP5}, {GLFW.GLFW_KEY_KP_6, Key.NP6}, {GLFW.GLFW_KEY_KP_7, Key.NP7},
            {GLFW.GLFW_KEY_KP_8, Key.NP8}, {GLFW.GLFW_KEY_KP_9, Key.NP9}, {GLFW.GLFW_KEY_KP_MULTIPLY, Key.NP_MUL},
            {GLFW.GLFW_KEY_KP_DIVIDE, Key.NP_ADD}, {GLFW.GLFW_KEY_KP_ADD, Key.NP_ADD},
            {GLFW.GLFW_KEY_KP_SUBTRACT, Key.NP_SUB}, {GLFW.GLFW_KEY_KP_DECIMAL, Key.NP_DECIMAL},
            {GLFW.GLFW_KEY_PERIOD, Key.PERIOD},
    }).collect(Collectors.collectingAndThen(Collectors.toMap(data -> (Integer)data[0], data -> (Key)data[1]), Collections::<Integer, Key>unmodifiableMap));
    
    private LayerDesc layer;
    private List<LayerDesc> layers;
    
    @Override
    public int CreateLayer() {
        int id = layers.size();
        
        int bufferID = GL32.glGenFramebuffers();
        GL32.glBindFramebuffer(GL32.GL_FRAMEBUFFER, bufferID);
        
        int texID = GL32.glGenTextures();
        
        GL32.glBindTexture(GL32.GL_TEXTURE_2D, texID);
        GL32.glTexImage2D(GL32.GL_TEXTURE_2D, 0, GL32.GL_RGBA8, screenSize.x, screenSize.y, 0, GL32.GL_RGBA, GL32.GL_UNSIGNED_BYTE, 0);
        GL32.glTexParameteri(GL32.GL_TEXTURE_2D, GL32.GL_TEXTURE_MAG_FILTER, GL32.GL_NEAREST);
        GL32.glTexParameteri(GL32.GL_TEXTURE_2D, GL32.GL_TEXTURE_MIN_FILTER, GL32.GL_NEAREST);
        GL32.glBindTexture(GL32.GL_TEXTURE_2D, 0);
        
        GL32.glFramebufferTexture(GL32.GL_FRAMEBUFFER, GL32.GL_COLOR_ATTACHMENT0, texID, 0);
        
        if (GL32.glCheckFramebufferStatus(GL32.GL_FRAMEBUFFER) != GL32.GL_FRAMEBUFFER_COMPLETE) throw new IllegalStateException("Couldn't create framebuffer");
        
        layer = new LayerDesc(texID, bufferID, id);
        layers.add(layer);
        return id;
    }
    
    @Override
    public int GetLayer() {
        return layer.id;
    }
    
    @Override
    public void SetLayer(int layer) {
        this.layer = layers.get(layer);
        GL32.glBindFramebuffer(GL32.GL_FRAMEBUFFER, this.layer.frambufferID);
    }
    
    @Override
    public void EnableLayer(int layer, boolean b) {
        layers.get(layer).bShow = b;
    }
    
    @Override
    public void SetLayerOffset(int layer, float x, float y) {
        layers.get(layer).offset = new VF2D(x, y);
    }
    
    @Override
    public void SetLayerScale(int layer, float x, float y) {
        layers.get(layer).scale = new VF2D(x, y);
    }
    
    @Override
    public void SetLayerTint(int layer, Pixel tint) {
        layers.get(layer).tint = tint;
    }
    
    @Override
    public void Clear(Pixel p) {
        GL32.glClearColor(((int)p.r & 0xff) / 255.f, ((int)p.g & 0xff) / 255.f, ((int)p.b & 0xff) / 255.f, ((int)p.a & 0xff) / 255.f);
        GL32.glClear(GL32.GL_COLOR_BUFFER_BIT);
    }
    
    @Override
    public void Draw(int x, int y, Pixel p) {
        GL32.glBegin(GL32.GL_POINTS);
        GL32.glColor4ub(p.r, p.g, p.b, p.a);
        GL32.glVertex2f(x+.5f, y+.5f);
        GL32.glEnd();
    }
    
    @Override
    public void DrawLine(int x1, int y1, int x2, int y2, Pixel p) {
        GL32.glBegin(GL32.GL_LINE_LOOP);
        GL32.glColor4ub(p.r, p.g, p.b, p.a);
        GL32.glVertex2f(x1+.5f, y1+.5f);
        GL32.glVertex2f(x2+.5f, y2+.5f);
        GL32.glEnd();
    }
    
    @Override
    public void DrawCircle(int x, int y, int radius, Pixel p) {
        if (radius < 0 || x < -radius || y < -radius || x - screenSize.x > radius || y - screenSize.y > radius)
            return;
        
        if (radius > 0) {
            int x0 = 0;
            int y0 = radius;
            int d = 3 - 2*radius;
            while (y0 >= x0) {
                Draw(x + x0, y - y0, p);
                Draw(x + y0, y + x0, p);
                Draw(x - x0, y + y0, p);
                Draw(x - y0, y - x0, p);
                if (x0 != 0 && x0 != y0) {
                    Draw(x + y0, y - x0, p);
                    Draw(x + x0, y + y0, p);
                    Draw(x - y0, y + x0, p);
                    Draw(x - x0, y - y0, p);
                }
                if (d < 0)
                    d += 4 * x0++ + 6;
                else
                    d += 4 * (x0++ - y0--) + 10;
            }
        } else
            Draw(x, y, p);
    }
    
    @Override
    public void FillCircle(int x, int y, int radius, Pixel p) {
        if (radius < 0 || x < -radius || y < -radius || x - screenSize.x > radius || y - screenSize.y > radius)
            return;
        
        if (radius > 0) {
            int x0 = 0;
            int y0 = radius;
            int d = 3 - 2*radius;
            while (y0 >= x0) {
                DrawLine(x - y0, y - x0, x + y0, y - x0, p);
                if (x0 > 0) DrawLine(x - y0, y + x0, x + y0, y + x0, p);
                
                if (d < 0)
                    d += 4 * x0++ + 6;
                else {
                    if (x0 != y0) {
                        DrawLine(x - x0, y - y0, x + x0, y - y0, p);
                        DrawLine(x - x0, y + y0, x + x0, y + y0, p);
                    }
                    d += 4 * (x0++ - y0--) + 10;
                }
            }
        } else
            Draw(x, y, p);
    }
    
    @Override
    public void DrawRect(int x, int y, int w, int h, Pixel p) {
        float nx1 = x+.5f;
        float ny1 = y+.5f;
        float nx2 = nx1+w-1;
        float ny2 = ny1+h-1;
        GL32.glBegin(GL32.GL_LINE_LOOP);
        GL32.glColor4ub(p.r, p.g, p.b, p.a);
        GL32.glVertex2f(nx1, ny1);
        GL32.glVertex2f(nx2, ny1);
        GL32.glVertex2f(nx2, ny2);
        GL32.glVertex2f(nx1, ny2);
        GL32.glEnd();
    }
    
    @Override
    public void FillRect(int x, int y, int w, int h, Pixel p) {
        int nx = x+w;
        int ny = y+h;
        GL32.glBegin(GL32.GL_QUADS);
        GL32.glColor4ub(p.r, p.g, p.b, p.a);
        GL32.glVertex2i(x, y);
        GL32.glVertex2i(nx, y);
        GL32.glVertex2i(nx, ny);
        GL32.glVertex2i(x, ny);
        GL32.glEnd();
    }
    
    @Override
    public void DrawTriangle(int x1, int y1, int x2, int y2, int x3, int y3, Pixel p) {
        GL32.glBegin(GL32.GL_LINE_LOOP);
        GL32.glColor4ub(p.r, p.g, p.b, p.a);
        GL32.glVertex2f(x1+.5f, y1+.5f);
        GL32.glVertex2f(x2+.5f, y2+.5f);
        GL32.glVertex2f(x3+.5f, y3+.5f);
        GL32.glEnd();
    }
    
    @Override
    public void FillTriangle(int x1, int y1, int x2, int y2, int x3, int y3, Pixel p) {
        GL32.glBegin(GL32.GL_TRIANGLES);
        GL32.glColor4ub(p.r, p.g, p.b, p.a);
        GL32.glVertex2i(x1, y1);
        GL32.glVertex2i(x2, y2);
        GL32.glVertex2i(x3, y3);
        GL32.glEnd();
    }
    
    @Override
    public void DrawSprite(int x, int y, Sprite sprite, int scale, Sprite.FLIP flip) {
        int u1 = 0;
        int v1 = 0;
        int u2 = 0;
        int v2 = 0;
        switch (flip) {
            case NONE:
                u2 = 1;
                v2 = 1;
                break;
            case HORIZONTAL:
                u2 = 1;
                v1 = 1;
                break;
            case VERTICAL:
                u1 = 1;
                v2 = 1;
                break;
            case BOTH:
                u1 = 1;
                v1 = 1;
        }
        GL32.glColor4f(1, 1, 1, 1);
        GL32.glBindTexture(GL32.GL_TEXTURE_2D, sprite.getId());
        GL32.glBegin(GL32.GL_QUADS);
        GL32.glTexCoord2f(u1, v1);
        GL32.glVertex2i(x, y);
        GL32.glTexCoord2f(u2, v1);
        GL32.glVertex2i(x + sprite.getWidth() * scale, y);
        GL32.glTexCoord2f(u2, v2);
        GL32.glVertex2i(x + sprite.getWidth() * scale, y + sprite.getHeight() * scale);
        GL32.glTexCoord2f(u1, v2);
        GL32.glVertex2i(x, y + sprite.getHeight() * scale);
        GL32.glEnd();
        GL32.glBindTexture(GL32.GL_TEXTURE_2D, 0);
    }
    
    @Override
    public void DrawPartialSprite(int x, int y, Sprite sprite, int ox, int oy, int w, int h, int scale, Sprite.FLIP flip) {
    }
    
    @Override
    public void DrawDecal(VF2D pos, Decal decal, VF2D scale, Pixel tint) {
        float w = decal.getSprite().getWidth() * scale.x;
        float h = decal.getSprite().getHeight() * scale.y;
        DecalInstance di = new DecalInstance();
        di.decal = decal;
        di.pos[0] = new VF2D(pos.x, pos.y);
        di.pos[1] = new VF2D(pos.x+w, pos.y);
        di.pos[2] = new VF2D(pos.x+w, pos.y+h);
        di.pos[3] = new VF2D(pos.x, pos.y+h);
        di.tint[0] = tint;
        layer.decals.add(di);
    }
    
    @Override
    public void DrawPartialDecal(VF2D pos, Decal decal, VF2D source_pos, VF2D source_size, VF2D scale, Pixel tint) {
        float x1 = pos.x;
        float y1 = pos.y;
        float x2 = pos.x + source_size.x * scale.x;
        float y2 = pos.y + source_size.y * scale.y;
        
        DecalInstance di = new DecalInstance();
        di.decal = decal;
        di.tint[0] = tint;
        
        di.pos[0] = new VF2D(x1, y1);
        di.pos[1] = new VF2D(x1, y2);
        di.pos[2] = new VF2D(x2, y2);
        di.pos[3] = new VF2D(x2, y1);
        
        float u1 = source_pos.x * decal.uvScale.x;
        float v1 = source_pos.y * decal.uvScale.y;
        float u2 = u1 + source_size.x * decal.uvScale.x;
        float v2 = v1 + source_size.y * decal.uvScale.y;
        
        di.uv[0] = new VF2D(u1, v1);
        di.uv[1] = new VF2D(u1, v2);
        di.uv[2] = new VF2D(u2, v2);
        di.uv[3] = new VF2D(u2, v1);
        layer.decals.add(di);
    }
    
    @Override
    public void DrawPartialDecal(VF2D pos, VF2D size, Decal decal, VF2D source_pos, VF2D source_size, Pixel tint) {
        float x1 = pos.x;
        float y1 = pos.y;
        float x2 = pos.x + size.x;
        float y2 = pos.y + size.y;
        
        DecalInstance di = new DecalInstance();
        di.decal = decal;
        di.tint[0] = tint;
        
        di.pos[0] = new VF2D(x1, y1);
        di.pos[1] = new VF2D(x1, y2);
        di.pos[2] = new VF2D(x2, y2);
        di.pos[3] = new VF2D(x2, y1);
        
        float u1 = source_pos.x * decal.uvScale.x;
        float v1 = source_pos.y * decal.uvScale.y;
        float u2 = u1 + source_size.x * decal.uvScale.x;
        float v2 = v1 + source_size.y * decal.uvScale.y;
        
        di.uv[0] = new VF2D(u1, v1);
        di.uv[1] = new VF2D(u1, v2);
        di.uv[2] = new VF2D(u2, v2);
        di.uv[3] = new VF2D(u2, v1);
        layer.decals.add(di);
    }
    
    @Override
    public void DrawExplicitDecal(Decal decal, VF2D[] pos, VF2D[] uv, Pixel[] col) {
        DecalInstance di = new DecalInstance();
        di.decal = decal;
        di.pos = pos;
        di.uv = uv;
        di.tint = col;
        layer.decals.add(di);
    }
    
    @Override
    public void DrawWarpedDecal(Decal decal, VF2D[] pos, Pixel tint) {
        DecalInstance di = new DecalInstance();
        di.decal = decal;
        di.tint[0] = tint;
        VF2D center = new VF2D();
        float rd = ((pos[2].x - pos[0].x) * (pos[3].y - pos[1].y) - (pos[3].x - pos[1].x) * (pos[2].y - pos[0].y));
        if (rd != 0) {
            rd = 1.f / rd;
            float rn = ((pos[3].x - pos[1].x) * (pos[0].y - pos[1].y) - (pos[3].y - pos[1].y) * (pos[0].x - pos[1].x)) * rd;
            float sn = ((pos[2].x - pos[0].x) * (pos[0].y - pos[1].y) - (pos[2].y - pos[0].y) * (pos[0].x - pos[1].x)) * rd;
            if (!(rn < 0.f || rn > 1.f || sn < 0.f || sn > 1.f)) center = pos[0].add(pos[2].subtract(pos[0]).multiply(rn));
            float[] d = new float[4]; for (int i = 0; i < 4; i++) d[i] = pos[i].subtract(center).mag();
            for (int i = 0; i < 4; i++) {
                float q = d[i] == 0.f ? 1.f : (d[i] + d[(i + 2) & 3]) / d[(i + 2) & 3];
                di.uv[i] = di.uv[i].multiply(q); di.w[i] *= q;
                di.pos[i] = new VF2D(pos[i]);
            }
            layer.decals.add(di);
        }
    }
    
    @Override
    public void DrawPartialWarpedDecal(Decal decal, VF2D[] pos, VF2D source_pos, VF2D source_size, Pixel tint) {
        DecalInstance di = new DecalInstance();
        di.decal = decal;
        di.tint[0] = tint;
        VF2D center = new VF2D();
        float rd = ((pos[2].x - pos[0].x) * (pos[3].y - pos[1].y) - (pos[3].x - pos[1].x) * (pos[2].y - pos[0].y));
        if (rd != 0) {
            float u1 = source_pos.x * decal.uvScale.x;
            float v1 = source_pos.y * decal.uvScale.y;
            float u2 = u1 + source_size.x * decal.uvScale.x;
            float v2 = v1 + source_size.y * decal.uvScale.y;
            
            di.uv[0] = new VF2D(u1, v1);
            di.uv[1] = new VF2D(u1, v2);
            di.uv[2] = new VF2D(u2, v2);
            di.uv[3] = new VF2D(u2, v1);
            
            rd = 1.f / rd;
            float rn = ((pos[3].x - pos[1].x) * (pos[0].y - pos[1].y) - (pos[3].y - pos[1].y) * (pos[0].x - pos[1].x)) * rd;
            float sn = ((pos[2].x - pos[0].x) * (pos[0].y - pos[1].y) - (pos[2].y - pos[0].y) * (pos[0].x - pos[1].x)) * rd;
            if (!(rn < 0.f || rn > 1.f || sn < 0.f || sn > 1.f)) center = pos[0].add(pos[2].subtract(pos[0]).multiply(rn));
            float[] d = new float[4]; for (int i = 0; i < 4; i++) d[i] = pos[i].subtract(center).mag();
            for (int i = 0; i < 4; i++) {
                float q = d[i] == 0.f ? 1.f : (d[i] + d[(i + 2) & 3]) / d[(i + 2) & 3];
                di.uv[i] = di.uv[i].multiply(q); di.w[i] *= q;
                di.pos[i] = new VF2D(pos[i]);
            }
            layer.decals.add(di);
        }
    }
    
    @Override
    public void DrawRotatedDecal(VF2D pos, Decal decal, float fAngle, VF2D center, VF2D scale, Pixel tint) {
    }
    
    @Override
    public void DrawPartialRotatedDecal(VF2D pos, Decal decal, float fAngle, VF2D center, VF2D source_pos, VF2D source_size, VF2D scale, Pixel tint) {
    }
    
    @Override
    public void FillRectDecal(VF2D pos, VF2D size, Pixel col) {
    }
    
    @Override
    public void GradientFillRectDecal(VF2D pos, VF2D size, Pixel colTL, Pixel colBL, Pixel colBR, Pixel colTR) {
    }
    
    @Override
    public VI2D GetTextSize(String text) {
        return null;
    }
    
    @Override
    public void DrawString(int x, int y, String sText, Pixel col, int scale) {
        int sx = 0;
        int sy = 0;
        GL32.glBindTexture(GL32.GL_TEXTURE_2D, fontSprite.getId());
        GL32.glBegin(GL32.GL_QUADS);
        GL32.glColor4ub(col.r, col.g, col.b, col.a);
        for (char c : sText.toCharArray()) {
            if (c == '\n') {
                sx = 0;
                sy += 8 * scale;
            } else {
                int ox = (c - 32) % 16;
                int oy = (c - 32) / 16;
                
                GL32.glTexCoord2f((ox) / 16.f, (oy) / 6.f); GL32.glVertex2i(x+sx, y+sy);
                GL32.glTexCoord2f((ox+1) / 16.f, (oy) / 6.f); GL32.glVertex2i(x+sx+8, y+sy);
                GL32.glTexCoord2f((ox+1) / 16.f, (oy+1) / 6.f); GL32.glVertex2i(x+sx+8, y+sy+8);
                GL32.glTexCoord2f((ox) / 16.f, (oy+1) / 6.f); GL32.glVertex2i(x+sx, y+sy+8);
                
                sx += 8 * scale;
            }
        }
        GL32.glEnd();
        GL32.glBindTexture(GL32.GL_TEXTURE_2D, 0);
    }
    
    @Override
    public void DrawStringDecal(VF2D pos, String sText, Pixel col, VF2D scale) {
    }
    
    @Override
    public int CreateSprite() {
        return GL32.glGenTextures();
    }
    
    @Override
    public void MakeSprite(Sprite sprite, int width, int height) {
        GL32.glBindTexture(GL32.GL_TEXTURE_2D, sprite.getId());
        GL32.glTexImage2D(GL32.GL_TEXTURE_2D, 0, GL32.GL_RGBA8, width, height, 0, GL32.GL_RGBA, GL32.GL_UNSIGNED_BYTE, 0);
        GL32.glTexParameteri(GL32.GL_TEXTURE_2D, GL32.GL_TEXTURE_MAG_FILTER, GL32.GL_NEAREST);
        GL32.glTexParameteri(GL32.GL_TEXTURE_2D, GL32.GL_TEXTURE_MIN_FILTER, GL32.GL_NEAREST);
        GL32.glBindTexture(GL32.GL_TEXTURE_2D, 0);
    }
    
    @Override
    public void LoadSpriteTexture(Sprite sprite, String path, boolean packed) {
        if (!packed) {
            int w, h;
            ByteBuffer image;
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer width = stack.mallocInt(1);
                IntBuffer height = stack.mallocInt(1);
                IntBuffer comp = stack.mallocInt(1);
                
                image = STBImage.stbi_load(path, width, height, comp, 4);
                
                w = width.get();
                h = height.get();
            }
            sprite.setWidth(w);
            sprite.setHeight(h);
            GL32.glBindTexture(GL32.GL_TEXTURE_2D, sprite.getId());
            GL32.glTexImage2D(GL32.GL_TEXTURE_2D, 0, GL32.GL_RGBA8, w, h, 0, GL32.GL_RGBA, GL32.GL_UNSIGNED_BYTE, image);
            GL32.glTexParameteri(GL32.GL_TEXTURE_2D, GL32.GL_TEXTURE_MAG_FILTER, GL32.GL_NEAREST);
            GL32.glTexParameteri(GL32.GL_TEXTURE_2D, GL32.GL_TEXTURE_MIN_FILTER, GL32.GL_NEAREST);
            GL32.glBindTexture(GL32.GL_TEXTURE_2D, 0);
        }
    }
    
    @Override
    public void DeleteSprite(Sprite sprite) {
        GL32.glDeleteTextures(sprite.getId());
    }
    
    @Override
    public int CreateDecal(Sprite s) {
        return s.getId();
    }
    
    @Override
    public void DeleteDecal(Decal decal) { }
    
    @Override
    public void Create(int screenW, int screenH, int pixelW, int pixelH, boolean fullScreen, boolean vSync) {
        GLFW.glfwSetErrorCallback(errorCallback);
        GLFW.glfwInit();
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE);
        
        this.screenSize = new VI2D(screenW, screenH);
        
        long monitor = NULL;
        int width, height;
        
        if (fullScreen) {
            float ratio = (float)screenW / screenH;
            
            monitor = GLFW.glfwGetPrimaryMonitor();
            GLFWVidMode mode = GLFW.glfwGetVideoMode(monitor);
            if (mode == null) {
                GLFW.glfwTerminate();
                throw new RuntimeException("Failed to get the GLFW Video Mode");
            }
            
            width = mode.width();
            height = mode.height();
            
            int windowW, windowH;
            
            windowW = (int)(ratio * height);
            if (windowW <= width) {
                this.windowSize = new VI2D(windowW, height);
                this.windowOffset = new VI2D((width - windowW) / 2, 0);
            } else {
                windowH = (int)(width / ratio);
                this.windowSize = new VI2D(width, windowH);
                this.windowOffset = new VI2D(0, (height - windowH) / 2);
            }
        } else {
            width = screenW * pixelW;
            height = screenH * pixelH;
            this.windowSize = new VI2D(width, height);
            this.windowOffset = new VI2D(0, 0);
        }
        window = GLFW.glfwCreateWindow(width, height, "", monitor, NULL);
        GLFW.glfwMakeContextCurrent(window);
        GL.createCapabilities();
        
        keyCallbacks = new LinkedList<>();
        mouseMoveCallbacks = new LinkedList<>();
        mouseClickCallbacks = new LinkedList<>();
        scrollCallbacks = new LinkedList<>();
        
        GLFW.glfwSetKeyCallback(window, keyCallback = GLFWKeyCallback.create((window, key, scancode, action, mods) -> {
            Key k = keyMap.getOrDefault(key, Key.NONE);
            ClickType type;
            switch(action) {
                case GLFW.GLFW_PRESS:
                    type = ClickType.PRESSED;
                    break;
                case GLFW.GLFW_RELEASE:
                    type = ClickType.RELEASED;
                    break;
                default:
                    return;
            }
            for (KeyCallback callback : keyCallbacks)
                callback.call(k, type);
        }));
        
        GLFW.glfwSetMouseButtonCallback(window, mouseButtonCallback = GLFWMouseButtonCallback.create((window, button, action, mods) -> {
            ClickType type;
            switch (action) {
                case GLFW.GLFW_PRESS:
                    type = ClickType.PRESSED;
                    break;
                case GLFW.GLFW_RELEASE:
                    type = ClickType.RELEASED;
                    break;
                default:
                    return;
            }
            for (MouseClickCallback callback : mouseClickCallbacks)
                callback.call(button, type);
        }));
        
        GLFW.glfwSetCursorPosCallback(window, cursorPosCallback = GLFWCursorPosCallback.create((window, xPos, yPos) -> {
            int x = (int)((xPos - windowOffset.x) / windowSize.x * screenW);
            int y = (int)((yPos - windowOffset.y) / windowSize.y * screenH);
            for (MouseMoveCallback callback : mouseMoveCallbacks)
                callback.call(x, y);
        }));
        
        GLFW.glfwSetScrollCallback(window, scrollCallback = GLFWScrollCallback.create((window, xOffset, yOffset) -> {
            for (ScrollCallback callback : scrollCallbacks)
                callback.call((int)yOffset);
        }));
        
        GLFW.glfwSwapInterval(vSync ? 1 : 0);
        
        GL32.glBlendFunc(GL32.GL_SRC_ALPHA, GL32.GL_ONE_MINUS_SRC_ALPHA);
        GL32.glEnable(GL32.GL_BLEND);
        
        GL32.glEnable(GL32.GL_TEXTURE_2D);
        
        layers = new ArrayList<>();
        CreateLayer();
        
        GL32.glLoadIdentity();
        GL32.glOrtho(0, screenW, screenH, 0, -1, 1);
        
        CreateFont();
    }
    
    private void CreateFont() {
        fontSprite = new Sprite();
        
        ByteBuffer image = MemoryUtil.memAlloc(128 * 48 * 4);
        
        char[] data = ("?Q`0001oOch0o01o@F40o0<AGD4090LAGD<090@A7ch0?00O7Q`0600>00000000" +
                "O000000nOT0063Qo4d8>?7a14Gno94AA4gno94AaOT0>o3`oO400o7QN00000400" +
                "Of80001oOg<7O7moBGT7O7lABET024@aBEd714AiOdl717a_=TH013Q>00000000" +
                "720D000V?V5oB3Q_HdUoE7a9@DdDE4A9@DmoE4A;Hg]oM4Aj8S4D84@`00000000" +
                "OaPT1000Oa`^13P1@AI[?g`1@A=[OdAoHgljA4Ao?WlBA7l1710007l100000000" +
                "ObM6000oOfMV?3QoBDD`O7a0BDDH@5A0BDD<@5A0BGeVO5ao@CQR?5Po00000000" +
                "Oc``000?Ogij70PO2D]??0Ph2DUM@7i`2DTg@7lh2GUj?0TO0C1870T?00000000" +
                "70<4001o?P<7?1QoHg43O;`h@GT0@:@LB@d0>:@hN@L0@?aoN@<0O7ao0000?000" +
                "OcH0001SOglLA7mg24TnK7ln24US>0PL24U140PnOgl0>7QgOcH0K71S0000A000" +
                "00H00000@Dm1S007@DUSg00?OdTnH7YhOfTL<7Yh@Cl0700?@Ah0300700000000" +
                "<008001QL00ZA41a@6HnI<1i@FHLM81M@@0LG81?O`0nC?Y7?`0ZA7Y300080000" +
                "O`082000Oh0827mo6>Hn?Wmo?6HnMb11MP08@C11H`08@FP0@@0004@000000000" +
                "00P00001Oab00003OcKP0006@6=PMgl<@440MglH@000000`@000001P00000000" +
                "Ob@8@@00Ob@8@Ga13R@8Mga172@8?PAo3R@827QoOb@820@0O`0007`0000007P0" +
                "O`000P08Od400g`<3V=P0G`673IP0`@3>1`00P@6O`P00g`<O`000GP800000000" +
                "?P9PL020O`<`N3R0@E4HC7b0@ET<ATB0@@l6C4B0O`H3N7b0?P01L3R000000020").toCharArray();
        
        int px = 0;
        int py = 0;
        
        for (int b = 0; b < 1024; b += 4) {
            int sym1 = (int)data[b] - 48;
            int sym2 = (int)data[b + 1] - 48;
            int sym3 = (int)data[b + 2] - 48;
            int sym4 = (int)data[b + 3] - 48;
            
            int r = sym1 << 18 | sym2 << 12 | sym3 << 6 | sym4;
            for (int i = 0; i < 24; i++) {
                byte k;
                
                if ((r & (1 << i)) == 0) {
                    k = (byte)0;
                } else {
                    k = (byte)255;
                }
                
                image.put((py * 128 + px) * 4, k);
                image.put((py * 128 + px) * 4 + 1, k);
                image.put((py * 128 + px) * 4 + 2, k);
                image.put((py * 128 + px) * 4 + 3, k);
                
                if (++py == 48) {px++; py = 0;}
            }
        }
        
        GL32.glBindTexture(GL32.GL_TEXTURE_2D, fontSprite.getId());
        GL32.glTexImage2D(GL32.GL_TEXTURE_2D, 0, GL32.GL_RGBA8, 128, 48, 0, GL32.GL_RGBA, GL32.GL_UNSIGNED_BYTE, image);
        GL32.glTexParameterf(GL32.GL_TEXTURE_2D, GL32.GL_TEXTURE_MIN_FILTER, GL32.GL_NEAREST);
        GL32.glTexParameterf(GL32.GL_TEXTURE_2D, GL32.GL_TEXTURE_MAG_FILTER, GL32.GL_NEAREST);
        GL32.glBindTexture(GL32.GL_TEXTURE_2D, 0);
        
        MemoryUtil.memFree(image);
        
        fontDecal = new Decal(fontSprite);
    }
    
    @Override
    public void Destroy() {
        layers.forEach(layer -> {
            GL32.glDeleteFramebuffers(layer.frambufferID);
            GL32.glDeleteTextures(layer.texID);
            layer.frambufferID = 0;
            layer.texID = 0;
        });
        
        keyCallback.free();
        mouseButtonCallback.free();
        cursorPosCallback.free();
        scrollCallback.free();
        
        GLFW.glfwDestroyWindow(window);
        
        GLFW.glfwTerminate();
        errorCallback.free();
    }
    
    @Override
    public boolean ShouldClose() {
        return GLFW.glfwWindowShouldClose(window);
    }
    
    @Override
    public void CloseHint(boolean close) {
        GLFW.glfwSetWindowShouldClose(window, close);
    }
    
    @Override
    public void OnPreUpdate() {
        GL32.glBindFramebuffer(GL32.GL_FRAMEBUFFER, layer.frambufferID);
        GL32.glViewport(0, 0, screenSize.x, screenSize.y);
    }
    
    @Override
    public void OnPostUpdate() {
        GL32.glBindFramebuffer(GL32.GL_FRAMEBUFFER, 0);
        GL32.glViewport(windowOffset.x, windowOffset.y, windowSize.x, windowSize.y);
        
        GL32.glClearColor(0, 0, 0, 1);
        GL32.glClear(GL32.GL_COLOR_BUFFER_BIT);
        
        for (LayerDesc layer : new ReverseListIterator<>(layers)) {
            if (layer.bShow) {
                Pixel tint = layer.tint;
                VF2D scale = layer.scale;
                VF2D offset = layer.offset;
                GL32.glBindTexture(GL32.GL_TEXTURE_2D, layer.texID);
                GL32.glBegin(GL32.GL_QUADS);
                GL32.glColor4ub(tint.r, tint.g, tint.b, tint.a);
                GL32.glTexCoord2f(0.f * scale.x + offset.x, 0.f * scale.y + offset.y);
                GL32.glVertex2i(0, screenSize.y);
                GL32.glTexCoord2f(1.f * scale.x + offset.x, 0.f * scale.y + offset.y);
                GL32.glVertex2i(screenSize.x, screenSize.y);
                GL32.glTexCoord2f(1.f * scale.x + offset.x, 1.f * scale.y + offset.y);
                GL32.glVertex2i(screenSize.x, 0);
                GL32.glTexCoord2f(0.f * scale.x + offset.x, 1.f * scale.y + offset.y);
                GL32.glVertex2i(0, 0);
                GL32.glEnd();
                
                for (DecalInstance decal : layer.decals) {
                    if (decal.decal == null) {
                        GL32.glBindTexture(GL32.GL_TEXTURE_2D, 0);
                        GL32.glBegin(GL32.GL_QUADS);
                        GL32.glColor4ub(decal.tint[0].r, decal.tint[0].g, decal.tint[0].b, decal.tint[0].a);
                        GL32.glTexCoord4f(decal.uv[0].x, decal.uv[0].y, 0, decal.w[0]);
                        GL32.glVertex2f(decal.pos[0].x, decal.pos[0].y);
                        GL32.glColor4ub(decal.tint[1].r, decal.tint[1].g, decal.tint[1].b, decal.tint[1].a);
                        GL32.glTexCoord4f(decal.uv[1].x, decal.uv[1].y, 0, decal.w[1]);
                        GL32.glVertex2f(decal.pos[1].x, decal.pos[1].y);
                        GL32.glColor4ub(decal.tint[2].r, decal.tint[2].g, decal.tint[2].b, decal.tint[2].a);
                        GL32.glTexCoord4f(decal.uv[2].x, decal.uv[2].y, 0, decal.w[2]);
                        GL32.glVertex2f(decal.pos[2].x, decal.pos[2].y);
                        GL32.glColor4ub(decal.tint[3].r, decal.tint[0].g, decal.tint[3].b, decal.tint[3].a);
                    } else {
                        GL32.glBindTexture(GL32.GL_TEXTURE_2D, decal.decal.getId());
                        GL32.glBegin(GL32.GL_QUADS);
                        GL32.glColor4ub(decal.tint[0].r, decal.tint[0].g, decal.tint[0].b, decal.tint[0].a);
                        GL32.glTexCoord4f(decal.uv[0].x, decal.uv[0].y, 0, decal.w[0]);
                        GL32.glVertex2f(decal.pos[0].x, decal.pos[0].y);
                        GL32.glTexCoord4f(decal.uv[1].x, decal.uv[1].y, 0, decal.w[1]);
                        GL32.glVertex2f(decal.pos[1].x, decal.pos[1].y);
                        GL32.glTexCoord4f(decal.uv[2].x, decal.uv[2].y, 0, decal.w[2]);
                        GL32.glVertex2f(decal.pos[2].x, decal.pos[2].y);
                    }
                    GL32.glTexCoord4f(decal.uv[3].x, decal.uv[3].y, 0, decal.w[3]);
                    GL32.glVertex2f(decal.pos[3].x, decal.pos[3].y);
                    GL32.glEnd();
                }
                layer.decals.clear();
            }
        }
        
        GL32.glBindTexture(GL32.GL_TEXTURE_2D, 0);
        
        GLFW.glfwSwapBuffers(window);
        GLFW.glfwPollEvents();
    }
    
    @Override
    public void SetTitle(String title) {
        GLFW.glfwSetWindowTitle(window, title);
    }
    
    @Override
    public int GetMouseButtonCount() {
        return GLFW.GLFW_MOUSE_BUTTON_LAST + 1;
    }
    
    @Override
    public void RegisterKeyCallBack(KeyCallback callback) {
        keyCallbacks.add(callback);
    }
    
    @Override
    public void RegisterMouseMoveCallback(MouseMoveCallback callback) {
        mouseMoveCallbacks.add(callback);
    }
    
    @Override
    public void RegisterMouseClickCallback(MouseClickCallback callback) {
        mouseClickCallbacks.add(callback);
    }
    
    @Override
    public void RegisterScrollCallback(ScrollCallback callback) {
        scrollCallbacks.add(callback);
    }
}
