package org.anarres.cpp;

/**
 * System version metadata for Anarres Java C Preprocessor ${version}.
 *
 * This class contains a main() and may be run to print the version.
 */
public class Version {

	/* Don't instantiate me */
	private Version() {
	}

	private static final String	VERSION = "${version}";

	private static final int	major;
	private static final int	minor;
	private static final int	patch;

	static {
		String[]	tmp = VERSION.split("\\.");
		major = Integer.parseInt(tmp[0]);
		minor = Integer.parseInt(tmp[1]);
		patch = Integer.parseInt(tmp[2]);
	}

	public static String getVersion() {
		return VERSION;
	}

	public static int getMajor() {
		return major;
	}

	public static int getMinor() {
		return minor;
	}

	public static int getPatch() {
		return patch;
	}

	public static void main(String[] args) {
		System.out.println("Version " + VERSION);
		System.out.println("getVersion() returns " + getVersion());
		System.out.println("getMajor() returns " + getMajor());
		System.out.println("getMinor() returns " + getMinor());
		System.out.println("getPatch() returns " + getPatch());
	}

}
