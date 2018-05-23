package com.mg.configParser.object;

import com.mg.configParser.object.Middleware;
import java.io.File;
public class SubInstance {
  private Middleware[] arr_m;
  int num_mid = 0;
  File sub_root;
  boolean is_multiInstance = false;
  
  SubInstance(File r){
    sub_root = r;
    File[] list = r.listFiles();
    for(int i=0;i<list.length;i++){
      if(list[i].isDirectory())
        num_mid++;
    }
    //System.out.println("Number of middle ware("+sub_root.getName()+") : "+num_mid);
    if(num_mid>0){
      is_multiInstance = true;
      arr_m = new Middleware[num_mid];
      for(int i=0;i<num_mid;i++){
        arr_m[i] = new Middleware(list[i]);
      }
    }        
  }
  
  public Middleware[] get_midList(){
    return arr_m;
  }
  
  public String getPath(){
    try{
      return sub_root.getName();
      }catch(Exception e){
        e.printStackTrace();
      }
      return "fail";
  }
    
}
