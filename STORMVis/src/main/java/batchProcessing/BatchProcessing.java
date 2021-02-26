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
import model.ParameterSet;

public class BatchProcessing {
	private static Random random;
	static List<DataSet> allDataSets = new ArrayList<DataSet>();
	static BatchConfig conf = new BatchConfig();
	
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
        
        //create output folder
        //(new File(out_path)).mkdir();
        
        //load simulation parameters from file
        if (!conf.load(config_path)) {
        	System.out.print("Can't locate config file!");
        	System.exit(-1);
        }
        
        //convert parameters
        conf.convertToParamterSet();
        
        //load models
        
        //do stuff and call STORM calculator
        //execute
	}
	
	private static void set_parameters(SimulationParameter data) {
		
	}
}
