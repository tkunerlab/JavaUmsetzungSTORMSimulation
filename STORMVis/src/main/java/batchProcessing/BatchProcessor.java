package batchProcessing;

import gui.ThreadCompleteListener;
import gui.Gui;
import model.DataSet;
import model.ParameterSet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

import javax.swing.JProgressBar;
import javax.swing.SwingWorker;


//closely resembles the STORMCalculator except, we are directly storing the outcomes and handle multiple runs
public class BatchProcessor extends SwingWorker<Void, Void>{
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

	@Override
	public Void doInBackground() {
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
		
		for(int i=0;i<params.size();i++){
			for(int j=0;j<allDataSets.size();j++){
	    		//create new directory for this run
	    		String fullpath = String.format("%s/model%d/set%d", base_path, j, i);
	    		runs.add(new BatchCalc(fullpath, this.allDataSets.get(j), params.get(i), this.config.output_tiffstack, this.config.reproducible, this.config.viewstatus,
	    				this.config.shifts, this.config.repeat_experiment));
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
		    					completed.add(k);
		    				}
		    			}
	    			}
	    			
	    			//this is the best time to send updates to the GUI and remove finished threads
	    			//do stuff to GUI
	    			for(int k=runs.size()-1;k>=0;k--) {
	    				runs.remove(completed.get(k));
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
		
		return null;
	}
	@Override
	public void done(){
		System.out.println("Worker finished");
		notifyListeners();
	}
	
	private  Set<ThreadCompleteListener> listeners
      = new CopyOnWriteArraySet<ThreadCompleteListener>();
	public void addListener(final ThreadCompleteListener listener) {
		listeners.add(listener);
	}
	public void removeListener(final ThreadCompleteListener listener) {
		listeners.remove(listener);
	}
	
	public void notifyListeners() {
		for (ThreadCompleteListener listener : listeners) {
			listener.notifyOfThreadComplete(this);
		}
	}
	
	public void publicSetProgress(int prog){
		setProgress(prog);
	}

}
