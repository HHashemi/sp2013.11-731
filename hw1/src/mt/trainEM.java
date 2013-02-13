package mt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class trainEM {
	List<String> sentPairsList = new ArrayList<String>();
	HashMap<String, Double> count_e_f = new HashMap<String, Double>();
	HashMap<Integer, Double> total_f = new HashMap<Integer, Double>();
	HashMap<Integer, Double> s_total_e = new HashMap<Integer, Double>();
	
	public void trainEMforIBM1(List<String> sentPairsList1) {
		this.sentPairsList = sentPairsList1;
		initializeUniformly();
		int converged=0;
		while(converged < mainIBM.converged){
			System.out.println("-------- interation #: " + converged);
			converged++;
//			sortPrint(this.t_e_f);
			// initialize
			initialize();

			int e,f;
			String[] sents, engSent, gerSent;
			int i;
			for(i=0; i<mainIBM.sentNoTrain && i<sentPairsList.size(); i++){
				sents = sentPairsList.get(i).split("\t");
				gerSent = sents[0].split(" ");
				engSent = sents[1].split(" ");
				
				// compute normalization
				for(int j=0; j<engSent.length; j++){
					e = mainIBM.engWordsIdx.get(engSent[j]);
					s_total_e.put(e, 0.0);
					for(int k=0; k<gerSent.length; k++){
						f = mainIBM.gerWordsIdx.get(gerSent[k]);
						s_total_e.put(e , s_total_e.get(e) + mainIBM.t_e_f_Idx.get(e + "-" + f));
					}
				}
				
				// collect counts
				for(int j=0; j<engSent.length; j++){
					e = mainIBM.engWordsIdx.get(engSent[j]);
					for(int k=0; k<gerSent.length; k++){
						f = mainIBM.gerWordsIdx.get(gerSent[k]);
						double temp = mainIBM.t_e_f_Idx.get(e + "-" + f)/s_total_e.get(e);
						count_e_f.put(e + "-" + f, count_e_f.get(e + "-" + f) + temp);
						total_f.put(f,  total_f.get(f) + temp);
					}
				}
			}
			// estimate probabilities	
			for(Entry<String, Integer> german : mainIBM.gerWordsIdx.entrySet()){
				for(Entry<String, Integer> english : mainIBM.engWordsIdx.entrySet()){
					if(!count_e_f.containsKey(english.getValue() + "-" + german.getValue()))
						continue;
					double temp = count_e_f.get(english.getValue() + "-" + german.getValue())/total_f.get(german.getValue());
					mainIBM.t_e_f_Idx.put(english.getValue() + "-" + german.getValue(), temp);
				}
			}	
		}// end while
	}

	private void sortPrint(HashMap<String, Double> t_e_f2) {
	    ArrayList<Entry<String, Double>> t_e_f_sorted = sortValue(t_e_f2);
	    System.out.println("--------");
	    for(int i=0; i<t_e_f_sorted.size() ; i++){
	    	System.out.println(t_e_f_sorted.get(i).getKey() + "-" + t_e_f_sorted.get(i).getValue());
	    }
		
	}

	public static ArrayList<Entry<String, Double>> sortValue(HashMap<String, Double> t_e_f2){

	       //Transfer as List and sort it
	       ArrayList<Map.Entry<String, Double>> l = new ArrayList(t_e_f2.entrySet());
	       Collections.sort(l, new Comparator<Map.Entry<String, Double>>(){

	         public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
	            return o1.getValue().compareTo(o2.getValue());
	        }});
	       Collections.reverse(l);
//	       System.out.println(l);
		   return l;
	}
	 
	/**
	 * initialize at the beginning of each iteration
	 */
	private void initialize() {
		for(String key: count_e_f.keySet()){
			count_e_f.put(key, 0.0);
		}
		
		for(Integer key: total_f.keySet()){
			total_f.put(key, 0.0);
		}
		
		//This code is not efficient.
//		for(Entry<String, Integer> german : mainIBM.gerWordsIdx.entrySet()){
//			for(Entry<String, Integer> english : mainIBM.engWordsIdx.entrySet()){
//				count_e_f.put(english.getValue() + "-" + german.getValue(), 0.0);
//			}
//			total_f.put(german.getValue(), 0.0);
//		}	

	}

	private void initializeUniformly() {
		String word;
		String[] sents, engSent, gerSent;
		int i;
		int engIdx = 0, gerIdx = 0;
		HashMap<String, Integer> uniformCounts = new HashMap<String, Integer>();
		
		for(i=0; i<mainIBM.sentNoTrain && i < sentPairsList.size(); i++){
			sents = sentPairsList.get(i).split("\t");
			gerSent = sents[0].split(" ");
			engSent = sents[1].split(" ");
					
			for(int j=0; j<gerSent.length; j++){
				word = gerSent[j];
				// store indexes of words
				if(!mainIBM.gerWordsIdx.containsKey(word)){
					mainIBM.gerWordsIdx.put(word, gerIdx);
					total_f.put(gerIdx, 0.0);
					gerIdx++;
				}
			}		
			
			for(int j=0; j<engSent.length; j++){
				word = engSent[j];				
				// store indexes of words
				if(!mainIBM.engWordsIdx.containsKey(word)){
					mainIBM.engWordsIdx.put(word, engIdx);					
					engIdx++;
				}
			}
			
			String e, f;
			for(int j=0; j<gerSent.length; j++){
				for(int k=0; k<engSent.length; k++){
					e = Integer.toString(mainIBM.engWordsIdx.get(engSent[k]));
					f = Integer.toString(mainIBM.gerWordsIdx.get(gerSent[j]));
					
					//add this hashmap to intitialize uniform distribution
					if(!uniformCounts.containsKey(e))
						uniformCounts.put(e, 1);
					else if (!count_e_f.containsKey(e+ "-" + f))
						uniformCounts.put(e, uniformCounts.get(e)+1);
					
					mainIBM.t_e_f_Idx.put(e + "-" + f, 0.0);
					count_e_f.put(e + "-" + f, 0.0);				
				}
			}		
		}
				
		//uniform based on each word
		for(String key: mainIBM.t_e_f_Idx.keySet()){
			String e = key.split("-")[0];
			Double prob = (double) 1/uniformCounts.get(e);
			mainIBM.t_e_f_Idx.put(key, prob);
		}
		
		//This code is not efficient. I used only matched words in sentence pairs
//		Double uniProb = (double) 1/mainIBM.gerWordsIdx.size();
//		for(Entry<String, Integer> german : mainIBM.gerWordsIdx.entrySet()){
//			for(Entry<String, Integer> english : mainIBM.engWordsIdx.entrySet()){
////				t_e_f.put(english.getKey() + "\t" + german.getKey(), uniProb);
//				mainIBM.t_e_f_Idx.put(english.getValue() + "-" + german.getValue(), uniProb);
//			}
//		}
		
		
	}

}
