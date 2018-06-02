package com.mg.configParser.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class confNode {
	Map<String, String> map_attr;
	ArrayList<confNode> arr_child;
	public String name;

	public confNode() {
		map_attr = new HashMap<String, String>();
		arr_child = new ArrayList<confNode>();
		name = "";
	}

	public confNode(String name) {
		map_attr = new HashMap<String, String>();
		arr_child = new ArrayList<confNode>();
		this.name = name;
	}

	public void insert(String key, String value) {
		if (map_attr.get(key) == null)
			map_attr.put(key, value);
		else {
			String temp = map_attr.get(key);
			temp = temp + "/split/" + value;
			map_attr.put(key, temp);
		}
	}

	public void insert(confNode t) {
		arr_child.add(t);
	}

	public void print(String tab) {
		System.out.print(tab + name);
		if (name.length() > 0)
			System.out.println("{");
		Iterator<String> it = map_attr.keySet().iterator();
		while (it.hasNext()) {
			String key = it.next();
			String value = map_attr.get(key);
			System.out.println(tab + "\t" + key + " " + value);
		}
		for (confNode cn : arr_child) {
			cn.print(tab + "\t");
		}
		if (name.length() > 0)
			System.out.println(tab + "}");

	}

	public String findValue(String key) {
		return map_attr.get(key);
	}

	public void findNodes(String n, ArrayList<confNode> arr_node) {
		if (arr_child.size() == 0)
			return;

		for (confNode cn : arr_child) {
			if (cn.name.startsWith(n)) {
				arr_node.add(cn);
			}
			cn.findNodes(n, arr_node);
		}
	}
}
