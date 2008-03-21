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

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import static org.anarres.cpp.Token.*;

/**
 * (Currently a simple test class).
 */
public class Main {

	public static void main(String[] args) throws Exception {
		List<String>	path = new ArrayList<String>();
		path.add("/usr/include");
		path.add("/usr/local/include");
		path.add("/usr/lib/gcc/i686-pc-linux-gnu/4.1.2/include");

		Source			source = new FileLexerSource(new File(args[0]));
		Preprocessor	pp = new Preprocessor(source);
		pp.setIncludePath(path);

		for (int i = 1; i < args.length; i++) {
			pp.push_source(new FileLexerSource(new File(args[i])),true);
		}

		Macro			m = new Macro("__WORDSIZE");
		m.addToken(new Token(INTEGER, -1, -1, "32", Integer.valueOf(32)));
		pp.addMacro(m);

		m = new Macro("__STDC__");
		m.addToken(new Token(INTEGER, -1, -1, "1", Integer.valueOf(1)));
		pp.addMacro(m);

		try {
			for (;;) {
				Token	tok = pp.token();
				if (tok != null && tok.getType() == Token.EOF)
					break;
				switch (2) {
					case 0:
						System.out.print(tok);
						break;
					case 1:
						System.out.print("[" + tok.getText() + "]");
						break;
					case 2:
						System.out.print(tok.getText());
						break;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			Source	s = pp.getSource();
			while (s != null) {
				System.out.println(" -> " + s);
				s = s.getParent();
			}

			/*
			Iterator<State>	it = pp.states.iterator();
			while (it.hasNext()) {
				System.out.println(" -? " + it.next());
			}
			*/

		}

		Map<String,Macro>	macros = pp.getMacros();
		List<String>		keys = new ArrayList<String>(
				macros.keySet()
					);
		Collections.sort(keys);
		Iterator<String>	mt = keys.iterator();
		while (mt.hasNext()) {
			String	key = mt.next();
			Macro	macro = macros.get(key);
			System.out.println("#" + "macro " + macro);
		}

	}

}
