package components;

import engine.Component;
import imgui.ImGui;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import imgui.type.ImString;
import render.Texture;
import util.AssetPool;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Animation extends Component {
    private boolean isPlaying = false;
    private List<Sprite> sprites;
    private float interval;
    private float timeToNext = 0;
    private int currentFrame;
    private boolean loop = false;
    private Sprite originalSprite;
    private String animationName;

    private boolean showFramesWindow = false;

    // Spritesheet UI
    private boolean showAddSpritesheet = false;
    private int ssSpriteW = 16;
    private int ssSpriteH = 16;
    private int ssCount = 10;
    private int ssSpacing = 0;

    private ImString imName = new ImString("New Animation", 64);
    private ImBoolean imLoop = new ImBoolean(false);

    private ImInt imSpriteW = new ImInt(ssSpriteW);
    private ImInt imSpriteH = new ImInt(ssSpriteH);
    private ImInt imCount   = new ImInt(ssCount);
    private ImInt imSpacing = new ImInt(ssSpacing);
    private Set<Integer> selectedFrames = new HashSet<>();

    private ImBoolean showFrames = new ImBoolean(false);

    private static final int GRID_COLS = 8;
    private static final float THUMB_SIZE = 48.0f;
    private static final float THUMB_PADDING = 6.0f;
    private int selectionAnchor = -1;



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

    public boolean isPlaying() {
        return isPlaying;
    }

    public void play(boolean loops) {
        originalSprite = gameObject.getComponent(SpriteRenderer.class).getSprite();
        this.isPlaying = true;
        this.timeToNext = 0;
        this.currentFrame = -1;
        this.loop = loops;
    }

    public void play() {
        originalSprite = gameObject.getComponent(SpriteRenderer.class).getSprite();
        this.isPlaying = true;
        this.timeToNext = 0;
        this.currentFrame = -1;
    }

    public void stop() {
        this.isPlaying = false;
        gameObject.getComponent(SpriteRenderer.class).setSprite(originalSprite);
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

        ImGui.popID();
    }

    private void drawFramesWindow() {
        ImGui.setNextWindowSize(600, 450, ImGuiCond.Once);
        if (!ImGui.begin("Animation Frames", showFrames)) {
            ImGui.end();
            return;
        }

        ImGui.text("Frames: " + sprites.size());
        ImGui.separator();

        if (sprites.isEmpty()) {
            ImGui.textDisabled("No frames yet");
        } else {

        float cellSize = THUMB_SIZE + THUMB_PADDING;
        int col = 0;

        for (int i = 0; i < sprites.size(); i++) {
            ImGui.pushID(i);

            Sprite sprite = sprites.get(i);
            int texId = sprite.getTexture().getId();

            // === IMAGE BUTTON ===
            boolean clicked = ImGui.imageButton(texId, THUMB_SIZE, THUMB_SIZE);

            boolean ctrl  = ImGui.getIO().getKeyCtrl();
            boolean shift = ImGui.getIO().getKeyShift();

            if (clicked) {

                // SHIFT RANGE SELECT
                if (shift && !selectedFrames.isEmpty()) {
                    int start = findNearestSelected(i);
                    if (start == -1) {
                        start = i;
                    }

                    clearSelection();
                    int from = Math.min(start, i);
                    int to   = Math.max(start, i);

                    for (int idx = from; idx <= to; idx++) {
                        selectedFrames.add(idx);
                    }

                    selectionAnchor = start;
                }

                // CTRL TOGGLE
                else if (ctrl) {
                    toggleSelection(i);
                    selectionAnchor = i;
                }

                // SINGLE SELECT
                else {
                    clearSelection();
                    selectedFrames.add(i);
                    selectionAnchor = i;
                }
            }


            if (isSelected(i)) {
                ImGui.getWindowDrawList().addRect(
                        ImGui.getItemRectMinX(),
                        ImGui.getItemRectMinY(),
                        ImGui.getItemRectMaxX(),
                        ImGui.getItemRectMaxY(),
                        0xFF4DA3FF, // modrá = selected
                        0.0f,
                        0,
                        3.0f
                );
            }


            if (i == currentFrame) {
                ImGui.getWindowDrawList().addRect(
                        ImGui.getItemRectMinX(),
                        ImGui.getItemRectMinY(),
                        ImGui.getItemRectMaxX(),
                        ImGui.getItemRectMaxY(),
                        0xFF00FF00
                );
            }

            if (ImGui.isItemHovered()) {
                ImGui.beginTooltip();
                ImGui.text("Frame " + i);
                ImGui.endTooltip();
            }



            // === DRAG SOURCE ===
            if (ImGui.beginDragDropSource()) {
                int[] payload;

                if (isSelected(i)) {
                    payload = selectedFrames.stream().mapToInt(Integer::intValue).toArray();
                } else {
                    payload = new int[]{i};
                }

                ImGui.setDragDropPayload("ANIM_FRAME_MULTI", payload);
                ImGui.text("Move " + payload.length + " frame(s)");
                ImGui.endDragDropSource();
            }


            // === DROP TARGET ===
            if (ImGui.beginDragDropTarget()) {
                int[] payload = ImGui.acceptDragDropPayload("ANIM_FRAME_MULTI");
                if (payload != null) {

                    // seřadit, ať nepadnou indexy
                    java.util.Arrays.sort(payload);

                    List<Sprite> moved = new ArrayList<>();
                    for (int idx = payload.length - 1; idx >= 0; idx--) {
                        moved.add(0, sprites.remove(payload[idx]));
                    }

                    int insertIndex = i;
                    sprites.addAll(insertIndex, moved);

                    clearSelection();
                    for (int k = 0; k < moved.size(); k++) {
                        selectedFrames.add(insertIndex + k);
                    }
                }
                ImGui.endDragDropTarget();
            }



            ImGui.popID();

            // === GRID FLOW ===
            col++;
            if (col < GRID_COLS) {
                ImGui.sameLine();
            } else {
                col = 0;
            }
        }
        }

        ImGui.separator();

        if (ImGui.button("Duplicate Selected") && !selectedFrames.isEmpty()) {
            List<Integer> sorted = selectedFrames.stream().sorted().toList();
            int offset = 0;
            for (int idx : sorted) {
                sprites.add(idx + 1 + offset, sprites.get(idx + offset).copy());
                offset++;
            }
            clearSelection();
            selectionAnchor = -1;
        }

        ImGui.sameLine();

        if (ImGui.button("Delete Selected") && !selectedFrames.isEmpty()) {
            selectedFrames.stream()
                    .sorted((a, b) -> b - a) // mazat odzadu
                    .forEach(idx -> sprites.remove((int) idx));
            clearSelection();
            selectionAnchor = -1;
        }

        if (ImGui.isMouseClicked(0) && ImGui.isWindowHovered()) {
            if (!ImGui.isAnyItemHovered()) {
                clearSelection();
                selectionAnchor = -1;
            }
        }



        ImGui.separator();

        drawDropArea();
        if (ImGui.button("Add Spritesheet")) { showAddSpritesheet = true; } if (showAddSpritesheet) { drawSpritesheetPopup(); }

        ImGui.end();
    }

    private void drawDropArea() {
        ImGui.text("Drop images here");
        ImGui.invisibleButton("##DROP", ImGui.getContentRegionAvailX(), 60);

        if (ImGui.beginDragDropTarget()) {
            Object payload = ImGui.acceptDragDropPayload("ASSET_FILE");
            if (payload != null) {
                String path = (String) payload;
                String filename = Paths.get(path).getFileName().toString();
                if (filename.endsWith(".png") || filename.endsWith(".jpg")) {
                    sprites.add(new Sprite(AssetPool.getTexture(filename)));
                }
            }
            ImGui.endDragDropTarget();
        }
    }



    private void drawSpritesheetPopup() {
        // místo openPopup/modal použij beginChild/okno bez modalu
        ImGui.setNextWindowSize(400, 250, ImGuiCond.Once);
        if (ImGui.begin("Add Spritesheet", ImGuiWindowFlags.NoCollapse)) {

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
                    String filename = Paths.get(path).getFileName().toString();
                    if (filename.endsWith(".png") || filename.endsWith(".jpg")) {
                        ssSpriteW = imSpriteW.get();
                        ssSpriteH = imSpriteH.get();
                        ssCount   = imCount.get();
                        ssSpacing = imSpacing.get();

                        Texture tex = AssetPool.getTexture(filename);
                        Spritesheet ss = new Spritesheet(tex, ssSpriteW, ssSpriteH, ssCount, ssSpacing);
                        addSpritesheet(ss);
                        showAddSpritesheet = false;
                    }
                }

                ImGui.endDragDropTarget();
            }

            if (ImGui.button("Cancel")) showAddSpritesheet = false;

            ImGui.end();
        }
    }

    private boolean isSelected(int index) {
        return selectedFrames.contains(index);
    }

    private void clearSelection() {
        selectedFrames.clear();
    }

    private void toggleSelection(int index) {
        if (selectedFrames.contains(index))
            selectedFrames.remove(index);
        else
            selectedFrames.add(index);
    }

    private int findNearestSelected(int index) {
        int nearest = -1;
        int minDist = Integer.MAX_VALUE;

        for (int sel : selectedFrames) {
            int dist = Math.abs(sel - index);
            if (dist < minDist) {
                minDist = dist;
                nearest = sel;
            }
        }
        return nearest;
    }


}
