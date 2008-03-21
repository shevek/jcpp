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

/* pp */ class State {
	boolean	parent;
	boolean	active;
	boolean	sawElse;

	/* pp */ State() {
		this.parent = true;
		this.active = true;
		this.sawElse = false;
	}

	/* pp */ State(State parent) {
		this.parent = parent.isParentActive() && parent.isActive();
		this.active = true;
		this.sawElse = false;
	}

	/* Required for #elif */
	/* pp */ void setParentActive(boolean b) {
		this.parent = b;
	}

	/* pp */ boolean isParentActive() {
		return parent;
	}

	/* pp */ void setActive(boolean b) {
		this.active = b;
	}

	/* pp */ boolean isActive() {
		return active;
	}

	/* pp */ void setSawElse() {
		sawElse = true;
	}

	/* pp */ boolean sawElse() {
		return sawElse;
	}

	public String toString() {
		return "parent=" + parent +
			", active=" + active +
			", sawelse=" + sawElse;
	}
}
