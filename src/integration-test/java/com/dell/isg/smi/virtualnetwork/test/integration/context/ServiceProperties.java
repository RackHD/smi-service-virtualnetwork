package com.dell.isg.smi.virtualnetwork.test.integration.context;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.PropertySource;


@TestConfiguration
@PropertySource(value={"file:integration_test.properties"}, ignoreResourceNotFound=true )
public class ServiceProperties {

	@Value("${ip:null}" )
	private String ip;

	@Value("${port:null}")
	private String port;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}
}
