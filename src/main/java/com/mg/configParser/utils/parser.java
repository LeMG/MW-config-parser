package com.mg.configParser.utils;

import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mg.configParser.object.Result;

public class parser {
	public Result r;

	public parser() {
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
}
