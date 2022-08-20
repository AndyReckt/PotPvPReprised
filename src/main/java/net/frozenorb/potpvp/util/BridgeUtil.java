package net.frozenorb.potpvp.util;

public class BridgeUtil {

	public static String barBuilder(int wins, String color) {

		if (wins == 1) {
			return color + "⬢&f❤❤";
		} else if (wins == 2) {
			return color + "⬢⬢&f❤";
		} else if (wins == 3) {
			return color + "⬢⬢⬢";
		}

		return "&f⬢⬢⬢";
	}
}
