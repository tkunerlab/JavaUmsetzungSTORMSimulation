package batchProcessing;

import gui.CreateTiffStack;
import gui.ThreadCompleteListener;
import gui.DataTypeDetector.DataType;
import inout.FileManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import model.DataSet;
import model.ParameterSet;
import calc.Calc;
import calc.CreateStack;
import calc.STORMCalculator;


//closely resembles the STORMCalculator except, we are directly storing the outcomes
public class BatchProcessor extends SwingWorker<Void, Void>{
	//variables
	List<float[][]> trList;
    DataSet currentDataSet;
    public Random random;
    public boolean isRunning = true;
    int viewstatus = 1;
    float[] shifts = {0 , 0, 0};
    String fullpath = "";
    boolean output_tiffstack = false;
    boolean reproducible = true;
	
	public BatchProcessor(String fullpath, DataSet set, boolean tiff_out, boolean reproducible, int viewstatus, float[] shifts) {
		this.fullpath = fullpath;
		this.currentDataSet = set;
		this.viewstatus = viewstatus;
		this.shifts = shifts;
		this.output_tiffstack = tiff_out;
		this.reproducible = reproducible;
	}
	
	public DataSet getCurrentDataSet() {
		return currentDataSet;
	}

	public void setCurrentDataSet(DataSet currentDataSet) {
		this.currentDataSet = currentDataSet;
	}

	@Override
	public Void doInBackground() {
		currentDataSet.isCalculating = true;
		long start = System.nanoTime();
		if(currentDataSet != null) {
			doSimulation();
		}
		System.out.println("Whole converting and simulation time: "+ (System.nanoTime()-start)/1e9 +"s");
		System.out.println("-------------------------------------");
		currentDataSet.isCalculating = false;
		return null;
	}
	@Override
	public void done(){
		System.out.println("Worker finished");
		currentDataSet.getProgressBar().setString("Calculation Done!");
		currentDataSet.isCalculating = false;
		notifyListeners();
	}
	
	private  Set<ThreadCompleteListener> listeners
      = new CopyOnWriteArraySet<ThreadCompleteListener>();
	public void addListener(final ThreadCompleteListener listener) {
		listeners.add(listener);
	}
	public void removeListener(final ThreadCompleteListener listener) {
		listeners.remove(listener);
	}
	
	public void notifyListeners() {
		for (ThreadCompleteListener listener : listeners) {
			listener.notifyOfThreadComplete(this);
		}
	}
	/**
	 * 
	 * Function actually doing the complete simulation.
	 * In case of surface models and line models the epitope positions are found first.
	 * After that the labels are placed
	 * In the last step STORM localizations are simulated based on the assumed fluorophore positions near the end of the labels.
	 *
	 */
	public void doSimulation() {
		setUpRandomNumberGenerator(this.reproducible);
		(new File(this.fullpath)).mkdirs();
		STORMCalculator calc = new STORMCalculator(currentDataSet, random);
		calc.doSimulation();
		
		//save outcome
		DataSet thisDataSet = calc.getCurrentDataSet();
		//ArrayList<Float> borders = conf.borders;
		ArrayList<Float> borders = new ArrayList<Float>();
		//if(conf.borders.size()!=6){
		borders.add(Calc.min(thisDataSet.stormData, 0));
		borders.add(Calc.max(thisDataSet.stormData, 0));
		borders.add(Calc.min(thisDataSet.stormData, 1));
		borders.add(Calc.max(thisDataSet.stormData, 1));
		borders.add(Calc.min(thisDataSet.stormData, 2));
		borders.add(Calc.max(thisDataSet.stormData, 2));
		//}
		
		//save output
		FileManager.ExportToFile(calc.getCurrentDataSet(), this.fullpath+"/plain.tif", this.viewstatus, borders, 
				currentDataSet.getParameterSet().getPixelsize(), currentDataSet.getParameterSet().getSigmaRendering(), this.shifts);
		
		//if needed to simulation again to create tiffstack
		if(this.output_tiffstack){
			setUpRandomNumberGenerator(this.reproducible);
			this.currentDataSet.getParameterSet().setSxy(0.0f);
			this.currentDataSet.getParameterSet().setSz(0.0f);
			calc = new STORMCalculator(currentDataSet, random);
			calc.doSimulation();
    		thisDataSet = calc.getCurrentDataSet();
    		
    		borders.clear();
    		borders.add(Calc.min(thisDataSet.stormData, 0));
    		borders.add(Calc.max(thisDataSet.stormData, 0));
    		borders.add(Calc.min(thisDataSet.stormData, 1));
    		borders.add(Calc.max(thisDataSet.stormData, 1));
    		borders.add(Calc.min(thisDataSet.stormData, 2));
    		borders.add(Calc.max(thisDataSet.stormData, 2));
    		
			ParameterSet psSet = thisDataSet.getParameterSet();
			int modelNumber = 2;
			if (psSet.isTwoDPSF()){
				modelNumber  = 1;
			}
			CreateStack.createTiffStack(thisDataSet.stormData, 1/psSet.getPixelToNmRatio(),
					psSet.getEmptyPixelsOnRim(),psSet.getEmGain(), borders, random,
					psSet.getElectronPerAdCount(), psSet.getFrameRate(), psSet.getMeanBlinkingTime(), psSet.getDeadTime(), psSet.getWindowsizePSF(),
					modelNumber,psSet.getQuantumEfficiency(), psSet.getNa(), psSet.getPsfwidth(), psSet.getFokus(), psSet.getDefokus(), psSet.getSigmaBg(),
					psSet.getConstOffset(), psSet.getCalibrationFile(), this.fullpath+"/tiffstack.tiff",psSet.isEnsureSinglePSF(), psSet.isDistributePSFoverFrames(),new CreateTiffStack(null, null, null, null));
		}
		
	}
	
	public void publicSetProgress(int prog){
		setProgress(prog);
	}
	
	private void setUpRandomNumberGenerator(boolean makeItReproducible) {
		if (makeItReproducible) {
			random = new Random(2);
		} else {
			random = new Random(System.currentTimeMillis());
		}
	}

}
