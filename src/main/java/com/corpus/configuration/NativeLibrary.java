package com.corpus.configuration;

import java.io.File;
import java.lang.reflect.Field;

/**
 * The <code>NativeLibrary</code> class contains several useful methods to load
 * dynamic libraries when the program starts, even if they are not in
 * "java.library.path".
 */
public final class NativeLibrary {
	/** Pattern of supported file extensions. */
	public static String extensions = ".*\\.(dll)|(dylib)|(jnilib)|(so)";

	/**
	 * Loads all code files within the specified <b>native</b> directory
	 * (children included!) from the local file system as dynamic libraries.<br>
	 * Example:
	 * <ul>
	 * <li>Mac OS: <i>directory</i>/mac</li>
	 * <li>Windows with JRE 32bit: <i>directory</i>/win/32</li>
	 * <li>Windows with JRE 64bit: <i>directory</i>/win/64</li>
	 * <li>XYZ with JRE 32bit: <i>directory</i>/xyz/32</li>
	 * <li>XYZ with JRE 64bit: <i>directory</i>/xyz/64</li>
	 * </ul>
	 * 
	 * @param nativeDirPath
	 *            The directory to load.
	 * @return <code>true</code> if the platform-specific libraries are loaded,
	 *         <code>false</code> otherwise.
	 */
	public static boolean loadSystem(String nativeDirPath) {
		final String osName = System.getProperty("os.name").toLowerCase();
		final String osArch = System.getProperty("sun.arch.data.model");
		final File nativeDir = new File(nativeDirPath);

		boolean isLoaded = false;

		if (nativeDir != null && nativeDir.isDirectory()) {
			final File[] nativeFiles = nativeDir.listFiles();

			for (int i = 0; !isLoaded && i < nativeFiles.length; i++) {
				if (osName.indexOf(nativeFiles[i].getName()) > -1) {
					if (osName.indexOf("mac") > 0) {
						isLoaded = loadDir(nativeFiles[i]);
					} else {
						isLoaded = loadDir(nativeFiles[i] + "/" + osArch);
					}
				}
			}
		}

		return isLoaded;
	}

	/**
	 * Loads all code files within the specified directory (children included!)
	 * from the local file system as dynamic libraries.
	 * 
	 * @param pathname
	 *            The directory to load.
	 * @return <code>true</code> if the directory is loaded, <code>false</code>
	 *         otherwise.
	 */
	public static boolean loadDir(String pathname) {
		return loadDir(new File(pathname));
	}

	/**
	 * Loads all code files within the specified directory (children included!)
	 * from the local file system as dynamic libraries.
	 * 
	 * @param dir
	 *            The directory to load.
	 * @return <code>true</code> if the directory is loaded, <code>false</code>
	 *         otherwise.
	 */
	public static boolean loadDir(File dir) {
		return loadDir(dir, false);
	}

	/**
	 * Loads all code files within the specified directory from the local file
	 * system as dynamic libraries.
	 * 
	 * @param dir
	 *            The directory to load.
	 * @param ignoreChildren
	 *            <code>true</code> if the children of the directory are
	 *            ignored, <code>false</code> otherwise.
	 * @return <code>true</code> if the directory is loaded, <code>false</code>
	 *         otherwise.
	 */
	public static boolean loadDir(File dir, boolean ignoreChildren) {
		boolean isLoaded = false;

		if (dir != null && dir.isDirectory()) {
			for (File file : dir.listFiles()) {
				isLoaded = isLoaded || loadFile(file);
				if (!isLoaded && !ignoreChildren && file.isDirectory()) {
					isLoaded = isLoaded || loadDir(file, true);
				}
			}
		}

		return isLoaded;
	}

	/**
	 * Loads a code file with the specified filename from the local file system
	 * as a dynamic library.
	 * 
	 * @param filename
	 *            The file to load.
	 * @return <code>true</code> if the file is loaded, <code>false</code>
	 *         otherwise.
	 */
	public static boolean loadFile(String filename) {
		return loadFile(new File(filename));
	}

	/**
	 * Loads a code file from the local file system as a dynamic library.
	 * 
	 * @param file
	 *            The file to load.
	 * @return <code>true</code> if the file is loaded, <code>false</code>
	 *         otherwise.
	 */
	public static boolean loadFile(File file) {
		boolean isLoaded = false;

		if (file != null && file.getName().matches(extensions)) {
			updatePath(file.getParent());
			System.load(file.getAbsolutePath());
			isLoaded = true;
		}

		return isLoaded;
	}

	/**
	 * Checks whether the directory is already in the "java.library.path". If
	 * this is not the case, it will be added.
	 * 
	 * @param dir
	 *            The directory, which should be added to the
	 *            "java.library.path".
	 */
	private static void updatePath(String dir) {
		final String PATH = "java.library.path";

		if (dir != null && System.getProperty(PATH).indexOf(dir) == -1) {
			System.setProperty(PATH, dir + ";" + System.getProperty(PATH));
			setSysPathNull();
		}
	}

	/**
	 * A "dirty hack" to set the path <code>null</code>. Therefore the JVM is
	 * forced to reevaluate his path and accept changes.
	 */
	private static void setSysPathNull() {
		try {
			Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
			fieldSysPath.setAccessible(true);
			fieldSysPath.set(null, null);
		} catch (Exception ex) {
			// no chance :(
		}
	}
}
