#version 330 core

// uniform float uTime;
// uniform sampler2D TEX_SAMPLER;

in vec4 fColor;
// in vec2 fTexCoords;

out vec4 color;

void main()
{
    // color = texture(TEX_SAMPLER, fTexCoords);
    color = fColor;
}