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
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.io.FileWriter;

import java.awt.Color;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import calc.Calc;
import calc.CreateStack;
import calc.STORMCalculator;
import gui.CreateTiffStack;
import gui.DataTypeDetector;
import gui.DataTypeDetector.DataType;
import ij.ImagePlus;
import ij.plugin.RGBStackMerge;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
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
    ArrayList<Float> borders = new ArrayList<Float>();
    String basepath = "";
    boolean output_tiffstack = false;
    boolean reproducible = true;
    int repeats = 1;
    public int id = -1;
    int sizex = 128;
    int sizey = 128;
	   
	public BatchCalc(String basepath, DataSet dataset, ParameterSet parameters, boolean tiff_out, boolean reproducible, int viewstatus, float[] shifts, float[] borders, int repeats, int id, int sizey, int sizex) {
		this.basepath = basepath;
		this.dataset = dataset;
		this.parameters = parameters;
		this.viewstatus = viewstatus;
		this.shifts = shifts;
		this.output_tiffstack = tiff_out;
		this.reproducible = reproducible;
		this.repeats = repeats;
		this.id = id;
		
		for(int i=0;i<borders.length;i++) {
			this.borders.add(borders[i]);
		}
		
		this.sizex = sizex;
		this.sizey = sizey;
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
		
		if(this.id>-1) {
			this.repeats = 1;
		}
		
		float sxy = this.parameters.getSxy();
		float sz = this.parameters.getSz();
		for(int r=0;r<this.repeats;r++) {
			if(Thread.interrupted()) {
				break;
			}
			String fullpath = this.basepath + File.separator;
			
			if(id==-1) {
				fullpath = String.format("%srun%d", fullpath, r);
			} else {
				fullpath = String.format("%sobj_%05d", fullpath, this.id);
			}
			(new File(fullpath)).mkdirs(); //create new directory
			setUpRandomNumberGenerator(this.reproducible);
			this.dataset.getParameterSet().setSxy(sxy); //BE CAREFUL we alter the origin
			this.dataset.getParameterSet().setSz(sz);
			this.dataset.setParameterSet(this.parameters);
			STORMCalculator calc = new STORMCalculator(this.dataset, this.random);
			calc.doSimulation();
			
			//save outcome
			DataSet thisDataSet = calc.getCurrentDataSet();
			ArrayList<Float> bds = new ArrayList<Float>();
			if(this.borders.size()==0) {
				bds.add(Calc.min(thisDataSet.stormData, 0));
				bds.add(Calc.max(thisDataSet.stormData, 0));
				bds.add(Calc.min(thisDataSet.stormData, 1));
				bds.add(Calc.max(thisDataSet.stormData, 1));
				bds.add(Calc.min(thisDataSet.stormData, 2));
				bds.add(Calc.max(thisDataSet.stormData, 2));
			} else {
				bds = new ArrayList<Float>(this.borders);
				bds.set(4, Calc.min(thisDataSet.stormData, 2)); //we need to be careful, only works for 2D data
				bds.set(5, Calc.max(thisDataSet.stormData, 2));
			}
			
			//save output
			String name = fullpath + File.separator + "plain.tif";
			FileManager.ExportToFile(thisDataSet, name, this.viewstatus, bds, 
					this.parameters.getPixelsize(), this.parameters.getSigmaRendering(), this.shifts);
			render_image(thisDataSet, fullpath + File.separator + "plain", this.viewstatus, bds, this.parameters.getPixelsize(), this.parameters.getSigmaRendering(), this.shifts, this.sizex, this.sizey);
			saveparameters(this.parameters, fullpath+File.separator+"plain_parameters.json");
			
			if(Thread.interrupted()) {
				break;
			}
			
			//to create tiffstack we need to do simulation again but with localization precision of 0nm
			if(this.output_tiffstack){
				setUpRandomNumberGenerator(this.reproducible);
				this.dataset.getParameterSet().setSxy(0.0f); //BE CAREFUL we alter the origin
				this.dataset.getParameterSet().setSz(0.0f);
				this.dataset.setParameterSet(this.parameters);
				calc = new STORMCalculator(this.dataset, this.random);
				calc.doSimulation();
				
				if(Thread.interrupted()) {
					break;
				}
				
	    		thisDataSet = calc.getCurrentDataSet();
	    		if(this.borders.size()==0) {
		    		bds.clear();
		    		bds.add(Calc.min(thisDataSet.stormData, 0));
		    		bds.add(Calc.max(thisDataSet.stormData, 0));
		    		bds.add(Calc.min(thisDataSet.stormData, 1));
		    		bds.add(Calc.max(thisDataSet.stormData, 1));
		    		bds.add(Calc.min(thisDataSet.stormData, 2));
		    		bds.add(Calc.max(thisDataSet.stormData, 2));
	    		}
	    		
				int modelNumber = 2;
				if (this.parameters.isTwoDPSF()){
					modelNumber  = 1;
				}
				
				name = fullpath + File.separator + "tiffstack.tif";
				CreateStack.createTiffStack(thisDataSet.stormData, 1/this.parameters.getPixelToNmRatio(),
						this.parameters.getEmptyPixelsOnRim(), this.parameters.getEmGain(), bds, this.random,
						this.parameters.getElectronPerAdCount(), this.parameters.getFrameRate(), this.parameters.getMeanBlinkingTime(), this.parameters.getDeadTime(), this.parameters.getWindowsizePSF(),
						modelNumber, this.parameters.getQuantumEfficiency(), this.parameters.getNa(), this.parameters.getPsfwidth(), this.parameters.getFokus(), this.parameters.getDefokus(), this.parameters.getSigmaBg(),
						this.parameters.getConstOffset(), this.parameters.getCalibrationFile(), name, this.parameters.isEnsureSinglePSF(), this.parameters.isDistributePSFoverFrames(),new CreateTiffStack(null, null, null, null));
				saveparameters(this.dataset.getParameterSet(), fullpath+File.separator+"tiff_parameters.json");
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
	
	public void saveparameters(ParameterSet param, String path) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();//new Gson();
		
		try(FileOutputStream fos = new FileOutputStream(path);
				OutputStreamWriter isr = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
			gson.toJson(param, isr);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void render_image(DataSet dset, String path, int mode, 
			ArrayList<Float> borders, double pixelsize, double sigmaOrig, float[] shifts, int pixelX, int pixelY) {
		float[][] stormData = dset.stormData;
		double sigma = sigmaOrig/pixelsize;
		int filterwidth = 9;
		
		//int pixelX = 0;
		//int pixelY = 0;
		if (mode == 0){
			mode = 1;
		}
		
		//calculate image size
		switch(mode) {
			case 1: //xy
				break;
			case 2: //xz
				break;
				
			case 3: //yz
				break;
		}
		
		ArrayList<Float> dims = new ArrayList<Float>();
		dims.add(borders.get(0));
		dims.add(borders.get(1));
		dims.add(borders.get(2));
		dims.add(borders.get(3));
		dims.add(borders.get(4));
		dims.add(borders.get(5));
		
		
		ArrayList<float[][]> coloredImage = new ArrayList<float[][]>();
		for (int i = 0; i<3; i++){
			float[][] ch = new float[pixelX][pixelY];
			coloredImage.add(ch);
		}
		
		coloredImage = Calc.addFilteredPoints3D(coloredImage, sigma, filterwidth, pixelsize, stormData,mode,borders,dims,dataset.getParameterSet().isColorProof());
		
		ImageProcessor ipRed = new FloatProcessor(pixelX,pixelY);
		ImageProcessor ipGreen = new FloatProcessor(pixelX,pixelY);
		ImageProcessor ipBlue = new FloatProcessor(pixelX,pixelY);
		ipRed.setFloatArray(coloredImage.get(0));
		ipGreen.setFloatArray(coloredImage.get(1));
		ipBlue.setFloatArray(coloredImage.get(2));
		ImagePlus imgPRed = new ImagePlus("", ipRed);
		ImagePlus imgPGreen = new ImagePlus("", ipGreen);
		ImagePlus imgPBlue = new ImagePlus("", ipBlue);
		System.out.println("3D Image rendered ("+imgPRed.getWidth()+"*"+imgPRed.getHeight()+")");
		ArrayList<ImagePlus> colImg = new ArrayList<ImagePlus>();
		colImg.add(imgPRed);
		colImg.add(imgPGreen);
		colImg.add(imgPBlue);
		String basename = path.substring(0, path.length()-4);
		/*if (!scalebar){
			ij.IJ.save(colImg.get(0),basename+tag+"redCh.tif");
			ij.IJ.save(colImg.get(1),basename+tag+"greenCh.tif");
			ij.IJ.save(colImg.get(2),basename+tag+"blueCh.tif");
		}
		*/
		ImagePlus[] imPlusStack = new ImagePlus[3];
		imPlusStack[0] = colImg.get(0);
		imPlusStack[1] = colImg.get(1);
		imPlusStack[2] = colImg.get(2);
		ImagePlus imgRGB = RGBStackMerge.mergeChannels(imPlusStack, true);
		ij.IJ.saveAs(imgRGB, "png", path);
	}

}
