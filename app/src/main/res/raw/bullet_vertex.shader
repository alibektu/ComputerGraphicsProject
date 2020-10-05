uniform mat4 u_Model;
uniform mat4 u_MVP;
uniform mat4 u_MVMatrix;


attribute vec4 a_Position;


varying vec4 v_Color;



void main() {
   v_Color = vec4(1,1,1,1);
   
   gl_Position = u_MVP * a_Position;
}
