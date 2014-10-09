package edu.uci.ics.luci.cacophony.node;

import java.util.List;

public interface Model {
	/**
	 * 
	 * @param observations The observations on which to train the model
	 */
	void train(List<Observation> observations);
	
	/**
	 * 
	 * @param observation The observation from which to make a prediction
	 */
	Object predict(Observation observation);
}
