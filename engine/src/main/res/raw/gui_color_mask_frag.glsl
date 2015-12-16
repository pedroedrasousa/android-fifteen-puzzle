precision mediump float;

uniform vec3		uColor;
uniform vec3		pickedColor;
uniform sampler2D	uTexture;
varying vec2		vTexCoords;

void main()
{
	float alpha = texture2D(uTexture, vTexCoords).w;

	if (alpha > 0.0)
		gl_FragColor = vec4(uColor, 1.0);
	else
		gl_FragColor = vec4(0.0);
}
