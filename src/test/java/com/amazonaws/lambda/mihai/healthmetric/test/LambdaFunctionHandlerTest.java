package com.amazonaws.lambda.mihai.healthmetric.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.amazonaws.lambda.mihai.healthmetric.handler.LambdaFunctionHandler;
import com.amazonaws.lambda.mihai.healthmetric.model.FilteredMail;
import com.amazonaws.lambda.mihai.healthmetric.model.MailFilter;
import com.amazonaws.lambda.mihai.healthmetric.model.Utils;
import com.amazonaws.lambda.mihai.healthmetric.service.CloudWatchService;
import com.amazonaws.lambda.mihai.healthmetric.service.S3Service;
import com.amazonaws.lambda.mihai.healthmetric.test.data.CloudWatchData;
import com.amazonaws.lambda.mihai.healthmetric.test.data.S3Data;
import com.amazonaws.lambda.mihai.healthmetric.test.utils.TestContext;
import com.amazonaws.lambda.mihai.healthmetric.test.utils.TestUtils;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.LambdaDestinationEvent;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.lambda.runtime.serialization.PojoSerializer;
import com.amazonaws.services.lambda.runtime.serialization.events.LambdaEventSerializers;

import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * A simple test harness for locally invoking your Lambda function handler.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LambdaFunctionHandlerTest {

	private S3Service s3Service = new S3Service();
	private CloudWatchService cwService = new CloudWatchService();
	
	private S3Client s3Client = Mockito.mock(S3Client.class);
	private CloudWatchClient cwClient =  Mockito.mock(CloudWatchClient.class);
	
	private Map<String, String> vars = new HashMap<String, String>();
	
	private LambdaFunctionHandler handler;
	
    public LambdaFunctionHandlerTest () {
    	handler = new LambdaFunctionHandler(s3Service, cwService);

    	s3Service.setS3Client(s3Client);
    	cwService.setCwClient(cwClient);
    	    	
    }
    
    @BeforeEach
    public void setUp() throws IOException {
       
    	S3Data.resetS3Data(s3Client);
    	CloudWatchData.resetCloudWatchData(cwClient);
    }

    private Context createContext() {
        TestContext ctx = new TestContext();

        ctx.setFunctionName("SiteHealthStatusMetric");

        return ctx;
    }
    
    @Test
    @DisplayName("Ensure correct flow in all layers: offline mail")
    public void testOfflineLambdaFunctionHandler()  throws IOException {
    	
    	SNSEvent event = TestUtils.parse("/sns-event.json", SNSEvent.class);
    	System.out.println("event " + event);
    	
        FilteredMail mail = new FilteredMail();
        mail.setS3Key("jetpack_down.txt");
        mail.setS3Bucket("ses-received-mail-mihaiadam");
        MailFilter filter = new MailFilter();
        mail.setFilter(filter);
        filter.setField("FROM");
        filter.setModifier("CONTAINS");
        filter.setValue("@jetpack");
            	 
        alterPrototypeEvent (event, mail);
    	
    	String response = handler.handleRequest(event, createContext());
    	
    	System.out.println("TEST RESP: " + response);
    	assertEquals("healthy : false", response, "should be unhealthy");

    }
    
    @Test
    @DisplayName("Ensure correct flow in all layers: still offline mail")
    public void testStillOfflineLambdaFunctionHandler()  throws IOException {
    	
    	SNSEvent event = TestUtils.parse("/sns-event.json", SNSEvent.class);
    	
        FilteredMail mail = new FilteredMail();
        mail.setS3Key("jetpack_still_down.txt");
        mail.setS3Bucket("ses-received-mail-mihaiadam");
        MailFilter filter = new MailFilter();
        mail.setFilter(filter);
        filter.setField("FROM");
        filter.setModifier("CONTAINS");
        filter.setValue("@jetpack");
            	
        alterPrototypeEvent (event, mail);
    	
    	String response = handler.handleRequest(event, createContext());
    	
    	System.out.println("TEST RESP: " + response);
    	assertEquals("healthy : false", response, "should be unhealthy");

    }
    
    @Test
    @DisplayName("Ensure correct flow in all layers: online")
    public void testOnlineLambdaFunctionHandler()  throws IOException {
    	
    	SNSEvent event = TestUtils.parse("/sns-event.json", SNSEvent.class);
    	
        FilteredMail mail = new FilteredMail();
        mail.setS3Key("jetpack_back_online.txt");
        mail.setS3Bucket("ses-received-mail-mihaiadam");
        MailFilter filter = new MailFilter();
        mail.setFilter(filter);
        filter.setField("FROM");
        filter.setModifier("CONTAINS");
        filter.setValue("@jetpack");
            	
        alterPrototypeEvent (event, mail);
    	
    	String response = handler.handleRequest(event, createContext());
    	
    	System.out.println("TEST RESP: " + response);
    	assertEquals("healthy : true", response, "should be healthy");

    }
    /**
     * 
     * @param event prototype event
     * @param mail mail data that is injected and changed into event
     */
    private void alterPrototypeEvent (SNSEvent event, FilteredMail mail) {
    	
        String snsMsg = event.getRecords().get(0).getSNS().getMessage();       
        
        InputStream eventStream = new ByteArrayInputStream(snsMsg.getBytes());
  		PojoSerializer<LambdaDestinationEvent> lambdaEventSerializer = LambdaEventSerializers.serializerFor(LambdaDestinationEvent.class, LambdaDestinationEvent.class.getClassLoader());
  		LambdaDestinationEvent lbdDestEvt = lambdaEventSerializer.fromJson(eventStream);
  		lbdDestEvt.setTimestamp(null);
  		lbdDestEvt.setResponsePayload(Utils.getJsonAsObject(Utils.getObjectAsJson(mail), Map.class));
  		
  		//FilteredMail mail = Utils.getJsonAsObject((new ObjectMapper()).writeValueAsString((Map)lbdDestEvt.getResponsePayload()), FilteredMail.class);
  		
    	event.getRecords().get(0).getSNS().setMessage(Utils.getObjectAsJson(lbdDestEvt));
    }
}
