package me.maanuvazquez.adblocker;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.prefs.Preferences;

import me.maanuvazquez.adblocker.exceptions.UnknownOSException;

public final class Utils {

	private Utils() {
		// Nothing
	}

	/**
	 * Returns the current OS
	 * 
	 * @return
	 */
	public static OSEnum getOperativeSystem() {

		String OS = System.getProperty("os.name").toLowerCase();
		OSEnum curOS = OSEnum.WINDOWS;

		try {

			if (OS.contains("win")) {
				curOS = OSEnum.WINDOWS;
			} else if (OS.contains("mac")) {
				curOS = OSEnum.MAC;
			} else if (OS.contains("nix") || OS.contains("nux") || OS.contains("aix")) {
				curOS = OSEnum.LINUX;
			} else {
				throw new UnknownOSException("Sistema operativo desconocido");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return curOS;
	}

	/**
	 * Creates the folder given to the method if it doesn´t exist
	 * 
	 * @param appFolder
	 */
	private static void createFolder(String appFolder) {
		File dir = new File(appFolder);
		if (!dir.exists()) {
			try {
				dir.mkdir();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	/**
	 * Returns the folder of the OS for apps
	 * 
	 * @return
	 */
	private static String getLocalSystemDataFolder() {

		OSEnum OS = getOperativeSystem();

		String appDataFolder = "";

		if (OS == OSEnum.LINUX) {
			appDataFolder = System.getProperty("user.home");
		} else if (OS == OSEnum.MAC) {
			appDataFolder = System.getProperty("user.home") + "/Library/Application Support";
		} else {
			appDataFolder = System.getenv("AppData");
		}

		return appDataFolder;

	}

	/**
	 * Returns the folder into AppData to save app files
	 * 
	 * @param directory
	 * @return app folder
	 */
	public static String getAppFolder(String directory) {

		String appFolder = getLocalSystemDataFolder() + System.getProperty("file.separator") + directory;

		createFolder(appFolder);

		System.out.println("# APPDATA: " + appFolder);

		return appFolder;

	}

	/**
	 * Opens the explorer with the given URL
	 * 
	 * @param url
	 */
	public static void openDocument(String url) {

		OSEnum OS = getOperativeSystem();

		if (OS == OSEnum.WINDOWS) {
			if (Desktop.isDesktopSupported()) {
				try {
					Desktop.getDesktop().browse(new URI(url));
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (URISyntaxException e1) {
					e1.printStackTrace();
				}
			} else {
				Runtime runtime = Runtime.getRuntime();
				try {
					runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else if (OS == OSEnum.LINUX) {

			Runtime runtime = Runtime.getRuntime();
			try {
				runtime.exec("xdg-open " + url);
			} catch (Exception e) {
				try {
					String[] browsers = { "epiphany", "firefox", "mozilla", "konqueror", "netscape", "opera", "links",
							"lynx" };

					StringBuffer cmd = new StringBuffer();
					for (int i = 0; i < browsers.length; i++)
						cmd.append((i == 0 ? "" : " || ") + browsers[i] + " \"" + url + "\" ");

					runtime.exec(new String[] { "sh", "-c", cmd.toString() });
				} catch (Exception r) {
					r.printStackTrace();

				}
			}
		} else {

			Runtime runtime = Runtime.getRuntime();
			try {
				runtime.exec("open " + url);
			} catch (Exception e) {
				e.printStackTrace();

			}
		}
	}

	/**
	 * Returns true if the app was open with admin rights
	 * 
	 * @return
	 */

	public static boolean checkAdminRights() {

		boolean adminRights = false;

		Preferences prefs = Preferences.systemRoot();
		PrintStream systemErr = System.err;
		synchronized (systemErr) {
			System.setErr(null);
			try {
				prefs.put("ad", "blocker"); // SecurityException on Windows
				prefs.remove("ad");
				prefs.flush(); // BackingStoreException on Linux
				adminRights = true;
			} catch (Exception e) {
				adminRights = false;
			} finally {
				System.setErr(systemErr);
			}
		}

		System.out.println("# RIGHTSCHECKER: Admin rights " + adminRights);

		return adminRights;
	}

}
