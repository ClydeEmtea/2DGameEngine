package gui;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.ImVec4;

public final class ImGuiUtils {

    private ImGuiUtils() {}

    public static boolean coloredButton(
            String label,
            float r, float g, float b, float a,
            float hr, float hg, float hb, float ha,
            float ar, float ag, float ab, float aa
    ) {
        ImGui.pushStyleColor(ImGuiCol.Button, r, g, b, a);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, hr, hg, hb, ha);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, ar, ag, ab, aa);

        boolean pressed = ImGui.button(label);

        ImGui.popStyleColor(3);
        return pressed;
    }

    public static boolean coloredButton(
            String label,
            ImVec4 normal,
            ImVec4 hovered,
            ImVec4 active
    ) {
        ImGui.pushStyleColor(ImGuiCol.Button, normal.x , normal.y, normal.z, normal.w);
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, hovered.x , hovered.y, hovered.z, hovered.w);
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, active.x , active.y, active.z, active.w);

        boolean pressed = ImGui.button(label);

        ImGui.popStyleColor(3);
        return pressed;
    }


    public static boolean redButton(String label) {
        return coloredButton(
                label,
                0.65f, 0.20f, 0.20f, 1.0f,   // normal
                0.75f, 0.30f, 0.30f, 1.0f,   // hover
                0.85f, 0.15f, 0.20f, 1.0f    // active
        );
    }

    public static boolean lightBlueButton(String label) {
        return coloredButton(
                label,
                0.25f, 0.55f, 0.85f, 1.0f,   // normal
                0.35f, 0.65f, 0.95f, 1.0f,   // hover
                0.20f, 0.50f, 0.80f, 1.0f    // active
        );
    }


    public static boolean disabledButton(String label) {
        ImGui.beginDisabled();
        boolean pressed = ImGui.button(label);
        ImGui.endDisabled();
        return pressed;
    }
}
