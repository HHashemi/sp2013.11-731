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
	public static int sentNoTrain = 1000;
	public static int converged = 5;
	static List<String> sentPairsList = new ArrayList<String>();
// 	static HashMap<String, Double> t_e_f = new HashMap<String, Double>();
	public static HashMap<String, Double> t_e_f_Idx = new HashMap<String, Double>();
	public static HashMap<String, Integer> engWordsIdx = new HashMap<String, Integer>(); 
	public static HashMap<String, Integer> gerWordsIdx = new HashMap<String, Integer>(); 
	
	public static void main(String args[]) throws IOException{
		//read sentence pairs form file
		//there are 98303 unique sentences, some of them are duplicate
		file objFile = new file();
		objFile.readSentencePairs("../data/dev-test-train.de-en", sentPairsList);
//		objFile.readSentencePairs("../data/test.Huma", sentPairsList);
		
		//calculate t(e|f)
		//TODO: Null, lower case?
		trainEM objEM = new trainEM();
		objEM.trainEMforIBM1(sentPairsList);

		//write alignments into file
		objFile.writeAlignments("../outputLower.txt", sentPairsList, t_e_f_Idx);
	}
}
