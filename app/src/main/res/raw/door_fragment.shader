precision mediump float;
varying vec4 v_Color;
varying vec2 v_Tex;

uniform float i;
uniform sampler2D texture;

void main() {

    float a;
    if ( i > 0.0f && i < 1.0f )
        a = i;
    else
      a = 1.0f;

        gl_FragColor = vec4(a, a, a, 1) * texture2D(texture, v_Tex);
}
