package yc.tomcat.core;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class ParseUrlPattern {

	private String basePath = TomcatConstants.BASE_PATH;
	private static Map<String,String> urlPattern = new HashMap<String,String>();
	
	public ParseUrlPattern() {
		parse();
	}

	private void parse() {
		//获取服务器中的所有项目
		File[] files = new File(basePath).listFiles();
		if(files == null || files.length <= 0) {
			return;
		}
		String projectName = null;
		File childFile = null;
		
		for(File fl : files) {
			projectName = fl.getName();
			//System.out.println(projectName);
			if(!fl.isDirectory()) {
				continue;
			}
			
			//获取这个项目下的web.xml文件
			childFile = new File(fl,"web.xml");
			if(!childFile.exists()) {
				continue;
			}
			ParseXml(projectName,childFile);
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public void ParseXml(String projectName,File childFile) {
	
		SAXReader read = new SAXReader();
		Document doc = null;
		
		try {
			doc = read.read(childFile);
			List<Element> mimes = doc.selectNodes("//servlet");
			
			//循环解析
			for(Element el : mimes) {
				urlPattern.put("/" + projectName + el.selectSingleNode("url-pattern").getText().trim(),el.selectSingleNode("servlet-class").getText().trim());
			}
		} catch (DocumentException e) {
			e.printStackTrace();
		}		
	}
	public static String getClass(String url) {
		return urlPattern.getOrDefault(url, null);
	}
	
	public Map<String,String> getUrlPattern(){
		return urlPattern;
	}
}
