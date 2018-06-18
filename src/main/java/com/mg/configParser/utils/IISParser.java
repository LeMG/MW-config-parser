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
			//deploy dir
			NodeList nl = root.getElementsByTagName("application");
			for(int i=0;i<nl.getLength();i++){
				writeXMLResult(nl.item(i),"deploy dir");
			}

			//logging
			nl = root.getElementsByTagName("logFile");
			for(int i=0;i<nl.getLength();i++){
				writeXMLResult(nl.item(i),"logging");
			}
			
			//error page
			nl = root.getElementsByTagName("httpErrors");
			r.insert("error page","ApplicatioHost.config");
			for(int i=0;i<nl.getLength();i++){
				writeXMLResult(nl.item(i),"error page");
			}


		}else if(name.compareTo("Web.config")==0){
			//http method
			NodeList nl = root.getElementsByTagName("verbs");
			for(int i=0;i<nl.getLength();i++){
				writeXMLResult(nl.item(i),"http method");
			}

			//dir listing
			nl = root.getElementsByTagName("directoryBrowse");
			for(int i=0;i<nl.getLength();i++){
				writeXMLResult(nl.item(i),"dir listing");
			}

			//error page
			nl = root.getElementsByTagName("httpErrors");
			r.insert("error page","Web.config");
			for(int i=0;i<nl.getLength();i++){
				writeXMLResult(nl.item(i),"error page");
			}


			//sym link
			nl = root.getElementsByTagName("fileExtensions");
			for(int i=0;i<nl.getLength();i++){
				String ext = nl.item(i).getAttributes().getNamedItem("fileExtension").getNodeValue();
				if(ext.compareToIgnoreCase(".lnk")==0){
					writeXMLResult(nl.item(i),"symlink");
				}
			}

			//server token(url rewrite)
			nl = root.getElementsByTagName("rule");
			for(int i=0;i<nl.getLength();i++){
				Element temp = (Element)nl.item(i);
				NodeList tl = temp.getElementsByTagName("match");
				for(int i2=0;i2<tl.getLength();i2++){
					if(tl.item(i2).getAttributes().getNamedItem("serverVariable").getNodeValue().compareTo("RESPONSE_SERVER")==0){
						writeXMLResult(nl.item(i),"server token");
					}
				}
			}

			//ext permission
			nl = root.getElementsByTagName("handlers");
			for(int i=0;i<nl.getLength();i++){
				writeXMLResult(nl.item(i),"ext permission");
			}

		}
	}


}
