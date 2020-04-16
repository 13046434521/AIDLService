precision mediump float;
uniform sampler2D y_TextureUnit;
uniform sampler2D uv_TextureUnit;

varying vec2 v_TexCoord;
void main() {
    //        vec4 c = vec4((texture2D(y_TextureUnit, v_TexCoord).r - 16.0/255.0) * 1.164);
    //        vec4 U = vec4(texture2D(u_TextureUnit, v_TexCoord).r - 128.0/255.0);
    //        vec4 V = vec4(texture2D(v_TextureUnit, v_TexCoord).r - 128.0/255.0);
    //        c += V * vec4(1.596, -0.813, 0, 0);
    //        c += U * vec4(0, -0.392, 2.017, 0);
    //        c.a = 1.0;
    //        gl_FragColor = c;

    //        vec3 yuv;
    //        yuv.x = texture2D(y_TextureUnit, v_TexCoord).r;
    //        yuv.y = texture2D(u_TextureUnit, v_TexCoord).r - 0.5;
    //        yuv.z = texture2D(v_TextureUnit, v_TexCoord).r - 0.5;
    //
    //        mat3 trans = mat3(1, 1 ,1,
    //                          0, -0.34414, 1.772,
    //                          1.402, -0.71414, 0
    //                          );
    //
    //        gl_FragColor = vec4(trans*yuv, 1.0);

    //    vec3 yuv;
    //    vec3 rgb;
    //    yuv.x = texture2D(y_TextureUnit, v_TexCoord).r -16./256.;
    //    yuv.y = texture2D(u_TextureUnit, v_TexCoord).r - 128./256.;
    //    yuv.z = texture2D(v_TextureUnit, v_TexCoord).g - 128./256.;
    //    rgb = mat3(1, 1, 1,
    //    0, -0.39465, 2.03211,
    //    1.13983, -0.58060, 0) * yuv;
    //    gl_FragColor = vec4(rgb, 1);

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