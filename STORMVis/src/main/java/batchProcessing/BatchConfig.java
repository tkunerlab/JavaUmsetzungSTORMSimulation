package batchProcessing;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import inout.SimulationParameterData;
import inout.SimulationParameterIO;
import model.ParameterSet;

import javax.swing.JOptionPane;

import gui.DataTypeDetector.DataType;

class BatchParameter {
	public String name = "";
	ArrayList<Float> values = new ArrayList<Float>();
	
	public BatchParameter(Float val) {
		this.values.add(val);
	}
	
	//constructor for ranged value
	public BatchParameter(String name, Float start, Float end, Float stepsize) {
		this.name = name;
		for(Float t=start;t<=end;t+=stepsize) {
			this.values.add(t);
		}
	}
	
	public BatchParameter(String name, String s) {
		this.name = name;
		//parse String and create array with values
		
		//check for single value
		int pos = s.indexOf('[');
		if(pos>=0) { 
			//remove square brackets
			int index = s.indexOf(']');
			if(index>-1) {
				s = s.substring(pos+1, index);
			} else {
				//malformatted line
				JOptionPane.showMessageDialog(null,"Line does not contain ] for parameter " + this.name + "\nStill trying to parse line", "Malformatted Input", JOptionPane.ERROR_MESSAGE);
				s = s.substring(1);
			}
			//find substring -> delimter=,
			String[] substrings = s.split(";");
			for(String string : substrings) {
				//check if we have a number or a range
				index = string.indexOf('/');
				if(index>0){
					//find )
					int index2 = string.indexOf(')');
					int index3 = string.indexOf("...");
					
					Float start = Float.parseFloat(string.substring(0, index3));
					Float end = Float.parseFloat(string.substring(index3+3, index));
					Float stepsize = Float.parseFloat(string.substring(index+2, index2));
					for(Float t=start;t<=end;t+=stepsize) {
						this.values.add(t);
					}
				} else {
					this.values.add(Float.parseFloat(string)); //convert
				}
			}
		} else {
			this.values.add(Float.parseFloat(s));
		}
	}
}

public class BatchConfig {
	public String name = "experiment";
	public String out_path = "";
	public ArrayList<String> models = new ArrayList<String>();
	public int repeat_experiment = 1;				//how often should a single parameter combo be simulated
	public boolean output_rendering = true;
	public boolean output_tiffstack = true;
	public String calibration_file = "";
	public ArrayList<BatchParameter> parameters = new ArrayList<BatchParameter>();
	public int total_combos = 1;
	public boolean reproducible = true;
	public int viewstatus = 1;
	public float[] shifts = {0 , 0, 0};
	public ArrayList<Float> borders = new ArrayList<Float>();
	
	public boolean load(String path) {
		//try to open file
		Scanner sc;
		try {
			File f = new File(path);
			sc = new Scanner(f);
		} catch(FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		
		String section = "";
		boolean no_end_triggered = false; //if we need to read across multiple lines
		String curline = "";
		while(sc.hasNextLine()) {
			String line = sc.nextLine();
			if(line.isEmpty() || line.length()<=3) {
				continue;
			}
			
			//first of all delete every comments
			int pos = line.indexOf('#');
			if(pos==0) {
				continue;
			} else if (pos>0) {
				line = line.substring(0, pos);
			}
			
			if(line.charAt(0)=='[') {
				int pos2 = line.indexOf(']');
				if(pos2>0) {
					section = line.substring(1, pos2);
				} else {
					JOptionPane.showMessageDialog(null,"Section does not end with ]!\n Aborting ...", "Malformatted Input", JOptionPane.ERROR_MESSAGE);
					System.exit(-1);
				}
			} else {
				//if we are in parameter section
				if(section.equals("Parameters") && !no_end_triggered) {
					//first of all remove all whitespaces
					line = line.replace(" ", "");
					line = line.replace("\t", "");
					pos = line.indexOf('=');
					if(pos>0) {
						BatchParameter param = new BatchParameter(line.substring(0, pos), line.substring(pos+1));
						this.total_combos = this.total_combos * param.values.size();
						this.parameters.add(param);
					} else {
						JOptionPane.showMessageDialog(null,"Line in Parameter section does not contain = !!!\n Ignoring Line", "Malformatted Input", JOptionPane.ERROR_MESSAGE);
					}
				} else if(section.equals("General") && !no_end_triggered) {
					pos = line.indexOf('=');
					//obtain experiment parameters
					if(line.contains("Name")) {
						//must be given within "name"
						pos = line.indexOf('\"');
						int pos2 = line.indexOf('\"', pos+1);
						if(pos2-pos>0) {
							this.name = line.substring(pos+1, pos2);
						} else {
							JOptionPane.showMessageDialog(null,"Run name is not given within \"!\n Using default", "Malformatted Input", JOptionPane.ERROR_MESSAGE);
						}
					} else if(line.contains("OutputPath")) {
						pos = line.indexOf('\"');
						int pos2 = line.indexOf('\"', pos+1);
						if(pos2-pos-1>0) {
							this.out_path = line.substring(pos+1, pos2);
						} else {
							JOptionPane.showMessageDialog(null,"Output path is not given within \"!\n Using default", "Malformatted Input", JOptionPane.ERROR_MESSAGE);
						}
					} else if(line.contains("OutputRendering")) {
						String sub = line.substring(pos+1);
						sub = sub.replaceAll(" ", "");
						sub = sub.replaceAll("\t", "");
						this.output_rendering = (Integer.parseInt(sub)>0);
					} else if(line.contains("OutputTiffStack")) {
						String sub = line.substring(pos+1);
						sub = sub.replaceAll(" ", "");
						sub = sub.replaceAll("\t", "");
						this.output_tiffstack = (Integer.parseInt(sub)>0);
					} else if(line.contains("CalibrationFile")) {
						pos = line.indexOf('\"');
						int pos2 = line.indexOf('\"', pos+1);
						if(pos2-pos>0) {
							this.calibration_file = line.substring(pos+1, pos2);
						} else {
							JOptionPane.showMessageDialog(null,"CalibrationFile path is not given within \"!\n Using none", "Malformatted Input", JOptionPane.ERROR_MESSAGE);
						}
					}
					
				} else if(section.equals("Simulation") && !no_end_triggered){
					pos = line.indexOf('=');
					//everything regarding global simulation options
					if(line.contains("RepeatExperiment")) {
						String sub = line.substring(pos+1);
						sub = sub.replaceAll(" ", "");
						sub = sub.replaceAll("\t", "");
						this.repeat_experiment = Integer.parseInt(sub);
					} else if(line.contains("Reproducible")) {
						String sub = line.substring(pos+1);
						sub = sub.replaceAll(" ", "");
						sub = sub.replaceAll("\t", "");
						this.reproducible = (Integer.parseInt(sub)>0);
					} else if(line.contains("ViewStatus")) {
						String sub = line.substring(pos+1);
						sub = sub.replaceAll(" ", "");
						sub = sub.replaceAll("\t", "");
						this.reproducible = (Integer.parseInt(sub)>0);
					} else if(line.contains("Models")) { //TODO: currently models only support single line reading -> expand to multiple rows
						pos = line.indexOf('[');
						int pos2 = line.indexOf(']', pos+1);
						if(pos2-pos-1>0) {
							String sub = line.substring(pos+1, pos2);
							String[] substrings = sub.split(";");
							for(String string: substrings) {
								pos = string.indexOf("\"");
								pos2 = string.indexOf("\"", pos+1);
								if(pos2-pos-1>0) {
									this.models.add(string.substring(pos+1, pos2));
								} else {
									JOptionPane.showMessageDialog(null,"Modelname is empty or not given within \"\"!\n Aborting ...", "Malformatted Input", JOptionPane.ERROR_MESSAGE);
									System.exit(-1);
								}
							}
						} else {
							JOptionPane.showMessageDialog(null,"Models are empty or not given within []!\n Aborting ...", "Malformatted Input", JOptionPane.ERROR_MESSAGE);
							System.exit(-1);
						}
					}
				} else {
					JOptionPane.showMessageDialog(null,"Section does not exist!\n Aborting ...", "Malformatted Input", JOptionPane.ERROR_MESSAGE);
					System.exit(-1);
				}
			}
		}
		sc.close();
		if(no_end_triggered) {
			JOptionPane.showMessageDialog(null,"Line  has no end!\n Aborting ...", "Malformatted Input", JOptionPane.ERROR_MESSAGE);
			System.exit(-1);
		}
		
		/*
		//simple test to check if parsing works
		System.out.print(this.name +"\n");
		System.out.print(this.out_path + "\n");
		System.out.print(this.repeat_experiment);
		System.out.print("\n");
		System.out.print(this.output_rendering);
		System.out.print("\n");
		System.out.print(this.output_tiffstack);
		System.out.print("\n");
		for(int i=0;i<this.parameters.size();i++) {
			System.out.print(parameters.get(i).name + " " + parameters.get(i).values.toString() + "\n");
		}
		*/
		
		return true;
	}
	
	//recursion function
	private void createParamGrid(int level) {
		
	}
	
	public ArrayList<ParameterSet> convertToParamterSet() {
		
		//solve this via recursion
		int rep = 1; //how often should we repeat a single value
		int occ = this.total_combos;
		Float[][] combos = new Float[this.parameters.size()][this.total_combos];
		for(int i=0;i<this.parameters.size();i++) {
			occ = occ/this.parameters.get(i).values.size();
			int running_index = 0;
			for(int o=0;o<occ;o++) {
				for(int e=0;e<this.parameters.get(i).values.size();e++) {
					for(int r=0;r<rep;r++) {
						combos[i][running_index] = this.parameters.get(i).values.get(e);
						running_index++;
					}
				}
			}
			
			//next level
			rep = rep*this.parameters.get(i).values.size();
		}
		
		/*
		String test = "";
		for(int i=0;i<this.parameters.size();i++) {
			test = test + Arrays.toString(combos[i]) + "\n";
		}
		test = test + "\n";
		
		System.out.print(test);
		*/
		
		
		ArrayList<ParameterSet> pset = new ArrayList<ParameterSet>();
		for(int j=0;j<this.total_combos;j++) {
			ParameterSet p = new ParameterSet();
			for(int i=0;i<this.parameters.size();i++) {
				Float c = combos[i][j];
				switch(this.parameters.get(i).name) {
					case "LabelingEfficiency":
						p.setPabs(new Float(c/100.0));
						break;
					case "MeanAngleField":
						p.setAoa(new Float(c/180.0*Math.PI));
						break;
					case "AngularDistribution":
						p.setSoa(new Float(c/180.0*Math.PI));
						break;
					case "DetectionEfficiency":
						p.setDeff(new Float(c/100.0));
						break;
					case "BackgroundLabel":
						p.setIlpmm3(c);
						break;
					case "LabelLength":
						p.setLoa(c);
						break;
					case "FluorophoresPerLabel":
						p.setFpab(c);
						break;
					case "DutyCycle":
						p.setDutyCycle(c);
						break;
					case "BleachConstant":
						p.setBleachConst(c);
						break;
					case "RecordedFrames":
						p.setFrames(c.intValue());
						break;
					case "locPrecisionXY":
						p.setSxy(c);
						break;
					case "locPrecisionZ":
						p.setSz(c);
						break;
					case "PsfWidth":
						p.setPsfwidth(c);
						break;
					case "AveragePhotonNumber":
						p.setMeanPhotonNumber(c.intValue());
						break;
					case "EpitopeDensityL": //for datatype == LINES
						p.setBspnm(c);
						break;
					case "EpitopeDensity":
						p.setBspsnm(c);
						break;
					case "RadiusOfFilaments":
						p.setRof(c);
						break;
					case "PointSize":
						p.setPointSize(c);
						break;
					case "LineWidth":
						p.setLineWidth(c);
						break;
					case "ApplyBleaching":
						p.setApplyBleaching(c>0);
						break;
					case "CoupleSigmaIntensity":
						p.setCoupleSigmaIntensity(c==0); //inverted ????
						break;
					case "PixelToNmRatio":
						p.setPixelToNmRatio(c);
						break;
					case "FrameRate":
						p.setFrameRate(c);
						break;
					case "DeadTime":
						p.setDeadTime(c);
						break;
					case "SigmaBg":
						p.setSigmaBg(c);
						break;
					case "ConstantOffset":
						p.setConstOffset(c);
						break;
					case "EmGain":
						p.setEmGain(c);
						break;
					case "QuantumEfficiency":
						p.setQuantumEfficiency(c);
						break;
					case "WindowSizePSF":
						p.setWindowsizePSF(c.intValue());
						break;
					case "EmptyPixelsOnRim":
						p.setEmptyPixelsOnRim(c.intValue());
						break;
					case "Na":
						p.setNa(c);
						break;
					case "Focus":
						p.setFokus(c);
						break;
					case "Defocus":
						p.setDefokus(c);
						break;
					case "TiffStackMode":
						p.setTwoDPSF(c>0);
						break;
					case "ElectronPerAdCount":
						p.setElectronPerAdCount(c);
						break;
					case "MeanBlinkingTime":
						p.setMeanBlinkingTime(c);
						break;
					case "DistributePSFOverFrames":
						p.setDistributePSFoverFrames(c>0);
						break;
					case "EnsureSinglePSF":
						p.setEnsureSinglePSF(c>0);
						break;
					case "MinIntensity":
						p.setMinIntensity(c.intValue());
						break;
					case "PixelSize":
						p.setPixelsize(c);
						break;
					case "SigmaRendering":
						p.setSigmaRendering(c);
						break;
					case "BlueGreenOnly":
						p.setColorProof(c>0);
						break;
					default:
						JOptionPane.showMessageDialog(null, "Given Parameter does not exists: " + this.parameters.get(i).name + "!", "Malformatted Input", JOptionPane.ERROR_MESSAGE);
						System.exit(-1);
						break;
						
				}
			}
			pset.add(p);
		}

		return pset;
	}

}
