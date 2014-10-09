package edu.uci.ics.luci.cacophony.node;

import java.util.List;

// This class is just for testing purposes.
public class ModelConstant implements Model {

	public void train(List<Observation> observations) {
		// do nothing
	}

	public Object predict(Observation observation) {
		return 12345;
	}
}
