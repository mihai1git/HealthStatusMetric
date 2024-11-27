package com.amazonaws.lambda.mihai.healthmetric.test.data;

import static org.mockito.Mockito.when;

import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.amazonaws.lambda.mihai.healthmetric.test.utils.TestUtils;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

public class S3Data {
	private static Logger logger = LogManager.getLogger(S3Data.class);
	
	public static void resetS3Data (S3Client s3Client) {
					
		when(s3Client.getObjectAsBytes(Mockito.any(GetObjectRequest.class))).thenAnswer(new Answer<ResponseBytes<GetObjectResponse>>() {
			
		     public ResponseBytes<GetObjectResponse> answer(InvocationOnMock invocation) throws Throwable {
		    	 
		    	InputStream data = null;
		    	 		    	 
		    	Object[] args = invocation.getArguments();
		    	
		    	logger.debug("s3Client.getObjectAsBytes " + (GetObjectRequest)args[0]);
		    			    	
		    	data = TestUtils.readStreamFromProjectResource("src/test/resources/" + ((GetObjectRequest)args[0]).key());
		    	
		    	
		    	return ResponseBytes.fromInputStream(GetObjectResponse.builder().build(), data);     	 
		    	 
		     }
		 });

	}
}
