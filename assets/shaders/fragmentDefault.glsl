#version 330 core

// uniform float uTime;
// uniform sampler2D TEX_SAMPLER;

in vec4 fColor;
in vec2 fTexCoords;
in float fTexID;

uniform sampler2D uTextures[8];

out vec4 color;

void main()
{
    // color = texture(TEX_SAMPLER, fTexCoords);
    if (fTexID > 0) {
        int id = int(fTexID);
        color = fColor * texture(uTextures[int(id)], fTexCoords);
        // color = vec4(fTexCoords, 0.0, 1.0);

    } else {
        color = fColor;
    }
}