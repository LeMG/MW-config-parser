package com.mg.configParser.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.mg.configParser.object.*;

public class httpdParser extends parser{
	
	public httpdParser(Middleware m){
		this.setObject(new Result(m.getPath(), m.get_type()));
		for(File cur:m.arr_config){
			String n = cur.getName();
			if(n.endsWith(".conf")){
				parsehttpd(cur);
			}
		}
	}

	public void parsehttpd(File target){
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
	    		String prev_key = "";
            		for(int i=0;i<arr_conf.size();i++){
               			//System.out.println("\t"+arr_conf.get(i));
	       			String conf = arr_conf.get(i);//.replace("[","").replace("]","");
	       			confNode cur = stack.get(top);
	       			//System.out.println("Cur node : "+cur.name);
	       			if(conf.startsWith("<")&&conf.charAt(1)!='/'){
		       			confNode temp = new confNode(conf);
				       cur.insert(temp);
				       stack.add(temp);
				       top++;
			       }else if(conf.startsWith("</")){
				       stack.remove(top);
				       top--;
			       }else if(conf.charAt(0)=='\''){
				       cur.insert(prev_key, "\n"+conf);
			       }else{
				       //System.out.println("cur conf : "+conf);
				       String[] arr_v = conf.split(" ");
				       String key = arr_v[0];
				       prev_key = key;
				       String value = "";
				       for(int i2=1;i2<arr_v.length;i2++){
					       value+=arr_v[i2]+" ";
				       }
				       cur.insert(key,value);
			       }
			}


		}catch(Exception e){
			e.printStackTrace();
		}
	}
  
}
