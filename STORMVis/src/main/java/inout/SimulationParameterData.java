package inout;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

public class SimulationParameterData {
	public float angleOfLabel;
	public float angularDeviation;
	public float labelingEfficiency;
	public float detectionEfficiency;
	public float backgroundPerMicroMeterCubed;
	public float labelEpitopeDistance;
	public float fluorophoresPerLabel;
	public int recordedFrames;
	public float dutyCycle;
	public float sigmaXY;
	public float sigmaZ;
	public int MeanPhotonNumber;
	public float radiusOfFilament;
	public float epitopeDensity;
	public boolean coupleSigmaIntensity;
	public boolean makeItReproducible;
	public ArrayList<Float> borders;
	public int viewStatus;
	public double pixelsize;
	public double sigmaRendering;
	public boolean colorProof;
	public boolean useSTORMBlinking;

	public SimulationParameterData(boolean makeItReproducible, int viewStatus, double pixelsize, double sigmaRendering,
			boolean colorProof, boolean useSTORMBlinking) {
		this.makeItReproducible = makeItReproducible;
		this.viewStatus = viewStatus;
		this.pixelsize = pixelsize;
		this.sigmaRendering = sigmaRendering;
		this.colorProof = colorProof;
		this.useSTORMBlinking = useSTORMBlinking;
	}

	public SimulationParameterData() {
		this.makeItReproducible = false;
		this.viewStatus = 1;
		this.pixelsize = 100.0;
		this.sigmaRendering = 100.0;
		this.colorProof = false;
		this.useSTORMBlinking = false;
	}

	public static void main(String[] args) {
		SimulationParameterData data = ExampleParameterData_Microtubules.make();
		File outputFile = new File(System.getProperty("user.home") + "/Desktop/outputs/foo.json");
		SimulationParameterIO.toFile(data, outputFile);
		data.angleOfLabel = (float) 0.0;
		data = SimulationParameterIO.fromFile(outputFile);
		outputFile = new File(System.getProperty("user.home") + "/Desktop/outputs/foo2.json");
		SimulationParameterIO.toFile(data, outputFile);
	}

}

class ExampleParameterData_Microtubules {
	public static SimulationParameterData make() {
		SimulationParameterData data = new SimulationParameterData(false, 1, 100, 100, false, false);
		data.angularDeviation = 0;
		data.angleOfLabel = (float) (Math.PI / 2);
		data.backgroundPerMicroMeterCubed = 0;
		data.coupleSigmaIntensity = true;
		data.detectionEfficiency = 100;
		data.epitopeDensity = (float) 1.625;
		data.fluorophoresPerLabel = 1.5f;
		data.dutyCycle = (float) (1.0 / 2000.f);
		data.labelEpitopeDistance = 16;
		data.labelingEfficiency = 10;
		data.makeItReproducible = true;
		data.MeanPhotonNumber = 4000;
		data.radiusOfFilament = (float) 12.5;
		data.recordedFrames = 10000;
		data.sigmaXY = 4;
		data.sigmaZ = 8;
		data.viewStatus = 1;
		data.colorProof = true;
		// TODO: not sure if I need to set those
		data.pixelsize = 100;
		data.sigmaRendering = 100;
		data.useSTORMBlinking = false;
		return data;
	}

}

class SimulationParameterIO {
	// Save to file Utility
	// TODO: I think this can be done much more nicely simply by inheritance, in
	// some way
	// But I don't have the time right now
	public static void toFile(SimulationParameterData data, File fname) {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		String myData = gson.toJson(data);
		if (!fname.exists()) {
			try {
				File directory = new File(fname.getParent());
				if (!directory.exists()) {
					directory.mkdirs();
				}
				fname.createNewFile();
			} catch (IOException e) {
				log("Excepton Occured: " + e.toString());
			}
		}

		try {
			// Convenience class for writing character files
			FileWriter crunchifyWriter;
			String writerPath = fname.getAbsolutePath();
			crunchifyWriter = new FileWriter(writerPath, true);

			// Writes text to a character-output stream
			BufferedWriter bufferWriter = new BufferedWriter(crunchifyWriter);
			bufferWriter.write(myData.toString());
			bufferWriter.close();

			log("Company data saved at file location: " + writerPath + " Data: " + myData + "\n");
		} catch (IOException e) {
			log("Hmm.. Got an error while saving Company data to file " + e.toString());
		}
	}

	// Read From File Utility
	public static SimulationParameterData fromFile(File fname) {
		if (!fname.exists())
			log("File doesn't exist");

		InputStreamReader isReader;
		try {
			isReader = new InputStreamReader(new FileInputStream(fname), "UTF-8");

			JsonReader myReader = new JsonReader(isReader);
			Gson gson = new Gson();
			SimulationParameterData data = gson.fromJson(myReader, SimulationParameterData.class);
			log("\nComapny Data loaded successfully from file " + fname.getPath());
			return data;

		} catch (Exception e) {
			log("error load cache from file " + e.toString());
			// TODO: this is bad.
			// the much more grow-up way of doing this is by defining an uninitialized (i.e.
			// empty)
			// Parameter object and to try to populate it when loading
			// I floading fails, the unmodified object is returned, which is uninitialized
			return new SimulationParameterData();
		}

	}

	private static void log(String string) {
		System.out.println(string);
	}

}