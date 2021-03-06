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

import calc.Calc;
import calc.CreateStack;
import calc.STORMCalculator;
import gui.CreateTiffStack;
import gui.DataTypeDetector;
import gui.DataTypeDetector.DataType;
import gui.ParserWrapper;
import inout.FileManager;
import inout.SimulationParameterData;
import inout.TriangleLineFilter;
import model.DataSet;
import model.EpitopeDataSet;
import model.ParameterSet;

public class startBatchProcessing {
	static List<DataSet> allDataSets = new ArrayList<DataSet>();
	private static Random random;
	private static String EXTENSIONIMAGEOUTPUT = ".tif";
	private static String outputFolder = "/Users/sandrobraun/Desktop/inputs";// "C:\\Users\\herrmannsdoerfer\\Desktop\\Tiff-StackTestModelle\\Mikrotubuli\\";
	static float[] shifts = { 0, 0, 0 };

	public static void main(String[] args) {
		allDataSets.add(new DataSet(new ParameterSet()));
		processMultipleFiles();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// File file = new File("Y:\\Users_shared\\SuReSim-Software Project\\SuReSim
		// Rebuttal\\Fire\\Simulation 12er Figure Table
		// FRC\\Modelle\\141107-MT-Modelrescaled1d.wimp");
		JOptionPane.showMessageDialog(null, "Sind die richtigen Parameter ausgewählt?");
		proceedFileImport(new File(UserDefinedInputOutput.getInputFile()));
		outputFolder = UserDefinedInputOutput.getOutputFolder();
//		DataSet data = ExamplesProvidingClass.getDataset(1);
//		furtherProceedFileImport(data, data.dataType);
		(new File(outputFolder)).mkdir();
		boolean tiffStackOutput = false;
		boolean suReSimOutput = true;
		int numberOfSimulationsWithSameParameterSet = 1; // number of outputs for the same parameter set
		SimulationParameter params = standardParameterVesicles();
		ArrayList<Float> sigmaXY = new ArrayList<Float>(Arrays.asList(4.f, 8.f, 12.f, 25.f));
		ArrayList<Float> sigmaZ = new ArrayList<Float>(Arrays.asList(8.f, 30.f, 40.f, 50.f));
		ArrayList<Float> le = new ArrayList<Float>(Arrays.asList(1.f, 10.f, 43.f, 100.f));
		ArrayList<Float> varAng = new ArrayList<Float>(Arrays.asList(0f));
		// ArrayList<Float> de = new
		// ArrayList<Float>(Arrays.asList(10.f,20.f,50.f,100.f));
		ArrayList<Float> koff = new ArrayList<Float>(Arrays.asList(5.f / 10000));
		// ArrayList<Integer> frames = new ArrayList<Integer>(Arrays.asList(10000));
		ArrayList<Float> labelLength = new ArrayList<Float>(Arrays.asList(15f));
		ArrayList<Integer> nbrLabel = new ArrayList<Integer>(Arrays.asList(8));
		ArrayList<Integer> photonOutput = new ArrayList<Integer>(Arrays.asList(4000));
		ArrayList<Float> bgLabelPerMM3 = new ArrayList<Float>(Arrays.asList(0f, 50f, 100f));
		ArrayList<Float> bindingAngles = new ArrayList<Float>(Arrays.asList(90f, -90f));
		ArrayList<Float> epitopeDensities = new ArrayList<Float>(Arrays.asList((float) (31.5 / (4 * Math.PI * 20 * 20)),
				(float) (69.8 / (4f * Math.PI * 20 * 20)), (float) (8.3 / (4f * Math.PI * 20f * 20f))));
		ArrayList<Float> sigmaRender = new ArrayList<Float>(Arrays.asList(1f, 5f, 10f));

		allDataSets.get(0).setProgressBar(new JProgressBar());

		int counter = 0;
		for (int r = 0; r < sigmaRender.size(); r++) {
			for (int q = 0; q < epitopeDensities.size(); q++) {
				for (int m = 0; m < bgLabelPerMM3.size(); m++) {
					for (int o = 0; o < bindingAngles.size(); o++) {
						for (int nl = 0; nl < nbrLabel.size(); nl++) {
							for (int ll = 0; ll < photonOutput.size(); ll++) {
								for (int s = 0; s < labelLength.size(); s++) {
									for (int a = 0; a < varAng.size(); a++) {
										for (int i = 0; i < sigmaXY.size(); i++) {
											for (int j = 0; j < le.size(); j++) {
												for (int k = 0; k < koff.size(); k++) {
													for (int p = 0; p < numberOfSimulationsWithSameParameterSet; p++) {
														counter += 1;
														params.data.epitopeDensity = (float) epitopeDensities.get(q);
														params.data.labelEpitopeDistance = labelLength.get(s);
														params.data.angularDeviation = (float) (varAng.get(a) / 180.
																* Math.PI);
														params.data.labelingEfficiency = le.get(j);
														params.data.sigmaXY = sigmaXY.get(i);
														params.data.sigmaZ = sigmaZ.get(i);
														params.data.dutyCycle = koff.get(k);
														params.data.fluorophoresPerLabel = nbrLabel.get(nl);
														params.data.backgroundPerMicroMeterCubed = bgLabelPerMM3.get(m);
														params.data.angleOfLabel = bindingAngles.get(o);
														params.data.sigmaRendering = sigmaRender.get(r);
														params.data.borders = getSpecificBorders();
														calculate(params);
														// params.detectionEfficiency = de.get(i);
														// params.recordedFrames = frames.get(i);
														try {
															params.data.borders = getBorders();
														} catch (NullPointerException e) {
															e.printStackTrace();
														}
														params.data.MeanPhotonNumber = photonOutput.get(ll);
														// String fname =
														// String.format("sig%1.0f_%1.0flabEff%1.0fPhoton%dbindAng%1.0fLabLen%1.0fver%d",
														// params.sigmaXY,params.sigmaZ,params.labelingEfficiency,params.MeanPhotonNumber,params.angleOfLabel*180/Math.PI,params.labelEpitopeDistance,p);
														String fname = String.format(
																"sig%1.0f_%1.0fLabEff%1.0fBGPerMM3%1.0fLabelLen%1.2fnbrLab%1.1fBindAngl%2.0fEpiDens%3.5fSigRend%1.0f",
																params.data.sigmaXY, params.data.sigmaZ,
																params.data.labelingEfficiency,
																params.data.backgroundPerMicroMeterCubed,
																params.data.labelEpitopeDistance,
																params.data.fluorophoresPerLabel, params.data.angleOfLabel,
																params.data.epitopeDensity, params.data.sigmaRendering);
														if (suReSimOutput) {
															new File(outputFolder + fname + File.separator).mkdir();
															exportData(outputFolder + fname + File.separator, fname,
																	params);
														}
														if (tiffStackOutput) {

															float[][] calibr = allDataSets.get(0).getParameterSet()
																	.getCalibrationFile();
															allDataSets.get(0).setProgressBar(new JProgressBar());
															params.data.sigmaXY = 0.f;
															params.data.sigmaZ = 0.f;
															calculate(params);
															CreateStack.createTiffStack(allDataSets.get(0).stormData,
																	1 / 133.f/** resolution */
																	, 10/** emptyspace */
																	, 10.f/** emGain */
																	, params.data.borders, random,
																	4.81f/** electrons per AD */
																	, (float) 30.f/** frameRate */
																	, 0.03f/** blinking duration */
																	, 0.00f /** dead time */
																	, 15/** sizePSF */
																	, 1/** modelNR */
																	, 1.f, (float) 1.45f/** NA */
																	, 647.f/** waveLength */
																	, 200.f/** zFocus */
																	, 600.f/** zDefocus */
																	, 35.7f/** sigmaNoise */
																	, 200.f/** constant offset */
																	, calibr/** calibration file */
																	,
																	outputFolder + fname + File.separator + fname
																			+ "TiffStack.tif",
																	false /* ensure single PSF */,
																	true /* split blinking over frames */,
																	new CreateTiffStack(null, null, null, null));

														}
														System.out.println(String.format("run %d of %d", counter,
																sigmaXY.size() * koff.size() * le.size() * varAng.size()
																		* labelLength.size()
																		* numberOfSimulationsWithSameParameterSet
																		* bgLabelPerMM3.size() * bindingAngles.size()
																		* nbrLabel.size() * epitopeDensities.size()
																		* sigmaRender.size()));
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	public static void processMultipleFiles() {
		boolean tiffStackOutput = false;
		boolean suReSimOutput = true;
		SimulationParameter params = standardParameterSingleEpitopes();
		params.data.useSTORMBlinking = false;
		String importPath = "/Users/sandrobraun/Desktop/inputs";
		String outputFolder = "/Users/sandrobraun/Desktop/outputs";
		(new File(outputFolder)).mkdir();
		File[] listFiles = (new File(importPath)).listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				File f = new File(dir.getAbsolutePath() + File.separator + name);
				return TriangleLineFilter.Utils.accept(f);
			}
		});
		for (int i = 0; i < listFiles.length; i++) {
			File file = listFiles[i];
			proceedFileImport(file);

			calculate(params);
			params.data.borders = getBorders();
			String fname = FilenameUtils.getBaseName(file.getName());

			if (suReSimOutput) {
				new File(outputFolder + fname + File.separator).mkdir();
				exportData(outputFolder + fname + File.separator, fname, params);
			}
			if (tiffStackOutput) {

				float[][] calibr = allDataSets.get(0).getParameterSet().getCalibrationFile();
				allDataSets.get(0).setProgressBar(new JProgressBar());
				params.data.sigmaXY = 0.f;
				params.data.sigmaZ = 0.f;
				calculate(params);
				CreateStack.createTiffStack(allDataSets.get(0).stormData, 1 / 133.f/** resolution */
						, 10/** emptyspace */
						, 10.f/** emGain */
						, params.data.borders, random, 4.81f/** electrons per AD */
						, (float) 30.f/** frameRate */
						, 0.03f/** blinking duration */
						, 0.00f /** dead time */
						, 15/** sizePSF */
						, 1/** modelNR */
						, 1.f, (float) 1.45f/** NA */
						, 647.f/** waveLength */
						, 200.f/** zFocus */
						, 600.f/** zDefocus */
						, 35.7f/** sigmaNoise */
						, 200.f/** constant offset */
						, calibr/** calibration file */
						, outputFolder + fname + File.separator + fname + "TiffStack.tif",
						false /* ensure single PSF */, true /* split blinking over frames */,
						new CreateTiffStack(null, null, null, null));

			}
		}
	}

	private static void createRandomEpitopesOnLine(double length, int nbrEpitopes) {
		float[][] bp = new float[nbrEpitopes][3];
		float[][] ep = new float[nbrEpitopes][3];
		for (int i = 0; i < nbrEpitopes; i++) {
			bp[i][0] = (float) (Math.random() * length);
			bp[i][1] = 0.f;
			bp[i][2] = 0.f;
			ep[i][0] = 0.f;
			ep[i][1] = 0.f;
			ep[i][2] = 1.f;
		}
		((EpitopeDataSet) allDataSets.get(0)).epitopeBase = bp;
		((EpitopeDataSet) allDataSets.get(0)).epitopeEnd = ep;
	}

	private static SimulationParameter standardParameterActin() {
		SimulationParameter params = new SimulationParameter();
		params.data.angularDeviation = 0;
		params.data.angleOfLabel = (float) (Math.PI / 2);
		params.data.backgroundPerMicroMeterCubed = 0;
		params.data.coupleSigmaIntensity = true;
		params.data.detectionEfficiency = 100;
		params.data.epitopeDensity = (float) 0.0115;
		params.data.fluorophoresPerLabel = 1;
		params.data.dutyCycle = (float) (1.0 / 2000.f);
		params.data.labelEpitopeDistance = 0.6f;
		params.data.labelingEfficiency = 10;
		params.data.makeItReproducible = false;
		params.data.MeanPhotonNumber = 4000;
		params.data.radiusOfFilament = (float) 12.5;
		params.data.recordedFrames = 10000;
		params.data.sigmaXY = 8;
		params.data.sigmaZ = 30;
		params.data.viewStatus = 1;
		params.data.colorProof = true;
		return params;
	}

	private static SimulationParameter standardParameterMicrotubules() {
		SimulationParameter params = new SimulationParameter();
		params.data.angularDeviation = 0;
		params.data.angleOfLabel = (float) (Math.PI / 2);
		params.data.backgroundPerMicroMeterCubed = 0;
		params.data.coupleSigmaIntensity = true;
		params.data.detectionEfficiency = 100;
		params.data.epitopeDensity = (float) 1.625;
		params.data.fluorophoresPerLabel = 1.5f;
		params.data.dutyCycle = (float) (1.0 / 2000.f);
		params.data.labelEpitopeDistance = 16;
		params.data.labelingEfficiency = 10;
		params.data.makeItReproducible = true;
		params.data.MeanPhotonNumber = 4000;
		params.data.radiusOfFilament = (float) 12.5;
		params.data.recordedFrames = 10000;
		params.data.sigmaXY = 4;
		params.data.sigmaZ = 8;
		params.data.viewStatus = 1;
		params.data.colorProof = true;
		return params;
	}

	private static SimulationParameter standardParameterSingleEpitopes() {
		SimulationParameter params = new SimulationParameter();
		params.data.angularDeviation = (float) (90.f / 180 * Math.PI);
		params.data.angleOfLabel = (float) (90.f / 180 * Math.PI);
		params.data.backgroundPerMicroMeterCubed = 0;
		params.data.coupleSigmaIntensity = false;
		params.data.detectionEfficiency = 100;
		params.data.epitopeDensity = (float) 1.625;
		params.data.fluorophoresPerLabel = 1;
		params.data.dutyCycle = (float) (1 / 5000.f);
		params.data.labelEpitopeDistance = 8;
		params.data.labelingEfficiency = 25f;
		params.data.makeItReproducible = true;
		params.data.MeanPhotonNumber = 3000;
		params.data.radiusOfFilament = (float) 12.5;
		params.data.recordedFrames = 15000;
		params.data.sigmaXY = 8;
		params.data.sigmaZ = 30;
		params.data.viewStatus = 1;
		params.data.sigmaRendering = 5;
		params.data.pixelsize = 10;
		return params;
	}

	private static SimulationParameter standardParameterRandomlyDistributedEpitopes() {
		SimulationParameter params = new SimulationParameter();
		params.data.angularDeviation = 1000;
		params.data.angleOfLabel = (float) (90.f / 180 * Math.PI);
		params.data.backgroundPerMicroMeterCubed = 0;
		params.data.coupleSigmaIntensity = true;
		params.data.detectionEfficiency = 100;
		params.data.epitopeDensity = (float) 1.625;
		params.data.fluorophoresPerLabel = 1;
		params.data.dutyCycle = (float) (1.0 / 2000.f);
		params.data.labelEpitopeDistance = 16;
		params.data.labelingEfficiency = 90;
		params.data.makeItReproducible = false;
		params.data.MeanPhotonNumber = 3000;
		params.data.radiusOfFilament = (float) 0;
		params.data.recordedFrames = 50000;
		params.data.sigmaXY = 6;
		params.data.sigmaZ = 30;
		params.data.viewStatus = 1;
		params.data.sigmaRendering = 5;
		params.data.pixelsize = 2.5;
		return params;
	}

	private static SimulationParameter standardParameterMicrotubules1nm() {
		SimulationParameter params = new SimulationParameter();
		params.data.angularDeviation = 0;
		params.data.angleOfLabel = (float) (Math.PI / 2);
		params.data.backgroundPerMicroMeterCubed = 50;
		params.data.coupleSigmaIntensity = true;
		params.data.detectionEfficiency = 100;
		params.data.epitopeDensity = (float) 1.625;
		params.data.fluorophoresPerLabel = 1;
		params.data.dutyCycle = (float) (1.0 / 2000.f);
		params.data.labelEpitopeDistance = 1;
		params.data.labelingEfficiency = 10;
		params.data.makeItReproducible = false;
		params.data.MeanPhotonNumber = 4000;
		params.data.radiusOfFilament = (float) 12.5;
		params.data.recordedFrames = 10000;
		params.data.sigmaXY = 4;
		params.data.sigmaZ = 8;
		params.data.viewStatus = 1;
		return params;
	}

	private static SimulationParameter standardParameterVesicles() {
		SimulationParameter params = new SimulationParameter();
		params.data.angularDeviation = 0;
		params.data.angleOfLabel = (float) (Math.PI / 2);
		params.data.backgroundPerMicroMeterCubed = 0;
		params.data.coupleSigmaIntensity = true;
		params.data.detectionEfficiency = 100;
		params.data.epitopeDensity = (float) 0.00626;
		params.data.fluorophoresPerLabel = 1.5f;
		params.data.dutyCycle = (float) (1.0 / 2000.f);
		params.data.labelEpitopeDistance = 16;
		params.data.labelingEfficiency = 10;
		params.data.makeItReproducible = true;
		params.data.MeanPhotonNumber = 4000;
		params.data.radiusOfFilament = (float) 12.5;
		params.data.recordedFrames = 10000;
		params.data.sigmaXY = 4;
		params.data.sigmaZ = 8;
		params.data.viewStatus = 1;
		params.data.colorProof = false;
		return params;
	}

	private static String getShortFilename(ArrayList<String> names, ArrayList<String> values) {
		String filename = "";

		return filename;
	}

	private static ArrayList<Float> getSpecificBorders() {
		ArrayList<Float> retList = new ArrayList<Float>();
		retList.add(-1000f);
		retList.add(1640f);
		retList.add(-1000f);
		retList.add(1640f);
		retList.add(-50f);
		retList.add(250f);
		return retList;
	}

	private static ArrayList<Float> getBorders() {
		DataSet thisDataSet = allDataSets.get(0);
		ArrayList<Float> retList = new ArrayList<Float>();
		retList.add(Calc.min(thisDataSet.stormData, 0));
		retList.add(Calc.max(thisDataSet.stormData, 0));
		retList.add(Calc.min(thisDataSet.stormData, 1));
		retList.add(Calc.max(thisDataSet.stormData, 1));
		retList.add(Calc.min(thisDataSet.stormData, 2));
		retList.add(Calc.max(thisDataSet.stormData, 2));

		return retList;

	}

	private static void furtherProceedFileImport(DataSet data, DataType type) {
		if (data.dataType.equals(DataType.TRIANGLES)) {
			System.out.println("Triangles parsed correctly.");
		} else if (type.equals(DataType.LINES)) {
			System.out.println("Lines parsed correctly.");
		} else if (type.equals(DataType.PLY)) {
			System.out.println("PLY file parsed.");
		}

		allDataSets.set(0, data);

	}

	private static void calculate(SimulationParameter params) {
		setUpRandomNumberGenerator(params.data.makeItReproducible);
		int currentRow = 0;
		allDataSets.get(currentRow).getParameterSet().setSoa((float) (params.data.angularDeviation));// sigma of angle
		allDataSets.get(currentRow).getParameterSet().setPabs((float) (params.data.labelingEfficiency / 100.));// Labeling
																											// efficiency
		allDataSets.get(currentRow).getParameterSet().setAoa((float) (params.data.angleOfLabel));
		allDataSets.get(currentRow).getParameterSet().setDeff((float) (params.data.detectionEfficiency / 100)); // detection
																											// efficiency
		allDataSets.get(currentRow).getParameterSet().setIlpmm3(params.data.backgroundPerMicroMeterCubed); // background per
																										// cubic micro
																										// meter
		allDataSets.get(currentRow).getParameterSet().setLoa(params.data.labelEpitopeDistance); // label epitope distance
		allDataSets.get(currentRow).getParameterSet().setFpab(params.data.fluorophoresPerLabel);// fluorophores per label
		allDataSets.get(currentRow).getParameterSet().setDutyCycle(params.data.dutyCycle);
		allDataSets.get(currentRow).getParameterSet().setBleachConst((float) 0);
		allDataSets.get(currentRow).getParameterSet().setFrames(params.data.recordedFrames); // recorded frames
		allDataSets.get(currentRow).getParameterSet().setSxy(params.data.sigmaXY);
		allDataSets.get(currentRow).getParameterSet().setSz(params.data.sigmaZ);
		allDataSets.get(currentRow).getParameterSet().setPsfwidth((float) 380);
		allDataSets.get(currentRow).getParameterSet().setMeanPhotonNumber(params.data.MeanPhotonNumber);
		allDataSets.get(currentRow).getParameterSet().setPixelsize(params.data.pixelsize);
		allDataSets.get(currentRow).getParameterSet().setSigmaRendering(params.data.sigmaRendering);
		allDataSets.get(currentRow).getParameterSet().setAbpf(params.data.fluorophoresPerLabel);
		allDataSets.get(currentRow).getParameterSet().setSigmaRendering(10);
		allDataSets.get(currentRow).getParameterSet().setColorProof(params.data.colorProof);
		allDataSets.get(currentRow).getParameterSet().setBorders(params.data.borders);
		allDataSets.get(currentRow).getParameterSet().setSigmaRendering(params.data.sigmaRendering);
		allDataSets.get(currentRow).getParameterSet().setuseSTORMBlinking(params.data.useSTORMBlinking);

		if (allDataSets.get(currentRow).dataType == DataType.LINES) {
			allDataSets.get(currentRow).getParameterSet().setBspnm(params.data.epitopeDensity);
			allDataSets.get(currentRow).getParameterSet().setRof(params.data.radiusOfFilament);
		} else {
			allDataSets.get(currentRow).getParameterSet().setBspsnm(params.data.epitopeDensity);
		}
		allDataSets.get(currentRow).getParameterSet().setMergedPSF(false);
		allDataSets.get(currentRow).getParameterSet().setApplyBleaching(false);
		allDataSets.get(currentRow).getParameterSet().setCoupleSigmaIntensity(params.data.coupleSigmaIntensity);
		STORMCalculator calc = new STORMCalculator(allDataSets.get(currentRow), random);
		calc = new STORMCalculator(allDataSets.get(currentRow), random);
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

	private static void proceedFileImport(File file) {
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
		furtherProceedFileImport(data, type);
	}

	private static void setUpRandomNumberGenerator(boolean makeItReproducible) {
		if (makeItReproducible) {
			random = new Random(2);
		} else {
			random = new Random(System.currentTimeMillis());
		}
	}

	private static void exportData(String path, String name, SimulationParameter params) {
		if (!name.endsWith(EXTENSIONIMAGEOUTPUT)) {
			name = name += EXTENSIONIMAGEOUTPUT;
		}
		path = path + name;
		System.out.println("Path to write project: " + path);
		System.out.println("project name: " + name);
		FileManager.ExportToFile(allDataSets.get(0), path, params.data.viewStatus, params.data.borders,
				allDataSets.get(0).getParameterSet().getPixelsize(), params.data.sigmaRendering, shifts);
	}
}

class SimulationParameter {
	SimulationParameterData data = new SimulationParameterData(false, 0, 10, 20, false, true);

	SimulationParameter() {

	}

}

class UserDefinedInputOutput {
	private static String inputFile;
	private static String outputFile;
	private static String outputFolder;
	private static String inputFolder;

	UserDefinedInputOutput() {

	}

	private static String getPath(int mode) {
		String tag = "";
		String lastInputPath = loadLastInputPath();
		JFileChooser fileChooserInput = new JFileChooser(lastInputPath);
		String lastOutputPath = loadLastOutputPath();
		JFileChooser fileChooserOutput = new JFileChooser(lastOutputPath);
		switch (mode) {
		case 0:
			tag = "Please select input data path:";
			fileChooserInput.setDialogTitle(tag);
			fileChooserInput.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fileChooserInput.showOpenDialog(null);
			inputFolder = fileChooserInput.getSelectedFile().toString();
			saveLastInputPath(inputFolder);
			return inputFolder + File.separator;
		case 1:
			tag = "Please select output data path:";
			fileChooserOutput.setDialogTitle(tag);
			fileChooserOutput.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fileChooserOutput.showOpenDialog(null);
			outputFolder = fileChooserOutput.getSelectedFile().toString();
			saveLastOutputPath(outputFolder);
			return outputFolder + File.separator;
		case 2:
			tag = "Please select input data Filename:";
			fileChooserInput.setDialogTitle(tag);
			fileChooserInput.setCurrentDirectory(new File(lastInputPath));
			fileChooserInput.showOpenDialog(null);
			fileChooserInput.setFileSelectionMode(JFileChooser.FILES_ONLY);
			inputFile = fileChooserInput.getSelectedFile().toString();
			saveLastInputPath(inputFile);
			return inputFile;
		case 3:
			tag = "Please select output data Filename:";
			fileChooserOutput.setDialogTitle(tag);
			fileChooserOutput.showOpenDialog(null);
			fileChooserOutput.setFileSelectionMode(JFileChooser.FILES_ONLY);
			outputFile = fileChooserOutput.getSelectedFile().toString();
			saveLastOutputPath(outputFile);
			return outputFile;
		default:
			return "something went wrong";
		}
	}

	private static String loadLastInputPath() {
		String lastPath = System.getProperty("user.home");
		try {
			lastPath = FileUtils.readFileToString(
					new File(System.getProperty("user.home") + File.separator + "lastPathChosenForLoading.txt"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			if (e1 instanceof FileNotFoundException) {
				System.out.println("File not found.");
			}
		}
		return lastPath;
	}

	private static void saveLastInputPath(String lastPath) {
		try {
			FileUtils.writeStringToFile(
					new File(System.getProperty("user.home") + File.separator + "lastPathChosenForLoading.txt"),
					lastPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static String loadLastOutputPath() {
		String lastPath = System.getProperty("user.home");
		try {
			lastPath = FileUtils.readFileToString(
					new File(System.getProperty("user.home") + File.separator + "lastPathChosenForSaving.txt"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			if (e1 instanceof FileNotFoundException) {
				System.out.println("File not found.");
			}
		}
		return lastPath;
	}

	private static void saveLastOutputPath(String lastPath) {
		try {
			FileUtils.writeStringToFile(
					new File(System.getProperty("user.home") + File.separator + "lastPathChosenForSaving.txt"),
					lastPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String getInputFolder() {
		return getPath(0);
	}

	public static String getOutputFolder() {
		return getPath(1);
	}

	public static String getInputFile() {
		return getPath(2);
	}

	public static String getOutputFile() {
		return getPath(3);
	}

}