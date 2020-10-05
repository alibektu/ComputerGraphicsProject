uniform mat4 u_Model;
uniform mat4 u_MVP;
uniform mat4 u_MVMatrix;
uniform vec3 u_LightPos;

attribute vec4 a_Position;
attribute vec2 a_Texture;

varying vec4 v_Color;
varying vec2 v_Tex;


void main() {
   v_Color = vec4(1,1,1,1);
   v_Tex = a_Texture;
   gl_Position = u_MVP * a_Position;
}
