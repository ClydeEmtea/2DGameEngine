#version 330 core

// uniform float uTime;
// uniform sampler2D TEX_SAMPLER;

in vec4 fColor;
in vec2 fTexCoords;
in float fTexID;
in float fRoundness;

uniform sampler2D uTextures[8];

out vec4 color;

void main()
{
    // ---- base color/texture ----
    vec4 baseColor;

    if (fTexID > 0) {
        baseColor = fColor * texture(uTextures[int(fTexID)], fTexCoords);
    } else {
        baseColor = fColor;
    }

    // ---- ROUNDING ----
    float r = clamp(fRoundness, 0.0, 0.5);

    if (r > 0.0) {

        vec2 uv = fTexCoords;

        // posuneme UV do rozsahu -0.5 .. 0.5
        vec2 p = uv - vec2(0.5);

        // velikost obdélníku
        vec2 halfSize = vec2(0.5) - r;

        // vzdálenost od zaobleného obdélníku (SDF)
        vec2 d = abs(p) - halfSize;
        float dist = length(max(d, 0.0)) - r;

        float alpha = smoothstep(0.01, 0.0, dist);
        baseColor.a *= alpha;

        if (baseColor.a <= 0.0)
        discard;
    }


    color = baseColor;
}