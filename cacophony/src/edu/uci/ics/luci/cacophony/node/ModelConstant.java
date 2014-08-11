package edu.uci.ics.luci.cacophony.node;

import java.util.List;

// This class is just for testing purposes.
public class ModelConstant implements Model {

	@Override
	public void train(List<Observation> observations) {
		// do nothing
	}

	@Override
	public Object predict(Observation observation) {
		return 12345;
	}
}
