package inout;

import java.io.File;
import java.util.ArrayList;

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