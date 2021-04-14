package gui;

import model.ParameterSet;

public interface BatchUpdateListener {
	void notifyBatchUpdate(int index, ParameterSet paramset, float[][] stormdata, float[][] antibodystart, float[][] antibodyend, float[][] fluorophores);
}
