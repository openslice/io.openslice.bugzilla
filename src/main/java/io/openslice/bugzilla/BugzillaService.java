package io.openslice.bugzilla;

import org.springframework.beans.factory.annotation.Value;
//import org.apache.camel.zipkin.starter.CamelZipkin;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;


@SpringBootApplication
//@CamelZipkin
@EnableDiscoveryClient
//@EnableRetry
@RefreshScope
@EnableAutoConfiguration
@EnableConfigurationProperties
public class BugzillaService {

//https://stackoverflow.com/questions/47566338/not-able-to-read-configuration-from-consul-in-spring-boot-application
	//https://github.com/indrabasak/spring-consul-example
	
	public static void main(String[] args) {		
		SpringApplication.run( BugzillaService.class, args);
	}
}
