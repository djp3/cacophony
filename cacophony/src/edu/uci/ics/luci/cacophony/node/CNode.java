package edu.uci.ics.luci.cacophony.node;

import java.util.ArrayList;
import java.util.List;
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
	
	private Long getNextUpdateTime(List<Long> durationTimes){
		// use 95% confidence interval
		final long ONE_SECOND = 1000;
		final long DEFAULT_WAITING_TIME = 30 * ONE_SECOND;

		if(durationTimes.isEmpty()){			
			return (System.currentTimeMillis() + DEFAULT_WAITING_TIME);
		}
		
		double mean = 0;
		for(int i=0; i<durationTimes.size(); i++){
			mean += durationTimes.get(i);
		}
		mean = mean / durationTimes.size();
		
		double variance = 0;
		for(int i=0; i<durationTimes.size(); i++){
			variance += Math.pow((mean-durationTimes.get(i)),2);
		}
		variance = variance / durationTimes.size();
		
		double stdDev = Math.sqrt(variance);
		double stdErrOfMean = stdDev / Math.sqrt(durationTimes.size());
		long waitingTime = (long)(mean - 1.96*stdErrOfMean);
		
		return System.currentTimeMillis() + waitingTime;
	}
}
