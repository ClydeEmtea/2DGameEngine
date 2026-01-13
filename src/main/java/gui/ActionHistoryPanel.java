package gui;

import actions.ActionManager;
import actions.EditorAction;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;

public class ActionHistoryPanel {

    public static void render(ActionManager actionManager) {

        ImGui.text("Action History");
        ImGui.separator();

        // Scrollovací oblast (300px výška)
        ImGui.beginChild(
                "ActionHistoryScroll",
                0,
                300,
                true,
                ImGuiWindowFlags.HorizontalScrollbar
        );

        // ---------- UNDO STACK ----------
        ImGui.pushStyleColor(ImGuiCol.Text, 0.7f, 0.9f, 1.0f, 1.0f);
        ImGui.text("Undo");
        ImGui.popStyleColor();
        ImGui.separator();

        for (EditorAction action : actionManager.getUndoStack()) {
            ImGui.text(action.getNameWithDetails());
        }

        ImGui.spacing();
        ImGui.separator();
        ImGui.spacing();

        // ---------- REDO STACK ----------
        ImGui.pushStyleColor(ImGuiCol.Text, 1.0f, 0.8f, 0.6f, 1.0f);
        ImGui.text("Redo");
        ImGui.popStyleColor();
        ImGui.separator();

        for (EditorAction action : actionManager.getRedoStack()) {
            ImGui.text(action.getNameWithDetails());
        }

        ImGui.endChild();

        ImGui.spacing();

        // ---------- CONTROLS ----------
        if (ImGui.button("Undo") && actionManager.canUndo()) {
            actionManager.undo();
        }

        ImGui.sameLine();

        if (ImGui.button("Redo") && actionManager.canRedo()) {
            actionManager.redo();
        }
    }
}
