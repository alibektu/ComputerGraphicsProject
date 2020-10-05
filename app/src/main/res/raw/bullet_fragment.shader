precision mediump float;
varying vec4 v_Color;

uniform float i;

void main() {

    float depth = gl_FragCoord.z / gl_FragCoord.w;

    float a;
    if ( i > 0.0f && i < 1.0f )
        a = i;
    else
      a = 1.0f;

    //temp = max(0.0, (90.0-depth) / 90.0) * vec4(0.0, a, 0.0, 1.0) + min(1.0, depth / 90.0) * v_Color;

    gl_FragColor = max(0.0, (90.0-depth) / 90.0) * vec4(a, a, a, 1.0) + min(1.0, depth / 90.0) * vec4(0, 0, 0, 1.0);

    //gl_FragColor = vec4(a, a, a, 1.0);
 }
