package com.mg.configParser.utils;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
		//Transformer trans = TransformerFactory.newInstance().newTransformer();
		//trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,"yes");
		//trans.setOutputProperty(OutputKeys.INDENT, "yes");

		Element root = doc.getDocumentElement();

		//remove comment
		ArrayList<Node> garbage = new ArrayList<Node>();
		NodeList rcl = root.getChildNodes();
		int len = rcl.getLength();
		for(int i=0;i<len;i++){
			Node n = rcl.item(i);
			if(n.getNodeName().compareTo("#comment")==0){
				garbage.add(n);
			}else if(n.getNodeName().compareTo("#text")==0&&n.getTextContent().trim().length()==0){
				garbage.add(n);
			}else{
				removeComment(n);
			}
		}
		for(Node n : garbage){
			root.removeChild(n);
		}

		if(name.compareToIgnoreCase("applicationHost.xml")==0){
			//logging
			NodeList nl = root.getElementsByTagName("logFile");
			for(int i=0;i<nl.getLength();i++){
				writeXMLResult(nl.item(i),"logging");
			}
			
			//error page
			nl = root.getElementsByTagName("httpErrors");
			for(int i=0;i<nl.getLength();i++){
				writeXMLResult(nl.item(i),"error page");
			}
		}
	}


}
