package METEOR;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;


public class simpleMETEOR {
	double alpha;
	public HashMap<Integer, String> calcSimpleMeteorScore(List<String> sentPairsList, double alpha) {	
		this.alpha = alpha;
		HashMap<Integer, String> simpleMeteorScoresList = new HashMap<Integer, String>();
		String hyp1, hyp2, ref;
		double h1,h2;
		for(int i=0; i<sentPairsList.size(); i++){
			String[] sents = sentPairsList.get(i).split("\t");
			hyp1 = sents[0];
			hyp2 = sents[1];
			ref = sents[2];
			
//			h1 = calcSimpleMeteorScore(hyp1, ref);
//			h2 = calcSimpleMeteorScore(hyp2, ref);
			
			h1 = calcMeteorScore(hyp1, ref);
			h2 = calcMeteorScore(hyp2, ref);
			
			if(h1 > h2)
				simpleMeteorScoresList.put(i,"-1");
			else if (h1 == h2)
				simpleMeteorScoresList.put(i,"0");
			else if (h1 < h2)
				simpleMeteorScoresList.put(i,"1");
		}		 
		
		return simpleMeteorScoresList;
	}

	private double calcMeteorScore(String hyp, String ref) {
		List<String> hypList = splitSentencToWords(hyp);
		List<String> refList = splitSentencToWords(ref);
		
		hypList.addAll(addNgrams(hypList));			
		refList.addAll(addNgrams(refList));
		
		int matchCount=0;
				
		for(int i=0; i<refList.size(); i++){
			if(hypList.contains(refList.get(i))){
				matchCount++;
			}
		}	
		
		double P = (double) matchCount/hypList.size();
		double R = (double) matchCount/refList.size();		
		if(P == 0 || R == 0)
			return 0;		
		double nom = P*R; 
		double denom = alpha * R + (1.0-alpha) * P;		
		double mean = nom/denom;		
		return mean;
	}
	
	private List<String> addNgrams(List<String> wordList) {
		List<String> ngram = new ArrayList<String>();
		
		//add bigram
		for(int i=0; i<wordList.size()-1; i++){
			ngram.add(wordList.get(i) + " " + wordList.get(i+1));
		}
		
		//add trigram
		for(int i=0; i<wordList.size()-2; i++){
			ngram.add(wordList.get(i) + " " + wordList.get(i+1) + " " + wordList.get(i+2));
		}
		
		//add 4-gram
//		for(int i=0; i<wordList.size()-3; i++){
//			ngram.add(wordList.get(i) + " " + wordList.get(i+1) + " " + wordList.get(i+2) + " " + wordList.get(i+3));
//		}
		
		return ngram;
	}

	/**
	 * separate the punctuations in the sentence
	 * @param sent
	 * @return
	 */
	private List<String> splitSentencToWords(String sent) {
		List<String> list = new ArrayList<String>();	
//		sent = sent.replaceAll("([?:!.,;\"'])", " $1 "); //consider punctuations as separate words
//		sent = sent.replaceAll("([?:!.,;\"'])", " "); 
//		sent = sent.replaceAll("([?:!.,;\"'()])", " ");
//		sent = sent.replaceAll("([?:!.,;\"'()\\/])", " ");
		sent = sent.replace("&quot;", " ");
		sent = sent.replace("&#39;", " ");
		sent= sent.replaceAll("\\W", " "); //remove all the punctuations
		
		sent = sent.replace("  ", " ");		
		String[] words = sent.split(" ");		
		
		for(int i=0; i<words.length; i++){
			if(words[i].length()>0)
				list.add(words[i]);
		}
		return list;
	}

	/**
	 * This is the implementation for simple METEOR
	 * @param hyp
	 * @param ref
	 * @return
	 */
	private double calcSimpleMeteorScore(String hyp, String ref) {
		String[] hypWords = hyp.split(" ");
		String[] refWords = ref.split(" ");
			
		int matchCount=0;
		for(int i=0; i<refWords.length; i++){
			for(int j=0; j<hypWords.length; j++){
				if(refWords[i].equals(hypWords[j])){
					matchCount++;
					break;
				}
			}
		}
		
		double P = (double) matchCount/hypWords.length;
		double R = (double) matchCount/refWords.length;
		
		if(P == 0 || R == 0)
			return 0;
		
		double nom = P*R;
		double denom = alpha * R + (1.0-alpha) * P;
		
		double mean = nom/denom;
		
		return mean;
	}

}
