package com.mg.secuconfig.utils;

import java.util.ArrayList;

import com.mg.secuconfig.object.Middleware;
import java.io.*;
import java.util.*;

public class nginxParser {

    public nginxParser(Middleware m){
        for(File t:m.arr_config){
            String config_name = t.getName();
            if(config_name.compareTo("nginx.conf")==0){
                parsenginx(t);
            }
    
        }
    }

    public void parsenginx(File target){
        try{
            InputStreamReader isr = new InputStreamReader(new FileInputStream(target));
            BufferedReader br = new BufferedReader(isr);
            String in = null;
            ArrayList<String> arr_conf = new ArrayList<String>();
            
	    //remove comments
            while((in=br.readLine())!=null){
                in = in.replace("\t"," ");
                String t[] = in.split(" ");
            
                ArrayList<String> arr_config = new ArrayList<String>();
                for(int i=0;i<t.length;i++){
                    if(t[i].length()>0){
                    arr_config.add(t[i]);
                    }
                }
                
                if(arr_config.size()==0||arr_config.get(0).charAt(0)=='#')
                    continue;
                else{
			String temp = "";
			for(int i=0;i<arr_config.size();i++){
				temp+=arr_config.get(i)+" ";
			}
			temp = temp.substring(0,temp.length()-1);
			if(temp.indexOf("#")>-1){
				temp = temp.substring(0,temp.indexOf("#"));
			}
			arr_conf.add(temp);
			//System.out.println(temp);
                }
            }
	    //build tree from the text config
	    ArrayList<confNode> stack = new ArrayList<confNode>();
	    confNode root = new confNode();
	    stack.add(root);
	    int top = 0;
            for(int i=0;i<arr_conf.size();i++){
               //System.out.println("\t"+arr_conf.get(i));
	       String conf = arr_conf.get(i);//.replace("[","").replace("]","");
	       confNode cur = stack.get(top);
	       //System.out.println("Cur node : "+cur.name);
	       if(conf.charAt(conf.length()-1)=='{'){
		       String nodeName = conf.replace("{","");
		       //System.out.println("node name : "+nodeName);
		       confNode temp = new confNode(nodeName);
		       cur.insert(temp);
		       stack.add(temp);
		       top++;
	       }else if(conf.charAt(conf.length()-1)=='}'){
		       stack.remove(top);
		       top--;
	       }else{
		       System.out.println("cur conf : "+conf);
		       String[] arr_v = conf.split(" ");
		       String key = arr_v[0];
		       String value = "";
		       for(int i2=1;i2<arr_v.length;i2++){
			       value+=arr_v[i2]+" ";
		       }
		       cur.insert(key,value);
	       }
            }
	    //test print
	    root.print("");
        
            /*
            process owner -> user
            dir listing -> autoindex off;
	    error-page -> error_page 400 401 402 40x.html;
            http method -> limit_except .... {deny all;} if($reuqest_method !...(allowlist)$){return 40x;}
            deploy dir -> location / { root ....;}
            server name -> server_tokens off;
            sym link -> disable_symlinks on;
            file permission(upload dir, extension) -> location 	*php{return 403;}

	     */
	    String proc_owner = "";
	    root.find("user",proc_owner);
	    String dir_listing = "";
	    root.find("autoindex",dir_listing);
	    String error_page = "";
	    root.find("error_page",error_page);
	    String http_method = "";
	    root.find("deny",http_method);
	    String deploy_dir = "";
	    root.find("root",deploy_dir);
	    String server_name = "";
	    root.find("server_tokens",server_name);
	    String sym_link = "";
	    root.find("disable_symlinks",sym_link);
	    String file_permission = "";
	    root.find("return",file_permission);
            
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    class confNode{
	    Map<String, String> map_attr;
	    ArrayList<confNode> arr_child;
	    String name;
	    confNode(){
		map_attr = new HashMap<String, String>();
		arr_child = new ArrayList<confNode>();
		name = "";
	    }
	    confNode(String name){
		    map_attr = new HashMap<String, String>();
		    arr_child = new ArrayList<confNode>();
		    this.name = name;
	    }
	    public void insert(String key, String value){
		    if(map_attr.get(key)==null)
			    map_attr.put(key, value);
		    else{
			    String temp = map_attr.get(key);
			    temp = temp+"/split/"+value;
			    map_attr.put(key,temp);
		    }
	    }
	    public void insert(confNode t){
		    arr_child.add(t);
	    }
	    public void print(String tab){
		    System.out.print(tab+name);
		    if(name.length()>0)
			    System.out.println("{");
		    Iterator<String> it = map_attr.keySet().iterator();
		    while(it.hasNext()){
			    String key = it.next();
			    String value = map_attr.get(key);
			    System.out.println(tab+"\t"+key+" "+value);
		    }
		    for(confNode cn:arr_child){
			    cn.print(tab+"\t");
		    }
		    if(name.length()>0)
			    System.out.println(tab+"}");

	    }
	    public void find(String key, String value){
		    value+=map_attr.get(key);
		    for(confNode c:arr_child)
			    c.find(key,value);
	    }
    }

}
