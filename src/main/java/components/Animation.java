package components;

import engine.Component;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import imgui.type.ImString;
import render.Texture;
import util.AssetPool;

import java.util.ArrayList;
import java.util.List;

public class Animation extends Component {
    private boolean isPlaying = false;
    private List<Sprite> sprites;
    private float interval;
    private float timeToNext = 0;
    private int currentFrame = 0;
    private boolean loop = false;
    private Sprite originalSprite;
    private String animationName;

    private boolean showFramesWindow = false;

    // Spritesheet UI
    private boolean showAddSpritesheet = false;
    private int ssSpriteW = 32;
    private int ssSpriteH = 32;
    private int ssCount = 1;
    private int ssSpacing = 0;

    private ImString imName = new ImString("New Animation", 64);
    private ImBoolean imLoop = new ImBoolean(false);

    private ImInt imSpriteW = new ImInt(32);
    private ImInt imSpriteH = new ImInt(32);
    private ImInt imCount   = new ImInt(1);
    private ImInt imSpacing = new ImInt(0);

    private ImBoolean showFrames = new ImBoolean(false);


    public Animation() {
        sprites = new ArrayList<Sprite>();
        this.interval = 1.0f;
        this.animationName = "New Animation";
    }

    public String getAnimationName() {
        return animationName;
    }

    public void setAnimationName(String animationName) {
        this.animationName = animationName;
    }

    public void addSpritesheet(Spritesheet spritesheet) {
        sprites.addAll(spritesheet.getSprites());
    }

    public void addSprite(Sprite sprite) {
        sprites.add(sprite);
    }

    public List<Sprite> getSprites() {
        return sprites;
    }

    public void setSprites(List<Sprite> sprites) {
        this.sprites = sprites;
    }

    public void setInterval(float interval) {
        this.interval = interval;
    }

    public float getInterval() {
        return interval;
    }

    public boolean isLoop() {
        return loop;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public void play(boolean loops) {
        originalSprite = gameObject.getComponent(SpriteRenderer.class).getSprite();
        this.isPlaying = true;
        this.timeToNext = interval;
        this.currentFrame = 0;
        this.loop = loops;
    }

    public void play() {
        originalSprite = gameObject.getComponent(SpriteRenderer.class).getSprite();
        this.isPlaying = true;
        this.timeToNext = interval;
        this.currentFrame = 0;
    }

    public void stop() {
        this.isPlaying = false;
    }

    @Override
    public void update(float dt) {

        if (isPlaying) {
            timeToNext -= dt;
            if (timeToNext <= 0) {
                timeToNext = interval;
                currentFrame++;
                if (currentFrame >= sprites.size()) {
                    if (loop)
                        currentFrame = 0;
                    else {
                        isPlaying = false;
                        gameObject.getComponent(SpriteRenderer.class).setSprite(originalSprite);
                        return;
                    }
                }
                System.out.println("Setting texture: " + sprites.get(currentFrame).getTexture());
                gameObject.getComponent(SpriteRenderer.class).setSprite(sprites.get(currentFrame));
            }
        }


    }

    @Override
    public void imgui() {
        ImGui.pushID(this.toString());
        super.imgui();

        ImGui.text(animationName);

        // =====================
        // Name
        // =====================
        imName.set(animationName);
        if (ImGui.inputText("Name", imName)) {
            animationName = imName.get();
        }

        // =====================
        // Interval
        // =====================
        float[] intervalArr = { interval };
        if (ImGui.dragFloat("Interval", intervalArr, 0.01f, 0.01f, 10.0f)) {
            interval = intervalArr[0];
        }

        // =====================
        // Loop
        // =====================
        imLoop.set(loop);
        if (ImGui.checkbox("Loops", imLoop)) {
            loop = imLoop.get();
        }

        // =====================
        // Controls
        // =====================
        if (ImGui.button("Play")) play(loop);
        ImGui.sameLine();
        if (ImGui.button("Stop")) stop();
        ImGui.sameLine();
        if (ImGui.button("Frames")) showFrames.set(true);

        if (showFrames.get()) {
            drawFramesWindow();
        }
        System.out.println(sprites.size());

        ImGui.popID();
    }

    private void drawFramesWindow() {
        ImGui.setNextWindowSize(500, 400, ImGuiCond.Once);
        if (!ImGui.begin("Animation Frames", showFrames)) {
            ImGui.end();
            return;
        }

        ImGui.text("Frames (" + sprites.size() + ")");
        ImGui.separator();

        // =====================
        // Sprites list
        // =====================
        for (int i = 0; i < sprites.size(); i++) {
            ImGui.pushID(i);

            Sprite s = sprites.get(i);

            ImGui.text("Frame " + i);

            // Drag source
            if (ImGui.beginDragDropSource()) {
                ImGui.setDragDropPayload("ANIM_FRAME", new int[]{i});
                ImGui.text("Frame " + i);
                ImGui.endDragDropSource();
            }

            // Drop target
            if (ImGui.beginDragDropTarget()) {
                int[] payload = ImGui.acceptDragDropPayload("ANIM_FRAME");
                if (payload != null) {
                    int from = payload[0];
                    if (from != i) {
                        Sprite tmp = sprites.remove(from);
                        sprites.add(i, tmp);
                    }
                }
                ImGui.endDragDropTarget();
            }

            ImGui.sameLine();
            if (ImGui.button("Delete")) {
                sprites.remove(i);
                ImGui.popID();
                break;
            }

            ImGui.popID();
        }

        ImGui.separator();

        // =====================
        // Drop new sprites
        // =====================
        ImGui.text("Drop images here");

        ImGui.invisibleButton("##DROP", ImGui.getContentRegionAvailX(), 50);

        if (ImGui.beginDragDropTarget()) {
            Object payload = ImGui.acceptDragDropPayload("ASSET_FILE");
            if (payload != null) {
                String path = (String) payload;
                if (path.endsWith(".png") || path.endsWith(".jpg")) {
                    sprites.add(new Sprite(
                            AssetPool.getTexture(path)
                    ));
                }
            }
            ImGui.endDragDropTarget();
        }

        // =====================
        // Spritesheet
        // =====================
        if (ImGui.button("Add Spritesheet")) {
            showAddSpritesheet = true;
        }

        if (showAddSpritesheet) {
            drawSpritesheetPopup();
        }

        ImGui.end();
    }

    private void drawSpritesheetPopup() {
        ImGui.openPopup("Add Spritesheet");

        if (ImGui.beginPopupModal("Add Spritesheet")) {

            ImGui.inputInt("Sprite Width", imSpriteW);
            ImGui.inputInt("Sprite Height", imSpriteH);
            ImGui.inputInt("Count", imCount);
            ImGui.inputInt("Spacing", imSpacing);

            ImGui.text("Drop texture below");

            ImGui.invisibleButton("##SS_DROP", 300, 60);

            if (ImGui.beginDragDropTarget()) {
                Object payload = ImGui.acceptDragDropPayload("ASSET_FILE");
                if (payload != null) {
                    String path = (String) payload;
                    if (path.endsWith(".png")) {
                        Texture tex = AssetPool
                                .getTexture(path);

                        Spritesheet ss = new Spritesheet(
                                tex,
                                ssSpriteW,
                                ssSpriteH,
                                ssCount,
                                ssSpacing
                        );

                        sprites.addAll(ss.getSprites());
                        showAddSpritesheet = false;
                        ImGui.closeCurrentPopup();
                    }
                }
                ImGui.endDragDropTarget();
            }

            if (ImGui.button("Cancel")) {
                showAddSpritesheet = false;
                ImGui.closeCurrentPopup();
            }

            ImGui.endPopup();
        }
    }




}
