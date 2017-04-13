package me.maanuvazquez.adblocker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdBlock {

	private List<String> newHostFile = new ArrayList<String>();
	private List<String> currentHostSecureDNS = new ArrayList<String>();
	private LinkedHashSet<String> currentHostDNS = new LinkedHashSet<String>();
	private LinkedHashSet<String> onlineHostDNS = new LinkedHashSet<String>();
	private OSEnum OS;
	private String hostPath;
	private String hostBackup;

	public AdBlock() {

		try {

			this.OS = Utils.getOperativeSystem();

			if (this.OS == OSEnum.WINDOWS) {
				hostPath = System.getenv("WINDIR") + "\\System32\\drivers\\etc\\hosts";
				hostBackup = System.getenv("WINDIR") + "\\System32\\drivers\\etc\\hosts.bak";
			} else if (this.OS == OSEnum.LINUX) {
				hostPath = "/etc/hosts";
				hostBackup = "/etc/hosts.bak";
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("# ADBLOCKER: " + this.OS + " found");
	}

	public void makeNewHostFile() {
		newHostFile.add("#");
		newHostFile.add("# HOST FILE BY ADBLOCKER");
		newHostFile.add("#");
		newHostFile.add("# This file contains the mappings of IP addresses to host names. Each");
		newHostFile.add("# entry should be kept on an individual line. The IP address should");
		newHostFile.add("# be placed in the first column followed by the corresponding host name.");
		newHostFile.add("# The IP address and the host name should be separated by at least one");
		newHostFile.add("# space.");
		newHostFile.add("#");
		newHostFile.add("# Additionally, comments (such as these) may be inserted on individual");
		newHostFile.add("# lines or following the machine name denoted by a '#' symbol.");
		newHostFile.add("#");
		newHostFile.add("#");
		newHostFile.add("# CURRENT HOSTS FILE LINES: " + this.getLines());
		newHostFile.add("# FILE GENERATED ON: " + new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
		newHostFile.add("#===============================================================");
		newHostFile.add("");
		if (this.currentHostSecureDNS.size() > 0 || this.currentHostDNS.size() > 0) {
			newHostFile.add("# CURRENT HOSTS FILE ENTRIES");
			this.newHostFile.addAll(currentHostSecureDNS);
			addAllToList(this.currentHostDNS);
			newHostFile.add("# CURRENT HOSTS FILE ENTRIES END");
			newHostFile.add("");
		}
		if (this.onlineHostDNS.size() > 0) {
			newHostFile.add("# ADDED HOSTS BY ADBLOCKER");
			addAllToList(this.onlineHostDNS);
			newHostFile.add("# ADDED HOSTS BY ADBLOCKER END");
		}
		createNewHostsFile();
		System.out.println("# ADBLOCKER: Hosts file created successfully");
	}

	public int fetchCurrentHostFile() {

		try {

			FileInputStream systemHostsFile = new FileInputStream(hostPath);
			BufferedReader hostsFileBuffer = new BufferedReader(new InputStreamReader(systemHostsFile));

			String line;
			while ((line = hostsFileBuffer.readLine()) != null) {
				if (!line.contains("#")) {
					if (!line.trim().isEmpty()) {
						if (line.contains("localhost") || line.contains("broadcasthost") || line.contains(":")) {
							this.currentHostSecureDNS.add(line);
						} else {
							Pattern pattern = Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+)\\s+(.*)");
							Matcher matcher = pattern.matcher(line);
							while (matcher.find()) {
								this.currentHostDNS.add(matcher.group(2));
							}
						}
					}
				}
			}

			hostsFileBuffer.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		int lines = this.currentHostDNS.size() + this.currentHostSecureDNS.size();

		System.out.println("# ADBLOCKER: Current Hosts File DNS Fetched: " + lines + " lines");
		return lines;
	}

	public int fetchAdBlockerList() {

		try {

			URL hostFileWeb = new URL("https://raw.githubusercontent.com/MaanuVazquez/AdBlocker/master/hosts");

			BufferedReader inputHost = new BufferedReader(new InputStreamReader(hostFileWeb.openStream()));
			String line;
			while ((line = inputHost.readLine()) != null)
				if (!line.trim().isEmpty()) {
					if (!line.contains("#")) {
						Pattern pattern = Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+)\\s+(.*)");
						Matcher matcher = pattern.matcher(line);
						while (matcher.find()) {
							this.onlineHostDNS.add(matcher.group(2));

						}
					}
				}

			inputHost.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("# ADBLOCKER: AdBlocker DNS List Fetched: " + this.onlineHostDNS.size() + " lines");

		return this.onlineHostDNS.size();
	}

	public int fetchOnlineHostFile(String website) {

		try {

			URL hostFileWeb = new URL(website);

			BufferedReader inputHost = new BufferedReader(new InputStreamReader(hostFileWeb.openStream()));
			String line;
			while ((line = inputHost.readLine()) != null)
				if (!line.trim().isEmpty()) {
					if (!line.contains("#")) {
						if (!line.contains("localhost") && !line.contains("broadcasthost") && !line.contains("::1")) {
							Pattern pattern = Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+)\\s+(.*)");
							Matcher matcher = pattern.matcher(line);
							while (matcher.find()) {
								this.onlineHostDNS.add(matcher.group(2));

							}
						}
					}
				}

			inputHost.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("# ADBLOCKER: Online DNS Lists Fetched: " + this.onlineHostDNS.size() + " lines");

		return this.onlineHostDNS.size();

	}

	public int addToWhiteList(List<String> source) {

		int currentLines = this.currentHostDNS.size() + this.onlineHostDNS.size();

		this.currentHostDNS.removeAll(source);
		this.onlineHostDNS.removeAll(source);

		currentLines -= this.currentHostDNS.size() + this.onlineHostDNS.size();

		System.out.println("# ADBLOCKER: Added Whitelist " + currentLines + " lines erased");

		return currentLines;
	}

	public int addMyList(List<String> source) {
		int currentLines = this.onlineHostDNS.size();

		this.onlineHostDNS.addAll(source);

		currentLines = this.onlineHostDNS.size() - currentLines;

		System.out.println("# ADBLOCKER: Added My Hosts List " + currentLines + " lines added");

		return currentLines;
	}

	public int cleanOnlineHostsList() {

		this.onlineHostDNS.removeAll(this.currentHostDNS);

		System.out.println("# ADBLOCKER: Online DNS List Cleaned: " + this.onlineHostDNS.size() + " lines");

		return this.onlineHostDNS.size();
	}

	public void backupCurrentHosts() {

		try {
			File host = new File(hostPath);
			host.renameTo(new File(hostBackup));
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("# ADBLOCKER: Hosts file backup");
	}

	public void createNewHostsFile() {

		Path file = Paths.get(hostPath);
		try {
			Files.write(file, newHostFile, Charset.forName("UTF-8"));
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	private void addAllToList(LinkedHashSet<String> source) {

		for (String item : source) {
			this.newHostFile.add("0.0.0.0 " + item);
		}

	}

	public int getLines() {
		return this.currentHostDNS.size() + this.onlineHostDNS.size() + this.currentHostSecureDNS.size() + 22;
	}

}
