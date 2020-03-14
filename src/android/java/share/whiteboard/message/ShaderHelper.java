package com.xylink.sdk.sample.share.whiteboard.message;

import android.opengl.GLES20;

public class ShaderHelper {

	// Program variables
	public static int sp_color;
	
	/* SHADER Solid
	 * 
	 * This shader is for rendering a colored primitive.
	 * 
	 */
	public static final String vs_vertext =
		"attribute 	vec4 		vPosition;" +
		"uniform 	mat4 		uMVPMatrix;" +
		"attribute 	vec4 		a_vColor;" +
		"varying vec4 v_Color;" +
	    "void main() {" +
	    "  gl_Position = uMVPMatrix * vPosition;" +
	    "  v_Color = a_vColor;" +
	    "}";
	
	public static final String fs_color =
		"precision mediump float;" +
		"varying vec4 v_Color;" +
	    "void main() {" +
	    "  gl_FragColor = v_Color;" +
	    "}"; 
	
	
	public static int loadShader(int type, String shaderCode){

	    // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
	    // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
	    int shader = GLES20.glCreateShader(type);

	    // add the source code to the shader and compile it
	    GLES20.glShaderSource(shader, shaderCode);
	    GLES20.glCompileShader(shader);
	    
	    // return the shader
	    return shader;
	}
}
