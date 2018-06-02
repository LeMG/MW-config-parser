package com.mg.configParser.object;

import com.mg.configParser.object.SubInstance;
import java.io.*;
import java.io.FileNotFoundException;

public class Root {
	String rootPath;

	private SubInstance[] arr_i;
	private int num_instance = 0;

	public void setPath(String str) throws FileNotFoundException {
		rootPath = str;
		doInit();
	}

	public void doInit() throws FileNotFoundException {
		if (rootPath.endsWith("\\")) {
			rootPath = rootPath.substring(0, rootPath.length() - 1);
		}
		System.out.println("Service Root : " + rootPath);
		File root = new File(rootPath);
		if (!root.exists()) {
			throw new FileNotFoundException();
		}
		File[] list = root.listFiles();

		for (int i = 0; i < list.length; i++) {
			if (list[i].isDirectory())
				num_instance++;
		}
		// System.out.println("Number of instance : "+num_instance);
		arr_i = new SubInstance[num_instance];
		for (int i = 0; i < num_instance; i++) {
			arr_i[i] = new SubInstance(list[i]);
		}
	}

	public SubInstance[] get_subList() {
		return arr_i;
	}

}
