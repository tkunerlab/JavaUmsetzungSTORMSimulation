/**
 * class creates a Tiff-Stack out of a table of blinking events
 * it takes account for random processes like noise
 * @author Niels Schlusser, Frank Herrmannsörfer
 * @date 20.10.2015
 */

package calc;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import org.apache.commons.io.FilenameUtils;

import gui.CreatePlot;
import gui.CreateTiffStack;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileSaver;
import ij.process.*;

public class CreateStack {

	/**
	 * constuctor
	 */
	public CreateStack() {
		
	}
	
	/**
	 * main method to test
	 */
	public static void main(String[] args){ 
		Random rand = new Random(2);
		int nbrPoints = 2000;
		float[][] c = new float[nbrPoints][5];
		//random creation of a list of tables as input
		for (int j = 0; j < nbrPoints; j++) {
			c[j][0] = (float) j*10;
			c[j][1] = (float) j*10;
			c[j][2] = (float) 400;
			c[j][3] = (float) j*2;
			c[j][4] = (float) 10000;
		}
//		for (int j = 0; j < nbrPoints/10; j++){
//			for (int k=0; k<1; k++){
//				c[j*10+k][0]=50;
//				c[j*10+k][1] = 50;
//				c[j*10+k][2]=k*90+1;
//				c[j*10+k][3]=j;
//				c[j*10+k][4]=4000;
//			}
//		}
		ArrayList<Float> borders = new ArrayList<Float>();
		borders.add((float) -99999);
		borders.add((float) 99999);
		borders.add((float) -99999);
		borders.add((float) 99999);
		borders.add((float) -99999);
		borders.add((float) 99999);
		System.out.println("finished simulation");
		//calibration used for simulation of 3D PSFs
		float[][] calibr = {{0.0001f,131.6016f,299.7855f},
						   {101.111f,124.3521f,247.8447f},
						   {202.222f,121.4928f,206.5095f},
						   {303.333f,126.1539f,177.7527f},
						   {404.444f,134.6805f, 157.5747f},
						   {505.556f,152.1423f, 148.3749f},
						   {606.667f,176.9409f, 145.7982f},
						   {707.778f,212.3208f, 152.4042f},
						   {808.889f,252.4194f,164.9916f},
						   {910f,308.4156f,188.8461f}};//rescaled
		
		createTiffStack(c, 1/133.f/**resolution*/ , 10/**emptyspace*/, 10 /**emGain*/,borders, rand,
				4.81f/**electron per A/Dcount */, (float) 50/**frameRate*/, 
				0.10f/**decayTime*/, 0.005f /**dead time*/, 10/**sizePSF*/,1/**modelNR*/, 1.f /**quantum efficiency*/, 
				(float) 1.45/**NA*/, 647/**waveLength*/, 400/**zFocus*/, 
				800/**zDefocus*/, 35.7f/**sigmaNoise*/, 200/**constant offset*/, calibr/**calibration file*/
				,"C:\\test.tif",
				true /* ensure single PSF*/, true /*split blinking over frames*/, new CreateTiffStack(null, null, null,null));

    } 
	
	
	
	/**
	 * method createTiffStack creates a tiff-stack out of a list of points
	 * @param input : list of float tables as input
	 * @param resolution : resolution, i.e. ratio pixel per nanometres 
	 * @param emptySpace : empty pixels on the edges
	 * @param emGain : EM Gain multiplier
	 * @param borders : borders describe which part of the input data will be rendered into tiff stacks
	 * @param rand : Instance of random number generator
	 * @param electronsPerADcount : Conversion factor from electrons to digital numbers
	 * @param frameRate : rate with which frames are taken
	 * @param decayTime : mean duration of a blinking event
	 * @param deadTime: dead time of the camera, all photons which would be detected during this time are lost
	 * @param sizePSF : size over which the PSF is spread
	 * @param modelNumber : kind of model for the PSF
	 * @param qe: quantum efficiency
	 * @param numerical aperture of the microscope
	 * @param wavelength of the used light
	 * @param zFocus : z-plane, in which the focus lies
	 * @param zDefocus : z-value, for which the microscope defocusses
	 * @param sigmaNoise : sigma of the Gaussian noise in the whole image
	 * @param offset : constant offset
	 * @param calib : calibration file for asymmetric Gaussian
	 * @param fname : filename for the output tiff stack, also the the list of ground truth data points is copied to the same folder
	 * @param ensureSinglePSF : option to ensure no overlapping PSFs
	 * @param splitIntensities : option to enable single blinking events to contribute to multiple frames
	 * @param cp : instance of CreateTiffStack SwingWorker to monitor progress
	 */
	public static int createTiffStack(float[][] input, float resolution, int emptySpace, float emGain,
			ArrayList<Float> borders, Random rand,
			float electronsPerADcount, float frameRate, float decayTime, float deadTime, int sizePSF, int modelNumber, float qe,
			float numericalAperture, float waveLength, float zFocus, float zDefocus, float sigmaNoise, 
			float offset, float[][] calib, String fname, boolean ensureSinglePSF, boolean splitIntensities, CreateTiffStack cp) { 
		if (input.length<=0){
			System.out.println("no points to simulate.");
			return 1;
		}
		List<float[]> lInput = convertList(input);
		
		float pixelsize = 1/resolution;
		//get mean intensity
		float meanInt = 0;
		for (int i=0; i<input.length; i++){
			meanInt = meanInt + input[i][4];
		}
		meanInt /=input.length;

		//simulate distribution of the intensity over different frames
		if (splitIntensities){
			lInput = distributePSF(lInput, frameRate, deadTime, decayTime, meanInt);
			//lInput = distributePSF(lInput, frameRate, decayTime, meanInt);
		}
		
		//find out minimum x- and y-values			
		float minX = globalMin(lInput, 0); 
		float minY = globalMin(lInput, 1);
		
		//subtract minimum values to obtain coordinates in [0, infty]x[0, infty]
		for (int j = 0; j < lInput.size(); j++) {   	//shifts the window to positive values
			lInput.get(j)[0] -= minX; 
			lInput.get(j)[0] +=	(emptySpace + sizePSF)/ resolution;
			lInput.get(j)[1] -= minY;
			lInput.get(j)[1] += (emptySpace + sizePSF)/ resolution;
		}
		for (int k = 0; k<2; k++){//increase borders to ensure that psfs are not cut 
			borders.set(k, borders.get(k) +(emptySpace + sizePSF)/ resolution-minX);
			borders.set(k+2, borders.get(k+2) +(emptySpace + sizePSF)/ resolution-minY);
		}
		
	
		//find out required height and width of the image in nm as global maxima in the list; convert into pixel		
		int pImgWidth = (int) (globalMax(lInput, 0)* resolution);
		pImgWidth++; //ensures rounding up
		int pImgHeight = (int) (globalMax(lInput, 1)* resolution);
		pImgHeight++;
		
		//create new image stack	
		ImageStack stackLeft = new ImageStack(pImgWidth + emptySpace + sizePSF, pImgHeight + emptySpace + sizePSF); //emptySpace on both sides
		
		//performing sorting operation by quicksort algorithm based on frame
		Collections.sort(lInput, new Comparator<float[]>() {
		    @Override
		    public int compare(float[] o1, float[] o2) {
		        if (o1[3] > o2[3]){
		            return 1;
		        }else if(o1[3] < o2[3]){
		            return -1;
		        }
		        return 0;
		    }
		});
		
		if (ensureSinglePSF){// for test purposes close PSF appearing in the same frame will be omitted by 
			lInput = stretchDateToEnsureNonOverlappingPSFs(lInput);
		}
		String basename = FilenameUtils.getBaseName(fname);
		String path = FilenameUtils.getFullPath(fname);
		
		writeLocalizationsToFile(lInput, path+"\\"+basename, borders, pixelsize);
		int lastFrame = (int) lInput.get(lInput.size()-1)[3];
		
		stackLeft= fillTiffStack(lInput, stackLeft, resolution, emptySpace, emGain,
				borders, rand, lastFrame, pImgWidth, pImgHeight, electronsPerADcount, frameRate,decayTime,
				deadTime, sizePSF, modelNumber, qe, numericalAperture, waveLength, zFocus, zDefocus, sigmaNoise,
				offset,calib,ensureSinglePSF, splitIntensities, cp);
		
		
		//save imagestack
		ImagePlus leftStack = new ImagePlus("", stackLeft);
		FileSaver fs = new FileSaver(leftStack);
		fs.saveAsTiffStack(fname);
		
		return 0;
	}	


	/**
	 * method fillTiffStack calculates the intensities for all pixels of the tiff stack
	 * @param input : list of float tables as input
	 * @param stackLeft : empty tiff stack which will be filled
	 * @param resolution : resolution, i.e. ratio pixel per nanometers 
	 * @param emptySpace : empty pixels on the edges
	 * @param emGain : EM Gain multiplier
	 * @param borders : borders describe which part of the input data will be rendered into tiff stacks
	 * @param rand : Instance of random number generator
	 * @param electronsPerADcount : Conversion factor from electrons to digital numbers
	 * @param frameRate : rate with which frames are taken
	 * @param decayTime : mean duration of a blinking event
	 * @param deadTime: dead time of the camera, all photons which would be detected during this time are lost
	 * @param sizePSF : size over which the PSF is spread
	 * @param modelNumber : kind of model for the PSF
	 * @param qe: quantum efficiency
	 * @param numerical aperture of the microscope
	 * @param wavelength of the used light
	 * @param zFocus : z-plane, in which the focus lies
	 * @param zDefocus : z-value, for which the microscope defocusses
	 * @param sigmaNoise : sigma of the Gaussian noise in the whole image
	 * @param offset : constant offset
	 * @param calib : calibration file for asymmetric Gaussian
	 * @param ensureSinglePSF : option to ensure no overlapping PSFs
	 * @param splitIntensities : option to enable single blinking events to contribute to multiple frames
	 * @param cp : instance of CreateTiffStack SwingWorker to monitor progress
	 */
	private static ImageStack fillTiffStack(List<float[]> lInput, ImageStack stackLeft, float resolution, int emptySpace, float emGain,
			ArrayList<Float> borders, Random rand, int lastFrame, int pImgWidth, int pImgHeight,
			float electronsPerADcount, float frameRate, float decayTime, float deadTime, int sizePSF, int modelNumber, float qe,
			float numericalAperture, float waveLength, float zFocus, float zDefocus, float sigmaNoise, 
			float offset, float[][] calib, boolean ensureSinglePSF, boolean splitIntensities, CreateTiffStack cp) {
		for (int frame = 0; frame<lastFrame;frame++){
			cp.publicSetProgress((int)100.*frame/lastFrame);
			// fill image stack with images
			FloatProcessor pro =  new FloatProcessor(pImgWidth + emptySpace + sizePSF, pImgHeight + emptySpace + sizePSF);
			while (lInput.size() > 0 && lInput.get(0)[3] == frame) {
				float [] currPSF = lInput.get(0);
				lInput.remove(0);
				if (Calc.isInRange(currPSF, borders)){
					//modelling the form of the PSF
									
					//at least once
					int pixelX = Math.round(currPSF[0]* resolution);
					int pixelY = Math.round(currPSF[1]* resolution);
		
					double sum = 0;
					
					switch(modelNumber) { 
					case 1:// symmetric Gaussian
						float sig = calcSig(currPSF[2], numericalAperture, waveLength, zFocus, zDefocus);
						for (int k = -sizePSF; k <= sizePSF; k++) {
							for (int m = -sizePSF; m <= sizePSF; m++) {
								float intensityPhotons = (float) (symmInt(pixelX + k, pixelY + m, currPSF[0],
										currPSF[1], currPSF[4],
										sig, resolution)) * qe;
								sum = sum + intensityPhotons;
								float val4 = pro.getf(pixelX + k, pixelY + m);
								val4 += calc.RandomClass.poissonNumber(calc.RandomClass.poissonNumber(intensityPhotons,rand),rand);//two poisson distributions, first for shot noise second for em gain factor of sqrt(2)
								pro.setf(pixelX + k, pixelY + m, val4);
								
							}
						}
						System.out.println("current Intensity: "+ currPSF[4]+" intensity gaussian: "+sum+" z: "+currPSF[2]+" frame: "+currPSF[3]+" sigma: "+sig);
						break;
						
					case 2: //asymmetric Gaussian
						SplineCalculator spl = new SplineCalculator(calib);
						spl.splines();
						sum = 0;
						if (spl.getSig(currPSF[2])==null){
							break;
						}
						for (int k = -sizePSF; k <= sizePSF; k++) {
							for (int m = -sizePSF; m <= sizePSF; m++) {
								float intensityPhotons = (float) (aSymmInt(pixelX + k, pixelY + m, currPSF[0],
										currPSF[1], currPSF[4],
										spl.getSig(currPSF[2]), resolution)) *qe;
								sum = sum + intensityPhotons;
								float val4 = pro.getf(pixelX + k, pixelY + m);
								val4 += calc.RandomClass.poissonNumber(calc.RandomClass.poissonNumber(intensityPhotons,rand),rand);//two poisson distributions, first for shot noise second for em gain factor of sqrt(2)
								pro.setf(pixelX + k, pixelY + m, val4);
							}
						}
						System.out.println("current Intensity: "+ currPSF[4]+" intensity gaussian: "+sum+" z: "+currPSF[2]+" frame: "+currPSF[3]+" sigma x: "+spl.getSig(currPSF[2])[0]+" sigma y: "+spl.getSig(currPSF[2])[1]);
						break;	
					}
				}
			}
			pro.multiply(emGain);
			pro.multiply(1/electronsPerADcount);
			pro.add(offset); //add constant offset
			pro.noise(sigmaNoise); //add Gaussian Readout noise (unit digital numbers NOT electrons)
			stackLeft.addSlice(pro); //adds a processor for each frame to the stack
		}
		return stackLeft;
	}

	/**
	 * this function ensures that no PSFs are rendered closer than 1500 nm to each other. 
	 * If two PSFs might be to close one in the same frame, one of the psfs is put in a new
	 * frame at the end of the image stack where a new frame is created
	 * @param lInput : list of blinking events
	 * @return finalList: altered list of blinking events ensuring a minimal distance of 1500 nm between the centers
	 */
	private static List<float[]> stretchDateToEnsureNonOverlappingPSFs(List<float[]> lInput) {
		List<float[]> finalList = new ArrayList<float[]>();
		double toleranceInNm = 1500;//minimal distance of centers
		List<float[]> alreadyPresentInCurrentFrame = new ArrayList<float[]>();
		int frame = 0;
		int maxFrame = (int) lInput.get(lInput.size()-1)[3];
		while (lInput.size()>0){
			while (lInput.size() > 0 && lInput.get(0)[3] == frame) {
				float [] currPSF = lInput.get(0);
				lInput.remove(0);
				boolean isTooClose = false;
				for (int i = 0; i<alreadyPresentInCurrentFrame.size(); i++){
					float[] test = alreadyPresentInCurrentFrame.get(i);
					if ((Math.pow((currPSF[0]-test[0]),2) + Math.pow((currPSF[1]-test[1]),2))<Math.pow((toleranceInNm),2)){
						currPSF[3]+=maxFrame+1; //add maximal frame number plus one
						lInput.add(lInput.size(), currPSF);
						isTooClose = true;
						break;
					}
				}
				if (isTooClose){
					
				}
				else{
					finalList.add(currPSF);
					alreadyPresentInCurrentFrame.add(currPSF);
				}
			}
			frame += 1;
			alreadyPresentInCurrentFrame.clear();
		}
		return finalList;
	}

	/**
	 * auxiliary method creates a list out of the list of tables of events
	 * @param input list
	 * @return output list
	 */
	private static List<float[]> convertList (float[][] lInput) {
		List<float[]> ret = new ArrayList<float[]>();
		for(int j = 0; j < lInput.length; j++) {
			float[] val = {lInput[j][0], lInput[j][1], lInput[j][2], lInput[j][3], lInput[j][4]};
			ret.add(val);
		}
	
		return ret;
	}	

	
	
	/**
	 * auxiliary method finds global maximum in the list of arrays for a given column
	 * @param input : input list of arrays/matrices
	 * @param col : column number 
	 * @return max: global maximum
	 */
	private static float globalMax(List<float[]> input, int col) {
		float max = (float) -1e19; //initialise max to be very small in order to find real maximum
		for(int i = 0; i < input.size(); i++) {
			if(input.get(i)[col] > max) {
				max = input.get(i)[col];
			}		
		}
		return max;
	}
	
	/**
	 * auxiliary method finds global minmum in the list of arrays for a given column
	 * @param input : input list of arrays/matrices
	 * @param col : column number 
	 * @return max: global maximum
	 */
	private static float globalMin(List<float[]> input, int col) {
		float min = (float) 1e19; //initialise max to be very large in order to find real minimum
		for(int i = 0; i < input.size(); i++) {
			if(input.get(i)[col] < min) {
				min = input.get(i)[col];
			}		
		}
		return min;
	}
	
	
	/**
	 * auxiliary method calculates a normalised, symmetric Gaussian-distributed 
	 * intensity at the value of integer pixels;
	 * coordinates of a pixel are coordinates of its lower left corner
	 * nevertheless, the intensity is the intensity in the centre of the pixel
	 * @param pX : x-coordinate in pixels
	 * @param pY : y-coordinate in pixels
	 * @param maxX : float-x-cordinate of the maximum in nm
	 * @param maxY : float-y-cordinate of the maximum in nm
	 * @param maxInt : intensity of the maximum at (maxX, maxY)
	 * @param sig : sigma value of the gaussian distribution in nm
	 * @param res : resolution, i.e. ratio pixel per nanometres
	 * @return Gauss-value
	 */
	private static float symmInt(int pX, int pY, float maxX, 
			float maxY, float maxInt, float sig, float res) {
		
		float ret = 0;
		float x = (float) pX;
		float y = (float) pY;
		float dx = (float) ((x + 0.5)/res) - maxX; //difference between maximum of PSF-Gauss and current pixel in nm
		float dy = (float) ((y + 0.5)/res) - maxY;
		
		
		double exponent = (double) (Math.pow(dx, 2) + Math.pow(dy, 2))/2 /Math.pow(sig, 2);
		ret = (float) Math.exp(-exponent)*maxInt;
		ret /= (2.f* Math.PI);
		sig = sig*res;
		ret /= Math.pow(sig, 2);
		return ret;
	}
	
	
	/**
	 * auxiliary method calculates a normalised, asymmetric Gaussian-distributed 
	 * intensity at the value of integer pixels;
	 * coordinates of a pixel are coordinates of its lower left corner
	 * nevertheless, the intensity is the intensity in the centre of the pixel
	 * @param pX : x-coordinate in pixels
	 * @param pY : y-coordinate in pixels
	 * @param maxX : float-x-cordinate of the maximum in nm
	 * @param maxY : float-y-cordinate of the maximum in nm
	 * @param maxInt : intensity of the maximum at (maxX, maxY)
	 * @param sig : array with both sigma values
	 * @param res : resolution, i.e. ratio pixel per nanometres
	 * @return Gauss-value
	 */
	private static float aSymmInt(int pX, int pY, float maxX, 
			float maxY, float maxInt, float[] sig, float res) {
		
		float ret = 0;
		float x = (float) pX;
		float y = (float) pY;
		float dx = (float) ((x + 0.5)/res) - maxX; //difference between maximum of PSF-Gauss and current pixel in nm
		float dy = (float) ((y + 0.5)/res) - maxY;
		
		double exponent = (double) Math.pow(dx, 2)/Math.pow(sig[0], 2)/2 + Math.pow(dy, 2)/Math.pow(sig[1], 2)/2;
		ret = (float) Math.exp(-exponent)*maxInt;
		ret /= (2* Math.PI);
		sig[0] *= res;
		sig[1] *= res;
		ret /= sig[0];
		ret /= sig[1];
		return ret;
	}
	

	/**
	 * auxiliary method calculates sigma dependent on z for the symmetric Gaussian
	 * @param maxZ : z-value of the maximum
	 * @param numerical aperture of the microscope
	 * @param Wavelength of the light
	 * @param zFoc : z-plane, in which the focus lies
	 * @param zDefoc : z-value, for which the microscope defocusses
	 * @return sigma for symmetric Gaussian
	 */
	private static float calcSig(float z, float numAperture, float waveLgth, float zFoc, float zDefoc) {
		float s = (float) Math.pow(2, Math.abs((z - zFoc)/(zFoc - zDefoc)));
		s = s*waveLgth/2.f /numAperture/2.35f;
		return s;
	}
	

	/**
	 * auxiliary method distributePSF distributes the PSF in an amount different frames
	 * initialisation of new ArrayList necessary since adding elements to a non-specified list is not possible
	 * @param iInp : input list 
	 * @param frRate : rate in which frames are taken
	 * @param decTime : time interval, over which the PSF distributes its intensity
	 * @return new list with PSF spread over different frames
	 */
	private static List<float[]> distributePSF(List<float[]> lInp, float frRate, float meanDecTime, float meanIntensity) {
		float frameTime = 1/frRate;
		
		List<float[]> ret = new ArrayList<float[]>(); //new list
		
		for(int i = 0; i < lInp.size(); i++) {
			float decTime = lInp.get(i)[4]/meanIntensity*meanDecTime;
			//float decTime = (float) (meanDecTime * -Math.log(1-Math.random()));
			//simulation of a random beginning time
			float beginningTime = (float) (Math.random()/frRate);
			float t = beginningTime + decTime;
			//spreads the intensity of the PSF on arbitrarily many frames
			if(t > frameTime) {
				float d = decTime;
				float overlap1 = (frameTime - beginningTime)/decTime; // fraction of decay time in original frame
				float[] val1 = {lInp.get(i)[0], lInp.get(i)[1], lInp.get(i)[2], lInp.get(i)[3],
						lInp.get(i)[4]*overlap1};
				d -= frameTime; 
				d += beginningTime;
				ret.add(val1);
				float overlap2 = frameTime/decTime; //fraction full frame over decay time
				int n = 0;
				while (d > frameTime) {
					float[] val2 = {lInp.get(i)[0], lInp.get(i)[1], lInp.get(i)[2], lInp.get(i)[3] + n + 1,
							lInp.get(i)[4]*overlap2};
					d = d - frameTime;
					n++;
					ret.add(val2);
				}
				float overlap3 = d/decTime; //fraction of the rest
				float[] val3 = {lInp.get(i)[0], lInp.get(i)[1], lInp.get(i)[2], lInp.get(i)[3] + n + 1,
						lInp.get(i)[4]*overlap3};
				ret.add(val3);
			}
			else{
				ret.add(lInp.get(i));
			}
		}
		return ret;
	}
	
	

	/**
	* auxiliary method distributePSF distributes the PSF in an amount different frames
	* initialization of new ArrayList necessary since adding elements to a non-specified list is not possible
	* @param iInp : input list 
	* @param frRate : rate in which frames are taken
	* @param deadTime : dead time describes a certain amount of time per frame where no photons are collected
	* @param meanDecTime : photon number as specified in meanIntensity is distributet over the mean decay time.
	* @param meanIntensity : describes the number of photons per mean decay time  
	* @return new list with PSF spread over different frames
	*/
	private static List<float[]> distributePSF(List<float[]> lInp, float frRate, float deadTime, 
			float meanDecTime, float meanIntensity) {
	List<float[]> ret = new ArrayList<float[]>(); //new list
	float frameTime = 1/frRate - deadTime;
	if (frameTime < 0){
		System.out.println("Dead time too long or framerate too high.");
		return ret;
	}
	
	for(int i = 0; i < lInp.size(); i++) {
		float decTime = lInp.get(i)[4]/meanIntensity*meanDecTime;
		//float decTime = (float) (meanDecTime * -Math.log(1-Math.random()));
		//simulation of a random beginning time between 0 and end of deadtime; 
		float beginningTime = (float) (Math.random()*frameTime + (Math.random()* deadTime));
		float t = beginningTime + decTime ;
		// checks if the blinking event begins before the beginning of the deadTime of the camera
		//spreads the intensity of the PSF on arbitrarily many frames
		if (beginningTime <= (frameTime)) {
			if(t > frameTime) {
				float overlap1 = (frameTime - beginningTime)/decTime; // fraction of decay time in first frame it occurs in
				float[] val1 = {lInp.get(i)[0], lInp.get(i)[1], lInp.get(i)[2], lInp.get(i)[3],
						lInp.get(i)[4]*overlap1};
				float d = decTime;
				d -= frameTime; 
				d += beginningTime;
				d -= deadTime;
				ret.add(val1);
				float overlap2 = frameTime/decTime; //fraction full frame over decay time
				int n = 0;
				while (d > frameTime) {
					float[] val2 = {lInp.get(i)[0], lInp.get(i)[1], lInp.get(i)[2], lInp.get(i)[3] + n + 1,
							lInp.get(i)[4]*overlap2};
					d = d - frameTime - deadTime;
					n++;
					ret.add(val2);
				}
				if (d>0){//if d > 0 there is a fraction of frameTime which contributes to the next frame, if d< 0 the blinking event ended during dead time
					float overlap3 = d/decTime; //fraction of the rest
					float[] val3 = {lInp.get(i)[0], lInp.get(i)[1], lInp.get(i)[2], lInp.get(i)[3] + n + 1,
							lInp.get(i)[4]*overlap3};
					ret.add(val3);
				}
			}
			else{
				ret.add(lInp.get(i));
			}
		}
		else if ((beginningTime + decTime) > (frameTime + deadTime)) {
			//events that begin during the  deadtime and last until the camera is exposed to signal again
			float x= decTime + beginningTime - deadTime -frameTime ;
			float overlap5 = frameTime/decTime; //fraction full frame over decay time
			int n = 0;
			while (x > frameTime) {
				float[] val2 = {lInp.get(i)[0], lInp.get(i)[1], lInp.get(i)[2], lInp.get(i)[3] + n + 1,
						lInp.get(i)[4]*overlap5};
				x = x - frameTime - deadTime;
				n++;
				ret.add(val2);
			}
			
			float overlap6 =  x/decTime;
			if (overlap6 > 0){
				float[] val3 = {lInp.get(i)[0], lInp.get(i)[1], lInp.get(i)[2], lInp.get(i)[3] + n + 1,
							lInp.get(i)[4]*overlap6};
					ret.add(val3);
				}
			}
		}
		return ret; 
	}


	/**
	 * method writes ground-truth file after distributing the blinking event;
	 * doesn't yet check whether event is in focus range
	 * @param stormData
	 * @param basename
	 * @param borders
	 */
	private static void writeLocalizationsToFile(List<float[]> stormData, String basename,ArrayList<Float> borders, float pixelsize) {
		try{
			FileWriter writer = new FileWriter(basename+"GroundTruth.txt");
			writer.append("Pos_x Pos_y Pos_z Frame Intensity\n");
			for (int i = 0; i<stormData.size(); i++){
				float[] tmp = stormData.get(i);
				if (Calc.isInRange(tmp, borders)){
					writer.append((tmp[0]-pixelsize/2.0)+" "+(tmp[1]-pixelsize/2.0)+" "+tmp[2]+" "+tmp[3]+" "+tmp[4]+"\n");
				}
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {e.printStackTrace();}
	}

}
