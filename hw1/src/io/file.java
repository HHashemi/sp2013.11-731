package io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import mt.mainIBM;

public class file {

	private HashMap<String, Double> align_e_f = new HashMap<String, Double>();
	
	/**
	 * read file and store sentence pairs
	 * @param string
	 * @param sentPairsList
	 * @throws IOException 
	 */
	public void readSentencePairs(String fileName,
			List<String> sentPairsList) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF8"));	
		String line;
		String sent1, sent2;
		int count = 0;
		while((line = br.readLine()) != null) {
			line = line.toLowerCase();  //make all the sentences lowercase
			line = line.replace(" ||| ", "\t");
			String[] sents = line.split("\t");
			sent1 = sents[0] + " NULL"; //null token at the beginning of german sentences
//			sent1 = sents[0];
			sent2 = sents[1];
			sentPairsList.add(sent1 + "\t" + sent2);
			count++;
		}		
		br.close();
	}

	/**
	 * write alignments for each sentence into a output file
	 * @param sentPairsList 
	 * @param string
	 * @param t_e_f
	 * @throws IOException 
	 */
	public void writeAlignmentsSimpleMax(String outFile, List<String> sentPairsList, HashMap<String, Double> t_e_f) throws IOException {
		Writer out = new BufferedWriter(new FileWriter(new File(outFile)));	
		int e,f;
		String[] sents, engSent, gerSent;
		int i;
		HashMap<Integer, Double> aligns = new HashMap<Integer, Double>();
		for(i=0; i<sentPairsList.size(); i++){
			sents = sentPairsList.get(i).split("\t");
			gerSent = sents[0].split(" ");
			engSent = sents[1].split(" ");
			
			//change print max based on English word first
			for(int j=0; j<engSent.length; j++){
				if(!mainIBM.engWordsIdx.containsKey(engSent[j])){
					continue;
				}
				e = mainIBM.engWordsIdx.get(engSent[j]);
				
				double maxProb = 0.0;
				int maxInd = 0;
				String maxWord = null;
				aligns.clear();
								
				for(int k=0; k<gerSent.length; k++){
					if(!mainIBM.gerWordsIdx.containsKey(gerSent[k])){
						continue;
					}					
					f = mainIBM.gerWordsIdx.get(gerSent[k]);
					// if this pair of e|f is not in the trained data
					if(!t_e_f.containsKey(e + "-" + f)){
						continue;
					}						
					aligns.put(k, t_e_f.get(e + "-" + f)); 
					if(maxProb < aligns.get(k)){
						maxProb = aligns.get(k);
						maxInd = k;
						maxWord = gerSent[k];
					}
				}
				//add alignments not just the most probable
				if(maxWord.equals("NULL"))
					continue;
				out.write(maxInd + "-" + j + " ");
//				for(Integer k : aligns.keySet()){
//					if(aligns.get(k) > mainIBM.threshold){
//						out.write(maxInd + "-" + j + " ");
//					}
//				}
			}

			out.write("\n");
			out.flush();
		}
		out.close();
	}
	
	/**
	 * Only to write the probabilities in a file
	 * @param outFile
	 * @param t_e_f
	 * @throws IOException
	 */
	public void writeProbabilities(String outFile, String outRaw, HashMap<String, Double> t_e_f) throws IOException {
		Writer out0 = new BufferedWriter(new FileWriter(new File(outRaw)));	
		for(String key: t_e_f.keySet()){
			String e = key.split("-")[0];
			String f = key.split("-")[1];
			out0.write(e + "\t" + f + "\t" + t_e_f.get(key) + "\n");
		}
		out0.close();
		
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile), "UTF-8"));
		
		HashMap<String, String> engWords = new HashMap<String, String>();
		HashMap<String, String> gerWords = new HashMap<String, String>();
		HashMap<String, String> uniqueEngWord = new HashMap<String, String>();
		
		for(String key: mainIBM.engWordsIdx.keySet()){
			engWords.put(Integer.toString(mainIBM.engWordsIdx.get(key)), key);
		}
		
		for(String key: mainIBM.gerWordsIdx.keySet()){
			gerWords.put(Integer.toString(mainIBM.gerWordsIdx.get(key)), key);
		}
		
		System.out.println("start writing probs");
		for(String key: t_e_f.keySet()){
			String e = key.split("-")[0];
			if(uniqueEngWord.containsKey(engWords.get(e)))
				continue;
			uniqueEngWord.put(engWords.get(e), "1");
			
			HashMap<String, Double> temp = new HashMap<String, Double>();
			for(String key2: t_e_f.keySet()){
				String e2 = key2.split("-")[0];
				String f2 = gerWords.get(key2.split("-")[1]);
				if(e2.equals(e))
					temp.put(f2, t_e_f.get(key2));
			}			
		    ArrayList<Entry<String, Double>> tempSorted = sortValue(temp);		    
		    out.write(engWords.get(e) + " :\n");
		    for(int i=0; i<tempSorted.size() ; i++){
				out.write("\t" + tempSorted.get(i).getKey() + "\t" + tempSorted.get(i).getValue() + "\n");
				out.flush();
				if(i>10)
					break;
		    }			
		}

		out.close();
	}
	
	public static ArrayList<Entry<String, Double>> sortValue(HashMap<String, Double> t){

	       //Transfer as List and sort it
	       ArrayList<Map.Entry<String, Double>> l = new ArrayList(t.entrySet());
	       Collections.sort(l, new Comparator<Map.Entry<String, Double>>(){

	         public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
	            return o1.getValue().compareTo(o2.getValue());
	        }});
	       Collections.reverse(l);
		   return l;
	}
	
	
	/**
	 * load file to fill t_e_f
	 * @param outFile
	 * @param sentPairsList
	 * @param t_e_f
	 * @throws IOException
	 */
	public void loadProbabilitiesFile(String inFile, List<String> sentPairsList) throws IOException {
		System.out.println("load probs...");
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), "UTF8"));	
		String line;
		int count = 0;
		while((line = br.readLine()) != null) {
			String[] words = line.split("\t");
			double d = Double.valueOf(words[2].trim()).doubleValue();
			//TODO: remove this part
			if(d<0.1)
				continue;
			mainIBM.t_e_f_Idx.put(words[0] + "-" + words[1], d);
			count++;
		}		
		br.close();
		
		System.out.println("load dev aligment file...");
		br = new BufferedReader(new InputStreamReader(new FileInputStream("../data/dev.align"), "UTF8"));		
		
		String word;
		int engIdx = 0, gerIdx = 0;
		String[] sents, engSent, gerSent;
		for(int i=0; i<mainIBM.sentNoTrain && i < sentPairsList.size(); i++){
						
			sents = sentPairsList.get(i).split("\t");
			gerSent = sents[0].split(" ");
			engSent = sents[1].split(" ");
					
			for(int j=0; j<gerSent.length; j++){
				word = gerSent[j];
				// store indexes of words
				if(!mainIBM.gerWordsIdx.containsKey(word)){
					mainIBM.gerWordsIdx.put(word, gerIdx);
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
			
			//add gold alignment to a hash table, but the alignments did not improve!!!! so I ignored it
			if((line = br.readLine()) != null){
				line = line.replace("?", "-");
				String[] words = line.split(" ");
				for(int j=0; j<words.length; j++){
					String[] align = words[j].split("-");
					String f = gerSent[ Integer.parseInt(align[0])];
					String e = engSent[ Integer.parseInt(align[1])];
					String key = mainIBM.engWordsIdx.get(e) + "-" +  mainIBM.gerWordsIdx.get(f);
					if(!align_e_f.containsKey(key)){
						align_e_f.put(key, 1.0);
					} else
						align_e_f.put(key, align_e_f.get(key)+1);
//					if(!mainIBM.t_e_f_Idx.containsKey(key))
//						mainIBM.t_e_f_Idx.put(key, 0.05);
						
				}
			}
		}
	}
	
	public void writeAlignmentsPostProcessing(String outFile, List<String> sentPairsList, HashMap<String, Double> t_e_f) throws IOException {
		System.out.println("post processing...");
		Writer out = new BufferedWriter(new FileWriter(new File(outFile)));	
		int e,f;
		String[] sents, engSent, gerSent;
		int i;
		HashMap<Integer, Double> aligns = new HashMap<Integer, Double>();
		for(i=0; i<sentPairsList.size(); i++){

			sents = sentPairsList.get(i).split("\t");
			gerSent = sents[0].split(" ");
			engSent = sents[1].split(" ");
			
			//change print max based on English word first
			for(int j=0; j<engSent.length; j++){
				if(!mainIBM.engWordsIdx.containsKey(engSent[j])){
					continue;
				}
				e = mainIBM.engWordsIdx.get(engSent[j]);
				
				double maxProb = 0.0;
				int maxInd = -1;
				String maxWord = "";
				aligns.clear();
								
				for(int k=0; k<gerSent.length; k++){
					
					//in HMM paper, "the difference in position index is smaller than 3"
					//so, I ignore other words
					if(Math.abs(j-k) > 10)
						continue;
					
					if(!mainIBM.gerWordsIdx.containsKey(gerSent[k])){
						continue;
					}					
					f = mainIBM.gerWordsIdx.get(gerSent[k]);
					// if this pair of e|f is not in the trained data
					if(!t_e_f.containsKey(e + "-" + f)){
						continue;
					}						
					aligns.put(k, t_e_f.get(e + "-" + f)); 
					if(maxProb < aligns.get(k)){						
						if((!Character.isLetter(engSent[j].charAt(0)) && Character.isLetter(gerSent[k].charAt(0))) ||
						   (Character.isLetter(engSent[j].charAt(0)) && !Character.isLetter(gerSent[k].charAt(0)))	){
							continue;
						}
						maxProb = aligns.get(k);
						maxInd = k;
						maxWord = gerSent[k];
					}
				}
				//add alignments not just the most probable
				if (maxInd == -1)
					continue;
				if(maxWord.equals("NULL"))
					continue;
				
				//only when everything is alright write the alignment
				out.write(maxInd + "-" + j + " ");
				for(Integer k : aligns.keySet()){
					if(aligns.get(k) > mainIBM.threshold && k!=maxInd){
						out.write(k + "-" + j + " ");
					}
				}
				out.flush();
			}

			out.write("\n");
			out.flush();
		}
		out.close();
	}
	
}
