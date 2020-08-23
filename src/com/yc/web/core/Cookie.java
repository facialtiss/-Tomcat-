package com.yc.web.core;

import yc.tomcat.core.TomcatConstants;

public class Cookie {

	private String name;
	private String value;
	private long maxAge;
	
	public Cookie(String name,String value){
		this.name = name;
		this.value = value;
		this.maxAge = TomcatConstants.SESSION_TIMEOUT;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public long getMaxAge() {
		return maxAge;
	}

	public void setMaxAge(long maxAge) {
		this.maxAge = maxAge;
	}
	
}
