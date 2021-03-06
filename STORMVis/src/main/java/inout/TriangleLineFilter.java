package inout;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class TriangleLineFilter extends FileFilter {

	@Override
	public boolean accept(File f) {
		if (f.isDirectory()) {
			return true;
		}

		return Utils.accept(f);
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return ".nff/.wimp/.ply/.txt";
	}

	public static class Utils {

		public final static String nff = "nff";
		public final static String wimp = "wimp";
		public final static String txt = "txt";
		public final static String ply = "ply";

		/*
		 * Get the extension of a file.
		 */
		public static String getExtension(File f) {
			String ext = null;
			String s = f.getName();
			int i = s.lastIndexOf('.');

			if (i > 0 && i < s.length() - 1) {
				ext = s.substring(i + 1).toLowerCase();
			}
			return ext;
		}

		public static boolean accept(File f) {
			String extension = Utils.getExtension(f);
			if (extension != null) {
				if (extension.equals(Utils.nff) 
					|| extension.equals(Utils.wimp) 
					|| extension.equals(Utils.txt)
					|| extension.equals(Utils.ply)) {
					return true;
				} else {
					return false;
				}
			}

			return false;
		}
	}

}
