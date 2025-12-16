#version 330 core

in vec4 fColor;
in vec2 fTexCoords;
in float fTexID;
in float fRoundness;

uniform sampler2D uTextures[8];

out vec4 color;

void main()
{
    vec4 baseColor;

    if (fTexID > 0.0) {
        // Clamp UVs na [0,1] a posun na střed texelu
        vec2 uv = clamp(fTexCoords, 0.0, 1.0);

        // Pokud je texture NEAREST, můžeme přidat poloviční offset texelu:
         vec2 texSize = textureSize(uTextures[int(fTexID)], 0);
         uv = uv * (texSize - 1.0) / texSize + 0.5 / texSize;

        baseColor = fColor * texture(uTextures[int(fTexID)], uv);
    } else {
        baseColor = fColor;
    }

    // ---- Rounded rectangle / circle ----
    float r = clamp(fRoundness, 0.0, 0.5);
    vec2 uvCentered = fTexCoords - vec2(0.5); // center UV
    float alpha = 1.0;

    if (r > 0.0) {
        vec2 q = abs(uvCentered) - vec2(0.5 - r);
        float dist = length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - r;

        alpha = smoothstep(0.0, 0.01, -dist);

        if (alpha <= 0.0) discard;
    }

    baseColor.a *= alpha;
    color = baseColor;
}
