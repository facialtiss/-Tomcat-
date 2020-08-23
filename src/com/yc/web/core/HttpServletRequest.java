package com.yc.web.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import yc.tomcat.core.TomcatConstants;

/**
 * 基于http协议的请求处理类
 * @author qiuyu
 *@date 2020年8月22日
 */
public class HttpServletRequest implements ServletRequest {

	private String method;//请求的方式
	private Map<String,String> parameter = new HashMap<String,String>();
	private String url;//请求资源的地址
	private String protocalVersion;
	private BufferedReader read;
	
	private InputStream is;//请求流
	private volatile HttpSession session = new HttpSession();
	private boolean checkJSessionId = false;
	private String jsessionid;
	private Cookie[] cookies;
	
	public HttpServletRequest(InputStream is) {
		this.is = is;
	}
	/**
	 * 解析请求头
	 */
	public void parse() {
		try {
			read = new BufferedReader(new InputStreamReader(is));
			String line = null;
			List<String> headStrs = new ArrayList<String>();
			while((line = read.readLine()) !=null && !"".equals(line)) {
				headStrs.add(line);		
				//System.out.println(line);
			}
			parseFirstLine(headStrs.get(0));//解析起始行
			parseParameter(headStrs);//解析获取参数
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	
	/**
	 * 解析起始行
	 * @param string
	 */
	private void parseFirstLine(String str) {
		String[] strs = str.split(" ");
		this.method = strs[0];
		if(strs[1].contains("?")) {
			this.url = strs[1].substring(0,strs[1].indexOf("?"));
		}else {
			this.url = strs[1];
		}
		this.protocalVersion = strs[2];
		
	}
	
	/**
	 * 解析获取参数
	 * @param headStrs
	 */
	private void parseParameter(List<String> headStrs) {
		//处理请求地址中的参数
		String str = headStrs.get(0).split(" ")[1];//获取请求地址
		if(str.contains("?")) {
			str = str.substring(str.indexOf("?") + 1);//获取请求参数
			String[] params = str.split("&");
			String[] temp;
			for(String param : params) {
				temp = param.split("=");
				this.parameter.put(temp[0], temp[1]);
			}
			//System.out.println(this.parameter);
		}
	
		
		if(TomcatConstants.REQUEST_METHOD_POST.equals(this.method)) {
			int len = 0;
			for(String head : headStrs) {
				if(head.contains("Content-Length:")) {
					len = Integer.parseInt(head.substring(head.indexOf(":") + 1).trim());
					break;
				}
			}
			if(len <=0) {
				return;
			}
			
			//如果要处理上传文件，则还需要获取Content-Type头部字段
			try {
				char[] ch = new char[1024 * 10];
				int count = 0,total = 0;//count是每一次读到的大小，total是读到的总大小
				StringBuffer sbf = new StringBuffer(1024 * 10);
				while((count = read.read(ch)) > 0) {
					sbf.append(ch,0,count);
					total += count;
					if(total >= len) {//说明读到的总大小已经大于或等于要读取的大小了，则跳出循环
						break;
					}
				}
				str = URLDecoder.decode(sbf.toString(),"utf-8");
				str = str.substring(str.indexOf("?") + 1);//获取请求参数
				String[] params = str.split("&");
				String[] temp;
				for(String param : params) {
					temp = param.split("=");
					this.parameter.put(temp[0], temp[1]);
				}
				
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
		//System.out.println(this.parameter);
	}
	
	
	public String getMethod() {
		return this.method;
	}

	public String getParameter(String key) {
		return this.parameter.getOrDefault(key, null);
	}

	public String getUrl() {
		return this.url;
	}

	public InputStream getIs() {
		return is;
	}

	public HttpSession getSession() {
		return this.session;
	}

	public boolean checkJSessionId() {
		return this.checkJSessionId;
	}
	
	@Override
	public Cookie[] getCookies() {
		// TODO Auto-generated method stub
		return this.cookies;
	}

	@Override
	public String getJSessionId() {
		// TODO Auto-generated method stub
		return this.jsessionid;
	}
	
	public String getProtocalVersion() {
		return protocalVersion;
	}
}
