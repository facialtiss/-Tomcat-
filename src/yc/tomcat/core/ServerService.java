package yc.tomcat.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;

import com.yc.web.core.HttpServletRequest;
import com.yc.web.core.HttpServletResponse;
import com.yc.web.core.Servlet;
import com.yc.web.core.ServletRequest;
import com.yc.web.core.ServletResponse;

public class ServerService implements Runnable{
	private Socket sk;
	private InputStream is;
	private OutputStream os;
	
	public ServerService(Socket sk) {
		this.sk = sk;
	}
	@Override
	public void run() {
	try {
		this.is = sk.getInputStream();
		this.os = sk.getOutputStream();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		return;
	}
		
	//处理请求
	ServletRequest request = new HttpServletRequest(is);
	
	//解析请求
	request.parse();
	
	//处理请求
	//请求的serverlet还是静态资源，如何判断是动态资源呢？如果是要映射到动态资源，则肯定会配置到对应项目的目的web.xml
	//所以我们必须在服务器启动的时候就自动扫描每个项目下的web.xml文件，解析其中的映射配置
	String url = request.getUrl();
	String urlStr = url.substring(1);//去掉最前面的/   -》 Fresh/login?id=123&name=yc
	String projectName = urlStr.substring(0,urlStr.indexOf("/"));
	
	ServletResponse response = new HttpServletResponse("/" + projectName, os);
	
	//是不是动态资源地址
	String clazz = ParseUrlPattern.getClass(url);
	if(clazz == null || "".equals(clazz)) {
		response.sendRedirect(url);
		return;
	}
	
	/*
	 * 处理动态资源
	 * 规则：所有动态资源处理代码-》servlet代码必须放到档期项目下面的bin目录下
	 */
	//要动态的加载当前访问的这个项目下面的bin目录下得类
	URLClassLoader loader = null;//类加载
	URL classPath = null;//需要加载的这个类
	//url = /Fresh/login?id=123&name=yc
	
	
	try {
		classPath = new URL("file",null,TomcatConstants.BASE_PATH + "\\" + projectName + "\\bin");
		
		//创建一个类加载器，高随他到这个路径下加载类
		loader = new URLClassLoader(new URL[] {classPath});
		
		//通过类加载器，加载我们需要的这个类， -》 是一个我们自己定义的servlet类
		Class<?> cls = loader.loadClass(clazz);
		
		Servlet servlet = (Servlet)cls.newInstance();//实例化这个类
		
		//将这个请求交给servlet的service()处理
		servlet.service(request, response);
	} catch (Exception e) {
			send500(e);
			e.printStackTrace();
	}
	
	}
	private void send500(Exception e) {
		try {
			String msg = "HTTP/1.1 500 Error\r\n\r\n" + e.getMessage();
			os.write(msg.getBytes());
			os.flush();
		} catch (IOException e1) {
			
			e1.printStackTrace();
		}finally {
			if(os!=null) {
				try {
					os.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			if(sk!=null) {
				try {
					sk.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
}
