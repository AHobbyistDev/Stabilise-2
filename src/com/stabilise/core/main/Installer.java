package com.stabilise.core.main;

/**
 * The game installer.
 */
public class Installer {
	
	private Installer() {
		// non-instantiable
	}
	
	/**
	 * Runs the installer program.
	 */
	/*
	public static void main(String[] args) {
		IOUtil.createDirQuietly(Resources.APP_DIR);
		try {
			FileUtils.deleteQuietly(new File("Install output.txt"));
			FileUtils.deleteQuietly(new File("Uninstall output.txt"));
			FileUtils.deleteQuietly(new File("Game successfully installed.txt"));
			FileUtils.copyDirectory(new File("files/.stabilise/"), Resources.APP_DIR);
			//FileUtils.copyFile(new File("files/StabiliseII.jar"), new File("StabiliseII.jar"));
			//FileUtils.copyFile(new File("files/Uninstaller.jar"), new File("Uninstaller.jar"));
			//FileUtils.deleteQuietly(new File("Installer.jar"));
		} catch(IOException e) {
			Log.get().postSevere("Could not copy game files!", e);
			Log.saveLog(false, "", new File("Install output.txt"));
			System.exit(0);
		}
		Log.get().postInfo("The game files have been installed to: \"" + Resources.APP_DIR.getAbsolutePath().toString() + "\"");
		Log.get().postInfo("You have successfully installed Stabilise II!");
		Log.get().postInfo("Simply run \"StabiliseII.jar\" and enjoy!");
		Log.saveLog(false, "", new File("Game successfully installed.txt"));
		System.exit(0);
	}
	*/
	
}
