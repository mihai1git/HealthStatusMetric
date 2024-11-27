package com.amazonaws.lambda.mihai.healthmetric.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.lambda.mihai.healthmetric.aspect.TraceAll;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchClient;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.MetricDatum;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataResponse;
import software.amazon.awssdk.services.cloudwatch.model.StandardUnit;

/**
 * layer between lambda logic and AWS CloudWatch Service
 * @author Mihai ADAM
 *
 */
@TraceAll
public class CloudWatchService {
	
	/**
	 * Business concept from CloudWatch Metrics Server: a Namespace is an organizational unit that holds multiple metrics (sets of data)
	 */
	private static final String AWS_METRICS_NAMESPACE_ROUTE53 = "MIKE/Route53";
	/**
	 * Business concept from CloudWatch Metrics Server: the name of a Metric
	 */
	private static final String AWS_METRICS_NAME = "ExternalHealthCheckStatus";
	/**
	 * Business concept from CloudWatch Metrics Server: name of a Dimension; a metric could have multiple Dimensions
	 */
	private static final String AWS_METRICS_DIMENSION_NAME = "HostedZoneId";
	/**
	 * Business concept from CloudWatch Metrics Server: value of a Dimension
	 */
	private static final String AWS_METRICS_DIMENSION_VALUE = "Z04702051WDZH2C7LSLUO";
	/**
	 * logger named by this class
	 */
	private Logger logger = LogManager.getLogger(CloudWatchService.class);
	
	/** 
	 * AWS client for CloudWatch Service 
	 */
	private CloudWatchClient cwClient;
	
	/**
	 * builds an instance of this Service on each invocation
	 * @return a new instance of this Service 
	 */
    public static CloudWatchService build() {
    	
    	CloudWatchClient cwClient = CloudWatchClient.builder()
    			.region(Region.US_EAST_1)
    			.build();

    	CloudWatchService cwSrv = new CloudWatchService();
    	cwSrv.setCwClient(cwClient);
    	
    	return cwSrv;
    }
    
    /**
     * convert online status to health status and put it into AWS CloudWatch Metrics Service
     * @param onoff online status of the website
     */
    public void putHealthStatusData (Boolean onoff) {
    	
        // Create MetricDatum objects.
        MetricDatum datum1 = MetricDatum.builder()
            .metricName(AWS_METRICS_NAME)
            .unit(StandardUnit.COUNT)
            .value((onoff)?1.0:0.0)
            .dimensions(Dimension.builder().name(AWS_METRICS_DIMENSION_NAME).value(AWS_METRICS_DIMENSION_VALUE).build())
            .build();


        List<MetricDatum> metricDataList = new ArrayList<MetricDatum>();
        metricDataList.add(datum1);
        
    	PutMetricDataRequest request = PutMetricDataRequest.builder()
                .namespace(AWS_METRICS_NAMESPACE_ROUTE53)
                .metricData(metricDataList)
                .build();
    	
    	PutMetricDataResponse resp = cwClient.putMetricData(request);
    	
    }

	public CloudWatchClient getCwClient() {
		return cwClient;
	}

	public void setCwClient(CloudWatchClient cwClient) {
		this.cwClient = cwClient;
	}
    
    
}
