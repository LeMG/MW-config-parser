package com.mg.configParser.object;

import java.io.File;

public class Middleware {
	File mid_root;
	public File[] arr_config;
	private String type = "unknown";

	public Middleware(File m) {
		mid_root = m;
		arr_config = mid_root.listFiles();
		set_MWType();
	}

	public File[] get_configList() {
		return arr_config;
	}

	public String get_type() {
		return new String(type);
	}

	public String getPath() {
		return mid_root.getAbsolutePath();
	}

	/**
	 * MW type 1. apache httpd 2. nginX 3. IIS 4. Apache Tomcat 5. Weblogic
	 **/

	private void set_MWType() {
		if (is_Tomcat())
			type = "Tomcat";
		else if (is_nginx())
			type = "nginx";
		else if (is_httpd())
			type = "httpd";

	}

	private boolean is_nginx() {
		for (File cur : arr_config) {
			String name = cur.getName();
			if (name.compareTo("nginx.conf") == 0) {
				return true;
			}
		}
		return false;
	}

	private boolean is_httpd() {
		for (File cur : arr_config) {
			String name = cur.getName();
			if (name.endsWith("httpd.conf")) {
				return true;
			} else if (name.startsWith("httpd") && name.endsWith(".conf")) {
				return true;
			}
		}
		return false;
	}

	private boolean is_Tomcat() {
		boolean web = false;
		boolean context = false;
		boolean server = false;

		for (int i = 0; i < arr_config.length; i++) {
			String name = arr_config[i].getName();

			if (name.compareTo("WEB-INF") == 0) {
				File[] sub_web_inf = arr_config[i].listFiles();
				for (File sub : sub_web_inf) {
					if (sub.getName().compareTo("web.xml") == 0) {
						web = true;
					}
				}
			}

			if (name.compareTo("web.xml") == 0)
				web = true;
			else if (name.compareTo("context.xml") == 0)
				context = true;
			else if (name.compareTo("server.xml") == 0)
				server = true;
		}
		return web && context && server;
	}

	public String getName() {
		return mid_root.getName();
	}
}
