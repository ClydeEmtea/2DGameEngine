#version 330 core

in vec4 fColor;
in vec2 fTexCoords;
in float fTexID;
in float fRoundness;

uniform sampler2D uTextures[8];

out vec4 color;

vec4 sampleTexture(int id, vec2 uv)
{
    // GLSL 330: sampler array MUSÍ mít konstantní index
    if (id == 0) return texture(uTextures[0], uv);
    else if (id == 1) return texture(uTextures[1], uv);
    else if (id == 2) return texture(uTextures[2], uv);
    else if (id == 3) return texture(uTextures[3], uv);
    else if (id == 4) return texture(uTextures[4], uv);
    else if (id == 5) return texture(uTextures[5], uv);
    else if (id == 6) return texture(uTextures[6], uv);
    else if (id == 7) return texture(uTextures[7], uv);

    return vec4(1.0); // fallback
}

void main()
{
    vec4 baseColor;

    if (fTexID > 0.0) {
        int id = int(fTexID);

        // Clamp UVs na [0,1]
        vec2 uv = clamp(fTexCoords, 0.0, 1.0);

        // Texel-center correction (NEAREST safe)
        vec2 texSize;
        if (id == 0) texSize = textureSize(uTextures[0], 0);
        else if (id == 1) texSize = textureSize(uTextures[1], 0);
        else if (id == 2) texSize = textureSize(uTextures[2], 0);
        else if (id == 3) texSize = textureSize(uTextures[3], 0);
        else if (id == 4) texSize = textureSize(uTextures[4], 0);
        else if (id == 5) texSize = textureSize(uTextures[5], 0);
        else if (id == 6) texSize = textureSize(uTextures[6], 0);
        else if (id == 7) texSize = textureSize(uTextures[7], 0);
        else texSize = vec2(1.0);

        uv = uv * (texSize - 1.0) / texSize + 0.5 / texSize;

        baseColor = fColor * sampleTexture(id, uv);
    } else {
        baseColor = fColor;
    }

    // ---- Rounded rectangle / circle ----
    float r = clamp(fRoundness, 0.0, 0.5);
    vec2 uvCentered = fTexCoords - vec2(0.5);
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
