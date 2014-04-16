package edu.uci.ics.luci.cacophony.node;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class CNode implements Runnable{
	
	CNodeConfiguration configuration;
	
	public CNodeConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(CNodeConfiguration configuration) {
		this.configuration = configuration;
	}

	public CNode(CNodeConfiguration configuration){
		this.configuration = configuration;
	}

	@Override
	public void run() {
		// TODO: use getNextUpdateTime() to decide when to reader target sensor
    SensorReader targetReader = new SensorReader(configuration.getTarget()); 
    try {
			SensorReading targetReading = targetReader.call();
		} catch (Exception e) {
			e.printStackTrace();
		}
    
    // TODO: don't retrieve feature values unless the target value has changed
		List<Callable<SensorReading>> tasks = new ArrayList<Callable<SensorReading>>(); 
    for(SensorConfig feature : configuration.getFeatures()){
      tasks.add(new SensorReader(feature));
    }
    ExecutorService executor = Executors.newCachedThreadPool();
    List<Future<SensorReading>> futures = null;
		try {
			futures = executor.invokeAll(tasks); // returns when all tasks are complete
		} catch (InterruptedException e) {
			e.printStackTrace();
			// TODO: Figure out if some of the tasks completed successfully.
		}
		
    for(Future<SensorReading> future : futures) {
      try {
      	SensorReading sensorReading = future.get();
      	if (sensorReading.getRawValue() != null){
    			WekaAttributeTypeValuePair wekaTypeValuePair = sensorReading.getSensorConfig().getTranslator().translate(sensorReading.getRawValue());
    			Object targetData = wekaTypeValuePair.getValue();
    			System.out.println(targetData);
          // TODO: store raw value in sqlite DB
    		}
      } catch (InterruptedException e) {
        e.printStackTrace();
    	} catch (ExecutionException e) {
        e.printStackTrace();
      }
    }
    //shut down the executor service now
    executor.shutdown();
		
		trainModel();
		Object prediction = predict();
		// store predicted value
	}
	
	private void trainModel() {
		//TODO: implement training here
	}
	
	private Object predict() {
		//TODO: implement. Should this return String instead of Object?
		return null;
	}
	

	protected static double calculateMean(List<Long> durationTimes) {
		if((durationTimes == null) || (durationTimes.size() == 0)){
			throw new InvalidParameterException("There needs to be samples");
		}
		
		double mean = 0.0;
		
		for(int i=0; i<durationTimes.size(); i++){
			mean += durationTimes.get(i);
		}
		mean = mean / durationTimes.size();
		
		return mean;
	}
	

	protected static double calculateStdDev(List<Long> durationTimes, double mean) {
		
		if((durationTimes == null) || (durationTimes.size() == 0)){
			throw new InvalidParameterException("There needs to be samples");
		}
		
		double variance = 0;
		for(int i=0; i < durationTimes.size(); i++){
			variance += Math.pow((mean-durationTimes.get(i)),2);
		}
		variance = variance / durationTimes.size();
		
		return Math.sqrt(variance);
	}
	
	
	protected static final Map<Double,Double> ztable = new HashMap<Double,Double>();
	static {
			ztable.put(Double.valueOf(0.50),Double.valueOf(0.50));
			ztable.put(Double.valueOf(0.80),Double.valueOf(1.28));
			ztable.put(Double.valueOf(0.90),Double.valueOf(1.64));
			ztable.put(Double.valueOf(0.95),Double.valueOf(1.96));
			ztable.put(Double.valueOf(0.99),Double.valueOf(2.58));
	}
	

	protected final static long ONE_SECOND = 1000;
	protected final static long DEFAULT_WAITING_TIME = 30 * ONE_SECOND;
	
	/**
	 * 
	 * See: http://en.wikipedia.org/wiki/Confidence_interval
	 * @param durationTimes a list of samples
	 * @param percentile 0 - 100
	 * @return value above which percentile of the samples are expected to occur 
	 */
	protected static Double getPercentile(double mean, double stdDev, int n, Double percentile){
		
		if(n <= 0){
			throw new InvalidParameterException("There needs to be samples");
		}
		
		Double zTableLookup = ztable.get(percentile);
		
		if(zTableLookup == null){
			throw new InvalidParameterException("We only support a few percentiles right now, and not: "+percentile);
		}
		
		double stdErrOfMean = stdDev / Math.sqrt(n);
		
		return zTableLookup * stdErrOfMean;
	}


	protected static long getWaitingTime(List<Long> durationTimes) {
		long waitingTime;
		
		try{
			double mean = calculateMean(durationTimes);
		
			double stdDev = calculateStdDev(durationTimes, mean);
		
			Double confidenceInterval = Double.valueOf(0.95);
		
			double percentile = getPercentile(mean,stdDev,durationTimes.size(),confidenceInterval);
			waitingTime = (long)(mean - percentile);
		}
		catch(InvalidParameterException e){
			waitingTime = DEFAULT_WAITING_TIME;
		}
		return waitingTime;
	}

	public static Long getNextUpdateTime(List<Long> durationTimes){
		long waitingTime = getWaitingTime(durationTimes);
		
		return System.currentTimeMillis() + waitingTime;
	}

}
