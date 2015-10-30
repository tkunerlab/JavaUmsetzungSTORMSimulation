package calc;


import gui.DataTypeDetector.DataType;

import java.util.List;
import java.util.Random;

import javax.swing.SwingWorker;

import model.DataSet;
import model.LineDataSet;
import model.ParameterSet;
import model.TriangleDataSet;

import org.javatuples.Pair;

/**
 * 
 * @brief Complete STORM simulation management
 *
 */
public class STORMCalculator extends SwingWorker<Void, Void>{

    List<float[][]> trList;
    
    DataSet currentDataSet;
    public Random random;
	
	public STORMCalculator(DataSet set, Random random) {
		this.currentDataSet = set;
		this.random = random;
	}
	
	
	public DataSet getCurrentDataSet() {
		return currentDataSet;
	}



	public void setCurrentDataSet(DataSet currentDataSet) {
		this.currentDataSet = currentDataSet;
	}


	@Override
	public Void doInBackground() {
		long start = System.nanoTime();
		if(currentDataSet != null) {
			doSimulation();
		}
		System.out.println("Whole converting and simulation time: "+ (System.nanoTime()-start)/1e9 +"s");
		System.out.println("-------------------------------------");
		return null;
	}
	@Override
	public void done(){
		System.out.println("Worker finished");
		currentDataSet.getProgressBar().setString("Calculation Done!");
	}
	public void doSimulation() {
		float[][] ep = null; 
		float[][] ap = null;
		if(currentDataSet.dataType == DataType.TRIANGLES) {
			TriangleDataSet currentTrs = (TriangleDataSet) currentDataSet;
			Pair<float[][],float[][]> p = Finder.findAntibodiesTri(currentTrs.primitives, currentDataSet, this);
			ep = p.getValue1(); 
			ap = p.getValue0();
			float [][] epCopy = new float[ep.length][];
			float [][] apCopy = new float[ap.length][];
			for(int i = 0; i< ep.length; i++){
				epCopy[i] = ep[i].clone();
				apCopy[i] = ap[i].clone();
			}
			currentDataSet.antiBodyEndPoints = epCopy;
			currentDataSet.antiBodyStartPoints = apCopy;
			
		}
		else if(currentDataSet.dataType == DataType.LINES) {
			LineDataSet currentLines = (LineDataSet) currentDataSet;
			Pair<float[][],float[][]> p = Finder.findAntibodiesLines(currentLines.data, currentDataSet, this);
			ap = p.getValue0();
			ep = p.getValue1();
			float [][] epCopy = new float[ep.length][];
			float [][] apCopy = new float[ap.length][];
			for(int i = 0; i< ep.length; i++){
				epCopy[i] = ep[i].clone();
				apCopy[i] = ap[i].clone();
			}
			currentDataSet.antiBodyEndPoints = epCopy;
			currentDataSet.antiBodyStartPoints = apCopy;
			
			
		}
		else if(currentDataSet.dataType == DataType.EPITOPES){
			ep = currentDataSet.antiBodyEndPoints;
			ap = currentDataSet.antiBodyStartPoints;
			ParameterSet ps = currentDataSet.parameterSet;
			for (int i = 0; i<currentDataSet.antiBodyStartPoints.length;i++){
				float[] normTri = new float[3];
				for (int j = 0; j<3; j++){
					normTri[j] = ep[i][j] - ap[i][j];
				}
				float angleDeviation = (float) (random.nextGaussian()*ps.getSoa()); //same procedure as for triangls
				float[] vec = Calc.getVectorTri(ps.getAoa()+angleDeviation,ps.getLoa(),this); //a random vector with the set angle is created
				vec = Finder.findRotationTri(normTri,vec); //the surface normal is rotated 
				double length = Math.sqrt(vec[0]*vec[0]+vec[1]*vec[1]+vec[2]*vec[2]);
				for (int j = 0; j<3; j++){
					ep[i][j] = (float) (ap[i][j] + vec[j]/length*currentDataSet.parameterSet.getLoa());
				}
				
			}
			currentDataSet.antiBodyEndPoints = ep;
			currentDataSet.antiBodyStartPoints = ap;
		}
		float[][] result = StormPointFinder.findStormPoints(ep, currentDataSet, this);
		/**
		 * writing results to the current dataset
		 */
		currentDataSet.stormData = result;
		currentDataSet.getProgressBar().setString("Calculation Done!");
	}
	
	public void publicSetProgress(int prog){
		setProgress(prog);
	}
	
}