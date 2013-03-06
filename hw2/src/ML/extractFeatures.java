package ML;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class extractFeatures {

	public List<String> extractFeaturesFromSentPairs(List<String> sentPairsList, List<String> sentLabelsList) {
		List<String> features = new ArrayList<String>();
		String hyp1, hyp2, ref;
		for(int i=0; i<sentPairsList.size(); i++){
			String[] sents = sentPairsList.get(i).split("\t");
			hyp1 = sents[0];
			hyp2 = sents[1];
			ref = sents[2];
			
			String inputVec = extractFeature(hyp1, hyp2, ref);
			String classNo = String.valueOf(Integer.valueOf(sentLabelsList.get(i)) + 1);
			
			features.add(inputVec + classNo);
		}
		
		return features;
	}

	public static String buildHeaderWekaTrain()
 	{
 		String header = "";
 		header += "@relation trainFile"+"\n";
 		
// 		header += "@attribute ' Depth' real"+"\n";
// 		header += "@attribute ' senLen' real"+"\n";
// 		header += "@attribute ' treeSize' real"+"\n";
// 		
// 		header += "@attribute ' NP' real"+"\n";
// 		header += "@attribute ' NNP' real"+"\n";
// 		header += "@attribute ' VP' real"+"\n";
// 		header += "@attribute ' NN' real"+"\n";
// 		// Add new features
// 		header += "@attribute ' LRDE' real"+"\n";   // difference of leftmost depth of tree and the rightmost depth of the tree
// 		header += "@attribute ' SDBr' real"+"\n";
// 		
// 		header += "@attribute ' PhPrPP' real"+"\n";   // Phrase type proportion:The length in number of words of each phrase type was counted, then divided by the sentence length.
// 		header += "@attribute ' PhPrNP' real"+"\n";	// 	     prepositional phrases (PP), noun phrases (NP), verb phrases (VP). 
// 		header += "@attribute ' PhPrVP' real"+"\n";	
// 		header += "@attribute ' PhPrPPEmb' real"+"\n";   
// 		header += "@attribute ' PhPrNPEmb' real"+"\n";	
// 		header += "@attribute ' PhPrVPEmb' real"+"\n";	
// 		
// 		header += "@attribute ' PhRaPP' real"+"\n";	
// 		header += "@attribute ' PhRaNP' real"+"\n";	
// 		header += "@attribute ' PhRaVP' real"+"\n";	
 		
 		//Hyp1
 		header += "@attribute ' unigram1' real"+"\n";
 		header += "@attribute ' Bigram1' real"+"\n";
 		header += "@attribute ' Trigram1' real"+"\n";
 		header += "@attribute ' 4gram1' real"+"\n";
 		header += "@attribute ' 5gram1' real"+"\n";
 		
 		//Hyp2
 		header += "@attribute ' unigram2' real"+"\n";
 		header += "@attribute ' Bigram2' real"+"\n";
 		header += "@attribute ' Trigram2' real"+"\n";
 		header += "@attribute ' 4gram2' real"+"\n";
 		header += "@attribute ' 5gram2' real"+"\n"; 
 		
 		//TODO: add more features length + ...
 		
 		header += "@ATTRIBUTE class 	{0,1,2}"+"\n";
 		header += "@data"+"\n";
 		return header;
 	}
	
	private String extractFeature(String hyp1, String hyp2, String ref) {
		String featuresStr="";
		
		List<String> hypList1 = splitSentencToWords(hyp1);
		List<String> hypList2 = splitSentencToWords(hyp2);
		List<String> refList = splitSentencToWords(ref);
		refList.addAll(addNgrams(refList));
		
 		featuresStr += calculateUnigramPrecision(hypList1, refList)+",";	
 		featuresStr += calculateBigramPrecision(hypList1, refList)+",";	
 		featuresStr += calculateTrigramPrecision(hypList1, refList)+",";	
 		featuresStr += calculate4gramPrecision(hypList1, refList)+",";	
 		featuresStr += calculate5gramPrecision(hypList1, refList)+",";	
 		
 		featuresStr += calculateUnigramPrecision(hypList2, refList)+",";	
 		featuresStr += calculateBigramPrecision(hypList2, refList)+",";	
 		featuresStr += calculateTrigramPrecision(hypList2, refList)+",";	
 		featuresStr += calculate4gramPrecision(hypList2, refList)+",";	
 		featuresStr += calculate5gramPrecision(hypList2, refList)+",";	
 				
// 		featuresStr += (new Double(new BigDecimal(calculateUnigramPrecision(hypList1, refList) - calculateUnigramPrecision(hypList2, refList)).setScale(3,BigDecimal.ROUND_HALF_UP).doubleValue()))+",";	
// 		featuresStr += (new Double(new BigDecimal(calculateBigramPrecision(hypList1, refList)- calculateBigramPrecision(hypList2, refList)).setScale(3,BigDecimal.ROUND_HALF_UP).doubleValue())) +",";	
// 		featuresStr += (new Double(new BigDecimal(calculateTrigramPrecision(hypList1, refList) - calculateTrigramPrecision(hypList2, refList)).setScale(3,BigDecimal.ROUND_HALF_UP).doubleValue()))+",";	
// 		featuresStr += (new Double(new BigDecimal(calculate4gramPrecision(hypList1, refList) - calculate4gramPrecision(hypList2, refList)).setScale(3,BigDecimal.ROUND_HALF_UP).doubleValue()))+",";	
// 		featuresStr += (new Double(new BigDecimal(calculate5gramPrecision(hypList1, refList) - calculate5gramPrecision(hypList2, refList)).setScale(3,BigDecimal.ROUND_HALF_UP).doubleValue()))+",";	
 		
		return featuresStr;
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
		for(int i=0; i<wordList.size()-3; i++){
			ngram.add(wordList.get(i) + " " + wordList.get(i+1) + " " + wordList.get(i+2) + " " + wordList.get(i+3));
		}	
		//add 5-gram
		for(int i=0; i<wordList.size()-4; i++){
			ngram.add(wordList.get(i) + " " + wordList.get(i+1) + " " + wordList.get(i+2) + " " + wordList.get(i+3) + " " + wordList.get(i+4));
		}
		
		return ngram;
	}

	private static Double calculate5gramPrecision(List<String> hypList, List<String> refList) {   
		String fivegram = "";
		double match = 0.0;
		double allFivegrams = 0.0;
		String one = "";
		String two = "";
		String three = "";
		String four = "";
		String five = "";
		int count = 0;
	    for (String label: hypList ) {
	    	if(one.equals("")){
	    		one = label;
	    		continue;
	    	} else if(two.equals("")){
	    		two = label;
	    		continue;
	    	} else if(three.equals("")){
	    		three = label;
	    		continue;
	    	} else if(four.equals("")){
	    		four = label;
	    		continue;
	    	} else if(count < hypList.size() - 3){
	    		five = label;
	    		fivegram = one + " " + two + " " + three + " " + four + " " + five;
				if(refList.contains(fivegram.toLowerCase())){
					match++;
				}
				one = two;
				two = three;
				three = four;
				four = five;
				allFivegrams++;
				
	    	}
	    	count++;
	    }      
		
		if(allFivegrams == 0.0)
			return 0.0;
		double prec = match/allFivegrams;
		BigDecimal roundfinalPrice = new BigDecimal(prec).setScale(3,BigDecimal.ROUND_HALF_UP);
		Double doublePrec= new Double(roundfinalPrice.doubleValue());	
		if(doublePrec == 1.0)
			doublePrec = 1.0;
		return doublePrec;
	}

	private static Double calculate4gramPrecision(List<String> hypList, List<String> refList) {   
		String fourgram = "";
		double match = 0.0;
		double allFourgrams = 0.0;
		String one = "";
		String two = "";
		String three = "";
		String four = "";
		int count = 0;
	    for (String label: hypList ) {
	    	if(one.equals("")){
	    		one = label;
	    		continue;
	    	} else if(two.equals("")){
	    		two = label;
	    		continue;
	    	} else if(three.equals("")){
	    		three = label;
	    		continue;
	    	} else if(count < hypList.size() - 2){
	    		four = label;
	    		fourgram = one + " " + two + " " + three + " " + four;
				if(refList.contains(fourgram.toLowerCase())){
					match++;
				}
				one = two;
				two = three;
				three = four;
				allFourgrams++;
				
	    	}
	    	count++;
	    }      
		
		if(allFourgrams == 0.0)
			return 0.0;
		double prec = match/allFourgrams;
		BigDecimal roundfinalPrice = new BigDecimal(prec).setScale(3,BigDecimal.ROUND_HALF_UP);
		Double doublePrec= new Double(roundfinalPrice.doubleValue());	
		if(doublePrec == 1.0)
			doublePrec = 1.0;
		return doublePrec;
	}
	
	private static Double calculateTrigramPrecision(List<String> hypList, List<String> refList) {
		String trigram = "";
		double match = 0.0;
		double allTrigrams = 0.0;
		String one = "";
		String two = "";
		String three = "";
		int count = 0;
	    for (String label: hypList ) {
	    	if(one.equals("")){
	    		one = label;
	    		continue;
	    	} else if(two.equals("")){
	    		two = label;
	    		continue;
	    	} else if(count < hypList.size() - 1){
	    		three = label;
	    		trigram = one + " " + two + " " + three;
				if(refList.contains(trigram.toLowerCase())){
					match++;
				}
				one = two;
				two = three;
				allTrigrams++;
				
	    	}
	    	count++;
	    }      
		
		if(allTrigrams == 0.0)
			return 0.0;
		double prec = match/allTrigrams;
		BigDecimal roundfinalPrice = new BigDecimal(prec).setScale(3,BigDecimal.ROUND_HALF_UP);
		Double doublePrec= new Double(roundfinalPrice.doubleValue());	
		if(doublePrec == 1.0)
			doublePrec = 1.0;
		return doublePrec;
	}
	
	
	private static Double calculateBigramPrecision(List<String> hypList, List<String> refList) {   
		String bigram = "";
		double match = 0.0;
		double allBigrams = 0.0;
		String one = "";
		String two = "";
		int count = 0;
	    for (String label: hypList ) {
	    	if(one.equals("")){
	    		one = label;
	    		continue;
	    	} else if(count < hypList.size()){
	    		two = label;
	    		bigram = one + " " + two;
				if(refList.contains(bigram.toLowerCase())){
					match++;
				}
				one = two;
				allBigrams++;
				
	    	}
	    	count++;
	    }      		
		if(allBigrams == 0.0)
			return 0.0;
		double prec = match/allBigrams;
		BigDecimal roundfinalPrice = new BigDecimal(prec).setScale(3,BigDecimal.ROUND_HALF_UP);
		Double doublePrec= new Double(roundfinalPrice.doubleValue());	
		if(doublePrec == 1.0)
			doublePrec = 1.0;
		return doublePrec;
	}

	private static Double calculateUnigramPrecision(List<String> hypList, List<String> refList) {	        
		double match = 0.0;
		double allUnigrams = 0.0;
		String one = "";
		int count = 0;
	    for (String label: hypList ) {
	    	one = label;
			if(refList.contains(one)){
				match++;
			}
			allUnigrams++;
	    	count++;
	    }      		
		if(allUnigrams == 0.0)
			return 0.0;
		double prec = match/allUnigrams;
		BigDecimal roundfinalPrice = new BigDecimal(prec).setScale(3,BigDecimal.ROUND_HALF_UP);
		Double doublePrec= new Double(roundfinalPrice.doubleValue());	
		if(doublePrec == 1.0)
			doublePrec = 1.0;
		return doublePrec;
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
}
