precision mediump float;

#define BRIGHTNESS	1.2

uniform float		uAlphaFactor;
uniform vec4		uColor;
uniform sampler2D	uTexture;
varying vec2		vTexCoords;

uniform vec2		uTexCoordsOffset;

void main()
{
	vec4 base = texture2D(uTexture, vTexCoords + uTexCoordsOffset);
	vec4 result;

	//uColor = uColor * 2.0;

	if (uColor.x == -1.0) {
		result = base;
	} else {
		// Color Overlay

		vec4 blend = uColor;

		vec4 white = vec4(1.0, 1.0, 1.0, 1.0);

		vec4 lumCoeff = vec4(0.2125, 0.7154, 0.0721, 1.0);

		float luminance = dot(base, lumCoeff);

		if (luminance < 0.45) {
		    result = 2.0 * blend * base;
		}
		else if (luminance > 0.55) {
		    result = white - 2.0 * (white - blend) * (white - base);
		}
		else {
		    vec4 result1 = vec4(2.0) * blend * base;
		    vec4 result2 = white - 2.0 * (white - blend) * (white - base);
		    result = mix(result1, result2, (luminance - 0.45) * 10.0);
		}
	}

	result.w = result.w * uAlphaFactor;

	gl_FragColor = result;
}
