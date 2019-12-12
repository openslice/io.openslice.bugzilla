/*-
 * ========================LICENSE_START=================================
 * io.openslice.bugzilla
 * %%
 * Copyright (C) 2019 openslice.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */


package io.openslice.bugzilla;


import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.net.ssl.SSLContext;

import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.LoggingLevel;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http4.HttpClientConfigurer;
import org.apache.camel.component.http4.HttpComponent;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import io.openslice.bugzilla.model.Bug;
import io.openslice.tmf.so641.model.ServiceOrderCreateNotification;

/**
 * @author ctranoris
 *
 */
/**
 * @author ctranoris
 *
 */
@Configuration
@RefreshScope
@Component
public class BugzillaRouteBuilder extends RouteBuilder {
	
	/**
	 * In consul
	 * Under config/openslice/osdata there will be a YAML properties 
	 * bugzilla:
	 *   host: locahost:8080
	 *   key: xxxxxxx
	 */

    @Value("${bugzillaurl}")
	private String BUGZILLAURL = "localhost:443/bugzilla";
    @Value("${bugzillakey}")
	private String BUGZILLAKEY = "";
	
	@Value("${EVENT_SERVICE_ORDER_CREATE}")
	private String EVENT_SERVICE_ORDER_CREATE = "";
	
	@Value("${EVENT_SERVICE_ORDER_STATE_CHANGED}")
	private String EVENT_SERVICE_ORDER_STATE_CHANGED = "";
	
	@Value("${EVENT_SERVICE_ORDER_DELETE}")
	private String EVENT_SERVICE_ORDER_DELETE = "";
	
	@Value("${EVENT_SERVICE_ORDER_ATTRIBUTE_VALUE_CHANGED}")
	private String EVENT_SERVICE_ORDER_ATTRIBUTE_VALUE_CHANGED = "";


	@Value("${EVENT_SERVICE_CREATE}")
	private String EVENT_SERVICE_CREATE = "";
	
	@Value("${EVENT_SERVICE_STATE_CHANGED}")
	private String EVENT_SERVICE_STATE_CHANGED = "";
	
	@Value("${EVENT_SERVICE_DELETE}")
	private String EVENT_SERVICE_DELETE = "";
	
	@Value("${EVENT_SERVICE_ATTRIBUTE_VALUE_CHANGED}")
	private String EVENT_SERVICE_ATTRIBUTE_VALUE_CHANGED = "";

    @PostConstruct
    public void postConstruct() {
        // to validate if properties are loaded
        System.out.println("** bugzilla.host: " + BUGZILLAURL);
        System.out.println("** bugzilla.key: " + BUGZILLAKEY);
    }

	private static final transient Log logger = LogFactory.getLog( BugzillaRouteBuilder.class.getName() );
	
	public void configure() {

		
//		if ( ( BUGZILLAURL == null ) || BUGZILLAURL.equals( "" ) ){
//			return; //no routing towards Bugzilla
//		}
//		if ( ( BUGZILLAKEY == null ) || BUGZILLAKEY.equals( "" ) ){
//			return;//no routing towards Bugzilla
//		}
//		

		HttpComponent httpComponent = getContext().getComponent("https4", HttpComponent.class);
		httpComponent.setHttpClientConfigurer(new MyHttpClientConfigurer());

		/**
		 * Create New Issue in Bugzilla. The body is a {@link Bug}
		 */
		from("direct:bugzilla.newIssue")
		.marshal().json( JsonLibrary.Jackson, true)
		.convertBodyTo( String.class ).to("stream:out")
		.errorHandler(deadLetterChannel("direct:dlq_bugzilla")
				.maximumRedeliveries( 4 ) //let's try for the next 120 mins to send it....
				.redeliveryDelay( 60000 ).useOriginalMessage()
				.deadLetterHandleNewException( false )
				//.logExhaustedMessageHistory(false)
				.logExhausted(true)
				.logHandled(true)
				//.retriesExhaustedLogLevel(LoggingLevel.WARN)
				.retryAttemptedLogLevel( LoggingLevel.WARN) )
		.setHeader(Exchange.HTTP_METHOD, constant(org.apache.camel.component.http4.HttpMethods.POST))
		.toD( "https4://" + BUGZILLAURL + "/rest.cgi/bug?api_key="+ BUGZILLAKEY +"&throwExceptionOnFailure=true")
		.to("log:DEBUG?showBody=true&showHeaders=true")
		.to("stream:out");
		
		/**
		 * Update issue in bugzilla. The body is a {@link Bug}. header.uuid is used to select the bug
		 */
		from("direct:bugzilla.updateIssue")
		.marshal().json( JsonLibrary.Jackson, true)
		.convertBodyTo( String.class ).to("stream:out")
		.errorHandler(deadLetterChannel("direct:dlq_bugzilla")
				.maximumRedeliveries( 4 ) //let's try for the next 120 minutess to send it....
				.redeliveryDelay( 60000 ).useOriginalMessage()
				.deadLetterHandleNewException( false )
				//.logExhaustedMessageHistory(false)
				.logExhausted(true)
				.logHandled(true)
				//.retriesExhaustedLogLevel(LoggingLevel.WARN)
				.retryAttemptedLogLevel( LoggingLevel.WARN) )
		.setHeader(Exchange.HTTP_METHOD, constant(org.apache.camel.component.http4.HttpMethods.PUT))
		.toD( "https4://" + BUGZILLAURL + "/rest.cgi/bug/${header.uuid}?api_key="+ BUGZILLAKEY +"&throwExceptionOnFailure=true")
		.to("log:DEBUG?showBody=true&showHeaders=true")
		.to("stream:out");
		
		
		
		
		/**
		 * Create user route, from activemq:topic:users.create
		 */
		
		from("activemq:topic:users.create").routeId( "users-create-route" )
		.unmarshal().json( JsonLibrary.Jackson, io.openslice.model.PortalUser.class, true)
		.bean( BugzillaClient.class, "transformUser2BugzillaUser")
		.marshal().json( JsonLibrary.Jackson,  true)
		.convertBodyTo( String.class ).to("stream:out")
//		.errorHandler(deadLetterChannel("direct:dlq_users")
//				.maximumRedeliveries( 4 ) //let's try 10 times to send it....
//				.redeliveryDelay( 60000 ).useOriginalMessage()
//				.deadLetterHandleNewException( false )
//				//.logExhaustedMessageHistory(false)
//				.logExhausted(true)
//				.logHandled(true)
//				//.retriesExhaustedLogLevel(LoggingLevel.WARN)
//				.retryAttemptedLogLevel( LoggingLevel.WARN) )
		.setHeader(Exchange.HTTP_METHOD, constant(org.apache.camel.component.http4.HttpMethods.POST))
		.toD( "https4://" + BUGZILLAURL + "/rest.cgi/user?api_key="+ BUGZILLAKEY +"&throwExceptionOnFailure=true")
		.to("stream:out");
		
		
//		from("timer://test?delay=10s&period=30000")
//		.bean( BugzillaClient.class, "getPortalUser")
//		.marshal().json( JsonLibrary.Jackson, true)
//		.convertBodyTo( String.class )
//		.to( "activemq:topic:users.create" );
		
		
		
		

		/**
		 * dead Letter Queue Users if everything fails to connect
		 */
		from("direct:dlq_users")
		.setBody()
//		.body(DeploymentDescriptor.class)
//		.bean( BugzillaClient.class, "transformDeployment2BugBody")
		.body(String.class)
		.to("stream:out");
		
		
		from("direct:bugzilla.bugmanage")
		.choice()
		.when( issueExists )
			.log( "Update ISSUE for ${body.alias} !" )		
			.process( BugHeaderExtractProcessor )
			.to("direct:bugzilla.updateIssue")
			.endChoice()
		.otherwise()
			.log( "New ISSUE for ${body.alias} !" )	
			.to("direct:bugzilla.newIssue")
			.endChoice();
		
		
//		/**
//		 * Create VxF Validate New Route
//		 */
//		String jenkinsURL = null;
////		if (PortalRepository.getPropertyByName("jenkinsciurl").getValue() != null) {
////			jenkinsURL = PortalRepository.getPropertyByName("jenkinsciurl").getValue();
////		}
//		if ( ( jenkinsURL != null ) && ( !jenkinsURL.equals( "" ) ) ){
//			from("activemq:topic:vxf.new.validation")
//			.delay(30000)			
//			.bean( BugzillaClient.class, "transformVxFValidation2BugBody")
//			.to("direct:bugzilla.newIssue");
//		}
		
		/**
		 * Service Order Topics
		 */
		
		
		from( EVENT_SERVICE_ORDER_CREATE )
		.unmarshal().json( JsonLibrary.Jackson, ServiceOrderCreateNotification.class, true)
		.bean( BugzillaClient.class, "transformNotification2BugBody")
		.to("direct:bugzilla.newIssue");

		from( EVENT_SERVICE_ORDER_ATTRIBUTE_VALUE_CHANGED )
		.unmarshal().json( JsonLibrary.Jackson, ServiceOrderCreateNotification.class, true)
		.bean( BugzillaClient.class, "transformNotification2BugBody")
		.to("direct:bugzilla.newIssue");
		
		from( EVENT_SERVICE_ORDER_DELETE )
		.unmarshal().json( JsonLibrary.Jackson, ServiceOrderCreateNotification.class, true)
		.bean( BugzillaClient.class, "transformNotification2BugBody")
		.to("direct:bugzilla.newIssue");
		
		from( EVENT_SERVICE_ORDER_STATE_CHANGED )
		.unmarshal().json( JsonLibrary.Jackson, ServiceOrderCreateNotification.class, true)
		.bean( BugzillaClient.class, "transformNotification2BugBody")
		.to("direct:bugzilla.newIssue");

		
		
		
		/**
		 * Update Validation Route
		 */
		from("activemq:topic:vxf.validationresult.update")
		.unmarshal().json( JsonLibrary.Jackson, io.openslice.model.ValidationStatus.class, true)
		.bean( BugzillaClient.class, "transformVxFValidation2BugBody")
		.to("direct:bugzilla.bugmanage");
		
		
		/**
		 * Create VxF Validate New Route
		 */
		from("activemq:topic:vxf.onboard")
		.unmarshal().json( JsonLibrary.Jackson, io.openslice.model.VxFOnBoardedDescriptor.class, true)
		.bean( BugzillaClient.class, "transformVxFAutomaticOnBoarding2BugBody")
		.to("direct:bugzilla.newIssue");

		/**
		 * Create VxF OffBoard New Route
		 */
		from("activemq:topic:vxf.offboard")
		.unmarshal().json( JsonLibrary.Jackson, io.openslice.model.VxFOnBoardedDescriptor.class, true)
		.bean( BugzillaClient.class, "transformVxFAutomaticOffBoarding2BugBody")
		.to("direct:bugzilla.bugmanage");
		
		/**
		 * Automatic OnBoarding Route Success
		 */		
		from("activemq:topic:vxf.onboard.success")
		.unmarshal().json( JsonLibrary.Jackson, io.openslice.model.VxFOnBoardedDescriptor.class, true)
		.delay(30000)		
		.bean( BugzillaClient.class, "transformVxFAutomaticOnBoarding2BugBody")
		.process( BugHeaderExtractProcessor )
		.to("direct:bugzilla.updateIssue");
		//.to("direct:bugzilla.bugmanage");
		

		/**
		 * Automatic OnBoarding Route Fail
		 */		
		from("activemq:topic:vxf.onboard.fail")
		.unmarshal().json( JsonLibrary.Jackson, io.openslice.model.VxFOnBoardedDescriptor.class, true)
		.delay(30000)		
		.bean( BugzillaClient.class, "transformVxFAutomaticOnBoarding2BugBody")
		.process( BugHeaderExtractProcessor )
		.to("direct:bugzilla.updateIssue");
		//.to("direct:bugzilla.bugmanage");	

		
		
		
		/**
		 * IMPORTANT NOTE: NSD ISSUE VALIDATION IS DISABLED FOR NOW
		 * SINCE THERE IS NO nsd VALIDATION!
		//Create NSD Validate New Route 
		from("activemq:topic:nsd.validate.new")
		.bean( BugzillaClient.class, "transformNSDValidation2BugBody")
		.to("direct:bugzilla.newIssue");
				
		//Create NSD Validation Update Route		 
		from("activemq:topic:nsd.validate.update")
		.bean( BugzillaClient.class, "transformNSDValidation2BugBody")
		.choice()
		.when( issueExists )
			.log( "Update ISSUE for validating ${body.alias} !" )		
			.process( BugHeaderExtractProcessor )
			.to("direct:bugzilla.updateIssue")
			.endChoice()
		.otherwise()
			.log( "New ISSUE for validating ${body.alias} !" )	
			.to("direct:bugzilla.newIssue")
			.endChoice();

		 */
		
		/**
		 * Create NSD onboard New Route
		 */
		from("activemq:topic:nsd.onboard")
		.unmarshal().json( JsonLibrary.Jackson, io.openslice.model.ExperimentOnBoardDescriptor.class, true)
		.bean( BugzillaClient.class, "transformNSDAutomaticOnBoarding2BugBody")
		.to("direct:bugzilla.newIssue");

		/**
		 * Create NSD offboard New Route
		 */
		from("activemq:topic:nsd.offboard")
		.unmarshal().json( JsonLibrary.Jackson, io.openslice.model.ExperimentOnBoardDescriptor.class, true)
		.bean( BugzillaClient.class, "transformNSDAutomaticOffBoarding2BugBody")
		.to("direct:bugzilla.bugmanage");
		
		/**
		 * Automatic OnBoarding Route Success
		 */		
		from("activemq:topic:nsd.onboard.success")
		.unmarshal().json( JsonLibrary.Jackson, io.openslice.model.ExperimentOnBoardDescriptor.class, true)
		.delay(30000)		
		.bean( BugzillaClient.class, "transformNSDAutomaticOnBoarding2BugBody")
		.process( BugHeaderExtractProcessor )
		.to("direct:bugzilla.updateIssue");

		
		/**
		 * Automatic OnBoarding Route Fail
		 */		
		from("activemq:topic:nsd.onboard.fail")
		.unmarshal().json( JsonLibrary.Jackson, io.openslice.model.ExperimentOnBoardDescriptor.class, true)
		.delay(30000)		
		.bean( BugzillaClient.class, "transformNSDAutomaticOnBoarding2BugBody")
		.process( BugHeaderExtractProcessor )
		.to("direct:bugzilla.updateIssue");


		/**
		 * Automatic NS Instantiation Route Success
		 */		
		from("activemq:topic:nsd.deployment.instantiation.success")
		.unmarshal().json( JsonLibrary.Jackson, io.openslice.model.DeploymentDescriptor.class, true)
		.delay(30000)		
		.bean( BugzillaClient.class, "transformNSInstantiation2BugBody")
		.to("direct:bugzilla.bugmanage");	

		/**
		 * Automatic NS Termination Route Success
		 */		
		from("activemq:topic:nsd.deployment.termination.success")
		.unmarshal().json( JsonLibrary.Jackson, io.openslice.model.DeploymentDescriptor.class, true)
		.delay(30000)		
		.bean( BugzillaClient.class, "transformNSInstantiation2BugBody")
		.to("direct:bugzilla.bugmanage");	

		from("activemq:topic:nsd.deployment.termination.fail")
		.unmarshal().json( JsonLibrary.Jackson, io.openslice.model.DeploymentDescriptor.class, true)
		.delay(30000)		
		.bean( BugzillaClient.class, "transformNSInstantiation2BugBody")
		.to("direct:bugzilla.bugmanage");	

				
		/**
		 * Create Deployment Route Issue
		 */
		from("activemq:topic:deployments.create")
		.unmarshal().json( JsonLibrary.Jackson, io.openslice.model.DeploymentDescriptor.class, true)
		.bean( BugzillaClient.class, "transformDeployment2BugBody")
		.to("direct:bugzilla.newIssue");
				
		/**
		 * Update Deployment Route
		 */
		from("activemq:topic:deployments.update")
		.unmarshal().json( JsonLibrary.Jackson, io.openslice.model.DeploymentDescriptor.class, true)
		.bean( BugzillaClient.class, "transformDeployment2BugBody")
		.process( BugHeaderExtractProcessor )
		.to("direct:bugzilla.updateIssue");
		
		/**
		 * OSM5 Communication
		 */		
		from("activemq:topic:communication.osm.fail")
		.delay(30000)		
		.bean( BugzillaClient.class, "transformOSMCommunicationFail2BugBody")
		.to("direct:bugzilla.bugmanage");
		
		from("activemq:topic:communication.osm5.success")
		.delay(30000)		
		.bean( BugzillaClient.class, "transformOSMCommunicationSuccess2BugBody")
		.to("direct:bugzilla.bugmanage");
		
		/**
		 * NS Scheduling Route
		 */		
		from("activemq:topic:nsd.schedule")
		.unmarshal().json( JsonLibrary.Jackson, io.openslice.model.DeploymentDescriptor.class, true)
		.delay(30000)		
		.bean( BugzillaClient.class, "transformNSInstantiation2BugBody")
		.to("direct:bugzilla.bugmanage");	
		
		/**
		 * Automatic NS Instantiation Route Fail
		 */		
		from("activemq:topic:nsd.deployment.instantiation.fail")
		.unmarshal().json( JsonLibrary.Jackson, io.openslice.model.DeploymentDescriptor.class, true)
		.delay(30000)		
		.bean( BugzillaClient.class, "transformNSInstantiation2BugBody")
		.to("direct:bugzilla.bugmanage");	
				
		/**
		 * Automatic NS Termination Route Success
		 */		
		from("activemq:topic:nsd.instance.termination.success")
		.unmarshal().json( JsonLibrary.Jackson, io.openslice.model.DeploymentDescriptor.class, true)
		.delay(30000)		
		.bean( BugzillaClient.class, "transformNSTermination2BugBody")
		.to("direct:bugzilla.bugmanage");	

		/**
		 * Automatic NS Termination Route Fail
		 */		
		from("activemq:topic:nsd.instance.termination.fail")
		.unmarshal().json( JsonLibrary.Jackson, io.openslice.model.DeploymentDescriptor.class, true)
		.delay(30000)		
		.bean( BugzillaClient.class, "transformNSTermination2BugBody")
		.to("direct:bugzilla.bugmanage");	

		/**
		 * Automatic NS Deletion Route Success
		 */		
		from("activemq:topic:nsd.instance.deletion.success")
		.unmarshal().json( JsonLibrary.Jackson, io.openslice.model.DeploymentDescriptor.class, true)
		.delay(30000)		
		.bean( BugzillaClient.class, "transformNSDeletion2BugBody")
		.to("direct:bugzilla.bugmanage");	
		
		/**
		 * Automatic NS Deletion Route Fail
		 */		
		from("activemq:topic:nsd.instance.deletion.fail")
		.unmarshal().json( JsonLibrary.Jackson, io.openslice.model.DeploymentDescriptor.class, true)
		.delay(30000)		
		.bean( BugzillaClient.class, "transformNSDeletion2BugBody")
		.to("direct:bugzilla.bugmanage");	
		
		/**
		 * Reject Deployment Route Issue
		 */
		from("activemq:topic:nsd.deployment.reject")
		.unmarshal().json( JsonLibrary.Jackson, io.openslice.model.DeploymentDescriptor.class, true)
		.delay(30000)		
		.bean( BugzillaClient.class, "transformDeployment2BugBody")
		.to("direct:bugzilla.bugmanage");		

		from("direct:issue.get")
		.setHeader(Exchange.HTTP_METHOD, constant(org.apache.camel.component.http4.HttpMethods.GET))
		.toD( "https4://" + BUGZILLAURL + "/rest.cgi/bug/${header.uuid}?api_key="+ BUGZILLAKEY +"&throwExceptionOnFailure=true");
		
		
		/**
		 * dead Letter Queue if everything fails to connect
		 */
		from("direct:dlq_bugzilla")
		.setBody()
		.body(String.class)
		.to("stream:out");
//		
	}

	Predicate issueExists = new Predicate() {
		
		@Override
		public boolean matches(Exchange exchange) {
			
			Bug aBug = exchange.getIn().getBody( Bug.class );
			Object m = null;
			try{
				FluentProducerTemplate template = exchange.getContext().createFluentProducerTemplate()
					.withHeader("uuid", aBug.getAliasFirst()  )
					.to( "direct:issue.get");
				m = template.request();
			}catch( CamelExecutionException e){
				logger.error( "issueExists: " + e.getMessage() );
				//e.printStackTrace();
			}
			
			if ( m != null )	
			{
				return true;
			}
			else {
				return false;
			}
			
		}
	};

	Processor BugHeaderExtractProcessor = new Processor() {
		
		@Override
		public void process(Exchange exchange) throws Exception {

			Map<String, Object> headers = exchange.getIn().getHeaders(); 
			Bug aBug = exchange.getIn().getBody( Bug.class ); 
		    headers.put("uuid", aBug.getAliasFirst()  );
		    exchange.getOut().setHeaders(headers);
		    
		    //copy Description to Comment
		    aBug.setComment( BugzillaClient.createComment( aBug.getDescription() ) );
		    //delete Description
		    aBug.setDescription( null );
		    aBug.setAlias( null ); //dont put any Alias		
		    aBug.setCc( null );
		    
		    exchange.getOut().setBody( aBug  );
		    // copy attachements from IN to OUT to propagate them
		    //exchange.getOut().setAttachments(exchange.getIn().getAttachments());
			
		}
	};
	
	
	


	public class MyHttpClientConfigurer implements HttpClientConfigurer {

		@Override
		public void configureHttpClient(HttpClientBuilder hc) {
			try {
				SSLContext sslContext;
				sslContext = new SSLContextBuilder().loadTrustMaterial(null, (certificate, authType) -> true).build();

				//hc.setSSLContext(sslContext).setSSLHostnameVerifier(new NoopHostnameVerifier()).build();

				SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory( sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
				hc.setSSLSocketFactory(sslConnectionFactory);
				Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
				        .register("https", sslConnectionFactory)
				        .build();

				HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(registry);

				hc.setConnectionManager(ccm);

			} catch (KeyManagementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (KeyStoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	
}


