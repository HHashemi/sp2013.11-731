import io.file;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import METEOR.simpleMETEOR;
import ML.extractFeatures;


/**
 * @author Hashemi
 *	implementation of simple METEOR score
 */
public class main {
	static double alpha = 0.1;
	static List<String> sentPairsList = new ArrayList<String>();
	static List<String> sentLabelsList = new ArrayList<String>();
	static HashMap<Integer, String> scoresList = new HashMap<Integer, String>();
	
	public static void main(String args[]) throws Exception{
		//read hypothesis and reference sentences
		file objFile = new file();
//		objFile.readSentencePairs("../data/train.hyp1-hyp2-ref", sentPairsList);
//		objFile.readSentencePairs("../data/train.hyp1-hyp2-ref-20000", sentPairsList);
		objFile.readSentencePairs("../data/test.hyp1-hyp2-ref", sentPairsList);

		
		//calculate simple METEOR score
		simpleMETEOR objSimple = new simpleMETEOR();
		scoresList = objSimple.calcSimpleMeteorScore(sentPairsList, alpha);
		
		//write evaluation scores into file
		objFile.writeEvaluation("../output.METEOR.txt", scoresList);
		
		
		//--------Machine Learning approach------
		sentPairsList.clear();
		sentLabelsList.clear();
		objFile.readSentencePairs("../data/train.hyp1-hyp2-ref", sentPairsList);
		objFile.readGoldFile("../data/train.gold", sentLabelsList);
		List<String> ftrain = (new extractFeatures()).extractFeaturesFromSentPairs(sentPairsList, sentLabelsList);
		objFile.writeWekaFile("../wekaTrain.arff", ftrain);
		
		sentPairsList.clear();
		sentLabelsList.clear();
		objFile.readSentencePairs("../data/test.hyp1-hyp2-ref", sentPairsList);
//		objFile.readSentencePairs("../data/train.hyp1-hyp2-ref-6000", sentPairsList);
//		objFile.readGoldFile("../data/train.gold-6000", sentLabelsList); //TOOD
		objFile.readGoldFile("../data/train.gold", sentLabelsList);
		List<String> ftest = (new extractFeatures()).extractFeaturesFromSentPairs(sentPairsList, sentLabelsList);
		objFile.writeWekaFile("../wekaTest.arff", ftest);
		
//		scoresList = (new ML.classifier()).runWeka("../wekaTrain.arff", "../wekaTrain.arff"); //Only for testing
//		scoresList = (new ML.classifier()).runWeka("../wekaTest.arff", "../wekaTest.arff"); //Only for testing
		scoresList = (new ML.classifier()).runWeka("../wekaTrain.arff", "../wekaTest.arff");
		objFile.writeEvaluation("../output.ML.txt", scoresList);
	}
}
