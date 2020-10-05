precision mediump float;
varying vec4 v_Color;
varying vec3 v_Grid;

uniform float i;

void main() {
    float depth = gl_FragCoord.z / gl_FragCoord.w; // Calculate world-space distance.

    vec4 temp;

   float a;
    if ( i > 0.0f && i < 1.0f )
        a = i;
    else
      a = 1.0f;

    if ((mod(abs(v_Grid.x), 10.0) < 0.1) || (mod(abs(v_Grid.z), 10.0) < 0.1)) {
        temp = max(0.0, (90.0-depth) / 90.0) * vec4(0.0, a, 0.0, 1.0)
                + min(1.0, depth / 90.0) * v_Color;
    } else {
        temp = v_Color;
    }

    gl_FragColor = temp;
}
