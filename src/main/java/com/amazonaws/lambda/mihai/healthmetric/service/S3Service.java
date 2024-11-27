package com.amazonaws.lambda.mihai.healthmetric.service;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.lambda.mihai.healthmetric.aspect.TraceAll;

import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

/**
 * layer between lambda logic and S3 filesystem; files are treated as objects
 * @author Mihai ADAM
 *
 */
@TraceAll
public class S3Service {

	/** 
	 * AWS client for S3 Service 
	 */
    private S3Client s3Client;
	/**
	 * logger named by this class
	 */
    private Logger logger = LogManager.getLogger(S3Service.class);
    
	/**
	 * builds an instance of this Service on each invocation
	 * @return a new instance of this Service 
	 */	  
    public static S3Service build() {
    	
    	S3Client s3Client = S3Client.builder()
    			.region(Region.US_WEST_2)
    			.build();
    	
    	S3Service s3dao = new S3Service();
    	s3dao.setS3Client(s3Client);
    	
    	return s3dao;
    }

    /**
     * 
     * @param bucketName where the email file is stored
     * @param key name of the email file
     * @return email file as String
     * @throws IOException when error while email read
     */
    public String getMailEMLFile (String bucketName, String key) throws IOException {
    	
    	GetObjectRequest objectRequest = GetObjectRequest.builder()
                .key(key)
                .bucket(bucketName)
                .build(); 
    	
    	 ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(objectRequest);
         byte[] data = objectBytes.asByteArray();
         
         //logger.debug(new String(data));
    	    	
    	return new String(data);
    }

	public S3Client getS3Client() {
		return s3Client;
	}

	public void setS3Client(S3Client s3Client) {
		this.s3Client = s3Client;
	}
    
    
}
