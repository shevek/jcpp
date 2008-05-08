/*
 * Anarres C Preprocessor
 * Copyright (C) 2007 Shevek
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

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
