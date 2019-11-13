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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.camel.CamelContext;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.openslice.bugzilla.model.Bug;
import io.openslice.bugzilla.model.Comment;
import io.openslice.bugzilla.model.User;
import io.openslice.model.DeploymentDescriptor;
import io.openslice.model.DeploymentDescriptorStatus;
import io.openslice.model.DeploymentDescriptorVxFPlacement;
import io.openslice.model.ExperimentMetadata;
import io.openslice.model.ExperimentOnBoardDescriptor;
import io.openslice.model.IMANOCommunicationStatus;
import io.openslice.model.OnBoardingStatus;
import io.openslice.model.PortalUser;
import io.openslice.model.VFImage;
import io.openslice.model.ValidationJob;
import io.openslice.model.ValidationStatus;
import io.openslice.model.VxFMetadata;
import io.openslice.model.VxFOnBoardedDescriptor;



/**
 * @author ctranoris
 *
 */
@Configuration
@RefreshScope
public class BugzillaClient {

	private static final transient Log logger = LogFactory.getLog(BugzillaClient.class.getName());


	static CamelContext contxt;

	@Value("${portaltitle}")
	private String PORTAL_TITLE = "";
	
	@Value("${maindomain}")
	private String MAIN_CFS_URL;


	@Value("${main_operations_product}")
	private String MAIN_OPERATIONS_PRODUCT;
	
	
	/** */
	private static final String BUGHEADER =   "THIS IS AN AUTOMATED ISSUE CREATED BY OPENSLICE SERVICES.\n"
											+ "*********************************************************\n";

	
	@Autowired
	public void setActx(CamelContext actx) {
		BugzillaClient.contxt = actx;
		logger.info( "BugzillaClient configure() contxt = " + contxt);
	}
	

	/**
	 * Request to the activeMQ channel to get VxF metadata
	 * @param vxfid
	 * @return
	 */
	private VxFMetadata getVxFFromID(long vxfid) {
		ProducerTemplate template = contxt.createProducerTemplate();
		String ret = template
				.requestBody( "activemq:queue:getVxFByID", vxfid , String.class);

		VxFMetadata vxf;
		try {
			ObjectMapper mapper = new ObjectMapper();
			vxf = mapper.readValue( ret, VxFMetadata.class);
			return vxf;
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		
		return null;
	}
	
	
	/**
	 * Request to the activeMQ channel to get VxF metadata
	 * @param vxfid
	 * @return
	 */
	private ExperimentMetadata getNSDFromID(long nsdid) {
		ProducerTemplate template = contxt.createProducerTemplate();
		String ret = template
				.requestBody( "activemq:queue:getNSDByID", nsdid , String.class);

		ExperimentMetadata nsd;
		try {
			ObjectMapper mapper = new ObjectMapper();
			nsd = mapper.readValue( ret, ExperimentMetadata.class);
			return nsd;
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		
		return null;
	}

	public static Comment createComment( String description ) {
		
		Comment c = new Comment();
		c.setComment(description);
		c.setIs_markdown( false );
		c.setIs_private( false );	
		return c;
	}
	
	public static PortalUser getPortalUser() {
		PortalUser u  = new PortalUser();
		u.setName("kokos");
		u.setEmail("lalalkis");
		return u;
	}
		
	public static User transformUser2BugzillaUser( final PortalUser portalUser ){
		
		//PortalUser portalUser = portalRepositoryRef.getUserByID(portaluserid);
		User u = new User();
		u.setEmail( portalUser.getEmail()  );
		u.setFullName( portalUser.getName() );
		u.setPassword( UUID.randomUUID().toString() ); //no password. The user needs to reset it in the other system (e.g. Bugzilla)
		

		logger.info( "In : portaluser getName = " + portalUser.getName() );
		
		
		return u;
		
	}
	
	
	public Bug transformNSInstantiation2BugBody( DeploymentDescriptor descriptor ) {
		

		String product = MAIN_OPERATIONS_PRODUCT;
		String component = "Operations Support" ;
		String summary = "[PORTAL] Deployment Request of NSD:" + descriptor.getExperiment().getName() + ",User: " + descriptor.getOwner().getUsername();
		String alias = descriptor.getUuid() ;

		String description = getDeploymentDescription( descriptor );		
		
		String status= "CONFIRMED";
		String resolution = null;
		
		if ( ( descriptor.getStatus() == DeploymentDescriptorStatus.TERMINATED ) || ( descriptor.getStatus() == DeploymentDescriptorStatus.COMPLETED ) || ( descriptor.getStatus() == DeploymentDescriptorStatus.RUNNING )) {
			status = "RESOLVED";
			resolution = "FIXED";
		} else if ( ( descriptor.getStatus() == DeploymentDescriptorStatus.REJECTED ) || ( descriptor.getStatus() == DeploymentDescriptorStatus.FAILED )) {
			status = "RESOLVED";
			resolution = "INVALID";
		}
		
		
		Bug b = createBug(product, component, summary, alias, description, descriptor.getOwner().getEmail(), status, resolution);
		// Create the email and assign it to the mentor.
		b.setAssignedTo(descriptor.getMentor().getEmail());
		return b;		
	}

	public Bug transformNSTermination2BugBody( DeploymentDescriptor descriptor ) {

		logger.debug("transformNSTermination2BugBody"+descriptor.toString());
		
		String product = MAIN_OPERATIONS_PRODUCT;
		String component = "Operations Support" ;
		String summary = "[PORTAL] Deployment Request of NSD:" + descriptor.getExperiment().getName() + ",User: " + descriptor.getOwner().getUsername();
		String alias = descriptor.getUuid() ;

		String description = getDeploymentDescription( descriptor );		
		
		String status= "CONFIRMED";
		String resolution = null;
		
		if ( ( descriptor.getStatus() == DeploymentDescriptorStatus.COMPLETED ) ) {
			status = "RESOLVED";
			resolution = "FIXED";
		} else if ( ( descriptor.getStatus() == DeploymentDescriptorStatus.TERMINATION_FAILED ) || ( descriptor.getStatus() == DeploymentDescriptorStatus.FAILED )) {
			status = "RESOLVED";
			resolution = "INVALID";
		}
		
		
		Bug b = createBug(product, component, summary, alias, description, descriptor.getOwner().getEmail(), status, resolution);
		return b;
		
	}
		
	public Bug transformNSDeletion2BugBody( DeploymentDescriptor descriptor ) {


		
		String product = MAIN_OPERATIONS_PRODUCT;
		String component = "Operations Support" ;
		String summary = "[PORTAL] Deployment Request of NSD:" + descriptor.getExperiment().getName() + ",User: " + descriptor.getOwner().getUsername();
		String alias = descriptor.getUuid() ;

		String description = getDeploymentDescription( descriptor );		
		
		String status= "CONFIRMED";
		String resolution = null;
		
		if ( ( descriptor.getStatus() == DeploymentDescriptorStatus.COMPLETED || descriptor.getStatus() == DeploymentDescriptorStatus.FAILED_OSM_REMOVED ) ) {
			status = "RESOLVED";
			resolution = "FIXED";
		} else if ( descriptor.getStatus() == DeploymentDescriptorStatus.DELETION_FAILED ) {
			status = "RESOLVED";
			resolution = "INVALID";
		}		
		
		Bug b = createBug(product, component, summary, alias, description, descriptor.getOwner().getEmail(), status, resolution);
		return b;
		
	}
		
	public Bug transformDeployment2BugBody( final DeploymentDescriptor descriptor) {
		logger.debug("transformDeployment2BugBody"+descriptor.toString());
		
		String product = MAIN_OPERATIONS_PRODUCT;
		String component = "Operations Support" ;
		String summary = "[PORTAL] Deployment Request of NSD:" + descriptor.getExperiment().getName() + ",User: " + descriptor.getOwner().getUsername();
		String alias = descriptor.getUuid() ;

		String description = getDeploymentDescription( descriptor );		
		
		String status= "CONFIRMED";
		String resolution = null;
		if ( ( descriptor.getStatus() == DeploymentDescriptorStatus.SCHEDULED ) || ( descriptor.getStatus() == DeploymentDescriptorStatus.INSTANTIATING ) || ( descriptor.getStatus() == DeploymentDescriptorStatus.RUNNING )) {
			status = "IN_PROGRESS";
			component = "NSD Deployment Request" ;
		} else  if ( ( descriptor.getStatus() == DeploymentDescriptorStatus.COMPLETED ) ) {
			status = "RESOLVED";
			resolution = "FIXED";
		} else if ( ( descriptor.getStatus() == DeploymentDescriptorStatus.REJECTED ) || ( descriptor.getStatus() == DeploymentDescriptorStatus.FAILED )) {
			status = "RESOLVED";
			resolution = "INVALID";
		}
		
		
		Bug b = createBug(product, component, summary, alias, description, descriptor.getOwner().getEmail(), status, resolution);
		// Create the email and assign it to the mentor.
		if ( descriptor.getMentor() != null) {
			b.setAssignedTo(descriptor.getMentor().getEmail());
		}
		return b;
	}
	
	
	public Comment transformDeployment2BugComment(  DeploymentDescriptor descriptor ) {
		
		String description = getDeploymentDescription( descriptor );
				
		Comment b = createComment( description);
		
		return b;
	}
	
	
	/**
	 * @param descriptor
	 * @return
	 */
	private String getDeploymentDescription( DeploymentDescriptor descriptor ) {

		StringBuilder description =  new StringBuilder( BUGHEADER );

		description.append( "\nSTATUS: " + descriptor.getStatus() + "\n");
		if ( descriptor.getStartDate() != null ) {
			description.append( "\nFeedback: " + descriptor.getFeedback() );
			description.append("\nScheduled Start Date: " + descriptor.getStartDate().toString() );
			description.append( "\nScheduled End Date: " + descriptor.getEndDate().toString() );
		} else {
			description.append( "\nNOT YET SCHEDULED \n");			
		}
		
		
		description.append(
						"\nDeployment Request by user :" + descriptor.getOwner().getUsername() 
						+"\nHere are the details:\n"
						+ "\nExperiment name: " + descriptor.getName() 
						+ "\nDescription: " + descriptor.getDescription() 
						+ "\nDate Created: " + descriptor.getDateCreated().toString() 
						+ "\nRequested Tentative Start date: " + descriptor.getStartReqDate().toString() 
						+ "\nRequested Tentative End date: " + descriptor.getEndReqDate().toString() 
						+ "\nExperiment (NSD) requested: " + descriptor.getExperiment().getName() );
		
		
		if ( descriptor.getMentor() != null ) {
			description.append( "\nMentor: " + descriptor.getMentor().getName() + ", " + descriptor.getMentor().getOrganization() ) ;
		}
		

		description.append( "\nConstituent VxF Placement " ) ;
		for (DeploymentDescriptorVxFPlacement pl : descriptor.getVxfPlacements()) {
			if (  ( pl.getConstituentVxF().getVxfref() != null ) && ( pl.getInfrastructure() != null )) {
				description.append( "\n  Constituent VxF: " + pl.getConstituentVxF().getVxfref().getName() + " - Infrastructure: " + pl.getInfrastructure().getName() );
			}
		}
		
		
				
						 
		description.append( "\n*************************************************\n");
		description.append( "\nTo manage this Request, go to: " + MAIN_CFS_URL + "/#!/edit_deployment/" + descriptor.getId() ); 
		return description.toString();
	}



	
	
	/**
	 * @param product
	 * @param component
	 * @param summary
	 * @param alias
	 * @param description
	 * @param ccemail
	 * @return
	 */
	public Bug createBug(String product, String component, String summary, String alias, String description, String ccemail, String status, String resolution ) {
		
		Bug b = new Bug();
		b.setProduct(product);
		b.setComponent(component);
		b.setSummary(summary);
		b.setVersion( "unspecified" );
		List<Object> aliaslist = new ArrayList<>();
		aliaslist.add(alias);		
		b.setAlias( aliaslist );
		List<String> cclist = new ArrayList<>();
		cclist.add( ccemail );		
		b.setCc(cclist); 
		b.setDescription(description.toString());		
		b.setStatus(status);
		b.setResolution(resolution);
				
		return b;
	}
	
	



	public Bug transformVxFValidation2BugBody( VxFMetadata vxf  ) {
		
		logger.info( "In transformVxFValidation2BugBody: alias = " + vxf.getUuid());
		String product = MAIN_OPERATIONS_PRODUCT;
		String component = "Validation" ;
		String summary = "[PORTAL] Validation Request for VxF:" + vxf.getName() + ", Owner: " + vxf.getOwner().getUsername();
		String alias = vxf.getUuid() ;
		
		StringBuilder description =  new StringBuilder( BUGHEADER );
		
		description.append( "\n\n VxF: " + vxf.getName());
		description.append( "\n Owner: " +  vxf.getOwner().getUsername() );
		description.append( "\n Vendor: " +  vxf.getVendor() );
		description.append( "\n Version: " + vxf.getVersion() );
		description.append( "\n Archive: " + vxf.getPackageLocation() );
		description.append( "\n UUID: " + vxf.getUuid()  );
		description.append( "\n ID: " + vxf.getId()   );
		description.append( "\n Date Created: " + vxf.getDateCreated().toString()   );
		description.append( "\n Date Updated: " + vxf.getDateUpdated().toString()   );

		description.append( "\n VDU Images: "    );
		for (VFImage img : vxf.getVfimagesVDU() ) {
			description.append( "\n\t Image: " + img.getName() + ", " + MAIN_CFS_URL + "/#!/vfimage_view/" + img.getId()    );
			
		}

		description.append( "\n"    );
		description.append( "\n Validation Status: " + vxf.getValidationStatus()  );
		//description.append( "\n Certified: " + String.valueOf( vxf.isCertified() ).toUpperCase() );
		
		description.append( "\n Validation jobs: "    );
		for (ValidationJob j : vxf.getValidationJobs()) {
			description.append( "\n\t" + j.getDateCreated().toString() + ", id:" + j.getJobid() + ", Status:" + j.getValidationStatus() +  ", Output:" + j.getOutputLog()   );
		}
		 
		description.append( "\n\n*************************************************\n");
		description.append( "\nTo manage this , go to: " + MAIN_CFS_URL + "/#!/vxf_edit/" + vxf.getId() ); 
		
		String status= "CONFIRMED";
		String resolution = null;
		if ( vxf.getValidationStatus().equals( ValidationStatus.UNDER_REVIEW ) )  {
			status = "CONFIRMED";
		} else  if (  vxf.getValidationStatus().equals( ValidationStatus.COMPLETED ) )  {
			status = "RESOLVED";
			resolution = "FIXED";
		}
		
		
		Bug b = createBug(product, component, summary, alias, description.toString(), vxf.getOwner().getEmail(), status, resolution);
		
		return b;
	}
	
	public Bug transformVxFAutomaticOnBoarding2BugBody( final VxFOnBoardedDescriptor obd ) {
		
		VxFOnBoardedDescriptor vxfobd = obd;
		
		logger.info( "In transformVxFAutomaticOnBoarding2BugBody: alias = " + vxfobd.getUuid());
		
		
		
		VxFMetadata avxf = getVxFFromID( vxfobd.getVxfid() );
				
		if ( avxf==null ) {
			logger.error( "Cannot retrieve VxF for vxf ID = " + vxfobd.getVxfid() );
			return null;
		}
		vxfobd.setVxf(avxf);

		String product = MAIN_OPERATIONS_PRODUCT;
		String component = "Onboarding" ;
		String summary = "[PORTAL] OSM OnBoarding Action for VxF:" + vxfobd.getVxf().getName() + ", Owner: " + vxfobd.getVxf().getOwner().getUsername();
		String alias = vxfobd.getUuid() ;
		
		StringBuilder description =  new StringBuilder( "**************************************************************\n"
				+ "THIS IS AN AUTOMATED ISSUE UPDATE CREATED BY PORTAL API.\n"
				+ "**************************************************************\n"
				+ " VxF OSM ONBOARDING ACTION \n"
				+ "**************************************************************\n");
		
		description.append( "\n\n VxF: " + vxfobd.getVxf().getName());
		description.append( "\n Owner: " +  vxfobd.getVxf().getOwner().getUsername() );
		description.append( "\n Vendor: " +  vxfobd.getVxf().getVendor() );
		description.append( "\n Version: " + vxfobd.getVxf().getVersion() );
		description.append( "\n Archive: " + vxfobd.getVxf().getPackageLocation() );
		description.append( "\n UUID: " + vxfobd.getVxf().getUuid()  );
		description.append( "\n ID: " + vxfobd.getVxf().getId()   );
		description.append( "\n Date Created: " + vxfobd.getVxf().getDateCreated().toString()   );
		description.append( "\n Date Updated: " + vxfobd.getVxf().getDateUpdated().toString()   );


		description.append( "\n" );
		description.append( "\n VxF OnBoarding Status: " + vxfobd.getOnBoardingStatus()  );
		description.append( "\n VxF OnBoarding Feedback: " + vxfobd.getFeedbackMessage()  );
		description.append( "\n Last Onboarding: " + vxfobd.getLastOnboarding());
		description.append( "\n Last Onboarding Deploy ID: " + vxfobd.getDeployId());
		description.append( "\n Onboarding MANO provider: " + vxfobd.getObMANOprovider().getName() );
		 
		description.append( "\n\n*************************************************\n");
		description.append( "\nTo manage this , go to: " + MAIN_CFS_URL + "/#!/vxf_edit/" + vxfobd.getVxf().getId() ); 
		
		String status= "CONFIRMED";
		String resolution = null;
		if ( vxfobd.getOnBoardingStatus().equals( OnBoardingStatus.ONBOARDED ) )  {
			status = "RESOLVED";
			resolution = "FIXED";
		} else  if ( vxfobd.getOnBoardingStatus().equals( OnBoardingStatus.FAILED ) ) {
//			status = "CONFIRMED";
			status = "RESOLVED";
			resolution = "INVALID";
		}		
		
		Bug b = createBug(product, component, summary, alias, description.toString(), vxfobd.getVxf().getOwner().getEmail(), status, resolution);
		return b;
	}
	
		
	
	


	public Bug transformNSDValidation2BugBody(ExperimentMetadata nsd) {
		logger.info( "In transformNSDValidation2BugBody: alias = " + nsd.getUuid());

		String product = MAIN_OPERATIONS_PRODUCT;
		String component = "Validation" ;
		String summary = "[PORTAL] Validation Request for NSD:" + nsd.getName() + ", Owner: " + nsd.getOwner().getUsername();
		String alias = nsd.getUuid() ;
		
		StringBuilder description =  new StringBuilder( BUGHEADER );
		description.append( "\n Validation Status: " + nsd.getValidationStatus()  );
		description.append( "\n Valid: " + String.valueOf( nsd.isValid() ).toUpperCase() );
		
		description.append( "\n\n NSD: " + nsd.getName());
		description.append( "\n Owner: " +  nsd.getOwner().getUsername() );
		description.append( "\n Vendor: " +  nsd.getVendor() );
		description.append( "\n Version: " + nsd.getVersion() );
		description.append( "\n Archive: " + nsd.getPackageLocation() );
		description.append( "\n UUID: " + nsd.getUuid()  );
		description.append( "\n ID: " + nsd.getId()   );
		description.append( "\n Date Created: " + nsd.getDateCreated().toString() );
		description.append( "\n Date Updated: " + nsd.getDateUpdated().toString() );
		

		 
		description.append( "\n\n*************************************************\n");
		description.append( "\nTo manage this , go to: " + MAIN_CFS_URL + "/#!/experiment_edit/" + nsd.getId() ); 
		
		String status= "CONFIRMED";
		String resolution = null;
		if ( nsd.getValidationStatus().equals( ValidationStatus.UNDER_REVIEW ) )  {
			status = "IN_PROGRESS";
		} else  if ( nsd.isValid()  &&  ( nsd.getValidationStatus().equals( ValidationStatus.COMPLETED ) ) ) {
			status = "RESOLVED";
			resolution = "FIXED";
		} else  if ( !nsd.isValid()  &&  ( nsd.getValidationStatus().equals( ValidationStatus.COMPLETED ) ) ) {
			status = "RESOLVED";
			resolution = "INVALID";
		}
		
		
		Bug b = createBug(product, component, summary, alias, description.toString(), nsd.getOwner().getEmail(), status, resolution);
		
		return b;
	}
	
	public  Bug transformNSDAutomaticOnBoarding2BugBody( ExperimentOnBoardDescriptor obd ) {
		
		ExperimentOnBoardDescriptor uexpobd = obd;
		
		logger.info( "In transformNSDAutomaticOnBoarding2BugBody: alias = " + uexpobd.getUuid());
		ExperimentMetadata ansd = getNSDFromID( uexpobd.getExperimentid() );
		
		if ( ansd==null ) {
			logger.error( "Cannot retrieve NSD for vxf ID = " + uexpobd.getExperimentid() );
			return null;
		}
		uexpobd.setExperiment(ansd);
		
		
		String product = MAIN_OPERATIONS_PRODUCT;
		String component = "Onboarding" ;
		String summary = "[PORTAL] OSM OnBoarding Action for NSD:" + uexpobd.getExperiment().getName() + ", Owner: " + uexpobd.getExperiment().getOwner().getUsername();
		String alias = uexpobd.getUuid() ;
		
		StringBuilder description =  new StringBuilder( "**************************************************************\n"
				+ "THIS IS AN AUTOMATED ISSUE UPDATE CREATED BY PORTAL API.\n"
				+ "**************************************************************\n"
				+ " NSD OSM ONBOARDING ACTION \n"
				+ "**************************************************************\n");
		
		description.append( "\n\n NSD: " + uexpobd.getExperiment().getName());
		description.append( "\n Owner: " +  uexpobd.getExperiment().getOwner().getUsername() );
		description.append( "\n Vendor: " +  uexpobd.getExperiment().getVendor() );
		description.append( "\n Version: " + uexpobd.getExperiment().getVersion() );
		description.append( "\n Archive: " + uexpobd.getExperiment().getPackageLocation() );
		description.append( "\n UUID: " + uexpobd.getExperiment().getUuid()  );
		description.append( "\n ID: " + uexpobd.getExperiment().getId()   );
		description.append( "\n Date Created: " + uexpobd.getExperiment().getDateCreated().toString()   );
		description.append( "\n Date Updated: " + uexpobd.getExperiment().getDateUpdated().toString()   );


		description.append( "\n" );
		description.append( "\n NSD OnBoarding Status: " + uexpobd.getOnBoardingStatus()  );
		description.append( "\n NSD OnBoarding Feedback: " + uexpobd.getFeedbackMessage()  );
		description.append( "\n Last Onboarding: " + uexpobd.getLastOnboarding());
		description.append( "\n Last Onboarding Deploy ID: " + uexpobd.getDeployId());
		description.append( "\n Onboarding MANO provider: " + uexpobd.getObMANOprovider().getName() );
		 
		description.append( "\n\n*************************************************\n");
		description.append( "\nTo manage this , go to: " + MAIN_CFS_URL + "/#!/experiment_edit/" + uexpobd.getExperiment().getId() ); 
		
		String status= "CONFIRMED";
		String resolution = null;
		if ( uexpobd.getOnBoardingStatus().equals( OnBoardingStatus.ONBOARDED ) )  {
			status = "RESOLVED";
			resolution = "FIXED";
		} else  if ( uexpobd.getOnBoardingStatus().equals( OnBoardingStatus.FAILED ) ) {
			status = "RESOLVED";
			resolution = "INVALID";
		}
		
		
		Bug b = createBug(product, component, summary, alias, description.toString(), uexpobd.getExperiment().getOwner().getEmail(), status, resolution);
		return b;
	}

	
	public Bug transformVxFAutomaticOffBoarding2BugBody( VxFOnBoardedDescriptor obd ) {


		VxFOnBoardedDescriptor vxfobd = obd;
		logger.info( "In transformVxFAutomaticOnBoarding2BugBody: alias = " + vxfobd.getUuid());

		VxFMetadata avxf = getVxFFromID( vxfobd.getVxfid() );
		if ( avxf==null ) {
			logger.error( "Cannot retrieve VxF for vxf ID = " + vxfobd.getVxfid() );
			return null;
		}
		vxfobd.setVxf(avxf);
		
		String product = MAIN_OPERATIONS_PRODUCT;
		String component = "Offboarding" ;
		String summary = "[PORTAL] OSM OffBoarding Action for VxF:" + vxfobd.getVxf().getName() + ", Owner: " + vxfobd.getVxf().getOwner().getUsername();
		String alias = vxfobd.getUuid() ;
		
		StringBuilder description =  new StringBuilder( "**************************************************************\n"
				+ "THIS IS AN AUTOMATED ISSUE UPDATE CREATED BY PORTAL API.\n"
				+ "**************************************************************\n"
				+ " VxF OSM OFFBOARDING ACTION \n"
				+ "**************************************************************\n");
		
		description.append( "\n\n VxF: " + vxfobd.getVxf().getName());
		description.append( "\n Owner: " +  vxfobd.getVxf().getOwner().getUsername() );
		description.append( "\n Vendor: " +  vxfobd.getVxf().getVendor() );
		description.append( "\n Version: " + vxfobd.getVxf().getVersion() );
		description.append( "\n Archive: " + vxfobd.getVxf().getPackageLocation() );
		description.append( "\n UUID: " + vxfobd.getVxf().getUuid()  );
		description.append( "\n ID: " + vxfobd.getVxf().getId()   );
		description.append( "\n Date Created: " + vxfobd.getVxf().getDateCreated().toString()   );
		description.append( "\n Date Updated: " + vxfobd.getVxf().getDateUpdated().toString()   );


		description.append( "\n" );
		description.append( "\n VxF OffBoarding Status: " + vxfobd.getOnBoardingStatus()  );
		description.append( "\n VxF OffBoarding Feedback: " + vxfobd.getFeedbackMessage()  );
		 
		description.append( "\n\n*************************************************\n");
		description.append( "\nTo manage this , go to: " + MAIN_CFS_URL + "/#!/vxf_edit/" + vxfobd.getVxf().getId() ); 
		
		String status= "CONFIRMED";
		String resolution = null;
		if ( vxfobd.getOnBoardingStatus().equals( OnBoardingStatus.OFFBOARDED ) )  {
			status = "RESOLVED";
			resolution = "FIXED";
		} else  if ( vxfobd.getOnBoardingStatus().equals( OnBoardingStatus.FAILED ) ) {
			status = "CONFIRMED";
//			status = "RESOLVED";
//			resolution = "INVALID";
		}		
		
		Bug b = createBug(product, component, summary, alias, description.toString(), vxfobd.getVxf().getOwner().getEmail(), status, resolution);
		return b;
	}
	
	public Bug transformNSDAutomaticOffBoarding2BugBody( ExperimentOnBoardDescriptor obd ) {


		ExperimentOnBoardDescriptor uexpobd = obd;
		ExperimentMetadata ansd = getNSDFromID( uexpobd.getExperimentid() );
		
		if ( ansd==null ) {
			logger.error( "Cannot retrieve NSD for vxf ID = " + uexpobd.getExperimentid() );
			return null;
		}
		uexpobd.setExperiment(ansd);
		
		String product = MAIN_OPERATIONS_PRODUCT;
		String component = "Offboarding" ;
		String summary = "[PORTAL] OSM OffBoarding Action for NSD:" + uexpobd.getExperiment().getName() + ", Owner: " + uexpobd.getExperiment().getOwner().getUsername();
		String alias = uexpobd.getUuid() ;
		
		StringBuilder description =  new StringBuilder( "**************************************************************\n"
				+ "THIS IS AN AUTOMATED ISSUE UPDATE CREATED BY PORTAL API.\n"
				+ "**************************************************************\n"
				+ " NSD OSM OFFBOARDING ACTION \n"
				+ "**************************************************************\n");
		
		description.append( "\n\n NSD: " + uexpobd.getExperiment().getName());
		description.append( "\n Owner: " +  uexpobd.getExperiment().getOwner().getUsername() );
		description.append( "\n Vendor: " +  uexpobd.getExperiment().getVendor() );
		description.append( "\n Version: " + uexpobd.getExperiment().getVersion() );
		description.append( "\n Archive: " + uexpobd.getExperiment().getPackageLocation() );
		description.append( "\n UUID: " + uexpobd.getExperiment().getUuid()  );
		description.append( "\n ID: " + uexpobd.getExperiment().getId()   );
		description.append( "\n Date Created: " + uexpobd.getExperiment().getDateCreated().toString()   );
		description.append( "\n Date Updated: " + uexpobd.getExperiment().getDateUpdated().toString()   );


		description.append( "\n" );
		description.append( "\n NSD OffBoarding Status: " + uexpobd.getOnBoardingStatus()  );
		description.append( "\n NSD OffBoarding Feedback: " + uexpobd.getFeedbackMessage()  );
		 
		description.append( "\n\n*************************************************\n");
		description.append( "\nTo manage this , go to: " + MAIN_CFS_URL + "/#!/experiment_edit/" + uexpobd.getExperiment().getId() ); 
		
		String status= "CONFIRMED";
		String resolution = null;
		if ( uexpobd.getOnBoardingStatus().equals( OnBoardingStatus.OFFBOARDED ) )  {
			status = "RESOLVED";
			resolution = "FIXED";
		} else  if ( uexpobd.getOnBoardingStatus().equals( OnBoardingStatus.FAILED ) ) {
			status = "RESOLVED";
			resolution = "INVALID";
		}
		
		
		Bug b = createBug(product, component, summary, alias, description.toString(), uexpobd.getExperiment().getOwner().getEmail(), status, resolution);
		return b;
	}

	

	public Bug transformOSMCommunicationFail2BugBody() {
		String product = MAIN_OPERATIONS_PRODUCT;
		String component = "Operations Support" ;
		String summary = "[PORTAL] OSM Communication Action";
		
		StringBuilder description =  new StringBuilder( "**************************************************************\n"
				+ "THIS IS AN AUTOMATED ISSUE UPDATE CREATED BY PORTAL API.\n"
				+ "**************************************************************\n"
				+ " OSM5 COMMUNICATION ACTION FAILURE\n"
				+ "**************************************************************\n");

		description.append( "\n\n "+ IMANOCommunicationStatus.getMessage());
				 		
		String status= "CONFIRMED";
		String resolution = null;		
		
		Bug b = createBug(product, component, summary, IMANOCommunicationStatus.getOsmCommunicationStatusUUID(), description.toString(), null, status, resolution);
		return b;
	}
		
	public Bug transformOSMCommunicationSuccess2BugBody() {
		String product = MAIN_OPERATIONS_PRODUCT;
		String component = "Operations Support" ;
		String summary = "[PORTAL] OSM Communication Action";
		
		StringBuilder description =  new StringBuilder( "**************************************************************\n"
				+ "THIS IS AN AUTOMATED ISSUE UPDATE CREATED BY PORTAL API.\n"
				+ "**************************************************************\n"
				+ " OSM5 COMMUNICATION ACTION RESTORED\n"
				+ "**************************************************************\n");

		description.append( "\n\n "+ IMANOCommunicationStatus.getMessage());
				 		
		String status = "RESOLVED";
		String resolution = "FIXED";
		
		Bug b = createBug(product, component, summary, IMANOCommunicationStatus.getOsmCommunicationStatusUUID(), description.toString(), null, status, resolution);
		return b;
	}
		
}
