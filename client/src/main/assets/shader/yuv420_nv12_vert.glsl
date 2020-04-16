attribute vec4 a_Position;
attribute vec2 a_TexCoord;
uniform mat4 a_MvpMatrix;
varying vec2 v_TexCoord;
void main() {
    gl_Position = a_MvpMatrix * a_Position;
    v_TexCoord = a_TexCoord;
}
