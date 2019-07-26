package io.openslice.bugzilla;

import org.springframework.beans.factory.annotation.Value;
//import org.apache.camel.zipkin.starter.CamelZipkin;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@SpringBootApplication
//@CamelZipkin
@EnableDiscoveryClient
public class BugzillaService {


	
	public static void main(String[] args) {		
		SpringApplication.run( BugzillaService.class, args);
	}
}
