package com.example.smartphonesensing2.localization.classification;

import com.example.smartphonesensing2.localization.histogram.TrainingData;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

//import com.sun.corba.se.impl.oa.poa.AOMEntry;







import com.example.smartphonesensing2.table.Table;

public class Bayesian {
	//String folder_name="3_Chosen_AP/2_PMF_AccessPoints_allCells/";
	String filepath="";
	public String classificationName="";
	
	
	static int nextMaxInded;
	
	
    // Set of training data. Each training data is associated to one access-point
    public ArrayList<TrainingData> tds = new ArrayList<TrainingData>();
	
    //for a given sample, keep track of all of the estimation calculated during each new posterior
    public ArrayList<Integer> ClassificationEstimations = new ArrayList<Integer>();
  
    //keep track of all the classification results and correct answer, in order to calculate accuracy of this classifier
   //   ArrayList<String> listOfClassification = new ArrayList<String>();
  //  ArrayList<String> listOfCorrectLocalization = new ArrayList<String>();
    
    
    static int numberOfCells = 17;
	static int numberOfObservations = 4;
	
	
	//ArrayList to keep localization result for a given sample 
	//static ArrayList<Integer> currentLocation = new ArrayList<Integer>();  //good one 
	public ArrayList<Integer> currentLocation = new ArrayList<Integer>();
	
	

	//information about classifier accuracy
	public static float error_percentage;
	public static float accuracy_percentage;
	
	
	//public float[] prior =  new float [numberOfCells];	
	//public float [] posterior = new float [numberOfCells];
	public float[] prior ;	
	public float [] posterior;
	
	static int callcount = 0;
	
	//List of the PMF of the training data
	//static ArrayList<Table> PMF_TrainingDataSet = new ArrayList<Table>();
	
	//static float [][] PM = new float [numberOfCells] [numberOfObservations];
   	public ArrayList<Float[][]> TrainingData_PMF = new ArrayList<Float[][]>();
	
	
   	public Bayesian(String filepath, String classifierName)
	{
		this.filepath=filepath;
		this.classificationName=classifierName;
	
		 /* initialize buffers */
		//for(int i=0; i<this.posterior.length; i++) this.posterior[i]=(float)0;
		//for(int i=0; i<this.prior.length; i++) this.prior[i]=(float)0;
		
	}

   	public void setNumberOfCells(int numOfCells){
   		this.numberOfCells = numOfCells;
   		
   		//create the array size correct after number of cells are knowns
   		prior =  new float [numberOfCells];	
   		posterior = new float [numberOfCells];
   		
   	    /* initialize buffers */
   		for(int i=0; i<this.posterior.length; i++) this.posterior[i]=(float)0;
		for(int i=0; i<this.prior.length; i++) this.prior[i]=(float)0;
		
   		
   	}
   	
   	public int  getNumberOfCells(){
   		return this.numberOfCells; 
   	}
   	
   	
	public String getClassiferName()
	{
		return classificationName;
	}
   	
   	
	
	public float [] get_posterior()
	{
		return this.posterior;
	}
	
	public float [] get_prior()
	{
		return this.prior;
	}
	
	
	

	/*Train classifier, to know what PMF Table to use */
	public void trainClassifier(ArrayList<TrainingData> trainingData)
	{
		this.tds = trainingData;
	}
	
	
	
	/*
	 * This function takes in the new observation sample, and returns the classification type. 
	 *   */

	//???????? The argument is a list of indexes of APs chosen by the user. And not rssi values ??????
	public int classifyObservation(ArrayList<Integer> observations) 
	{
	
		int bayesian_result = 0;
   
    	float [] classification_result = new float [2];
    	
    	float[] sense_results = new float [numberOfCells];         

  
    	int temp;
    	int cellNumber;
    	int max_rssi;
    	int ap_index;
    	
		
		/*for each training data, and its corresponding observation, apply the sense model 
		 * So find the probability of being in Cell[i], and having that rssi value for that given AP, 
		 * Obtain an array with that probability for each cell
		*/
		
		for(int t=0; t<tds.size(); t++)
		{
			
			ap_index = NextStrongestAP(observations);
		
			System.out.println("AP name: "+tds.get(ap_index).getName() + "observation:"+observations.get(ap_index));
			//fetch the conditional probability of being in all cells and having that given rssi value for that given AP
			sense_results = senseOneAP(observations.get(ap_index), tds.get(ap_index).getPMF()); //P(e[i]=r|H)
			posterior = vector_mult(this.prior, sense_results);	
		
			System.arraycopy(this.posterior, 0, this.prior, 0, this.posterior.length); // update prior after 1 step.    
			
			classification_result=getMaxValueandClassify(posterior);
			
			cellNumber= (int)(classification_result[0] +1);
			
			//update end result only if classification had a valid cell id
			if( cellNumber >= 1)
			{
				bayesian_result = (int)(classification_result[0] +1);
			}
			
		 //   System.out.println("cellnumber:"+cellNumber);
		    ClassificationEstimations.add( (int)(classification_result[0] +1));
	     //	System.out.println("Cell:" + ClassificationEstimations.get(t)); 
	
		    
		    
		}
				
	    return bayesian_result;
	}
	

    
    /*
  * This functions receives measured rssi from a given accesspoint (ei), and the distribution of a specific AP over all cells
  * returns the P(ei|H), so the probability being in each cell, and having the given rssi for that specific AP
  * 
  * */
    
    public static float [] senseOneAP(float ap_observation, float [][] pmf_accesspoint)
    {
    	float [] sense_result = new float [numberOfCells]; //buffer to hold P(ei|H)
    	boolean rssi_found = false;
    	 
    	System.out.println("\n rssi sample:" + ap_observation);
    	
    	/* find the column of the rssi value */
    	for(int r=0; r<pmf_accesspoint[0].length; r++)
    	{
    		/* rssi value in PM found*/
    		if(pmf_accesspoint[0][r] == ap_observation){
    			rssi_found = true;
    			
    			/* fetch the probability for each cell having that rssi value for that AP.  P(e1|H) */
    			for(int c=0; c<numberOfCells;  c++)
    			{
    				sense_result[c]= pmf_accesspoint[c+1][r]; //for the PM skip first row, since its the label ID
    			
    			}
    			break;
    		} 
    		
    	}
    	if(rssi_found == false){
    		System.out.println("Rssi value not found");
     		/* if rssi is not present. find the closest rssi value nearby*/		
    	}
   
    	return sense_result;
    }
 
     
    
    public static float [] vector_mult(float[] vectorA,  float[] vectorB){
    	float [] result = new float [numberOfCells];
    	
    	for(int i=0; i<vectorA.length; i++)
    	{
    		result[i] = vectorA[i] * vectorB[i];
    		
    	}
    	return result;
    	
    	
    	
    	
    }
    
    /*This functions reads a lit of numbers and returns the the index of the highest number */
    public static float [] getMaxValueandClassify(float[] numbers){  
    	  float maxValue = numbers[0];
    	  float cellid = 0;
    	  float []results = new float [2];
    	  
    	  for(int i=1;i < numbers.length;i++){  
    	    if(numbers[i] > maxValue){  
    	      maxValue = numbers[i];  
    	      cellid= (float)i;
    	    }  
    	  }  
    	  results [0]= cellid;
    	  results [1]= maxValue;
    	  
    	  return results;  
    	}  
    	  
    /*This functions reads a lit of numbers and returns the the indexes of the highest number */

    public  ArrayList<Integer> getMaxValueandClassify2(float[] numbers){  
  	  float maxValue = numbers[0];
  	  float cellid = 0;
  	  int maxIndex = 0;
  	  int maxIndex2 = 0;
  	  int tempIndex=0;
  	  
  	  ArrayList<Integer> listofIndexes = new  ArrayList<Integer>(); 
  	ArrayList<Float> maxValues = new  ArrayList<Float>(); 
  /*	
	  List data = Arrays.asList(numbers); // !!!!
	  ArrayList data2 = new ArrayList<Integer>(); // !!!!
	  List data3 = new ArrayList(); // !!!!
	  
	  Collections.addAll(data2, numbers);
	  Collections.addAll(data3, numbers);
	*/    
	  
		   
  	  float []results = new float [2];
  	
  	  
	   float [] temp = new float [numbers.length];
	  	System.arraycopy(numbers, 0, temp,0 , numbers.length);
	  
	  	Arrays.sort(temp);
	  	
  	  
	  	float first = temp[temp.length-1]; // get highest number 
	  	maxIndex=temp.length-1;
		float second = temp[temp.length-2];  // get 2nd highest number 
		maxIndex2=temp.length-2;
		
		float tempvalue;
		 
		
  	  /*check if unknown results
  	   * all or more than two numbers are equivalent to max value */
  	  if(unknownLocation(numbers))
  	  {
  		
  		  //set the arraylist of current cells to default
  		
  			/* set uniform distribution for prior */
  			for(int i=0; i<numbers.length; i++)
  			{
  				listofIndexes.add(i);
  				//currentLocation.add(i);// at the beginning all rooms is the current location
  			}
  		  return listofIndexes;  
  		  
  	  }else{ //find top 2 results
  		  	

	    	if (first < second)
	        {
	    		tempvalue = first;
	            first = second;
	            second = tempvalue;
	            
	            tempIndex= maxIndex;
	            maxIndex = maxIndex2;
	            maxIndex2 = tempIndex;
	            
	          
	        }
	    	
	        for (int i = temp.length-3; i >=0;	i--)
	        {
	            if (temp[i] >= first)
	            {
	            	second = first;
	                first = temp[i];
	                
	                maxIndex2=maxIndex;
	                maxIndex=i;
	                
	            }
	            else if (temp[i] > second)
	            {
	            	second = temp[i];
	            	maxIndex2=i;
	            }
	        }
  		  
  	   
	        
	        maxValues.add(first);
	        maxValues.add(second);
	      //  listofIndexes.add(maxIndex);
	  	//	listofIndexes.add(maxIndex2);
	  		
	        //if values are not put only maxx
	       /* if(listofIndexes.get(0) != listofIndexes.get(1)){
	        	listofIndexes.remove(1);
            }*/
            
  	  }     

  	  float valuetoFetch;
  	  int valueIndex = 0;
  	  //for the given indexed find out the original array index 
  	  for(int i=0; i<maxValues.size(); i++)
  	  {
  		  valuetoFetch=maxValues.get(i);
  		  
  		  //fetch the original index of that current max value
  		  for(int j=0; j<numbers.length; j++)
  		  {
  			  if(numbers[j] == valuetoFetch)
  			  {
  				  valueIndex = j;
  				listofIndexes.add(valueIndex);
  				  System.out.println("Max value index:"+valueIndex);
  				 System.out.println("Cell :"+(valueIndex+1));
  			  }
  		  }
  		
  	  }
  	  
  	  
  	  
  	  return listofIndexes;  
  	}  
    
  	  
     /* This function checks to see if the posterior has a good result*/
     public static boolean unknownLocation(float [] posterior)
     {
    	   int passBoundary =2; //if more than 2 cells are the same then unknown location
    	   int count=0;
    	   
    	   float [] temp = new float [posterior.length];
    	   
			boolean flag = false; // all cells are not the same
			boolean flag2 = false; // more than two cells are the same
			
			System.arraycopy(posterior, 0, temp,0 , posterior.length);
			
			//put max value to the right
			Arrays.sort(temp);
			
			float first = temp[temp.length-1];
			float second = temp[temp.length-2];
			float tempvalue;
			
			
			 if (posterior.length == 0) {
			        return false;
			    }
			 
			 
			 
			 
		/*	else {
			    	
			    	if (first < second)
			        {
			    		tempvalue = first;
			            first = second;
			            second = tempvalue;
			            
			            flag = false; // if more than one max value are not the same. cells are known
			            
			            return flag; 
			        }
			    	else if(first ==second){
			    		count ++;
			    	}
			  
			    	
			        for (int i = posterior.length-3; i >=0;	i--)
			        {
			            if (temp[i] >= first)
			            {
			            	second = first;
			                first = temp[i];
			            }
			            else if (temp[i] > second)
			            {
			            	second = temp[i];
			            }
			        }
			    	
			*/    	

					 /* check more than two cells are the same to max value */
				for(int i = 0; i < posterior.length && !flag2; i++)
				{
				  if (posterior[i] == first ) count++;
				 
				  if(count>passBoundary+1)
				  { 
					  flag2 = true;
					  break;
				  }
			
				}
			    
			 
			 
			 
				 /* check more than two cells are the same to max value */
				/*
			    for(int i = 1; i < posterior.length && !flag2; i++)
						{
						  if (posterior[i] == posterior[i-1] ) count++; 
						  else {
							  flag2 = false;
							  break;
						  }
						  if(count>passBoundary) flag2 = true;
					
						}
					    
			
				
				
			*/
			
			
			return flag ||flag2;
	 }

    
    
    public void display_1D(float [] data)
    {
    	for(int i=0; i<data.length; i++)
    	{
    		System.out.println(" ["+i + "]: "+ data[i]);
    	}
    }
    
    
/*This function takes the path of the PMF file of a given access point,
 * and returns it as a 2D array */    
	static public float [][] fetch_pmf(String filename_path)
	{
		
		//String [][] array = new String [17+1][100+1] ;
		//float [][] array_float = new float [17+1][100] ; //good one
		float [][] array_float = new float [numberOfCells+1][100] ;
		
	//	Table table = new Table("TestTable");
		int r=0,c=0;
		
		try{
			File file = new File(filename_path);
		
		
			Scanner lineScanner= new Scanner(file);
	
			int file_id= 1;
			
	//		Table PMF_AP = new Table("fileid " +file_id++);
			
			// The values are comma separated
			lineScanner.useDelimiter("\\s*[,\n\r]\\s*");
			
			
			// Iterate over each token
			while(lineScanner.hasNextLine()) {
				
				String line =lineScanner.nextLine();
				
				Scanner textScanner = new Scanner(line);
				textScanner.useDelimiter("\\s*[,\n\r]\\s*");
			
				
				while(textScanner.hasNext())
				{
				
					String token = textScanner.next();
			//		array[r][c] = token;
					
					if(c!=0)
					{
						array_float[r][c-1]= Float.valueOf(token).floatValue(); 
					}
					
					
					c++;
				}
				
				textScanner.close();
				c=0;
				r++;

			} 
			
			lineScanner.close();
			r=0;
			}
			catch(FileNotFoundException e){
				System.out.println(" File not found"+e.getMessage());
			}
			
			return array_float;	
	}
	
	

    /* This function restarts the localization classification
     * so the prior and posterior set back to their initial believe.
     * The initial believe for this application is uniform*/
	public void setInitialBelieve()
	{
		/* set uniform distribution for prior */
		for(int i=0; i<prior.length; i++)
		{
			this.prior[i]= 	1/(float)(numberOfCells); //initial prior is uniform
			this.posterior[i]= 	1/(float)(numberOfCells); //initial prior is uniform
			currentLocation.add(i);// at the beginning all rooms is the current location
		}
	
		
		
		return;
	}
    
    
	/* return the current location*/
	public ArrayList<Integer> getCurrentLocation()
	{
		return currentLocation;
	}
	
	/* This function takes in a new list of current locations, and assigns it to the current location global variable. 
	 * It first resets it.*/
	public void updataCurrentLocation(ArrayList<Integer> newLocation){
		
		currentLocation.clear();//reset data to be blank
		currentLocation=newLocation; //get new location
		
	}
	
    
	/*
	  *@parameter 1 :observation corresponding to a given training data. 
	  *@parameter 2: Training data pmf 
	  *Functionality is to apply the sense model. So for a given observation it fetches the probabilty of being in each individual cell and having that rssi value.
	  * 
	  * */
	    public static float [] senseOneAP(Integer ap_observation, Table pmfTable)
    
	    //public static float [] senseOneAP2(Integer ap_observation, Float[][] td_pmfTable)
	    {
	    	float [] sense_result = new float [numberOfCells]; //buffer to hold P(ei|H)
	    		    	 
	    	System.out.println("\n rssi sample:" + ap_observation.intValue());
	  
	    	//check if rssi value is within rssi range
	    //	if(ap_observation>0 || ap_observation<pmfTable.getTable().length )
	    	if(   ap_observation > -(pmfTable.getTable()[0].length) &&  ap_observation < 0 )
	    	    
	    	{
	   		
	    		/* fetch the probability for each cell having that rssi value for that AP.  P(e1|H) */
    			for(int c=0; c<numberOfCells;  c++)
    			{
    				//sense_result[c]= pmfTable.getValue(3, 3); 
        			
    				sense_result[c]= pmfTable.getValue(c, Math.abs(ap_observation.intValue())); 
    				System.out.println("sense result["+c+"] : "+sense_result[c]);
    	    			
    			}
    		
	    	}else{
	    		System.out.println("rssi not in range");
	    	}
	    	
	    	
	    	   
	    	return sense_result;
	    }
    
    
    
    /* @parameter 1: a list of rssi value for a given sample
     * Find the next strongest rssi value 
     * */
		public static int findNextMaxRssi(ArrayList<Integer> observations2)
		{
			int max_rssi = 0;
			Integer [] temp = new Integer [observations2.size()];
			
			observations2.toArray(temp);
			
			//sort array in ascending order
	        Arrays.sort(temp);
	       
	       // if(nextMaxInded<observations2.size()){
	        //take last index and then continue taking that to the left
	        max_rssi=temp[temp.length -1-nextMaxInded++].intValue();
	       /* }
	        else{
	        	System.out.println("No More AccessPoints to fetch rssi values");
	        //	return (Integer) null;
	        }
	        */
			return max_rssi;
			
		}
		
		public void resetparameters() {
			// TODO Auto-generated method stub
			
			nextMaxInded=0;
			
		}
		

		/* @parameter 1: a list of rssi value for a given sample
		 * find the next Access-Point with the highest rssi value
		 * */
		
		
		//@parameter 1: list of rssi values, corresponding to the chosen AP. Index(0) belongs to AP #0 etc
		//this function takes a list of rssi values and returns the index of the next highest rssi value.
		//this index corresponds to which training data to take
       public static int NextStrongestAP(ArrayList<Integer> observations2)  
       {
    	   int max_rssi =0;
    	   int ap_index = 0;
    	   
    		//find the next strongest AP signal 
			max_rssi=findNextMaxRssi(observations2);
			
			//find index of the next highest rssi value
	        for(int i=0; i<observations2.size(); i++)
	        {
	        	if(observations2.get(i).intValue() == max_rssi)
	        	{
	        		ap_index=i;
	        	}
	        }
			
    	    return ap_index;
    	   
       }
		
		

       
       
       
       
   	/*
   	 * This function takes in the new observation sample, but only for One AP, and returns the classification type
   	 * In form of the cell number. 
   	 * Each classifier must chose carefully whichTraining data it is sending
   	 *   */
    
   	public ArrayList<Integer> classifyObservation( int observation, TrainingData td)
   	{
   	
   		ArrayList<Integer> location = new ArrayList<Integer>();
      
       	float[] sense_results = new float [numberOfCells];         

     	
   	   //fetch the conditional probability of being in all cells and having that given rssi value for that given AP
   		sense_results = senseOneAP(observation, td.getPMF()); //P(e[i]=r|H)
   		posterior = vector_mult(this.prior, sense_results);	

   		System.arraycopy(this.posterior, 0, this.prior, 0, this.posterior.length); // update prior after 1 step.    

   		location = getMaxValueandClassify2(this.posterior);
   			
   		return location;
   		
   		
   	}

       
       
       
       
       
       
       
}
