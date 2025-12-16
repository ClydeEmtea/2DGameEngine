#version 330 core

in vec4 fColor;
in vec2 fTexCoords;
in float fTexID;
in float fRoundness;

uniform sampler2D uTextures[8];

out vec4 color;

void main()
{
    // ---- Base color / texture ----
    vec4 baseColor;
    if (fTexID > 0.0) {
        baseColor = fColor * texture(uTextures[int(fTexID)], fTexCoords);
    } else {
        baseColor = fColor;
    }

    // ---- Rounded rectangle / circle ----
    float r = clamp(fRoundness, 0.0, 0.5);
    vec2 uv = fTexCoords - vec2(0.5); // center UV
    float alpha = 1.0;

    if (r > 0.0) {
        // Standard SDF for rounded rectangle
        vec2 q = abs(uv) - vec2(0.5 - r);
        float dist = length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - r;

        // smoothstep pro plynulé okraje
        alpha = smoothstep(0.0, 0.01, -dist);

        // discard úplně průhledné pixely
        if (alpha <= 0.0) discard;
    }

    baseColor.a *= alpha;
    color = baseColor;
}
