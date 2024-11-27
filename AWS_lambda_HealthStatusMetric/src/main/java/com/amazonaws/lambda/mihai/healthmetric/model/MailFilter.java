package com.amazonaws.lambda.mihai.healthmetric.model;

import java.io.IOException;
import java.util.Arrays;
import java.util.ListIterator;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * domain entity for a filter that filters mails
 * @author mike
 *
 */
public class MailFilter {

	/** Value: FROM */
	public static final String FILTER_FIELD_FROM = "FROM";
	/** Value: TO */
	public static final String FILTER_FIELD_TO = "TO";
	/** Value: SUBJECT */
	public static final String FILTER_FIELD_SUBJECT = "SUBJECT";
	/** Value: EXACT */
	public static final String FILTER_MODIFIER_EXACT = "EXACT";
	/** Value: CONTAINS */
	public static final String FILTER_MODIFIER_CONTAINS = "CONTAINS";
	/**
	 * logger named by this class
	 */
	private Logger logger = LogManager.getLogger(MailFilter.class);

	/**
	 * field of the email that is considered in filter procedure; ex: FROM, TO
	 */
	private String field;
	/**
	 * value that is matched with value from the <code>field</code> of the email
	 */
	private String value;
	/**
	 * type of match among the value from the <code>field</code> and <code>value</code>
	 */
	private String modifier;
	
	/**
	 * selects email parameter according to filter details: email field (FROM, TO), modifier of the field (CONTAINS, EXACT), value of the field (e.g. jetpack)
	 * @param message mail package specific object that holds an email
	 * @return true if mail is selected 
	 * @throws Exception when reading from email data
	 */
	public Boolean filtered(MimeMessage message) throws Exception {
	
		Boolean filtered = Boolean.FALSE;
		
        logger.debug("Subject : " + message.getSubject());
        logger.debug("From : " + Arrays.toString(message.getFrom()));
        logger.debug("Body : " +  message.getContent());
        
        String contentType = message.getContentType();
        
        if (contentType.contains("multipart")) {
        	logger.debug("Multipart EMail File");
        	Multipart multiPart = (Multipart) message.getContent();
        	int numberOfParts = multiPart.getCount();
        	logger.debug("Parts:::"+numberOfParts);
        }
        		
		switch (field) {
		case FILTER_FIELD_FROM:
			Address[] fromAddrs = message.getFrom();
			
			if (FILTER_MODIFIER_CONTAINS.equals(modifier)) {
				ListIterator<Address> iter = Arrays.asList(fromAddrs).listIterator();
				while(iter.hasNext()) {
					if (value.contains(iter.next().toString())) {
						filtered = Boolean.TRUE;
						break;
					}
				}
			}
			if (FILTER_MODIFIER_EXACT.equals(modifier)) {
				Address addr = new InternetAddress(value);
				ListIterator<Address> iter = Arrays.asList(fromAddrs).listIterator();
				while(iter.hasNext()) {
					if (addr.equals(iter.next())) {
						filtered = Boolean.TRUE;
						break;
					}
				}
			}
			
			break;
			
		case FILTER_FIELD_TO:
			break;
			
		case FILTER_FIELD_SUBJECT:	
			break;

		default:
			break;
		}
		
		
		return filtered;
	}
	/**
	 * 
	 * @param message mail message
	 * @return plain text of the mail body, even if it contains Multiparts
	 * @throws MessagingException when errors in processing mail body Multiparts
	 * @throws IOException when reading email 
	 */
	public static String extractTextContent(Message message) throws MessagingException, IOException {
	    Object content = message.getContent();
	    return getTextFromMessage(content);
	}
	/**
	 * recursive function through Multiparts
	 * @param content of the mail
	 * @return plain text of the mail body, even if it contains Multiparts
	 * @throws MessagingException when errors in processing mail body Multiparts
	 * @throws IOException when reading email 
	 */
	public static String getTextFromMessage(Object content) throws MessagingException, IOException {
	    if (content instanceof Multipart) {
	        Multipart multipart = (Multipart) content;
	        StringBuilder text = new StringBuilder();
	        for (int i = 0; i < multipart.getCount(); i++) {
	            BodyPart bodyPart = multipart.getBodyPart(i);
	            text.append(getTextFromMessage(bodyPart.getContent()));
	        }
	        return text.toString();
	    } else if (content instanceof String) {
	        return (String) content;
	    }
	    return "";
	}
	
	
	
	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getModifier() {
		return modifier;
	}

	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	@Override
	public String toString() {
		return " field : " + field
				+ " modifier : " + modifier
				+ " value : " + value;
	}
}
