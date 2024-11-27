package com.amazonaws.lambda.mihai.healthmetric.test.data;

import static org.mockito.Mockito.when;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;

public class CloudWatchData {

	private static Logger logger = LogManager.getLogger(CloudWatchData.class);
	
	public static void resetCloudWatchData (CloudWatchClient cwClient) {
		
		when(cwClient.putMetricData(Mockito.any(PutMetricDataRequest.class))).thenAnswer(new Answer<String>() {
			
		     public String answer(InvocationOnMock invocation) throws Throwable {
		    	 
		    	String data = null;
		    	 
		    	Object[] args = invocation.getArguments();
		    	
		    	logger.debug("cwClient.putMetricData " + (PutMetricDataRequest)args[0]);
		    	
			    
		    	return data;	    	 
		    	 
		     }
		 });
	}
}
