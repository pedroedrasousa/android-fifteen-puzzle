uniform mat4	uMVPMatrix;
uniform mat4	uMVMatrix;

attribute vec4	aVertPos;
attribute vec2	aTexCoords;
varying vec2	vTexCoords;

void main()
{                       
	vTexCoords = aTexCoords;
	gl_Position = uMVPMatrix * uMVMatrix * aVertPos;
}
