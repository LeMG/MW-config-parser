package com.mg.secuconfig.utils;

import com.mg.secuconfig.object.Middleware;
import java.io.File;
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
  
  public void parsenginx(File conf){
	  try{
		  InputStreamReader isr = new InputStreamReader(new FileInputStream(conf));
		  BufferedReader br = new BufferedReader(isr);
		  String in = null;
		  String[] arr_conf = new String[];
		  while((in=br.readLine())!=null){
			  in = in.replace("\t"," ");
			  String t[] = in.split(" ");
			  
			  ArrayList<String> arr_config = new ArrayList<String>();
			  for(int i=0;i<t.length;i++){
				  if(t[i].length()>0){
					  arr_config.add(t[i]);
				  }
			  }
			  if(arr_config.get(0).compareTo("#")==0)
			    continue;
			  else{
			    
   }
		  }
	  }catch(Exception e){
		  e.printStackTrace();
	  }
  	
  }
  
}