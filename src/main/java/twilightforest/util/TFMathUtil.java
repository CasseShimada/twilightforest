package twilightforest.util;

import java.util.Arrays;

public final class TFMathUtil {
	private TFMathUtil() {
	}

	public static double interpolateToTarget(double oldValue, double targetValue, double deltaTicks, double timeConstant) {
		return targetValue - (targetValue - oldValue) * Math.exp(-deltaTicks / timeConstant);
	}

	public static double probabilityOfAtLeastOneSuccess(double successProbability, double tries) {
		return 1 - Math.pow(1 - successProbability, tries);
	}

	public static int taxicabGeometryDistance(int... coordinateDiffs) {
		return Arrays.stream(coordinateDiffs).map(Math::abs).sum();
	}

	public static int chebyshevGeometryDistance(int... coordinateDiffs) {
		return Arrays.stream(coordinateDiffs).map(Math::abs).max().orElse(0);
	}
}
