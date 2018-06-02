package com.mg.configParser.utils;

import java.util.ArrayList;

import com.mg.configParser.object.*;
import java.io.*;
import java.util.*;

public class nginxParser extends parser {

	public nginxParser(Middleware m) {
		this.setObject(new Result(m.getPath(), m.get_type()));
		for (File t : m.arr_config) {
			String config_name = t.getName();
			if (config_name.compareTo("nginx.conf") == 0) {
				parsenginx(t);
			}
		}
	}

	public void parsenginx(File target) {
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
				if (conf.charAt(conf.length() - 1) == '{') {
					String nodeName = conf.replace("{", "");
					// System.out.println("node name : "+nodeName);
					confNode temp = new confNode(nodeName);
					cur.insert(temp);
					stack.add(temp);
					top++;
				} else if (conf.charAt(conf.length() - 1) == '}') {
					stack.remove(top);
					top--;
				} else if (conf.charAt(0) == '\'') {
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
			// test print
			root.print("");

			/*
			 * process owner -> user dir listing -> autoindex off; error-page ->
			 * error_page 400 401 402 40x.html; http method -> limit_except ....
			 * {deny all;} if($reuqest_method !...(allowlist)$){return 40x;}
			 * deploy dir -> location / { root ....;} server name ->
			 * server_tokens off; sym link -> disable_symlinks on; file
			 * permission(upload dir, extension) -> location *php{return 403;}
			 */
			System.out.println("\tProcess owner : " + root.findValue("user"));
			this.r.insert("process owner", "user " + root.findValue("user"));

			ArrayList<confNode> arr_node = new ArrayList<confNode>();
			String el = root.findValue("error_log");
			if (el != null) {
				String[] arrEl = el.split("/split/");
				for (String ep : arrEl) {
					r.insert("logging", "error_log " + ep);
				}
			}
			root.findNodes("http", arr_node);
			r.insert("logging", "http");
			for (confNode cn : arr_node) {
				String lf = cn.findValue("log_format");
				if (lf != null) {
					r.insert("logging", "\tlog_format " + lf);
				}
			}
			for (confNode cn : arr_node) {
				String logPath = cn.findValue("access_log");
				if (logPath != null) {
					r.insert("logging", "\taccess_log " + logPath);
				}
				String elogPath = cn.findValue("error_log");
				if (elogPath != null) {
					r.insert("logging", "\terror_log " + elogPath);
				}
			}
			arr_node.clear();

			root.findNodes("server", arr_node);
			for (confNode cn : arr_node) {
				String alogPath = cn.findValue("access_log");
				String elogPath = cn.findValue("error_log");
				if (alogPath != null || elogPath != null) {
					r.insert("logging", "server(" + cn.findValue("server_name")
							+ ")");
					if (alogPath != null) {
						r.insert("logging", "\taccess_log " + alogPath);
					} else {
						r.insert("logging", "\terror_log " + elogPath);
					}
				}
			}
			arr_node.clear();
			root.findNodes("location", arr_node);
			for (confNode cn : arr_node) {
				String alogPath = cn.findValue("access_log");
				String elogPath = cn.findValue("error_log");
				if (alogPath != null || elogPath != null) {
					r.insert("logging", cn.name);
					if (alogPath != null) {
						r.insert("logging", "\taccess_log " + alogPath);
					} else {
						r.insert("logging", "\terror_log " + elogPath);
					}
				}

			}

			root.findNodes("location", arr_node);
			String autoindex = "";
			for (confNode cn : arr_node) {
				String v = cn.findValue("autoindex");
				if (v != null && v.startsWith("on"))
					autoindex += cn.name + "/split/";
			}
			if (autoindex.length() == 0) {
				System.out.println("\tDir listing : off");
				r.insert("dir listing", "autoindex off(default)");
			} else {
				String[] arr_loc = autoindex.split("/split/");
				System.out.print("\tUsing dir listing : ");
				for (String loc : arr_loc) {
					r.insert("dir listing", loc + "\n\tautoindex on");
					System.out.print(loc + ", ");
				}
				System.out.println();
			}
			arr_node.clear();

			root.findNodes("server", arr_node);
			System.out.println("\tError page config");
			for (confNode cn : arr_node) {
				System.out.println("\t\t" + cn.name + "("
						+ cn.findValue("server_name") + ")");
				r.insert("error page", "server(" + cn.findValue("server_name")
						+ ")");
				String[] error_group = cn.findValue("error_page").split(
						"/split/");
				for (String v : error_group) {
					System.out.println("\t\t\t" + v);
					r.insert("error page", "\terror_page " + v);
				}
			}
			arr_node.clear();

			root.findNodes("location", arr_node);
			System.out.println("\tConfig for limitting http methods");
			for (confNode cn : arr_node) {
				r.insert("http method", cn.name);
				ArrayList<confNode> arr_limit = new ArrayList<confNode>();
				cn.findNodes("limit_except", arr_limit);
				if (arr_limit.size() > 0) {
					System.out.println("\t\t" + cn.name + "{");
					for (confNode limit : arr_limit) {
						r.insert("http method", "\t" + limit.name);
						System.out.println("\t\t\t" + limit.name);
						String deny = limit.findValue("deny");
						String allow = limit.findValue("allow");
						r.insert("http method", "\t\tdeny " + deny);
						System.out.println("\t\t\t\tdeny " + deny);
						r.insert("http method", "\t\tallow " + allow);
						System.out.println("\t\t\t\tallow " + allow);
					}
					System.out.println("\t\t}");
				}
			}
			arr_node.clear();

			root.findNodes("location", arr_node);
			for (confNode cn : arr_node) {
				String strRoot = cn.findValue("root");
				if (strRoot != null) {
					System.out
							.println("\t" + cn.name + "\n\t\troot " + strRoot);
					r.insert("deploy dir", cn.name);
					r.insert("deploy dir", "\troot " + strRoot);
				}
			}
			arr_node.clear();

			root.findNodes("http", arr_node);
			for (confNode cn : arr_node) {
				String server_tokens = cn.findValue("server_tokens");
				if (server_tokens != null) {
					r.insert("server token", cn.name + "\n\tserver_tokens "
							+ server_tokens);
					System.out.println("\tserver_tokens(" + cn.name + ") : "
							+ server_tokens);
				}
			}
			arr_node.clear();
			root.findNodes("server", arr_node);
			for (confNode cn : arr_node) {
				String server_tokens = cn.findValue("server_tokens");
				if (server_tokens != null) {
					r.insert("server token", cn.name + "\n\tserver_tokens "
							+ server_tokens);
					System.out.println("\tserver_tokens(" + cn.name + ") : "
							+ server_tokens);
				}
			}
			arr_node.clear();
			root.findNodes("location", arr_node);
			for (confNode cn : arr_node) {
				String server_tokens = cn.findValue("server_tokens");
				if (server_tokens != null) {
					r.insert("server token", cn.name + "\n\tserver_tokens "
							+ server_tokens);
					System.out.println("\tserver_tokens(" + cn.name + ") : "
							+ server_tokens);
				}
			}
			arr_node.clear();

			root.findNodes("http", arr_node);
			ArrayList<String> arr_symlink = new ArrayList<String>();

			for (confNode cn : arr_node) {
				String http_symlink = cn.findValue("disable_symlinks");
				if (http_symlink != null) {
					arr_symlink.add("disable_symlinks(" + cn.name + ") : "
							+ http_symlink);
				}
			}
			arr_node.clear();
			root.findNodes("server", arr_node);
			for (confNode cn : arr_node) {
				String server_symlink = cn.findValue("disable_symlinks");
				if (server_symlink != null) {
					arr_symlink.add("disable_symlinks(" + cn.name + ") : "
							+ server_symlink);
				}
			}
			arr_node.clear();
			root.findNodes("location", arr_node);
			for (confNode cn : arr_node) {
				String loc_symlink = cn.findValue("disable_symlinks");
				if (loc_symlink != null) {
					arr_symlink.add("disable_symlinks(" + cn.name + ") : "
							+ loc_symlink);
				}
			}
			if (arr_symlink.size() == 0) {
				r.insert("symlink", "disable_symlinks on");
				System.out.println("\tdisable_symlinks : on(defualt)");
			} else {
				for (String conf : arr_symlink) {
					System.out.println("\t" + conf);
					r.insert("symlink", "disable_symlinks " + conf);
				}
			}
			arr_node.clear();

			root.findNodes("location", arr_node);
			String[] arr_ext = { "php", "sh", "html", "htm", "js", "jsp",
					"asp", "bat", "ht" };
			System.out
					.println("\tScript, source files execute permission check");
			for (confNode cn : arr_node) {
				boolean check_ext = false;
				for (String ext : arr_ext) {
					if (cn.name.contains(ext) && !cn.name.contains("=")) {
						check_ext = true;
						break;
					}
				}
				if (check_ext) {
					System.out.println("\t\t" + cn.name + "{");
					String rt = cn.findValue("return");
					String deny = cn.findValue("deny");
					r.insert("ext permission", cn.name);
					if (rt != null) {
						System.out.println("\t\t\treturn " + rt);
						r.insert("ext permission", "\treturn " + rt);
					}
					if (deny != null) {
						System.out.println("\t\t\tdeny " + deny);
						r.insert("ext permission", "\tdeny " + deny);
					}
					System.out.println("\t\t}");
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/*
	 * class confNode{ Map<String, String> map_attr; ArrayList<confNode>
	 * arr_child; String name; confNode(){ map_attr = new HashMap<String,
	 * String>(); arr_child = new ArrayList<confNode>(); name = ""; }
	 * confNode(String name){ map_attr = new HashMap<String, String>();
	 * arr_child = new ArrayList<confNode>(); this.name = name; } public void
	 * insert(String key, String value){ if(map_attr.get(key)==null)
	 * map_attr.put(key, value); else{ String temp = map_attr.get(key); temp =
	 * temp+"/split/"+value; map_attr.put(key,temp); } } public void
	 * insert(confNode t){ arr_child.add(t); } public void print(String tab){
	 * System.out.print(tab+name); if(name.length()>0) System.out.println("{");
	 * Iterator<String> it = map_attr.keySet().iterator(); while(it.hasNext()){
	 * String key = it.next(); String value = map_attr.get(key);
	 * System.out.println(tab+"\t"+key+" "+value); } for(confNode cn:arr_child){
	 * cn.print(tab+"\t"); } if(name.length()>0) System.out.println(tab+"}");
	 * 
	 * } public String findValue(String key){ return map_attr.get(key); } public
	 * void findNodes(String n, ArrayList<confNode> arr_node){
	 * if(arr_child.size()==0) return;
	 * 
	 * for(confNode cn:arr_child){ if(cn.name.startsWith(n)){ arr_node.add(cn);
	 * } cn.findNodes(n,arr_node); } } }
	 */
}
