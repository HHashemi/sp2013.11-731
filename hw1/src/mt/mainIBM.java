package mt;

import io.file;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author hashemi
 *
 */
public class mainIBM {
	public static int sentNoTrain = 100000;
	public static double threshold = 0.9;
	public static int converged = 5;
	static List<String> sentPairsList = new ArrayList<String>();
	public static HashMap<String, Double> t_e_f_Idx = new HashMap<String, Double>();
	public static HashMap<String, Integer> engWordsIdx = new HashMap<String, Integer>(); 
	public static HashMap<String, Integer> gerWordsIdx = new HashMap<String, Integer>(); 
	
	public static void main(String args[]) throws IOException{
		//read sentence pairs form file
		//there are 98303 unique sentences, some of them are duplicate
		//added Null word to German sentences and lower case all the words
		file objFile = new file();
		objFile.readSentencePairs("../data/dev-test-train.de-en", sentPairsList);
		
		//calculate t(e|f)
		trainEM objEM = new trainEM();
		objEM.trainEMforIBM1(sentPairsList);
		
		//write alignments into file
		objFile.writeAlignmentsSimpleMax("../output-basicIBM1.txt", sentPairsList, t_e_f_Idx);
		sentPairsList.clear();
		objFile.writeProbabilities("../probabilities-100000.txt", "../rawProbablities-100000.txt", t_e_f_Idx);
		
		//post processing before writing alignments
		objFile.loadProbabilitiesFile("../rawProbablities-100000.txt", sentPairsList);
		objFile.writeAlignmentsPostProcessing("../output-100000-postEditing.txt", sentPairsList, t_e_f_Idx);
	}
}
