package com.mg.configParser.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.mg.configParser.object.*;

public class httpdParser extends parser {

	public httpdParser(Middleware m) {
		this.setObject(new Result(m.getPath(), m.get_type()));
		for (File cur : m.arr_config) {
			String n = cur.getName();
			if (n.endsWith(".conf")) {
				parsehttpd(cur);
			}
		}
	}

	public void parsehttpd(File target) {
		try {
			InputStreamReader isr = new InputStreamReader(new FileInputStream(
					target));
			BufferedReader br = new BufferedReader(isr);
			String in = null;
			ArrayList<String> arr_conf = new ArrayList<String>();

			// remove comments
			while ((in = br.readLine()) != null) {
				in = in.replace("\t", " ");
				String t[] = in.split(" ");

				ArrayList<String> arr_config = new ArrayList<String>();
				for (int i = 0; i < t.length; i++) {
					if (t[i].length() > 0) {
						arr_config.add(t[i]);
					}
				}

				if (arr_config.size() == 0
						|| arr_config.get(0).charAt(0) == '#')
					continue;
				else {
					String temp = "";
					for (int i = 0; i < arr_config.size(); i++) {
						temp += arr_config.get(i) + " ";
					}
					temp = temp.substring(0, temp.length() - 1);
					if (temp.indexOf("#") > -1) {
						temp = temp.substring(0, temp.indexOf("#"));
					}
					arr_conf.add(temp);
					// System.out.println(temp);
				}
			}

			// build tree from the text config
			ArrayList<confNode> stack = new ArrayList<confNode>();
			confNode root = new confNode();
			stack.add(root);
			int top = 0;
			String prev_key = "";
			for (int i = 0; i < arr_conf.size(); i++) {
				// System.out.println("\t"+arr_conf.get(i));
				String conf = arr_conf.get(i);// .replace("[","").replace("]","");
				confNode cur = stack.get(top);
				// System.out.println("Cur node : "+cur.name);
				if (conf.startsWith("<") && conf.charAt(1) != '/') {
					confNode temp = new confNode(conf);
					cur.insert(temp);
					stack.add(temp);
					top++;
				} else if (conf.startsWith("</")) {
					stack.remove(top);
					top--;
				} else if (conf.charAt(0) == '\'' || conf.charAt(0) == '\"') {
					cur.insert(prev_key, "\n" + conf);
				} else {
					// System.out.println("cur conf : "+conf);
					String[] arr_v = conf.split(" ");
					String key = arr_v[0];
					prev_key = key;
					String value = "";
					for (int i2 = 1; i2 < arr_v.length; i2++) {
						value += arr_v[i2] + " ";
					}
					cur.insert(key, value);
				}
			}

			// {"process owner","account management","logging","dir listing",
			// "error page",
			// "http method", "deploy dir", "symlink",
			// "sever token","ext permission" };

			String po = root.findValue("User");
			String group = root.findValue("Group");
			r.insert("process owner", "User " + po);
			r.insert("process owner", "Group " + group);

			String el = root.findValue("ErrorLog");
			if (el != null) {
				String[] arr_el = el.split("/split/");
				for (String el_path : arr_el) {
					r.insert("logging", "ErrorLog " + el_path);
				}
			}
			ArrayList<confNode> arrConf = new ArrayList<confNode>();
			root.findNodes("<IfModule", arrConf);
			for (confNode cn : arrConf) {
				String custom = cn.findValue("CustomLog");
				if (custom != null) {
					String[] arr_custom = cn.findValue("CustomLog").split(
							"/split/");
					for (String log : arr_custom) {
						r.insert("logging", "CustomLog " + log);
					}
				}
			}
			arrConf.clear();

			root.findNodes("<Directory", arrConf);
			for (confNode cn : arrConf) {
				String op = cn.findValue("Options");
				if (op.contains("Indexes")) {
					r.insert("dir listing", cn.name);
					r.insert("dir listing", "\tOptions " + op);
					r.insert("dir listing", "</Directory>");
				}
			}
			arrConf.clear();

			String ep = root.findValue("ErrorDocument");
			if (ep != null) {
				String[] arr_ep = ep.split("/split/");
				for (String e : arr_ep) {
					r.insert("error page", "ErrorDocument " + e);
				}
			}
			root.findNodes("<Directory", arrConf);
			for (confNode cn : arrConf) {
				ep = null;
				ep = cn.findValue("ErrorDocument");
				if (ep != null) {
					String[] arr_ep = ep.split("/split/");
					if (arr_ep.length > 0)
						r.insert("error page", cn.name);
					for (String e : arr_ep) {
						r.insert("error page", "\tErrorDocument " + e);
					}
					if (arr_ep.length > 0)
						r.insert("error page", "</Directory>");
				}
			}
			arrConf.clear();

			root.findNodes("<Limit", arrConf);
			for (confNode cn : arrConf) {
				r.insert("http method", cn.name);
				r.insert("http method", "\tOrder " + cn.findValue("Order"));
				String a = cn.findValue("Allow");
				if (a != null)
					r.insert("http method", "\t Deny " + a);
				String d = cn.findValue("Deny");
				if (d != null)
					r.insert("http method", "\tDeny " + d);
			}
			arrConf.clear();

			String docRoot = root.findValue("DocumentRoot");
			String serverRoot = root.findValue("ServerRoot");
			r.insert("deploy dir", "ServerRoot " + serverRoot);
			if (docRoot != null)
				r.insert("deploy dir", "DocumentRoot " + docRoot);
			root.findNodes("VirtualHost", arrConf);
			for (confNode cn : arrConf) {
				docRoot = null;
				docRoot = cn.findValue("DocumentRoot");
				if (docRoot != null) {
					r.insert("deploy dir", cn.name);
					r.insert("deploy dir", "\tDocumentRoot " + docRoot);
					r.insert("deploy dir", "</VirtualHost>");
				}
			}
			arrConf.clear();

			root.findNodes("<Directory", arrConf);
			for (confNode cn : arrConf) {
				String op = cn.findValue("Options");
				if (op.contains("FollowSymLinks")) {
					r.insert("symlink", cn.name);
					r.insert("symlink", "\tOptions " + op);
					r.insert("symlink", "</Directory>");
				}
			}
			arrConf.clear();

			String serverTokens = root.findValue("ServerTokens");
			if (serverTokens != null)
				r.insert("server token", "ServerTokens " + serverTokens);

			root.findNodes("<Directory", arrConf);
			for (confNode cn : arrConf) {
				String op = cn.findValue("Options");
				String op2 = cn.findValue("RemoveType");
				ArrayList<confNode> arrFiles = new ArrayList<confNode>();
				cn.findNodes("<Files", arrFiles);
				if ((op != null && op.contains("IncludesNoExec"))
						|| op2 != null || arrFiles.size() > 0) {
					r.insert("ext permission", cn.name);
					if (op.contains("IncludesNoExec")) {
						r.insert("ext permission", "\tOptions " + op);
					}
					if (op2 != null) {
						r.insert("ext permission", "\tRemoveType " + op2);
					}
					for (confNode c : arrFiles) {
						r.insert("ext permission", "\t" + c.name);
						String o = c.findValue("Order");
						r.insert("ext permission", "\t\tOrder " + o);
						o = c.findValue("Deny");
						r.insert("ext permission", "\t\tDeny " + o);
						o = c.findValue("Allow");
						r.insert("ext permission", "\t\tAllow " + o);
						r.insert("ext permission", "\t</Files>");
					}
					r.insert("ext permission", "</Directory>");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
