package batchProcessing;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import java.util.Arrays;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.awt.Color;


import calc.Calc;
import calc.CreateStack;
import calc.STORMCalculator;
import gui.CreateTiffStack;
import gui.DataTypeDetector;
import gui.DataTypeDetector.DataType;
import inout.FileManager;
import model.DataSet;
import model.ParameterSet;

//SwingWorker which executes a single parameter combination (times the number of experiment repeats)
public class BatchCalc extends Thread {
	//variables
    DataSet dataset;
    ParameterSet parameters;
    Random random;
    int viewstatus = 1;
    float[] shifts = {0 , 0, 0};
    String basepath = "";
    boolean output_tiffstack = false;
    boolean reproducible = true;
    int repeats = 1;
	   
	public BatchCalc(String basepath, DataSet dataset, ParameterSet parameters, boolean tiff_out, boolean reproducible, int viewstatus, float[] shifts, int repeats) {
		this.basepath = basepath;
		this.dataset = dataset;
		this.parameters = parameters;
		this.viewstatus = viewstatus;
		this.shifts = shifts;
		this.output_tiffstack = tiff_out;
		this.reproducible = reproducible;
		this.repeats = repeats;
	}
	
	
	public DataSet getCurrentDataSet() {
		return this.dataset;
	}

	public void setCurrentDataSet(DataSet currentDataSet) {
		this.dataset = currentDataSet;
	}

	@Override
	public void run() {
		long start = System.nanoTime();
		if(this.dataset!= null) {
			this.dataset.isCalculating = true;
			doSimulation();
			this.dataset.isCalculating = false;
		} else {
			System.out.print("Sumting wong!");
		}
		System.out.println("Whole converting and simulation time: "+ (System.nanoTime()-start)/1e9 +"s");
		System.out.println("-------------------------------------");
	}
	
	public void doSimulation() {
		float sxy = this.parameters.getSxy();
		float sz = this.parameters.getSz();
		for(int r=0;r<this.repeats;r++) {
			String fullpath = this.basepath + File.separator;
			fullpath = String.format("%srun%d", fullpath, r);
			(new File(fullpath)).mkdirs(); //create new directory
			setUpRandomNumberGenerator(this.reproducible);
			this.dataset.getParameterSet().setSxy(sxy); //BE CAREFUL we alter the origin
			this.dataset.getParameterSet().setSz(sz);
			this.dataset.setParameterSet(this.parameters);
			STORMCalculator calc = new STORMCalculator(this.dataset, this.random);
			calc.doSimulation();
			
			//save outcome
			DataSet thisDataSet = calc.getCurrentDataSet();
			ArrayList<Float> borders = new ArrayList<Float>();
			borders.add(Calc.min(thisDataSet.stormData, 0));
			borders.add(Calc.max(thisDataSet.stormData, 0));
			borders.add(Calc.min(thisDataSet.stormData, 1));
			borders.add(Calc.max(thisDataSet.stormData, 1));
			borders.add(Calc.min(thisDataSet.stormData, 2));
			borders.add(Calc.max(thisDataSet.stormData, 2));
			
			//save output
			String name = fullpath + File.separator + "plain.tif";
			FileManager.ExportToFile(thisDataSet, name, this.viewstatus, borders, 
					this.parameters.getPixelsize(), this.parameters.getSigmaRendering(), this.shifts);
			
			//to create tiffstack we need to do simulation again but with localization precision of 0nm
			if(this.output_tiffstack){
				setUpRandomNumberGenerator(this.reproducible);
				this.dataset.getParameterSet().setSxy(0.0f); //BE CAREFUL we alter the origin
				this.dataset.getParameterSet().setSz(0.0f);
				this.dataset.setParameterSet(this.parameters);
				calc = new STORMCalculator(this.dataset, this.random);
				calc.doSimulation();
				
	    		thisDataSet = calc.getCurrentDataSet();
	    		borders.clear();
	    		borders.add(Calc.min(thisDataSet.stormData, 0));
	    		borders.add(Calc.max(thisDataSet.stormData, 0));
	    		borders.add(Calc.min(thisDataSet.stormData, 1));
	    		borders.add(Calc.max(thisDataSet.stormData, 1));
	    		borders.add(Calc.min(thisDataSet.stormData, 2));
	    		borders.add(Calc.max(thisDataSet.stormData, 2));
	    		
				int modelNumber = 2;
				if (this.parameters.isTwoDPSF()){
					modelNumber  = 1;
				}
				
				name = fullpath + File.separator + "tiffstack.tif";
				CreateStack.createTiffStack(thisDataSet.stormData, 1/this.parameters.getPixelToNmRatio(),
						this.parameters.getEmptyPixelsOnRim(), this.parameters.getEmGain(), borders, this.random,
						this.parameters.getElectronPerAdCount(), this.parameters.getFrameRate(), this.parameters.getMeanBlinkingTime(), this.parameters.getDeadTime(), this.parameters.getWindowsizePSF(),
						modelNumber, this.parameters.getQuantumEfficiency(), this.parameters.getNa(), this.parameters.getPsfwidth(), this.parameters.getFokus(), this.parameters.getDefokus(), this.parameters.getSigmaBg(),
						this.parameters.getConstOffset(), this.parameters.getCalibrationFile(), name, this.parameters.isEnsureSinglePSF(), this.parameters.isDistributePSFoverFrames(),new CreateTiffStack(null, null, null, null));
			}
		}
		
	}
	
	private void setUpRandomNumberGenerator(boolean makeItReproducible) {
		if (makeItReproducible) {
			this.random = new Random(2);
		} else {
			this.random = new Random(System.currentTimeMillis());
		}
	}

}
