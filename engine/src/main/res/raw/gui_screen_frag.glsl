precision mediump float;

uniform vec3		pickedColor;
uniform sampler2D	uTexture;
uniform sampler2D	uTexture2;
uniform sampler2D	uTextureMask;
varying vec2		vTexCoords;

void main()
{
	vec4 mask = texture2D(uTextureMask, vTexCoords);

	if (mask.xyz == pickedColor.xyz)
		gl_FragColor = texture2D(uTexture2, vTexCoords);
	else
		gl_FragColor = texture2D(uTexture, vTexCoords);
}
