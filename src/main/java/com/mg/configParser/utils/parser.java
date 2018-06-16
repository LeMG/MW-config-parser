package com.mg.configParser.utils;

import java.io.StringWriter;
import java.util.ArrayList;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mg.configParser.object.Result;

public class parser {
	public Transformer trans;// = TransformerFactory.newInstance().newTransformer();

	public Result r;

	public parser() {
		try{
			trans = TransformerFactory.newInstance().newTransformer();
			trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,"yes");
			trans.setOutputProperty(OutputKeys.INDENT, "yes");
		}catch(Exception e){
			e.printStackTrace();
		}
		r = new Result();
	}

	public void setObject(Result r) {
		this.r = r;
	}
	
	public void removeComment(Node cur){
		NodeList nl = cur.getChildNodes();
		int len = nl.getLength();
		ArrayList<Node> garbage = new ArrayList<Node>();
		for(int i=0;i<len;i++){
			Node n = nl.item(i);
			if(n.getNodeName().compareTo("#comment")==0){
				garbage.add(n);
			}else if(n.getNodeName().compareTo("#text")==0&&n.getTextContent().trim().length()==0)
				garbage.add(n);
			else{
				NodeList cnl = n.getChildNodes();
				int len2 = cnl.getLength();
				for(int i2=0;i2<len2;i2++){
					removeComment(cnl.item(i2));
				}
			}
		}
		for(Node n : garbage)
			cur.removeChild(n);
	}
	public void writeXMLResult(Node n, String item){
		try{
			StringWriter sw = new StringWriter();
			trans.transform(new DOMSource(n), new StreamResult(sw));
			r.insert(item,sw.toString());
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
