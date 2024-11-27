package com.amazonaws.lambda.mihai.healthmetric.model;

/**
 * domain entity for an email that was filtered
 * @author mike
 */
public class FilteredMail {

	/** 
	 * S3 bucket where the email file is stored 
	 */
	private String s3Bucket;
	/**
	 * name of the email file
	 */
	private String s3Key;
	/**
	 * filter that filtered/selected this email
	 */
	private MailFilter filter;
	
	@Override
	public String toString() {
		return " s3Bucket : " + s3Bucket
				+ " s3Key : " + s3Key
				+ " filter : " + filter;
	}

	public String getS3Key() {
		return s3Key;
	}

	public void setS3Key(String s3Key) {
		this.s3Key = s3Key;
	}

	public MailFilter getFilter() {
		return filter;
	}

	public void setFilter(MailFilter filter) {
		this.filter = filter;
	}

	public String getS3Bucket() {
		return s3Bucket;
	}

	public void setS3Bucket(String s3Bucket) {
		this.s3Bucket = s3Bucket;
	}

	
	
}
