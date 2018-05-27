package com.mg.configParser.object;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

public class Result {
	static String os = System.getProperty("os.name");
	String host_name;
	String mw_name;
	String mw_type;
	String[] item = {"process owner","account management","logging","dir listing", "error page", "http method", "deploy dir", "symlink", "sever token","ext permission"  };

	Map<String,StringBuffer> data;

	public Result(){
	}
	public Result(String h, String mwn, String mwt){
		this.host_name = h;
		this.mw_name = mwn;
		this.mw_type = mwt;
		this.data = new HashMap<String, StringBuffer>();
	}
	public Result(String path, String mwt){
		String os = System.getProperty("os.name");
		String delimiter = "/";
		if(os.contains("windows")||os.contains("Windows")||os.contains("WINDOWS")){
			delimiter = "\\";
		}
		String[] a = path.split(delimiter);
		this.mw_type = mwt;
		this.mw_name = a[a.length-1];
		this.host_name =a[a.length-2];
		this.data = new HashMap<String, StringBuffer>();
	}

	public void insert(String key,String value){
		StringBuffer cur = this.data.get(key);
		value = value==null?"null":value;
		if(cur==null){
			this.data.put(key,new StringBuffer(value));
		}else{
			cur.append("\n"+value);
			this.data.put(key,cur);
		}
	}
	public String getData(String key){
		return data.get(key)==null?"null":data.get(key).toString();
	}

	public void write(Sheet s, int rowIndex, int colIndex){
		for(int i=0;i<10;i++){
			Row r = s.getRow(rowIndex+i);
			Cell c = r.getCell(colIndex);
			c.setCellValue(getData(item[i]));
		}
	}	

}
