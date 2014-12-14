package com.stabilise.core.main;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.stabilise.core.Resources;
import com.stabilise.util.Log;

/**
 * The game uninstaller
 */
public class Uninstaller {
	
	private Uninstaller() {
		// non-instantiable
	}
	
	/**
	 * Runs the installer program.
	 * 
	 * @param args Unusued.
	 */
	public static void main(String[] args) {
		try {
			FileUtils.deleteQuietly(new File("Install output.txt"));
			FileUtils.deleteQuietly(new File("Uninstall output.txt"));
			FileUtils.deleteQuietly(new File("Game successfully installed.txt"));
			FileUtils.deleteDirectory(Resources.APP_DIR);
			//FileUtils.deleteQuietly(new File("StabiliseII.jar"));
			//FileUtils.deleteQuietly(new File("Uninstaller.jar"));
			//FileUtils.copyFile(new File("files/Installer.jar"), new File("Installer.jar"));
		} catch(IOException e) {
			Log.critical("Could not uninstall game!", e);
			Log.saveLog(false, "", new File("Uninstall output.txt"));
			System.exit(0);
		}
		//Log.message("You have successfully uninstalled Stabilise II.");
		//Log.message("To reinstall, simply run \"Installer.jar\".");
		//Log.saveLog(false, "", new File("README.txt"));
		System.exit(0);
	}
	
}
