package com.yc.web.core;

import java.io.IOException;
import java.io.PrintWriter;

public interface ServletResponse {
/**
 * 给出响应的方法
 * @return
 * @throws IOException 
 */
	public PrintWriter getWriter() throws IOException;
	
	/**
	 * 重定向的方法
	 * @param url 要定位的地址
	 */
	public void sendRedirect(String url);
	
	public void sendStatic(String url);
}
