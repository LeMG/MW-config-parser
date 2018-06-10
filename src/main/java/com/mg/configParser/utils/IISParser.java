package com.mg.configParser.utils;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import org.w3c.dom.Document;

import com.mg.configParser.object.Middleware;
import com.mg.configParser.object.Result;

public class IISParser extends parser{
	File target;
	String name;
	DocumentBuilderFactory dbf = null;
	DocumentBuilder db = null;
	Document doc = null;

	public IISParser(Middleware m){
		this.setObject(new Result(m.getPath(), m.get_type()));
		try {
			for (File t : m.arr_config) {
				target = t;
				name = target.getName();
				if (name.endsWith("config")) {
					dbf = DocumentBuilderFactory.newInstance();
					db = dbf.newDocumentBuilder();
					doc = db.parse(target);
					parseIIS();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	private void parseIIS() throws TransformerException{
		Transformer trans = TransformerFactory.newInstance().newTransformer();
		trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,"yes");
		trans.setOutputProperty(OutputKeys.INDENT, "yes");


	}


}
