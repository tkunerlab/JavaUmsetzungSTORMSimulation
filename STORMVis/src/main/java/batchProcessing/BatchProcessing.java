package batchProcessing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.cli.*;

import calc.Calc;
import calc.CreateStack;
import calc.STORMCalculator;
import gui.CreateTiffStack;
import gui.DataTypeDetector;
import gui.DataTypeDetector.DataType;
import gui.ParserWrapper;
import inout.FileManager;
import inout.SimulationParameterData;
import inout.SimulationParameterIO;
import inout.TriangleLineFilter;
import model.DataSet;
import model.EpitopeDataSet;
import model.LineDataSet;
import model.ParameterSet;
import model.TriangleDataSet;

public class BatchProcessing {
	private static Random random;
	static ArrayList<DataSet> allDataSets = new ArrayList<DataSet>();
	static BatchConfig conf = new BatchConfig();
	static boolean use_output = false;
	static String base_path = "";
	
	public static void main(String[] args) {
		//simple argument parser taken from https://stackoverflow.com/questions/367706/how-do-i-parse-command-line-arguments-in-java
		Options options = new Options();
		
		Option input = new Option("i", "input", true, "input file path");
        input.setRequired(true);
        options.addOption(input);

        Option output = new Option("o", "output", true, "output file");
        output.setRequired(false);
        options.addOption(output);
        
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);

            System.exit(-1);
        }

        String config_path = cmd.getOptionValue("input");
        String out_path= cmd.getOptionValue("output");
        
        
        //load simulation parameters from file
        if (!conf.load(config_path)) {
        	System.out.print("Can't locate config file!");
        	System.exit(-1);
        }
        
        //create output folder
        if(conf.out_path.length()>0) {
        	use_output = true;
        	
        	String fullpath = "";
        	//put strings together
        	if(conf.out_path.charAt(conf.out_path.length()-1)=='/'){
        		fullpath = conf.out_path + conf.name;
        	} else {
        		fullpath = conf.out_path + "/" + conf.name;
        	}
        	
        	if(fullpath.charAt(fullpath.length()-1)=='/') {
        		fullpath = fullpath.substring(0, fullpath.length()-2);
        	}
        	base_path = fullpath;
        	
        	(new File(fullpath)).mkdirs();
        	System.out.print("Saving data in " + fullpath + "\n");
        }
        
        //convert parameters
        ArrayList<ParameterSet> params = conf.convertToParamterSet();
        
        //check if model files exist and import model
        for(int i=0;i<conf.models.size();i++){
        	File f = new File(conf.models.get(i));
        	if(!f.exists() || f.isDirectory()) { 
        		JOptionPane.showMessageDialog(null,"Cannot load model from " + conf.models.get(i), "Model Error", JOptionPane.ERROR_MESSAGE);
				System.exit(-1);
        	}
        	load_model(f);
        }
        
        //do stuff and call STORM calculator
        for(int i=0;i<params.size();i++){
        	for(int j=0;j<allDataSets.size();j++){
        		for(int r=0;r<conf.repeat_experiment;r++){
		    		allDataSets.get(j).setParameterSet(params.get(i)); //set new parameters to model
		    		
		    		//create new directory for this run
		    		String fullpath = String.format("%s/model%d/set%d/run%d", base_path, j, i, r);
		    		(new File(fullpath)).mkdirs();
		    		
		    		
		    		//do calculation
		    		allDataSets.get(j).setProgressBar(new JProgressBar());
		    		STORMCalculator calc = new STORMCalculator(allDataSets.get(j), random);
		    		calc.execute();
		    		while (!calc.isDone()) {
		    			try {
		    				Thread.sleep(100);
		    				// System.out.println(calc.isCancelled()+" "+calc.isDone());
		    			} catch (InterruptedException e) {
		    				// TODO Auto-generated catch block
		    				e.printStackTrace();
		    			}
		    		}
		    		DataSet thisDataSet = calc.getCurrentDataSet();
		    		ArrayList<Float> borders = conf.borders;
		    		if(conf.borders.size()!=6){
		    			borders.add(Calc.min(thisDataSet.stormData, 0));
		    			borders.add(Calc.max(thisDataSet.stormData, 0));
		    			borders.add(Calc.min(thisDataSet.stormData, 1));
		    			borders.add(Calc.max(thisDataSet.stormData, 1));
		    			borders.add(Calc.min(thisDataSet.stormData, 2));
		    			borders.add(Calc.max(thisDataSet.stormData, 2));
		    		}
		    		
		    		//save output
		    		FileManager.ExportToFile(calc.getCurrentDataSet(), fullpath+"/plain.tif", conf.viewstatus, borders, 
		    				params.get(i).getPixelsize(), params.get(i).getSigmaRendering(), conf.shifts);
		    		
		    		//set localization to 0 if we use tiffstack
		    		//new File(outputFolder + fname + File.separator).mkdir();
					//exportData(outputFolder + fname + File.separator, fname,
					//		params);
		    		
		    		//if needed create tiffstack here
		    		/*
		    		if(conf.output_tiffstack){
		    			setUpRandomNumberGenerator(conf.reproducible);
						CreateTiffStack cts = new CreateTiffStack(allDataSets.get(j), path, random,
								selfReference);
						cts.addPropertyChangeListener(selfReference);
						cts.execute();
						FileManager.writeLogFile(allDataSets.get(currentRow).getParameterSet(),
								path.substring(0, path.length() - 4) + "TiffStack", borders, true);
		    		}
		    		*/
        		}
        	}
        }
	}
	
	static private void load_model(File file){
		System.out.println("Path: " + file.getAbsolutePath());
		DataType type = DataType.UNKNOWN;
		try {
			type = DataTypeDetector.getDataType(file.getAbsolutePath());
			System.out.println(type.toString());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		DataSet data = ParserWrapper.parseFileOfType(file.getAbsolutePath(), type);
		data.setName(file.getName());
		data.setProgressBar(new JProgressBar());
		
		if (data.dataType.equals(DataType.TRIANGLES)) {
			System.out.println("Triangles parsed correctly.");
		} else if (type.equals(DataType.LINES)) {
			System.out.println("Lines parsed correctly.");
		} else if (type.equals(DataType.PLY)) {
			System.out.println("PLY file parsed.");
		}

		allDataSets.add(data);
	}
	
	private static void setUpRandomNumberGenerator(boolean makeItReproducible) {
		if (makeItReproducible) {
			random = new Random(2);
		} else {
			random = new Random(System.currentTimeMillis());
		}
	}
}
