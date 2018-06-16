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

public class wildflyParser extends parser{
	File target;
	String name;
	DocumentBuilderFactory dbf = null;
	DocumentBuilder db = null;
	Document doc = null;

	public wildflyParser(Middleware m) {
		this.setObject(new Result(m.getPath(), m.get_type()));
		try {
			for (File t : m.arr_config) {
				target = t;
				name = target.getName();
				if (name.endsWith("xml")) {
					dbf = DocumentBuilderFactory.newInstance();
					db = dbf.newDocumentBuilder();
					doc = db.parse(target);
					parseWildfly();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	private void parseWildfly() throws TransformerException{
		Transformer trans = TransformerFactory.newInstance().newTransformer();
		trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,"yes");
		trans.setOutputProperty(OutputKeys.INDENT,"yes");
		//"process owner", "account management", "logging",
		//	"dir listing", "error page", "http method", "deploy dir",
		//	"symlink", "server token", "ext permission"
		String name = target.getName();	

		Element root = doc.getDocumentElement();
		ArrayList<Node> garbage = new ArrayList<Node>();
		NodeList rcl = root.getChildNodes();
		int len = rcl.getLength();
		for(int i=0;i<len;i++){
			if(rcl.item(i).getNodeName().compareTo("#comment")==0){
				garbage.add(rcl.item(i));
			}else if(rcl.item(i).getNodeName().compareTo("#text")==0&&rcl.item(i).getTextContent().trim().length()==0)
				garbage.add(rcl.item(i));
			else
				removeComment(rcl.item(i));
		}
		for(Node n:garbage)
			root.removeChild(n);

		if(name.contains("standalone")&&name.endsWith(".xml")){
			//deploy dir
			NodeList nl = root.getElementsByTagName("deployment-scanner");
			for(int i=0;i<nl.getLength();i++){
				Node n = nl.item(i);
				StringWriter sw = new StringWriter();
				trans.transform(new DOMSource(n),new StreamResult(sw));
				r.insert("deploy dir",sw.toString());
			}

		}else if(name.contains("domain")&&name.endsWith(".xml")){
			
		}else if(name.contains("web")&&name.endsWith(".xml")){
			//error page
			NodeList nl = root.getElementsByTagName("error-page");
			for(int i=0;i<nl.getLength();i++){
				Node n = nl.item(i);
				StringWriter sw = new StringWriter();
				trans.transform(new DOMSource(n), new StreamResult(sw));
				r.insert("error page",sw.toString());
			}

			///http method
			nl = root.getElementsByTagName("security-constraint");
			for(int i=0;i<nl.getLength();i++){
				Node n = nl.item(i);
				if(((Element)n).getElementsByTagName("http-method").getLength()>0){
					StringWriter sw = new StringWriter();
					trans.transform(new DOMSource(n), new StreamResult(sw));
					r.insert("http method", sw.toString());
				}
			}
		}
	}
}
