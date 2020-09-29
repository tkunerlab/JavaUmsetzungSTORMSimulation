package inout;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

public class SimulationParameterIO {
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
			FileWriter parameterWriter;
			String writerPath = fname.getAbsolutePath();
			parameterWriter = new FileWriter(writerPath, true);

			// Writes text to a character-output stream
			BufferedWriter bufferWriter = new BufferedWriter(parameterWriter);
			bufferWriter.write(myData.toString());
			bufferWriter.close();

			log("SimulationParamterData saved at file location: " + writerPath + " Data: " + myData + "\n");
		} catch (IOException e) {
			log("Hmm.. Got an error while saving SimulationParamterData to file " + e.toString());
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
			log("\nSimulationParamterData loaded successfully from file " + fname.getPath());
			return data;

		} catch (Exception e) {
			log("error load cache from file " + e.toString());
			// TODO: this is bad.
			// the much more grow-up way of doing this is by defining an uninitialized (i.e.
			// empty)
			// Parameter object and to try to populate it when loading
			// If loading fails, the unmodified object is returned, which is uninitialized
			// See here
			// https://github.com/google/gson/blob/master/UserGuide.md#TOC-Primitives-Examples
			return new SimulationParameterData();
		}

	}

	private static void log(String string) {
		System.out.println(string);
	}

}