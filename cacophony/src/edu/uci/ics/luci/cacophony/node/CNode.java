package edu.uci.ics.luci.cacophony.node;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class CNode implements Runnable{
	
	int STORAGE_TIME_WINDOW_SIZE = 10; 	
	CNodeConfiguration configuration;
	
	public CNodeConfiguration getConfiguration() {
		return configuration;
	}

	public CNode(CNodeConfiguration configuration) throws StorageException{
		this.configuration = configuration;
		
		List<SensorConfig> sensorConfigs = new ArrayList<SensorConfig>();
		sensorConfigs.addAll(configuration.getFeatures());
		sensorConfigs.add(configuration.getTarget());
		SensorReadingsDAO.initializeDBIfNecessary(sensorConfigs);
	}

	@Override
	public void run() {
    SensorReader targetReader = new SensorReader(configuration.getTarget()); 
    SensorReading targetReading = null;
    try {
			targetReading = targetReader.call();
		} catch (Exception e) {
			e.printStackTrace();
		}
    
    // TODO: don't retrieve feature values unless the target value is non-null and has changed
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
		
		List<SensorReading> sensorReadings = new ArrayList<SensorReading>();
    for(Future<SensorReading> future : futures) {
      try {
      	sensorReadings.add(future.get());
      } catch (InterruptedException e) {
        e.printStackTrace();
    	} catch (ExecutionException e) {
        e.printStackTrace();
      }
    }
    sensorReadings.add(targetReading);
    try {
			SensorReadingsDAO.store(sensorReadings);
		} catch (StorageException e) {
		// TODO: log the error and figure out what action to take
			e.printStackTrace();
		}
    
    //shut down the executor service now
    executor.shutdown();
		
		try {
			trainModel();
		} catch (UnknownSensorException e) {
			// TODO: log the error and figure out what action to take
			e.printStackTrace();
		} catch (StorageException e) {
			// TODO: log the error and figure out what action to take
			e.printStackTrace();
		}
		Object prediction = predict(sensorReadings);
		// store predicted value
		
		List<Date> storageTimes;
		try {
			storageTimes = SensorReadingsDAO.retrieveStorageTimes(STORAGE_TIME_WINDOW_SIZE);
			Thread.sleep(getWaitingTime(storageTimes));
		} catch (StorageException e) {
			// TODO: log the error and figure out what action to take
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO: log the error and figure out what action to take
			e.printStackTrace();
		}
	}
	
	private void trainModel() throws UnknownSensorException, StorageException {
		List<SensorConfig> sensors = new ArrayList<SensorConfig>(configuration.getFeatures());
		sensors.add(configuration.getTarget());
		List<Observation> observationsRaw;
		observationsRaw = SensorReadingsDAO.retrieve(sensors);
		
		for (Observation obs : observationsRaw){
			List<WekaAttributeTypeValuePair> featuresTranslated = new ArrayList<WekaAttributeTypeValuePair>();
			for (SensorReading reading : obs.getFeatures()){
				if (reading.getRawValue() != null){
						featuresTranslated.add(reading.getTranslatedValue());
				}
			}
			WekaAttributeTypeValuePair translatedTarget = obs.getTarget().getTranslatedValue();
			// TODO: Need to pass translated values to actual Weka code for training model
		}
	}
	
	private Object predict(List<SensorReading> featureValues) {
		//TODO: Implement. Should this take a model as a parameter?.
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
	 * @param stdDev the standard deviation of the samples
	 * @param n the number of samples
	 * @param percentile 0 - 100
	 * @return value above which percentile of the samples are expected to occur 
	 */
	protected static Double getPercentile(double stdDev, int n, Double percentile){
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

	/**
	 * Get the time to wait before checking for a change in a sensor's value.
	 * @param A list of Dates, where each Date is the time an observation was stored in the DB (which should roughly be the same time the observation was made)
	 * @return The amount of time, in milliseconds, to wait before checking for a change in the sensor's value 
	 */
	protected static long getWaitingTime(List<Date> storageTimes) {
		if (storageTimes.size() < 2) {		
			return DEFAULT_WAITING_TIME;
		}
		
		// Want list of times in descending order (newest to oldest) so we can find intervals between each time
		Collections.sort(storageTimes);
		Collections.reverse(storageTimes);
		List<Long> storageTimeIntervalsMilliseconds = new ArrayList<Long>();
		for (int i=0; i<storageTimes.size()-1; ++i) {
			Date newer = storageTimes.get(i);
			Date older = storageTimes.get(i+1);
			storageTimeIntervalsMilliseconds.add(newer.getTime() - older.getTime());
		}
		
		long waitingTime;
		try{
			double mean = calculateMean(storageTimeIntervalsMilliseconds);
			double stdDev = calculateStdDev(storageTimeIntervalsMilliseconds, mean);
			double confidenceInterval = Double.valueOf(0.95);
			double percentile = getPercentile(stdDev,storageTimeIntervalsMilliseconds.size(),confidenceInterval);
			waitingTime = Math.abs((long)(mean - percentile));
		}
		catch(InvalidParameterException e){
			waitingTime = DEFAULT_WAITING_TIME;
		}
		return waitingTime;
	}
}
