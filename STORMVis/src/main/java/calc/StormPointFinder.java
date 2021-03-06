package calc;


import gnu.trove.list.array.TIntArrayList;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JProgressBar;

import org.jzy3d.maths.Array;

import model.DataSet;
import model.ParameterSet;

/**
 * @brief finding stormPoints from endPoints
 *
 */
public class StormPointFinder {
	public static float[][] findStormPoints(float[][] listEndPoints, DataSet ds, STORMCalculator calc) {
		JProgressBar progressBar = ds.getProgressBar();
		ParameterSet ps = ds.getParameterSet();
		float sxy = ps.getSxy();
		float sz = ps.getSz();
		float fpab = ps.getFpab();
		boolean background = false;
		if (ps.getIlpmm3()>0){
			background = true;
		}
		
		float psfWidth = ps.getPsfwidth();
		float ilpmm3 = ps.getIlpmm3();
		boolean applyBleaching = ps.getApplyBleaching();
		
		if (background) { //unspecific labeling
			listEndPoints = addBackground(listEndPoints, ilpmm3,calc,ds.parameterSet.getBorders());	
    	}
		if (fpab != 1){
			listEndPoints = addMultipleFluorophoresPerAntibody(listEndPoints, fpab,calc);
		}
		ds.fluorophorePos = listEndPoints.clone();
		float[][] stormPoints;
		if (applyBleaching){
			stormPoints = createStormPointsRealisticBleaching(listEndPoints, ps, sxy, sz,  psfWidth, progressBar, 
					calc,ps.getFrames(), ps.getMeanPhotonNumber(), ps.getBleachConst(), applyBleaching);
		}
		else{
			if (ps.isUseSTORMBlinking()){
//			
			stormPoints = createStormPoints(listEndPoints, ps, sxy, sz, psfWidth, progressBar,
					calc, ps.getFrames(), ps.getMeanPhotonNumber(),ps.getMinIntensity());
			}
			else{
				stormPoints = createStormPointsPafp(listEndPoints, ps, 
						sxy, sz, psfWidth, progressBar,
						calc, ps.getFrames(), ps.getMeanPhotonNumber(), 0.01f,7.8f, 
						0.4f, 15.7f, 0.24f, 5.4f);
			}
		}
		
		return stormPoints;
		
		
		//stormPoints = assignFrameAndIntensity(stormPoints, ps.getFrames(), ps.getMeanPhotonNumber());
		//return new float[2][2];
	}
	
	//creates multiple blinking events based on the endpoints of the antibodies
	//also multiple fluorophores per antibody are taken into account
	private static float[][] createStormPoints(float[][] listEndPoints, ParameterSet ps, 
			float sxy, float sz, float psfWidth, JProgressBar progressBar,
			STORMCalculator calc, int frames, int meanPhotonNumber, int minPhotonNumber) {
		
		double k = 1.0/(meanPhotonNumber - minPhotonNumber);
		double norm = 1/k*Math.exp(-k*minPhotonNumber);
		//inverse density function: log(-k*randomNumber*norm+exp(-k*minPhotonNumber))/-k
		
					
		float[][] stormPoints = null;
		//individual number of blinking events per fluorophore
		float[] nbrBlinkingEvents = new float[listEndPoints.length];
		float abpf = ps.getFrames() * ps.getDutyCycle()* ps.getDeff(); //average blinking per fluorophore
		for (int i = 0; i < listEndPoints.length; i++) {
			nbrBlinkingEvents[i] = RandomClass.poissonNumber(abpf, calc.random);//(float) (calc.random.nextFloat() * Math.sqrt(abpf) + abpf);
			if(nbrBlinkingEvents[i] < 0) {
				nbrBlinkingEvents[i] = 0;
			}
		}
		float[][] stormPointsTemp = null;
		List<float[]> allStormPoints = new ArrayList<float[]>();
		System.out.println("floor: "+ Math.floor(Calc.max(nbrBlinkingEvents)));
		progressBar.setString("Create Localizations");
		for (int i = 0; i <= Math.floor(Calc.max(nbrBlinkingEvents)); i++) {
			//if (i%(Math.floor(Calc.max(nbrBlinkingEvents))/100)==0) {
				calc.publicSetProgress((int) (1.*i/Math.floor(Calc.max(nbrBlinkingEvents))*100.));
			//}
			List<Integer> idxArray = new ArrayList<Integer>();
			int countOne = 0;
			for (int j = 0; j < nbrBlinkingEvents.length; j++) {
				if(nbrBlinkingEvents[j] > i) {
					idxArray.add(new Integer(j));
					countOne++;
				}
			}
			float[] x = new float[countOne];
			float[] y = new float[countOne];
			float[] z = new float[countOne];
			int[] frame = new int[countOne];
			float[] intensity = new float[countOne];
			
			float[][] listEndPointsTranspose = Calc.transpose(listEndPoints);
			
			//detections in higher frames might already be bleached idxList contains only valid indices
			ArrayList<Integer> idxList= new ArrayList<Integer>();
			
			for (int k1 = 0; k1 < idxArray.size(); k1++) {
				intensity[k1] = (float) (Math.log(-k*calc.random.nextDouble()*norm+Math.exp(-k*minPhotonNumber))/-k);
				frame[k1] = (int) (calc.random.nextDouble()*frames);
				idxList.add(k1);
				if (ps.getCoupleSigmaIntensity()){
					x[k1] = (float) (listEndPointsTranspose[0][idxArray.get(k1).intValue()] + calc.random.nextGaussian()*(sxy/Math.sqrt(intensity[k1]/meanPhotonNumber)));
					y[k1] = (float) (listEndPointsTranspose[1][idxArray.get(k1).intValue()] + calc.random.nextGaussian()*(sxy/Math.sqrt(intensity[k1]/meanPhotonNumber)));
					z[k1] = (float) (listEndPointsTranspose[2][idxArray.get(k1).intValue()] + calc.random.nextGaussian()*(sz/Math.sqrt(intensity[k1]/meanPhotonNumber)));
				}
				else {
					x[k1] = (float) (listEndPointsTranspose[0][idxArray.get(k1).intValue()] + calc.random.nextGaussian()*(sxy));
					y[k1] = (float) (listEndPointsTranspose[1][idxArray.get(k1).intValue()] + calc.random.nextGaussian()*(sxy));
					z[k1] = (float) (listEndPointsTranspose[2][idxArray.get(k1).intValue()] + calc.random.nextGaussian()*(sz));
				}
				
			}
			
			// TODO: intensity distribution
			stormPointsTemp = new float[idxList.size()][5];
						
			for(int j = 0; j < idxList.size(); j++) {
				stormPointsTemp[j][0] = x[idxList.get(j)];
				stormPointsTemp[j][1] = y[idxList.get(j)];
				stormPointsTemp[j][2] = z[idxList.get(j)];
				stormPointsTemp[j][3] = frame[idxList.get(j)];
				stormPointsTemp[j][4] = intensity[idxList.get(j)];
			
				allStormPoints.add(stormPointsTemp[j]);
			}
		}
		stormPoints = Calc.toFloatArray(allStormPoints);
		System.out.println("Number localizations: "+ allStormPoints.size());
		calc.publicSetProgress((int) (100));
		return stormPoints;
	}

	
	private static float[][] createStormPointsRealisticBleaching(float[][] listEndPoints, ParameterSet ps, 
			float sxy, float sz, float psfWidth, JProgressBar progressBar,
			STORMCalculator calc, int frames, int meanPhotonNumber,float kBleach, boolean applyBleaching) {
		progressBar.setString("Create Localizations");
		double k = -Math.log(0.5)/(meanPhotonNumber-1000);
		double factor = 500;
		ArrayList<Float> intensities = new ArrayList<Float>();
		for (int i = 1000; i<20*meanPhotonNumber; i++){
			for (int j = 0; j<Math.floor(factor * Math.exp(-k*i)); j++){
				//System.out.println(Math.ceil(factor * Math.exp(-k*i)));
				intensities.add((float) i);
			}
		}
		factor = 1000;
		ArrayList<Integer> maxFrames = new ArrayList<Integer>();
		for (int i = 0; i<listEndPoints.length; i++){
			while (true){
				int maxFrame =(int) (calc.random.nextDouble() * frames*10);
				//with bleaching activated higher frames have a lower probability to be populated
				double randomNumber = calc.random.nextDouble(); //random number is equally distributed between 0 and 1 and it is used
				double tmp = Math.exp(-kBleach*maxFrame);
				if (randomNumber < tmp){ //to be tested for the probability that this frame gets this 
					maxFrames.add(maxFrame); //localization. If it is smaller maxFrame is stored and the while loop is exited
					break;
				}		//if it is to large a new maxFrame is determined.

			}
		}
				
		List<float[]> allStormPoints = new ArrayList<float[]>();
		float[][] stormPoints = null;
		float x;
		float y;
		float z;
		for (int i = 0; i< listEndPoints.length; i++){
			calc.publicSetProgress((int) (1.*i/listEndPoints.length*100.));
			for (int frame = 0; frame < maxFrames.get(i); frame ++){
				double blinkingTest = calc.random.nextDouble();
				if (frame>frames){
					break;
				}
				if (blinkingTest <= ps.getDutyCycle()){
					float intensity = intensities.get((int) (calc.random.nextDouble()*intensities.size()-1));
					if (ps.getCoupleSigmaIntensity()){
						x = (float) (listEndPoints[i][0] + calc.random.nextGaussian()*(sxy/Math.sqrt(intensity/meanPhotonNumber)));
						y = (float) (listEndPoints[i][1] + calc.random.nextGaussian()*(sxy/Math.sqrt(intensity/meanPhotonNumber)));
						z = (float) (listEndPoints[i][2] + calc.random.nextGaussian()*(sz/Math.sqrt(intensity/meanPhotonNumber)));
					}
					else {
						x = (float) (listEndPoints[i][0] + calc.random.nextGaussian()*(sxy));
						y = (float) (listEndPoints[i][1] + calc.random.nextGaussian()*(sxy));
						z = (float) (listEndPoints[i][2] + calc.random.nextGaussian()*(sz));
					}
					float tmpLoc[] = {x,y,z,frame,intensity};
					allStormPoints.add(tmpLoc);
				}
			}
		}
		stormPoints = Calc.toFloatArray(allStormPoints);
		System.out.println("Number localizations: "+ allStormPoints.size());
		calc.publicSetProgress((int) (100));
		return stormPoints;
	}
	
	private static float[][] createStormPointsPafp(float[][] listEndPoints, ParameterSet ps, 
			float sxy, float sz, float psfWidth, JProgressBar progressBar,
			STORMCalculator calc, int frames, int meanPhotonNumber, float ka,float kd, 
			float kr1, float kr2, float alpha, float kb) {
		for (int i = 0; i< 100; i++){
			System.err.println("Pafp mode selected!!!");
		}
		progressBar.setString("Create Localizations");
		double k = -Math.log(0.5)/(meanPhotonNumber-1000);
		double factor = 500;
		ArrayList<Float> intensities = new ArrayList<Float>();
		for (int i = 1000; i<20*meanPhotonNumber; i++){
			for (int j = 0; j<Math.floor(factor * Math.exp(-k*i)); j++){
				//System.out.println(Math.ceil(factor * Math.exp(-k*i)));
				intensities.add((float) i);
			}
		}
		factor = 1000;
		ArrayList<Integer> startFrames = new ArrayList<Integer>();
		for (int i = 0; i<listEndPoints.length; i++){
			while (true){
				int maxFrame =(int) (calc.random.nextDouble() * frames*10);
				//find first frame in which the activation appears
				double randomNumber = calc.random.nextDouble(); //random number is equally distributed between 0 and 1 and it is used
				double tmp = Math.exp(-ka*maxFrame*(ps.getDeadTime()+1/ps.getFrameRate()));
				if (randomNumber < tmp){ //to be tested for the probability that this frame gets this 
					startFrames.add(maxFrame); //localization. If it is smaller maxFrame is stored and the while loop is left
					break;
				}		//if it is to large a new maxFrame is determined.

			}
		}
		List<float[]> allStormPoints = new ArrayList<float[]>();
		float[][] stormPoints = null;
		float x;
		float y;
		float z;
		for (int i = 0; i< listEndPoints.length; i++){
			calc.publicSetProgress((int) (1.*i/listEndPoints.length*100.));
			int startFrame = startFrames.get(i);
			if (startFrame>frames){
				continue;
			}
			int counter = 0;
			float dt = (ps.getDeadTime()+1/ps.getFrameRate()); //time
			//probabilities that a transfer occurs in this frame
			double pAtoBleach = 1 - Math.exp(-kb*dt);
			double pAtoDark = 1 - Math.exp(-kd*dt);
			double pDarktoA = 1-1/(1+alpha) * (Math.exp(-kr1*dt)+alpha*Math.exp(-kr2*dt));
			boolean notBleached = true;
			int state = 1; //1 corresponds to active, 2 corresponds to dark, 3 corresponds to bleached
			while (notBleached){
				switch (state){
				case 1://active
					double randomNumber = calc.random.nextDouble();
					double randomNumber4 = calc.random.nextDouble();
					boolean aToBleach =  (pAtoBleach > randomNumber);
					boolean aToDark = (pAtoDark > randomNumber4);
					if (aToBleach && aToDark){//if both would have happened chose based on the likelihood for each process which happens
						double randomNumber2 = calc.random.nextDouble() * (pAtoBleach + pAtoDark);
						if (randomNumber2>pAtoDark){//bleaching occurs
							state = 3;
							notBleached = false;
						}
						else{
							state = 2;
						}
					}
					else if(aToBleach){
						state = 3;
						notBleached = false;
					}
					else if(aToDark){
						state = 2;
					}
					else{//stays in active state
						float intensity = intensities.get((int) (calc.random.nextDouble()*intensities.size()-1));
						if (ps.getCoupleSigmaIntensity()){
							x = (float) (listEndPoints[i][0] + calc.random.nextGaussian()*(sxy/Math.sqrt(intensity/meanPhotonNumber)));
							y = (float) (listEndPoints[i][1] + calc.random.nextGaussian()*(sxy/Math.sqrt(intensity/meanPhotonNumber)));
							z = (float) (listEndPoints[i][2] + calc.random.nextGaussian()*(sz/Math.sqrt(intensity/meanPhotonNumber)));
						}
						else {
							x = (float) (listEndPoints[i][0] + calc.random.nextGaussian()*(sxy));
							y = (float) (listEndPoints[i][1] + calc.random.nextGaussian()*(sxy));
							z = (float) (listEndPoints[i][2] + calc.random.nextGaussian()*(sz));
						}
						float tmpLoc[] = {x,y,z,startFrame + counter,intensity};
						allStormPoints.add(tmpLoc);
					}
					break;
				case 2:
					double randomNumber3 = calc.random.nextDouble();
					if (pDarktoA > randomNumber3){
						state = 1;
					}
					break;
				case 3://should never be reached
					break;
				}
				counter += 1;
			}
		}
		stormPoints = Calc.toFloatArray(allStormPoints);
		System.out.println("Number localizations: "+ allStormPoints.size());
		calc.publicSetProgress((int) (100));
		return stormPoints;
	}
	
	private static float[][] addMultipleFluorophoresPerAntibody(float[][] listEndPoints, float fpab
			,STORMCalculator calc) {
		int[] idx = new int[listEndPoints.length]; 
		for (int i = 0; i<listEndPoints.length;i++) { //get random number of fluorophore for each antibody
			idx[i] = RandomClass.poissonNumber(fpab, calc.random);//(int) Math.abs(Math.floor(calc.random.nextGaussian() * Math.sqrt(fpab)+fpab));
		}
		for (int i = 0; i < idx.length; i++) {//make sure that each antibodies has at least 1 fluorophore
			if(idx[i] == 0) {
				idx[i] = 1;
			}
		}
		List<float[]> listEndPointsAugmented = new ArrayList<float[]>();
		for (int i=1; i <= Calc.max(idx);i++) {
			List<float[]> alteredPoints = new ArrayList<float[]>();
			for (int k = 0; k < idx.length; k++) {
				if(idx[k]>=i) {
					alteredPoints.add(Array.clone(listEndPoints[k]));
				}
			}
			
			float[][] altPoints = Calc.toFloatArray(alteredPoints);
			
			for (int c = 0; c < altPoints.length; c++) {
				for (int u = 0; u < 3; u++) {
					//find fluorophores in a sphere around the endpoint of the antibody
					altPoints[c][u] = (float) (altPoints[c][u] + calc.random.nextGaussian()*1.5);
				}
			}
			
			for (int p = 0; p < altPoints.length; p++) {
				float[] tmp = new float[5];
				tmp = altPoints[p];
				listEndPointsAugmented.add(Array.clone(tmp));
			}
		}
		listEndPoints = Calc.toFloatArray(listEndPointsAugmented);
		return listEndPoints;
	}

	private static float[][] addBackground(float[][] listEndPoints, float ilpmm3,STORMCalculator calc, List<Float> borders) {
		float xmin = borders.get(0);//Calc.min(listEndPoints, 0);
		float xmax = borders.get(1);//Calc.max(listEndPoints, 0);
		float ymin = borders.get(2);//Calc.min(listEndPoints, 1);
		float ymax = borders.get(3);//Calc.max(listEndPoints, 1);
		float zmin = borders.get(4);//Calc.min(listEndPoints, 2);
		float zmax = borders.get(5);//Calc.max(listEndPoints, 2);

		//ilpmm3: //incorrect localizations per micrometer ^3
		int numberOfIncorrectLocalizations = (int) Math.floor(ilpmm3*(xmax-xmin)/1e3*(ymax-ymin)/1e3*(zmax-zmin)/1e3);
		System.out.println("noil:" + numberOfIncorrectLocalizations);
		float[] x = Calc.randVector(numberOfIncorrectLocalizations, xmin, xmax,calc);
		float[] y = Calc.randVector(numberOfIncorrectLocalizations, ymin, ymax,calc);
		float[] z = Calc.randVector(numberOfIncorrectLocalizations, zmin, zmax,calc);
		if(numberOfIncorrectLocalizations == 0) {
			System.out.println("No coordinates to append.");
		}
		else {
			for (int j = 0; j < numberOfIncorrectLocalizations; j++) {
				listEndPoints = Calc.appendLine(listEndPoints, new float[]{x[j],y[j],z[j]});
			}
		}
		return listEndPoints;
	}
}
