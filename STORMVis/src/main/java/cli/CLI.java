package cli;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JProgressBar;

import org.apache.commons.io.FilenameUtils;

import calc.Calc;
import calc.STORMCalculator;
import gui.DataTypeDetector;
import gui.DataTypeDetector.DataType;
import gui.ParserWrapper;
import inout.FileManager;
import inout.SimulationParameterData;
import inout.SimulationParameterIO;
import model.DataSet;
import model.ParameterSet;

public class CLI {
	private static Random random;
	private static String EXTENSIONIMAGEOUTPUT = ".tif";
	static List<DataSet> allDataSets = new ArrayList<DataSet>();
	static float[] shifts = { 0, 0, 0 };

	public static void main(String[] args) {
		String inputModelFile = "/Users/sandrobraun/Desktop/inputs/model_file.txt";
		String outputFolder = "/Users/sandrobraun/Desktop/outputs/cli_out";
		String inputSimulationConfig = "/Users/sandrobraun/Desktop/inputs/simulationParameters.json";
		boolean tiffStackOutput = false;
		boolean suReSimOutput = true;
		DataSet data = new DataSet(new ParameterSet());
		data.setProgressBar(new JProgressBar());
		allDataSets.add(data);

		proceedFileImport(new File(inputModelFile));
		SimulationParameterData params = SimulationParameterIO.fromFile(new File(inputSimulationConfig));

		set_simulation_parameters(params);
		calculate(params);
		params.borders = getBorders();
		String fname = FilenameUtils.getBaseName((new File(inputModelFile)).getName());
		if (suReSimOutput) {
			new File(outputFolder).mkdir();
			exportData(outputFolder, fname, params);
		}
	}

	private static void proceedFileImport(File file) {
		// load data into class variable
		System.out.println("Path: " + file.getAbsolutePath());
		DataType type = DataType.UNKNOWN;
		int currentRow = 0;
		try {
			type = DataTypeDetector.getDataType(file.getAbsolutePath());
			allDataSets.get(currentRow).setDataType(type); // dirty hack to prevent exlicit type cast
			System.out.println(type.toString());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		DataSet data = ParserWrapper.parseFileOfType(file.getAbsolutePath(), type);
		data.setName(file.getName());
		if (data.dataType.equals(DataType.TRIANGLES)) {
			System.out.println("Triangles parsed correctly.");
		} else if (type.equals(DataType.LINES)) {
			System.out.println("Lines parsed correctly.");
		} else if (type.equals(DataType.PLY)) {
			System.out.println("PLY file parsed.");
		}
		allDataSets.set(0, data);
		allDataSets.get(0).setProgressBar(new JProgressBar());
	}

	private static void calculate(SimulationParameterData params) {
		STORMCalculator calc = new STORMCalculator(allDataSets.get(0), random);
		// calc = new STORMCalculator(allDataSets.get(currentRow));
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
		// When calc has finished, grab the new dataset
		// allDataSets.set(currentRow, calc.getCurrentDataSet());
		// visualizeAllSelectedData();
	}

	private static void set_simulation_parameters(SimulationParameterData params) {
		setUpRandomNumberGenerator(params.makeItReproducible);
		int currentRow = 0;

		allDataSets.get(currentRow).getParameterSet().setSoa((float) (params.angularDeviation));// sigma of angle
		allDataSets.get(currentRow).getParameterSet().setPabs((float) (params.labelingEfficiency / 100.));// Labeling
		// efficiency
		allDataSets.get(currentRow).getParameterSet().setAoa((float) (params.angleOfLabel));
		allDataSets.get(currentRow).getParameterSet().setDeff((float) (params.detectionEfficiency / 100)); // detection
		// efficiency
		allDataSets.get(currentRow).getParameterSet().setIlpmm3(params.backgroundPerMicroMeterCubed); // background per
		// cubic micro
		// meter
		allDataSets.get(currentRow).getParameterSet().setLoa(params.labelEpitopeDistance); // label epitope distance
		allDataSets.get(currentRow).getParameterSet().setFpab(params.fluorophoresPerLabel);// fluorophores per label
		allDataSets.get(currentRow).getParameterSet().setDutyCycle(params.dutyCycle);
		allDataSets.get(currentRow).getParameterSet().setBleachConst((float) 0);
		allDataSets.get(currentRow).getParameterSet().setFrames(params.recordedFrames); // recorded frames
		allDataSets.get(currentRow).getParameterSet().setSxy(params.sigmaXY);
		allDataSets.get(currentRow).getParameterSet().setSz(params.sigmaZ);
		allDataSets.get(currentRow).getParameterSet().setPsfwidth((float) 380);
		allDataSets.get(currentRow).getParameterSet().setMeanPhotonNumber(params.MeanPhotonNumber);
		allDataSets.get(currentRow).getParameterSet().setPixelsize(params.pixelsize);
		allDataSets.get(currentRow).getParameterSet().setSigmaRendering(params.sigmaRendering);
		allDataSets.get(currentRow).getParameterSet().setAbpf(params.fluorophoresPerLabel);
		allDataSets.get(currentRow).getParameterSet().setSigmaRendering(10);
		allDataSets.get(currentRow).getParameterSet().setColorProof(params.colorProof);
		allDataSets.get(currentRow).getParameterSet().setBorders(params.borders);
		allDataSets.get(currentRow).getParameterSet().setSigmaRendering(params.sigmaRendering);
		allDataSets.get(currentRow).getParameterSet().setuseSTORMBlinking(params.useSTORMBlinking);

		if (allDataSets.get(currentRow).dataType == DataType.LINES) {
			allDataSets.get(currentRow).getParameterSet().setBspnm(params.epitopeDensity);
			allDataSets.get(currentRow).getParameterSet().setRof(params.radiusOfFilament);
		} else {
			allDataSets.get(currentRow).getParameterSet().setBspsnm(params.epitopeDensity);
		}
		allDataSets.get(currentRow).getParameterSet().setMergedPSF(false);
		allDataSets.get(currentRow).getParameterSet().setApplyBleaching(false);
		allDataSets.get(currentRow).getParameterSet().setCoupleSigmaIntensity(params.coupleSigmaIntensity);
	}

	private static void setUpRandomNumberGenerator(boolean makeItReproducible) {
		if (makeItReproducible) {
			random = new Random(2);
		} else {
			random = new Random(System.currentTimeMillis());
		}
	}

	private static ArrayList<Float> getBorders() {
		ArrayList<Float> retList = new ArrayList<Float>();
		int currentRow = 0;
		retList.add(Calc.min(allDataSets.get(currentRow).stormData, 0));
		retList.add(Calc.max(allDataSets.get(currentRow).stormData, 0));
		retList.add(Calc.min(allDataSets.get(currentRow).stormData, 1));
		retList.add(Calc.max(allDataSets.get(currentRow).stormData, 1));
		retList.add(Calc.min(allDataSets.get(currentRow).stormData, 2));
		retList.add(Calc.max(allDataSets.get(currentRow).stormData, 2));

		return retList;

	}

	private static void exportData(String path, String name, SimulationParameterData params) {
		if (!name.endsWith(EXTENSIONIMAGEOUTPUT)) {
			name = name += EXTENSIONIMAGEOUTPUT;
		}
		path = path + File.separator + name;
		System.out.println("Path to write project: " + path);
		System.out.println("project name: " + name);
		int currentRow = 0;
		FileManager.ExportToFile(allDataSets.get(currentRow), path, params.viewStatus, params.borders,
				allDataSets.get(currentRow).getParameterSet().getPixelsize(), params.sigmaRendering, shifts);
	}

}
