import io.file;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import METEOR.simpleMETEOR;


/**
 * @author Hashemi
 *	implementation of simple METEOR score
 */
public class main {
	static double alpha = 0.1;
	static List<String> sentPairsList = new ArrayList<String>();
	static HashMap<Integer, String> scoresList = new HashMap<Integer, String>();
	
	public static void main(String args[]) throws IOException{
		//read hypothesis and reference sentences
		file objFile = new file();
//		objFile.readSentencePairs("../data/train.hyp1-hyp2-ref", sentPairsList);
		objFile.readSentencePairs("../data/test.hyp1-hyp2-ref", sentPairsList);

		
		//calculate simple METEOR score
		simpleMETEOR objSimple = new simpleMETEOR();
		scoresList = objSimple.calcSimpleMeteorScore(sentPairsList, alpha);
		
		//write evaluation scores into file
		objFile.writeEvaluation("../output.test.txt", scoresList);
	}
}
