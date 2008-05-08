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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import static org.anarres.cpp.Token.*;

/**
 * (Currently a simple test class).
 */
public class Main {

    protected static class Option extends LongOpt {
        private String  eg;
        private String  help;
        public Option(String word, int arg, int ch,
                        String eg, String help) {
            super(word, arg, null, ch);
            this.eg = eg;
            this.help = help;
        }
    }

	private List<String>		i_default;
	private List<String>		i_system;
	private List<String>		i_user;
	private List<String>		i_quote;
	private Map<String,String>	d_default;
	private Map<String,String>	d_user;
	private Set<Warning>		warnings;
	private List<String>		f_include;

	private static final Option[]	OPTS = new Option[] {
		new Option("help",   LongOpt.NO_ARGUMENT,       'h', null,
			"Displays help and usage information."),
		new Option("define", LongOpt.REQUIRED_ARGUMENT, 'D', "name=definition",
			"Defines the given macro."),
		new Option("undefine", LongOpt.REQUIRED_ARGUMENT, 'U', "name",
			"Undefines the given macro, previously either builtin or defined using -D."),
		new Option("include", LongOpt.REQUIRED_ARGUMENT, 'i', "file",
			"Process file as if \"#" + "include \"file\"\" appeared as the first line of the primary source file."),
		new Option("incdir", LongOpt.REQUIRED_ARGUMENT, 'I', "dir",
			"Adds the directory dir to the list of directories to be searched for header files."),
		new Option("warning", LongOpt.REQUIRED_ARGUMENT, 'W', "type",
			"Enables the named warning class ("  + getWarnings() + ")."),
		new Option("no-warnings", LongOpt.NO_ARGUMENT, 'w', null,
			"Disables ALL warnings."),
		new Option("version", LongOpt.NO_ARGUMENT, 'V', null,
			"Prints jcpp's version number (" + Version.getVersion() + ")"),
	};

	private static CharSequence getWarnings() {
		StringBuilder	buf = new StringBuilder();
		for (Warning w : Warning.values()) {
			if (buf.length() > 0)
				buf.append(", ");
			String	name = w.name().toLowerCase();
			buf.append(name.replace('_', '-'));
		}
		return buf;
	}

	public static void main(String[] args) throws Exception {
		(new Main()).run(OPTS, args);
	}

	public Main() {
		i_default = new ArrayList<String>();
		i_system = new ArrayList<String>();
		i_user = new ArrayList<String>();
		i_quote = new ArrayList<String>();
		d_default = new HashMap<String,String>();
		d_user = new HashMap<String,String>();
	}

	public void run(Option[] opts, String[] args) throws Exception {
        String	sopts = getShortOpts(opts);
        Getopt	g = new Getopt("jcpp", args, sopts, opts);
		int		c;
		String	arg;
		int		idx;

		Preprocessor	pp = new Preprocessor();
		pp.addFeature(Feature.LINEMARKERS);

        GETOPT: while ((c = g.getopt()) != -1) {
            switch (c) {
				case 'D':
					arg = g.getOptarg();
					idx = arg.indexOf('=');
					if (idx == -1)
						d_user.put(arg, "1");
					else
						d_user.put(arg.substring(0, idx),
									arg.substring(idx + 1));
					break;
				case 'U':
					d_default.remove(g.getOptarg());
					d_user.remove(g.getOptarg());
					break;
				case 'I':
					i_user.add(g.getOptarg());
					break;
				case 'W':
					arg = g.getOptarg().toUpperCase();
					arg = arg.replace('-', '_');
					if (arg.equals("all"))
						pp.addWarnings(EnumSet.allOf(Warning.class));
					else
						pp.addWarning(Enum.valueOf(Warning.class, arg));
					break;
                case 'w':
					pp.getWarnings().clear();
					break;
				case 'i':
					// pp.addInput(new File(g.getOptarg()));
					// Comply exactly with spec.
					pp.addInput(new StringLexerSource(
						"#" + "include \"" + g.getOptarg() + "\"\n"
					));
					break;
				case 'V':
					System.out.println("Anarres Java C Preprocessor version " + Version.getVersion());
					System.out.println("Copyright (C) 2008 Shevek (http://www.anarres.org/).");
					System.out.println("This is free software; see the source for copying conditions.  There is NO");
					System.out.println("warranty; not even for MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.");
					return;
                case 'h':
                    usage(getClass().getName(), opts);
					return;
                default:
                case '?':
                    throw new Exception("Illegal option " + c);
			}
		}

		/* XXX include-path, include-files. */

		for (Map.Entry<String,String> e : d_default.entrySet())
			pp.addMacro(e.getKey(), e.getValue());
		for (Map.Entry<String,String> e : d_user.entrySet())
			pp.addMacro(e.getKey(), e.getValue());

		/* XXX Include paths. */

		for (int i = g.getOptind(); i < args.length; i++)
			pp.addInput(new FileLexerSource(new File(args[i])));

		try {
			for (;;) {
				Token	tok = pp.token();
				if (tok != null && tok.getType() == Token.EOF)
					break;
				System.out.print(tok.getText());
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			Source	s = pp.getSource();
			while (s != null) {
				System.out.println(" -> " + s);
				s = s.getParent();
			}
		}

	}


    private static String getShortOpts(Option[] opts)
                        throws Exception {
        StringBuilder   buf = new StringBuilder();
        for (int i = 0; i < opts.length; i++) {
            char    c = (char)opts[i].getVal();
            for (int j = 0; j < buf.length(); j++)
                if (buf.charAt(j) == c)
                    throw new Exception(
                            "Duplicate short option " + c
                                );
            buf.append(c);
            switch (opts[i].getHasArg()) {
                case LongOpt.NO_ARGUMENT:
                    break;
                case LongOpt.OPTIONAL_ARGUMENT:
                    buf.append("::");
                    break;
                case LongOpt.REQUIRED_ARGUMENT:
                    buf.append(":");
                    break;
            }
        }
        return buf.toString();
    }

    /* This is incomplete but nearly there. */
    /**
     * Wraps a string.
     *
     * The output form is:
     * <pre>
     * prefix     in[0]
     * &lt;--indent-&gt; in[1]
     * &lt;--indent-&gt; in[2]
     * &lt;-----width----&gt;
     * </pre>
     */
    /* XXX There's some of this in commons. */
    private static String wrap(String in, String prefix,
                            int indent, int width) {
        StringBuilder   buf = new StringBuilder(prefix);

        while (buf.length() < indent)
            buf.append(' ');

        int             start = 0;

        while (start < in.length()) {
            while (start < in.length() &&
                    Character.isWhitespace(in.charAt(start)))
                start++;

            int     end = start + width - indent;

            if (end > in.length()) {
                buf.append(in.substring(start));
                break;
            }

            int     idx = end;
            while (!Character.isWhitespace(in.charAt(idx)))
                idx--;

            if (idx == start) {
                idx = end - 1;
                buf.append(in.substring(start, idx));
                buf.append('-');
            }
            else {
                buf.append(in.substring(start, idx));
                start = idx;
            }

            start = idx;
        }

        return buf.toString();
	}

    private static void usage(String command, Option[] options) {
        StringBuilder   text = new StringBuilder("Usage: ");
        text.append(command).append('\n');
        for (int i = 0; i < options.length; i++) {
            StringBuilder   line = new StringBuilder();
            Option          opt = options[i];
            line.append("    --").append(opt.getName());
            switch (opt.getHasArg()) {
                case LongOpt.NO_ARGUMENT:
                    break;
                case LongOpt.OPTIONAL_ARGUMENT:
                    line.append("[=").append(opt.eg).append(']');
                    break;
                case LongOpt.REQUIRED_ARGUMENT:
                    line.append('=').append(opt.eg);
                    break;
            }
            line.append(" (-").append((char)opt.getVal()).append(")");
            if (line.length() < 30) {
                while (line.length() < 30)
                    line.append(' ');
            }
            else {
                line.append('\n');
                for (int j = 0; j < 30; j++)
                    line.append(' ');
            }
            /* This should use wrap. */
            line.append(opt.help);
            line.append('\n');
            text.append(line);
        }

        System.out.println(text);
    }



	public static void oldmain(String[] args) throws Exception {
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
