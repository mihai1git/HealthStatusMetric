package com.amazonaws.lambda.mihai.healthmetric.handler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.lambda.mihai.healthmetric.model.FilteredMail;
import com.amazonaws.lambda.mihai.healthmetric.model.MailFilter;
import com.amazonaws.lambda.mihai.healthmetric.model.Utils;
import com.amazonaws.lambda.mihai.healthmetric.service.CloudWatchService;
import com.amazonaws.lambda.mihai.healthmetric.service.S3Service;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.LambdaDestinationEvent;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.lambda.runtime.serialization.PojoSerializer;
import com.amazonaws.services.lambda.runtime.serialization.events.LambdaEventSerializers;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * handler that is used by cloud platform<br>
 * entry point in the application
 * @author mikeaws
 *
 */
public class LambdaFunctionHandler implements RequestHandler<SNSEvent, String> {
	/**
	 * logger named by this class
	 */
	private Logger logger = LogManager.getLogger(LambdaFunctionHandler.class);
	/**
	 * POJO from the Service layer; wrapper for AWS SDK S3 Service client
	 */
	private S3Service s3Service;
	/**
	 * POJO from the Service layer; wrapper for AWS SDK CloudWatch Service client
	 */
	private CloudWatchService cwService;
	/**
	 * constructor used in cloud environment
	 */
    public LambdaFunctionHandler() {
    	s3Service = S3Service.build();
    	cwService = CloudWatchService.build();
    	
    }

    /**
     * constructor used only in local test environment
     * @param s3Service {@link com.amazonaws.lambda.mihai.healthmetric.service.S3Service}
     * @param cwService {@link com.amazonaws.lambda.mihai.healthmetric.service.CloudWatchService}
     */
    public LambdaFunctionHandler(
    		S3Service s3Service, 
    		CloudWatchService cwService) {
    	
    	this.s3Service = s3Service;
    	this.cwService = cwService;
    	
    }
    /**
     * entry point in this handler class; invoked by Lambda Service in cloud environment 
     */
    @Override
    public String handleRequest(SNSEvent event, Context context) {
    	//logger.debug("SNSEvent: " + event);
        String snsMsg = event.getRecords().get(0).getSNS().getMessage();       
        
        InputStream eventStream = new ByteArrayInputStream(snsMsg.getBytes());
  		PojoSerializer<LambdaDestinationEvent> lambdaEventSerializer = LambdaEventSerializers.serializerFor(LambdaDestinationEvent.class, LambdaDestinationEvent.class.getClassLoader());
  		LambdaDestinationEvent lbdDestEvt = lambdaEventSerializer.fromJson(eventStream);
  		
           	
    	try {
      		FilteredMail mail = Utils.getJsonAsObject((new ObjectMapper()).writeValueAsString((Map)lbdDestEvt.getResponsePayload()), FilteredMail.class);

            logger.debug("mail info: " + mail);
            
            if (!validMessage(mail)) {
            	throw new RuntimeException("Filtered mail key not for this processor !!!");
            }
            
            String bucket = mail.getS3Bucket();
            String key = mail.getS3Key();
            
	    	String emlFile = s3Service.getMailEMLFile(bucket, key);
	        
	        
	        Properties props = new Properties();
	        Session mailSession = Session.getDefaultInstance(props);
	        InputStream inputStream = new ByteArrayInputStream(emlFile.getBytes(StandardCharsets.UTF_8));
	        MimeMessage message = new MimeMessage(mailSession, inputStream);
	        
	        logger.debug("Subject : " + message.getSubject());
	        logger.debug("From : " + Arrays.toString(message.getFrom()));
	        logger.debug("Body : " +  message.getContent());
	        
	        String contentType = message.getContentType();
	        if (contentType.contains("multipart")) {
	        	Multipart multiPart = (Multipart) message.getContent();
	        	logger.debug("Multipart EMail File with Parts:::" + multiPart.getCount());
	        }
	        
	        String body = MailFilter.extractTextContent(message);
	        
	        //format: "Error reference: <site number>/<status>" 
	        //status in (server, blocked, client, intermittent, redirection, success, unknown)
	        Boolean statusHealthy = Boolean.TRUE;
	        if (body.contains("Error reference: 214785651/success")) {
	        	statusHealthy = Boolean.TRUE;
	        		        	
	        } else if (body.contains("Error reference: 214785651/")) {
	        	statusHealthy = Boolean.FALSE;
	        	
	        } else {
	        	statusHealthy = Boolean.TRUE;
	        }
	        
	        cwService.putHealthStatusData(statusHealthy);
	                    
            return "healthy : " + statusHealthy;
    	} catch (Exception e) {
            e.printStackTrace();            
            throw new RuntimeException(e);
        }
    }
    /**
     * 
     * @param mailInfo event data for analysed mail
     * @return true if event is valid: is for a mail that came from website monitor
     */
    public Boolean validMessage(FilteredMail mailInfo) {
    	Boolean valid = Boolean.TRUE;
    	
    	if (!(MailFilter.FILTER_FIELD_FROM.equals(mailInfo.getFilter().getField())
    			&& MailFilter.FILTER_MODIFIER_CONTAINS.equals(mailInfo.getFilter().getModifier())
    			&& "@jetpack".equals(mailInfo.getFilter().getValue()))) {
    		
    		valid = Boolean.FALSE;
    	}
    	
    	return valid;
    }

}
