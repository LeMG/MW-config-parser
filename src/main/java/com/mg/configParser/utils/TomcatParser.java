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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mg.configParser.object.*;

public class TomcatParser extends parser {
	File target;
	String name;
	DocumentBuilderFactory dbf = null;
	DocumentBuilder db = null;
	Document doc = null;

	public TomcatParser(File t) {
		target = t;
		try {
			name = target.getName();
			if (name.endsWith("xml")) {
				dbf = DocumentBuilderFactory.newInstance();
				db = dbf.newDocumentBuilder();
				doc = db.parse(target);
				parse_Tomcat();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public TomcatParser(Middleware m) {
		this.setObject(new Result(m.getPath(), m.get_type()));
		try {
			for (File t : m.arr_config) {
				target = t;
				name = target.getName();
				if (name.endsWith("xml")) {
					dbf = DocumentBuilderFactory.newInstance();
					db = dbf.newDocumentBuilder();
					doc = db.parse(target);
					parse_Tomcat();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	public void test() {
		Element root = doc.getDocumentElement();
		NodeList nl = root.getElementsByTagName("servlet");
		System.out.println(nl.getLength());
	}

	public void parse_Tomcat(/* Result r */) throws TransformerException {
		//Transformer trans = TransformerFactory.newInstance().newTransformer();
		//trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		//trans.setOutputProperty(OutputKeys.INDENT, "yes");
		if (name.endsWith(".xml")) {
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
			
			if (name.contains("web.xml")) {// &&target.getAbsolutePath().contains("WEB-INF")==false){
				boolean is = false;

				if (target.getAbsolutePath().contains("WEB-INF") == true) {
					is = true;
				}
				/* Driectory listing */
				NodeList nl = root.getElementsByTagName("init-param");
				for (int i = 0; i < nl.getLength(); i++) {
					Node n = nl.item(i);
					String param_name = n.getFirstChild().getNextSibling()
							.getTextContent();
					String param_value = n.getLastChild().getPreviousSibling()
							.getTextContent();
					if (param_name.compareTo("listings") == 0) {
						StringWriter sw = new StringWriter();
						trans.transform(new DOMSource(n), new StreamResult(sw));

						//r.insert("dir listing", param_name + " : "
						//		+ param_value);
						r.insert("dir listing", sw.toString());
						System.out.println("\t" + param_name + " : "
								+ param_value);
					}
				}

				/* Custome Error Page */
				nl = root.getElementsByTagName("error-page");
				for (int i = 0; i < nl.getLength(); i++) {
					Node n = nl.item(i);
					StringWriter sw = new StringWriter();
					trans.transform(new DOMSource(n), new StreamResult(sw));
					r.insert("error page", sw.toString());
					// System.out.println(n.getTextContent());
					String code = n.getFirstChild().getNextSibling()
							.getTextContent();
					String location = n.getLastChild().getPreviousSibling()
							.getTextContent();
					//r.insert("error page", code + " : " + location);
					System.out.println("\t" + code + " : " + location);
				}

				// restrict http method
				nl = root.getElementsByTagName("security-constraint");
				for (int i = 0; i < nl.getLength(); i++) {
					Node n = nl.item(i);
					NodeList child_nl = n.getChildNodes();
					String r_name = null;
					String url_pattern = null;
					ArrayList<String> al_method = new ArrayList<String>();
					if(((Element)n).getElementsByTagName("http-method").getLength()>0||((Element)n).getElementsByTagName("http-method-omission").getLength()>0){
						StringWriter sw = new StringWriter();
						trans.transform(new DOMSource(n), new StreamResult(sw));
						r.insert("http method", sw.toString());
					}
					/*
					for (int i2 = 0; i2 < child_nl.getLength(); i2++) {
						Node cur = child_nl.item(i2);
						String cur_name = cur.getNodeName();
						if (cur_name.compareTo("web-resource-collection") == 0) {
							// resource name
							Node c = cur.getFirstChild();
							while (c.getNodeName().compareTo(
									"web-resource-name") != 0) {
								c = c.getNextSibling();
							}
							if (c.getNodeName().compareTo("web-resource-name") == 0) {
								r_name = c.getTextContent();
								// r.insert("http method",r_name);
							}

							// URL pattern
							c = cur.getFirstChild();
							while (c.getNodeName().compareTo("url-pattern") != 0) {
								c = c.getNextSibling();
							}
							url_pattern = c.getTextContent();
							// r.insert("http method",url_pattern);

							// Restricted methods
							c = cur.getFirstChild();
							do {
								if (c.getNodeName().compareTo("http-method") == 0) {
									al_method.add(c.getTextContent());
								}
								c = c.getNextSibling();
							} while (c != null);
						}
					}*/
					/*if (al_method.size() > 0) {
						r.insert("http method", "url pattern : " + url_pattern);
						System.out.println("\tTraget URL : " + url_pattern);
						System.out.print("\tMethod list(restricted) : ");
						for (int i2 = 0; i2 < al_method.size(); i2++) {
							System.out.print(al_method.get(i2) + " ");
							r.insert("http method", "\t" + al_method.get(i2));
						}
						System.out.println();
					}*/

				}

			} else if (name.compareTo("server.xml") == 0) {
				// Deploy directory
				NodeList nl = root.getElementsByTagName("Host");
				String appBase = "webapps";
				for (int i = 0; i < nl.getLength(); i++) {
					Node n = nl.item(i);
					NamedNodeMap nmap = n.getAttributes();
					appBase = nmap.getNamedItem("appBase").getNodeValue()==null?appBase:"webapps";
					StringWriter sw = new StringWriter();
					trans.transform(new DOMSource(n), new StreamResult(sw));
					r.insert("deploy dir", sw.toString());

				}
				System.out.println("\tDeploy directory(appBase) : " + appBase);
				// r.insert("deploy dir", appBase);

				// Server Name
				nl = root.getElementsByTagName("Connector");
				String server_name = "default";
				for (int i = 0; i < nl.getLength(); i++) {
					Node n = nl.item(i);
					Node temp = n.getAttributes().getNamedItem("server");
					if (temp != null) {
						server_name = temp.getNodeValue();
						// System.out.println("Server : "+server_name);
						StringWriter sw = new StringWriter();
						trans.transform(new DOMSource(n), new StreamResult(sw));
						r.insert("server token", sw.toString());
						System.out.println("server token " + sw.toString());
					}
				}

				// Use symbolic link
				// nl = root.getElementsByTagName("Context");
				nl = root.getElementsByTagName("Resource");
				String symlink = "default(not use)";
				for (int i = 0; i < nl.getLength(); i++) {
					Node n = nl.item(i);
					Node temp = n.getAttributes().getNamedItem("allowLinking");
					if (temp != null) {
						symlink = temp.getNodeValue();
						StringWriter sw = new StringWriter();
						trans.transform(new DOMSource(n), new StreamResult(sw));
						r.insert("symlink", sw.toString());
					}
				}
				System.out.println("\tUse Symbolic Link(Server.xml) : "
						+ symlink);
				//r.insert("symlink", symlink + "(server.xml)");

			} else if (name.compareTo("context.xml") == 0) {
				// NodeList nl = root.getElementsByTagName("Context");
				NodeList nl = root.getElementsByTagName("Resource");
				for (int i = 0; i < nl.getLength(); i++) {
					Node n = nl.item(i);
					Node temp = n.getAttributes().getNamedItem("allowLinking");
					if (temp != null) {
						StringWriter sw = new StringWriter();
						trans.transform(new DOMSource(n), new StreamResult(sw));
						r.insert("symlink", sw.toString());
					}
				}
				// Use symbolic link
				/*Node temp = root.getAttributes().getNamedItem("allowLinking");
				String symlink = "default(not use)";
				if (temp != null) {
					symlink = temp.getNodeValue();
				}
				System.out.println("\tUse Symbolic Link(context.xml) : "
						+ symlink);
				r.insert("symlink", symlink + "(context.xml)");*/

			}

		} else {
			/*
			 * /*linux config /*
			 */

		}
	}
}
