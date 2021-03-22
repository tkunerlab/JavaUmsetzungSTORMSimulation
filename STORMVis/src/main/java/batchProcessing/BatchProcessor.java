package batchProcessing;

import gui.ThreadCompleteListener;
import gui.DataTypeDetector.DataType;
import gui.Gui;
import model.DataSet;
import model.ParameterSet;
import model.TriangleDataSet;
import model.LineDataSet;
import model.PointsOnlyDataSet;
import model.EpitopeDataSet;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JProgressBar;

import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.primitives.Polygon;


//closely resembles the STORMCalculator except, we are directly storing the outcomes and handle multiple runs
//currently needs to run in the main thread due to list dataset objects not beeing thread safe (concurrent access exception)
public class BatchProcessor{
	//variables
	List<DataSet> allDataSets = new ArrayList<DataSet>();
	BatchConfig config = new BatchConfig();
	String base_path = "";
	int num_threads = 1;
	Gui reference_gui;
	
	public BatchProcessor(List<DataSet> datasets, BatchConfig conf, int num_threads, Gui reference) {
		this.allDataSets = datasets;
		this.config = conf;
		this.num_threads = num_threads;
		this.reference_gui = reference;
	}

	
	public void run() {
		//do stuff here
		//create output folder
        if(this.config.out_path.length()>0) {
        	String fullpath = this.config.out_path;
        	//replace / or \ with current File separator
        	
        	//put strings together
        	if(fullpath.charAt(fullpath.length()-1)=='/'){
        		fullpath = this.config.out_path + this.config.name;
        	} else {
        		fullpath = this.config.out_path + File.separator + this.config.name;
        	}
        	
        	if(fullpath.charAt(fullpath.length()-1)=='/') {
        		fullpath = fullpath.substring(0, fullpath.length()-2);
        	}
        	this.base_path = fullpath;
        	
        	(new File(fullpath)).mkdirs();
        	System.out.print("Saving data in " + fullpath + "\n");
        }
		
		
		ArrayList<ParameterSet> params = this.config.convertToParamterSet();
		

		ArrayList<BatchCalc> runs = new ArrayList<BatchCalc>();	
		ArrayList<Integer> modelindices = new ArrayList<Integer>();
		long lastupdate = System.nanoTime();
		for(int i=0;i<params.size();i++){
			for(int j=0;j<allDataSets.size();j++){
	    		//create new directory for this run
	    		String fullpath = String.format("%s/model%d/set%d", base_path, j, i);
	    		ParameterSet ptemp = new ParameterSet(params.get(i)); //deep copy parameter set
	    		DataSet pdata = dataset_deepcopy(this.allDataSets.get(j)); //deep copy dataset
	    		
	    		runs.add(new BatchCalc(fullpath, pdata, ptemp, this.config.output_tiffstack, this.config.reproducible, this.config.viewstatus,
	    				this.config.shifts, this.config.repeat_experiment));
	    		modelindices.add(j);
	    		runs.get(runs.size()-1).start();
	    		
	    		//check if we exceed the maximum number of allowed threads.
	    		if(runs.size()>=this.num_threads) {
	    			
	    			int num_active = this.num_threads;
	    			ArrayList<Integer> completed = new ArrayList<Integer>();
	    			while(num_active>=this.num_threads) {
		    			//wait for a thread to end
		    			try {
	    					Thread.sleep(100);
	    				} catch (InterruptedException e) {
	    					// TODO Auto-generated catch block
	    					e.printStackTrace();
	    				}
		    			
		    			//count number of active threads
		    			num_active = 0;
		    			for(int k=0;k<runs.size();k++) {
		    				if(runs.get(k).isAlive()) {
		    					num_active++;
		    				} else {
		    					completed.add(k); //add thread to the remove list
		    				}
		    			}
	    			}
	    			
	    			//this is the best time to send updates to the GUI and remove finished threads
	    			//do stuff to GUI
	    			if(this.reference_gui != null) {
	    				double diff = (System.nanoTime()-lastupdate)/1e9; //
	    				if(diff>= 5) {
	    					//take one of the finished threads and use the data to update the GUI
	    					if(completed.size()>0) {
	    						DataSet dset = runs.get(completed.get(0).intValue()).getCurrentDataSet();
	    						int ind = modelindices.get(completed.get(0).intValue());
	    						//copy datasetpoints
	    						if(dset.stormData != null) {
	    							this.allDataSets.get(ind).stormData = Arrays.stream(dset.stormData).map(float[]::clone).toArray(float[][]::new); //some Java 8 magic
	    						} 
	    						if(dset.antiBodyStartPoints != null) {
	    							this.allDataSets.get(ind).antiBodyStartPoints = Arrays.stream(dset.antiBodyStartPoints).map(float[]::clone).toArray(float[][]::new);
	    						} 
	    						if(dset.antiBodyEndPoints != null) {
	    							this.allDataSets.get(ind).antiBodyEndPoints = Arrays.stream(dset.antiBodyEndPoints).map(float[]::clone).toArray(float[][]::new);
	    						} 
	    						if(dset.fluorophorePos != null) {
	    							this.allDataSets.get(ind).fluorophorePos = Arrays.stream(dset.fluorophorePos).map(float[]::clone).toArray(float[][]::new);
	    						}
	    						//call to update GUI
	    						this.allDataSets.get(ind).getParameterSet().setGeneralVisibility(true);
	    						for(int a=0;a<this.allDataSets.size();a++) {
	    							if(a==ind) {
	    								continue;
	    							}
	    							this.allDataSets.get(a).getParameterSet().setGeneralVisibility(false);
	    						}
	    						System.out.println("Update GUI!!!");
	    						//this.reference_gui.batchproc_draw();
	    						
	    						lastupdate = System.nanoTime();
	    					}
	    				}
	    			}
	    			
	    			for(int k=completed.size()-1;k>=0;k--) {
	    				runs.remove(completed.get(k).intValue());
	    				modelindices.remove(completed.get(k).intValue());
	    			}
	    			
	    		}
	    	
    		}
        }
		
		int num_active = this.num_threads;
		while(num_active>=this.num_threads) {
			//wait for a thread to end
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//count number of active threads
			num_active = 0;
			for(int k=0;k<runs.size();k++) {
				if(runs.get(k).isAlive()) {
					num_active++;
				}
			}
		}
		
		return;
	}
	
	//very ugly
	private DataSet dataset_deepcopy(DataSet reference) {
		switch(reference.dataType) {
			case LINES: {
					LineDataSet dnew = new LineDataSet(new ParameterSet(reference.getParameterSet()));
					//copy plain dataset fields
					dnew.name = reference.name;
					dnew.color = reference.color;
					if(reference.stormData != null) {
						dnew.stormData = Arrays.stream(reference.stormData).map(float[]::clone).toArray(float[][]::new); //some Java 8 magic
					} else {
						dnew.stormData = null;
					}
					if(reference.antiBodyStartPoints != null) {
						dnew.antiBodyStartPoints = Arrays.stream(reference.antiBodyStartPoints).map(float[]::clone).toArray(float[][]::new);
					} else {
						dnew.antiBodyStartPoints = null;
					}
					if(reference.antiBodyEndPoints != null) {
						dnew.antiBodyEndPoints = Arrays.stream(reference.antiBodyEndPoints).map(float[]::clone).toArray(float[][]::new);
					} else {
						dnew.antiBodyEndPoints = null;
					}
					if(reference.fluorophorePos != null) {
						dnew.fluorophorePos = Arrays.stream(reference.fluorophorePos).map(float[]::clone).toArray(float[][]::new);
					} else {
						dnew.fluorophorePos = null;
					}
					dnew.progressBar = new JProgressBar();
					
					//copy
					LineDataSet dset = (LineDataSet)reference;
					dnew.pointNumber = dset.pointNumber;
					dnew.objectNumber = dset.objectNumber;
					for (int i = 0; i<dset.data.size(); i++){
						 ArrayList<Coord3d> tmp = new ArrayList<Coord3d>();
						 for (int j = 0; j<dset.data.get(i).size(); j++){
							 Coord3d tmpCoord = new Coord3d();
							 tmpCoord.x = dset.data.get(i).get(j).x;
							 tmpCoord.y = dset.data.get(i).get(j).y;
							 tmpCoord.z = dset.data.get(i).get(j).z;
							 tmp.add(tmpCoord);
						 }
						 //dnew.data.set(i, tmp);
						 dnew.data.add(tmp);
					 }
					
					return dnew;
				}
			case PLY:
			case TRIANGLES: {
					TriangleDataSet dnew = new TriangleDataSet(new ParameterSet(reference.getParameterSet()));
					//copy plain dataset fields
					dnew.name = reference.name;
					dnew.color = reference.color;
					if(reference.stormData != null) {
						dnew.stormData = Arrays.stream(reference.stormData).map(float[]::clone).toArray(float[][]::new); //some Java 8 magic
					} else {
						dnew.stormData = null;
					}
					if(reference.antiBodyStartPoints != null) {
						dnew.antiBodyStartPoints = Arrays.stream(reference.antiBodyStartPoints).map(float[]::clone).toArray(float[][]::new);
					} else {
						dnew.antiBodyStartPoints = null;
					}
					if(reference.antiBodyEndPoints != null) {
						dnew.antiBodyEndPoints = Arrays.stream(reference.antiBodyEndPoints).map(float[]::clone).toArray(float[][]::new);
					} else {
						dnew.antiBodyEndPoints = null;
					}
					if(reference.fluorophorePos != null) {
						dnew.fluorophorePos = Arrays.stream(reference.fluorophorePos).map(float[]::clone).toArray(float[][]::new);
					} else {
						dnew.fluorophorePos = null;
					}
					dnew.progressBar = new JProgressBar();
					
					//copy primitives and drawable triangles
					TriangleDataSet dset = (TriangleDataSet)reference;
					List<Polygon> newList = new ArrayList<Polygon>();
			    	for (Polygon p: dset.drawableTriangles){
			    		Polygon pNew = new Polygon();
			    		pNew.add(new Point(new Coord3d(p.get(0).xyz.x, p.get(0).xyz.y, p.get(0).xyz.z)));
			    		pNew.add(new Point(new Coord3d(p.get(1).xyz.x, p.get(1).xyz.y, p.get(1).xyz.z)));
			    		pNew.add(new Point(new Coord3d(p.get(2).xyz.x, p.get(2).xyz.y, p.get(2).xyz.z)));
			    		newList.add(pNew);
			    	}
			    	dnew.drawableTriangles.clear();
			    	dnew.drawableTriangles = newList;
			    	
			    	List<float[][]> prim = new ArrayList<float[][]>();
			    	dnew.primitives.clear();
			    	for(Polygon p : dset.drawableTriangles) {
				    	float[][] tr = new float[3][3];
				    	for(int i = 0; i < p.getPoints().size(); i++) {
					    	tr[i][0] = p.getPoints().get(i).xyz.x;
					    	tr[i][1] = p.getPoints().get(i).xyz.y;
					    	tr[i][2] = p.getPoints().get(i).xyz.z;
				    	}
				    	prim.add(tr);
			    	}
			    	dnew.primitives = prim;
					
					return dnew;
				}
			case POINTS:
			{
				PointsOnlyDataSet dnew = new PointsOnlyDataSet(new ParameterSet(reference.getParameterSet()));
				//copy plain dataset fields
				dnew.name = reference.name;
				dnew.color = reference.color;
				if(reference.stormData != null) {
					dnew.stormData = Arrays.stream(reference.stormData).map(float[]::clone).toArray(float[][]::new); //some Java 8 magic
				} else {
					dnew.stormData = null;
				}
				if(reference.antiBodyStartPoints != null) {
					dnew.antiBodyStartPoints = Arrays.stream(reference.antiBodyStartPoints).map(float[]::clone).toArray(float[][]::new);
				} else {
					dnew.antiBodyStartPoints = null;
				}
				if(reference.antiBodyEndPoints != null) {
					dnew.antiBodyEndPoints = Arrays.stream(reference.antiBodyEndPoints).map(float[]::clone).toArray(float[][]::new);
				} else {
					dnew.antiBodyEndPoints = null;
				}
				if(reference.fluorophorePos != null) {
					dnew.fluorophorePos = Arrays.stream(reference.fluorophorePos).map(float[]::clone).toArray(float[][]::new);
				} else {
					dnew.fluorophorePos = null;
				}
				dnew.progressBar = new JProgressBar();
				
				return dnew;
			}
			case EPITOPES:
			{
				EpitopeDataSet dnew = new EpitopeDataSet(new ParameterSet(reference.getParameterSet()));
				//copy plain dataset fields
				dnew.name = reference.name;
				dnew.color = reference.color;
				if(reference.stormData != null) {
					dnew.stormData = Arrays.stream(reference.stormData).map(float[]::clone).toArray(float[][]::new); //some Java 8 magic
				} else {
					dnew.stormData = null;
				}
				if(reference.antiBodyStartPoints != null) {
					dnew.antiBodyStartPoints = Arrays.stream(reference.antiBodyStartPoints).map(float[]::clone).toArray(float[][]::new);
				} else {
					dnew.antiBodyStartPoints = null;
				}
				if(reference.antiBodyEndPoints != null) {
					dnew.antiBodyEndPoints = Arrays.stream(reference.antiBodyEndPoints).map(float[]::clone).toArray(float[][]::new);
				} else {
					dnew.antiBodyEndPoints = null;
				}
				if(reference.fluorophorePos != null) {
					dnew.fluorophorePos = Arrays.stream(reference.fluorophorePos).map(float[]::clone).toArray(float[][]::new);
				} else {
					dnew.fluorophorePos = null;
				}
				dnew.progressBar = new JProgressBar();
				
				EpitopeDataSet dset = (EpitopeDataSet)reference;
				if(dset.epitopeBase != null) {
					dnew.epitopeBase = Arrays.stream(dset.epitopeBase).map(float[]::clone).toArray(float[][]::new);
				} else {
					dnew.epitopeBase = null;
				}
				if(dset.epitopeEnd != null) {
					dnew.epitopeEnd = Arrays.stream(dset.epitopeEnd).map(float[]::clone).toArray(float[][]::new);
				} else {
					dnew.epitopeEnd = null;
				}
				
				return dnew;
			}
			case UNKNOWN:
			{
				DataSet dnew = new DataSet(new ParameterSet(reference.getParameterSet()));
				//copy plain dataset fields
				dnew.name = reference.name;
				dnew.color = reference.color;
				if(reference.stormData != null) {
					dnew.stormData = Arrays.stream(reference.stormData).map(float[]::clone).toArray(float[][]::new); //some Java 8 magic
				} else {
					dnew.stormData = null;
				}
				if(reference.antiBodyStartPoints != null) {
					dnew.antiBodyStartPoints = Arrays.stream(reference.antiBodyStartPoints).map(float[]::clone).toArray(float[][]::new);
				} else {
					dnew.antiBodyStartPoints = null;
				}
				if(reference.antiBodyEndPoints != null) {
					dnew.antiBodyEndPoints = Arrays.stream(reference.antiBodyEndPoints).map(float[]::clone).toArray(float[][]::new);
				} else {
					dnew.antiBodyEndPoints = null;
				}
				if(reference.fluorophorePos != null) {
					dnew.fluorophorePos = Arrays.stream(reference.fluorophorePos).map(float[]::clone).toArray(float[][]::new);
				} else {
					dnew.fluorophorePos = null;
				}
				dnew.progressBar = new JProgressBar();
				
				return dnew;
			}
			default: {
				return null;
			}
		}
	}

}
