package com.pedroedrasousa.engine;

import com.pedroedrasousa.engine.math.Vec3;

import java.util.Vector;

/**
 * Factory used to generate unique colors for unequivocally identify objects in an auxiliary buffer.
 * The color black will never be returned as it is used as background color.
 *
 * @author Pedro Edra Sousa
 */
public class UniqueColorFactory {

    /**
     * The delta between colors. Must be factor of 1.0f.
     */
	public static final float COLOR_DELTA = 0.2f;

    /**
     * Allowed error delta when comparing color values.
     */
    public static final float COLOR_ERROR_DELTA	= COLOR_DELTA * 0.3f;

    /**
     * The next color to be returned.
     */
	private static int nextColor;

    /**
     * Lookup table containing all the available colors.
     */
	private static Vector<Vec3> colorLookupTable;

    /**
     * Initialise the color lookup table.
     */
	static {
		resetColor();
		colorLookupTable = new Vector<>();
		
		// Build color lookup table.
		for (float b = 0.0f; b <= 1.0f; b += COLOR_DELTA)
			for (float g = 0.0f; g <= 1.0f; g += COLOR_DELTA)
				for (float r = 0.0f; r <= 1.0f; r += COLOR_DELTA)
					colorLookupTable.add(new Vec3(r, g, b));
	}

    /**
     * Builds a new unique color.
     *
     * @return A {@link Vec3} representing the color as RGB values.
     */
	public static Vec3 buildUniqueColor() {
		return colorLookupTable.get(nextColor++);
	}

    /**
     * Resets the factory. Colors will reused.
     */
	public static void resetColor() {
		nextColor = 1;
	}
}
