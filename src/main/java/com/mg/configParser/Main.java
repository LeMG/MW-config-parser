package com.mg.configParser;

import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Workbook;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.mg.configParser.object.*;
import com.mg.configParser.utils.*;
public class Main {
    public static void main(String[] args) {
	if(args.length==1&&args[0].compareTo("-t")==0){
        	args = new String[2];
        	args[0] = "-p";
        	args[1] = "/sdcard/JavaNIDE/TestService";
	}
	Workbook wb = new XSSFWorkbook();
        
	int num_param = args.length;
        switch (num_param) {
            case 0:
                System.out.println("Number of arguments doesn't matched!");
                System.out.println("Check usage with -h or -help");
                return;
            case 1:
                if (args[0].compareTo("-h") == 0 || args[0].compareTo("-help") == 0) {
                    System.out.println("* M/W Config Parser");
                    System.out.println("* How to use ");
                    System.out.println("* Before use please put the config files in the following structure");
                    System.out.println("* Service root(DIR)");
                    System.out.println("* \t Host(Server) list(Dir)");
                    System.out.println("*\t\t Middleware list(Dir)");
                    System.out.println("* \t\t\t config files");
                    System.out.println("* ");
                    System.out.println("* -h, -help : print usage");
                    System.out.println("* -p PATH : Check middleware config values related with security");
                    System.out.println("*           PATH param must be absolute path)");
                    System.out.println("* -c : Middleware config file list");
                } else if (args[0].compareTo("-c") == 0) {
                    System.out.println("* Middleware config file list");
                    System.out.println("* Apache Tomcat config file list");
                    System.out.println("* Middleware Dir/server.xml\n*\t\t context.xml\nweb.xml(WEB-INF/web.xml)");
                } else {
                    System.out.println("Invalid argument!");
                    System.out.println("Check usage with -h or -help");
                }
                return;
            case 2:
                if (args[0].compareTo("-p") == 0) {
                    String path = args[1];
                    try {
                        Root r = new Root();
                        r.setPath(path);
			ArrayList<Result> arr_result = new ArrayList<Result>();

                        SubInstance[] t_sub = r.get_subList();
                        for (SubInstance cur_sub: t_sub) {
                            System.out.println("Host name : " + cur_sub.getPath());
                            Middleware[] t_mid = cur_sub.get_midList();
                            for (Middleware m: t_mid) {
                                System.out.println(m.getName() + "(" + m.get_type() + ")");
				Result result = new Result(cur_sub.getPath(),m.getName(),m.get_type());
				parser p=null;
                                switch (m.get_type()) {
                                    case "Tomcat":
                                        p = new TomcatParser(m);
                                        break;
                                    case "nginx":
                                        p = new nginxParser(m);
                                        break;
                                    default:
                                        System.out.println("Unknown Middle ware");
                                }
				if(p!=null)
					arr_result.add(p.r);
                            }
                        }
                    } catch (FileNotFoundException e) {
                        System.out.println("Target file/directory not found!");
                    }
                } else {
                    System.out.println("Invalid argument!");
                    System.out.println("Check usage with -h or -help");
                }
                return;
            default:
                System.out.println("Number of arguments doesn't matched!");
                System.out.println("Check usage with -h or -help");
               return;
        }

        /*
        System.out.println(System.getProperty("os.name"));
        Root r = new Root();   
        r.setPath("/sdcard/javaNIDE/TestService/");
        SubInstance[] t_sub = r.get_subList();
        Middleware[] t_mid = t_sub[0].get_midList();
        System.out.println(t_mid[0].get_type());
        int i = 1;
        for(Middleware m:t_mid){
          System.out.println(i++);
          XMLParser t_parser = new XMLParser(m);
        }
        */
    }
}
