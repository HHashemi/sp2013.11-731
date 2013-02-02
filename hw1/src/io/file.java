package io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mt.mainIBM;

public class file {

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
//			sent1 = "NULL " + sents[0]; //null token at the beginning of german sentences
			sent1 = sents[0];
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
	public void writeAlignments(String outFile, List<String> sentPairsList, HashMap<String, Double> t_e_f) throws IOException {
		Writer out = new BufferedWriter(new FileWriter(new File(outFile)));	
		int e,f;
		String[] sents, engSent, gerSent;
		int i;
		HashMap<Integer, Double> aligns = new HashMap<Integer, Double>();
//		for(i=mainIBM.sentNoTestFrom; i<mainIBM.sentNoTestTo && i<sentPairsList.size(); i++){
		for(i=0; i<sentPairsList.size(); i++){
			sents = sentPairsList.get(i).split("\t");
			gerSent = sents[0].split(" ");
			engSent = sents[1].split(" ");
			
			
			for(int j=0; j<gerSent.length; j++){
				if(!mainIBM.gerWordsIdx.containsKey(gerSent[j])){
					continue;
				}
				f = mainIBM.gerWordsIdx.get(gerSent[j]);
				
				double maxProb = 0.0;
				int maxInd = 0;
				aligns.clear();
				for(int k=0; k<engSent.length; k++){
					if(!mainIBM.engWordsIdx.containsKey(engSent[k])){
						continue;
					}					
					e = mainIBM.engWordsIdx.get(engSent[k]);
					// if this pair of e|f is not in the trained data
					if(!t_e_f.containsKey(e + "-" + f)){
//						System.out.println(e + "\t" + f);
						continue;
					}						
					aligns.put(k, t_e_f.get(e + "-" + f)); 
					if(maxProb < aligns.get(k)){
						maxProb = aligns.get(k);
						maxInd = k;
					}
				}
				//TODO: add ? for alignments not just the most probable
				out.write(j + "-" + maxInd + " ");
			}
			out.write("\n");
			out.flush();
		}
		out.close();
	}

}
