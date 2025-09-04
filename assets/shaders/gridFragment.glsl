#version 330 core

in vec2 vPos;

uniform vec2 uCameraPos;
uniform float uCellSize;
uniform vec4 uColor;

out vec4 fragColor;

void main()
{
    // pozice pixelu relativně k gridu
    vec2 worldPos = vPos + uCameraPos;

    // modulo pro grid
    vec2 grid = abs(fract(worldPos / uCellSize - 0.5) - 0.5) / fwidth(worldPos / uCellSize);
    float line = min(grid.x, grid.y);

    fragColor = uColor * (1.0 - min(line, 1.0));
}
