precision mediump float;
uniform sampler2D y_TextureUnit;
uniform sampler2D uv_TextureUnit;

varying vec2 v_TexCoord;
void main() {
    float r, g, b, y, u, v;
    y = texture2D(y_TextureUnit, v_TexCoord).r;
    vec4 uvData = texture2D(uv_TextureUnit, v_TexCoord);
    u = uvData.r - 0.5;
    v = uvData.a - 0.5;
    r = y + 1.13983*v;
    g = y - 0.39465*u - 0.58060*v;
    b = y + 2.03211*u;
    gl_FragColor = vec4(r, g, b, 1.0);
}