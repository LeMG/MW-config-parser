package com.mg.configParser;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.poi.ss.util.CellRangeAddress;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.mg.configParser.object.*;
import com.mg.configParser.utils.*;
import org.apache.poi.ss.usermodel.*;

public class Main {
	static int stdIndex = 4;
	static int num_param = 0;
	//static MainWindow mw;
	public static void main(String[] args) {
		if (args.length == 1 && args[0].compareTo("-t") == 0) {
			args = new String[2];
			args[0] = "-p";
			args[1] = "/sdcard/JavaNIDE/TestService";
		}

		num_param = args.length;
		switch (num_param) {
		case 0:
			MainWindow mw = new MainWindow();
			System.out.println("Check usage with -h or -help");
			return;
		case 1:
			if (args[0].compareTo("-h") == 0 || args[0].compareTo("-help") == 0) {
				System.out.println("- How to use ");
				System.out
						.println("  * Before use please put the config files in the following structure");
				System.out.println("    Service root(DIR)");
				System.out.println("    \tHost(Server) list(Dir)");
				System.out.println("    \t\tMiddleware list(Dir)");
				System.out.println("    \t\t\tconfig files");
				System.out.println("  * -h, -help : print usage");
				System.out
						.println("  * -p PATH : Check middleware config values related with security");
				System.out
						.println("  *           PATH param must be absolute path)");
				System.out.println("  * -c : Middleware config file list");
			} else if (args[0].compareTo("-c") == 0) {
				System.out.println("- Middleware config file list");
				System.out.println("  * Apache Tomcat config file list");
				System.out.println("    (server.xml / context.xml / web.xml)");
				System.out.println("  * Apache httpd");
				System.out
						.println("    httpd.conf and other *.conf files which are included in httpd.conf)");
				System.out.println("  * nginx");
				System.out.println("    (nginx.conf)");
				System.out.println("  * Microsoft IIS");
				System.out.println("    (applicationhost.xml)");
				System.out.println("  * Oracle Weblogic");
				System.out.println("    (config.xml)");
			} else if(args[0].compareTo("-w")==0){
				
			}else {
				System.out.println("Invalid argument!");
				System.out.println("Check usage with -h or -help");
			}
			return;
		case 2:
			if (args[0].compareTo("-p") == 0) {
				String path = args[1];
				parse(path);
			} else {
				System.out.println("Invalid argument!");
				System.out.println("Check usage with -h or -help");
			}
			break;
		default:
			System.out.println("Number of arguments doesn't matched!");
			System.out.println("Check usage with -h or -help");
			return;
		}

		/*
		 * System.out.println(System.getProperty("os.name")); Root r = new
		 * Root(); r.setPath("/sdcard/javaNIDE/TestService/"); SubInstance[]
		 * t_sub = r.get_subList(); Middleware[] t_mid = t_sub[0].get_midList();
		 * System.out.println(t_mid[0].get_type()); int i = 1; for(Middleware
		 * m:t_mid){ System.out.println(i++); XMLParser t_parser = new
		 * XMLParser(m); }
		 */
	}
	
	public static void parse(String path){
		if(num_param==0)MainWindow.clearProgress();
		try {
			if(num_param==0)MainWindow.setProgress("Path : "+path);
			Root r = new Root();
			r.setPath(path);
			ArrayList<Result> arr_result = new ArrayList<Result>();

			SubInstance[] t_sub = r.get_subList();
			int numMW = 0;
			if(num_param==0)MainWindow.setProgress("Parsing config files...");
			for (SubInstance cur_sub : t_sub) {
				if(num_param==0)MainWindow.setProgress("Host name : " + cur_sub.getPath());
				Middleware[] t_mid = cur_sub.get_midList();
				numMW += t_mid.length;
				for (Middleware m : t_mid) {
					if(num_param==0)MainWindow.setProgress(m.getName() + "(" + m.get_type()
							+ ")");
					Result result = new Result(cur_sub.getPath(),
							m.getName(), m.get_type());
					parser p = new parser(); 
					switch (m.get_type()) {
					case "Tomcat":
						p = new TomcatParser(m);
						break;
					case "nginx":
						p = new nginxParser(m);
						break;
					case "httpd":
						p = new httpdParser(m);
						break;
					default:
						p.r = new Result(m.getPath(),m.get_type());
						System.out.println("Unknown Middle ware");
					}
					arr_result.add(p.r);
				}
			}
			if(num_param==0)MainWindow.setProgress("Parsing doen!");
			writeResult(arr_result, t_sub, numMW);

		} catch (FileNotFoundException e) {
			System.out.println("Target file/directory not found!");
		}
	}

	public static void createForm(Sheet s, int numMW) {
		/*
		 * 1. process owner 2. account management 3. logging 4. dir listing 5.
		 * error page 6. http method 7. deploy dir 8. symlink 9. server token
		 * 10. ext permission
		 */
		ArrayList<String> arr_item = new ArrayList<String>();
		arr_item.add("1. Process owner(using dedicated account)");
		arr_item.add("2. Account management(account acl)");
		arr_item.add("3. Log management");
		arr_item.add("4. Directory listing");
		arr_item.add("5. Using custom error-page");
		arr_item.add("6. Restrict unrequired http methods");
		arr_item.add("7. Deploy directory setting");
		arr_item.add("8. Symbolic link disabled");
		arr_item.add("9. Hidding server information in http header");
		arr_item.add("10. File access control(File upload dir)");

		for (int i = 0; i < 20; i++) {
			Row r = s.createRow(i);
			for (int i2 = 0; i2 < numMW + 5; i2++) {
				r.createCell(i2);
			}
		}
		Row r = s.getRow(stdIndex);
		Cell c = r.getCell(0);
		c.setCellValue("Item");
		s.addMergedRegion(new CellRangeAddress(stdIndex, stdIndex + 1, 0, 0));
		for (int i = stdIndex + 2; i < stdIndex + 10 + 2; i++) {
			r = s.getRow(i);
			c = r.getCell(0);
			c.setCellValue(arr_item.get(i - (stdIndex + 2)));
		}
	}
	
	static void writeResult(ArrayList<Result> arr_result, SubInstance[] t_sub, int numMW){
		Workbook wb = new XSSFWorkbook();
		CellStyle style = wb.createCellStyle();
		style.setBorderTop(BorderStyle.THIN);
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		style.setTopBorderColor(IndexedColors.BLACK.getIndex());
		style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		style.setRightBorderColor(IndexedColors.BLACK.getIndex());
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		String sheet = "Result";
		Sheet s = wb.createSheet(sheet);
		createForm(s, numMW);

		for (int i = 0; i < 12; i++) {
			Row row = s.getRow(stdIndex + i);
			for (int i2 = 0; i2 < numMW + 1; i2++) {
				Cell c = row.getCell(i2);
				c.setCellStyle(style);
			}
		}

		int curIndex = 1;
		for (int i = 0; i < t_sub.length; i++) {
			Row row = s.getRow(stdIndex);
			Cell c = row.getCell(curIndex);
			c.setCellValue(t_sub[i].getPath());
			if (t_sub[i].getNumMid() > 1)
				s.addMergedRegion(new CellRangeAddress(stdIndex,
						stdIndex, curIndex, curIndex
								+ t_sub[i].getNumMid() - 1));
			for (int i2 = 0; i2 < t_sub[i].get_midList().length; i2++) {
				Middleware m = t_sub[i].get_midList()[i2];
				row = s.getRow(stdIndex + 1);
				c = row.getCell(curIndex + i2);
				c.setCellValue(m.getName() + "(" + m.get_type()
						+ ")");
			}
			curIndex = curIndex + t_sub[i].getNumMid();
		}
		for (int i = 0; i < arr_result.size(); i++) {
			arr_result.get(i).write(s, stdIndex + 2, i + 1);
		}

		s.autoSizeColumn(0);
		for (int i = 1; i < numMW + 1; i++) {
			// s.autoSizeColumn(i);
			s.setColumnWidth(i, 30 * 256);
		}

		try {
			String fileName = "mwConfigParser_result_"
					+ (new SimpleDateFormat("yyyyMMdd"))
							.format(new Date()) + ".xlsx";
			if(num_param==0)MainWindow.setProgress("Creating Excel file : "+fileName);
			OutputStream ops = new FileOutputStream(fileName);
			wb.write(ops);
		} catch (Exception e) {
			if(num_param==0)MainWindow.setProgress("Exception occured during create Excel file");
			if(num_param==0)MainWindow.setProgress(e.getMessage());
			e.printStackTrace();
		}
	}
}
