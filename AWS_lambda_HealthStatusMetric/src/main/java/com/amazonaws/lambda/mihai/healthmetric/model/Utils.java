package com.amazonaws.lambda.mihai.healthmetric.model;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * any utilities class
 * @author mike
 */
public class Utils {

	/**
	 * 
	 * @param <T> ANY type of Object that is deserialized from JSON
	 * @param jsonObject object as JSON
	 * @param objectClass class of any object that will be deserialized
	 * @return the deserialized object
	 */
	public static <T> T getJsonAsObject(String jsonObject, Class<T> objectClass) {
    	
		T result = null;
		
		ObjectMapper om = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
		
		om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		om.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false);
		om.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, false);
		
		try {
			result = om.readValue(jsonObject.getBytes(), objectClass);
			
		} catch (Exception ex) {
			ex.printStackTrace();
			throw new RuntimeException(ex);
		}
		
		
		return result;
	}
	
	   /**
	    * 
	    * @param object that will be serialized as JSON
	    * @return object as JSON string
	    */
	    public static String getObjectAsJson (Object object) {
	    	StringBuffer sb = new StringBuffer();
	    	    	
	    	ObjectMapper om = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
	    	//ObjectWriter ow = om.writer().withDefaultPrettyPrinter();
			ObjectWriter ow = om.writer();
	    	
	    	try {
	    		sb.append(ow.writeValueAsString(object));
	    		
	    	} catch (JsonProcessingException ex) {
	    		ex.printStackTrace();
	    	}
	    	
	    	return sb.toString();
	    }
}
