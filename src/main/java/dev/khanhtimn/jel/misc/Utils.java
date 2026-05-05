package dev.khanhtimn.jel.misc;

import java.util.List;

public final class Utils {

	/**
	 * Generates a list of floats by linearly interpolating between keyframe points.
	 * @param levels 0-indexed levels for each keyframe
	 * @param values corresponding values at each keyframe
	 * @param count size of the generated array
	 * @return list of floats for LevelBasedValue.lookup
	 */
	public static List<Float> interpolate(int[] levels, float[] values, int count) {
		Float[] result = new Float[count];
		for (int i = 0; i < count; i++) {
			int segStart = 0;
			for (int k = 1; k < levels.length; k++) {
				if (levels[k] >= i) {
					segStart = k - 1;
					break;
				}
			}
			int l0 = levels[segStart];
			int l1 = levels[segStart + 1];
			float v0 = values[segStart];
			float v1 = values[segStart + 1];
			if (l1 == l0) {
				result[i] = v1;
			} else {
				float t = (float) (i - l0) / (l1 - l0);
				result[i] = v0 + t * (v1 - v0);
			}
		}
		return List.of(result);
	}

	private Utils() {
	}
}
