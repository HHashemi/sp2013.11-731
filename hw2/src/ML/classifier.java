package ML;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.Random;

import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class classifier {
//	static String classifiers []= {"0","J48","BN","RandForest","NB","MLP","SMO","VotedPerceptron","DecisionTable","Logistic"};
	private static String classifiers []= {"J48","BN","RandForest","NB","MLP","SMO","VotedPerceptron","DecisionTable","Logistic"};
	private static String classifiersML []= {"MLP"};
	public static int folds = 10;
	
	public static final HashMap<String, Integer> classifierIdxs = new HashMap<String, Integer>(){
        {
            put("J48", 1);
            put("BN", 2);
            put("RandForest", 3);
            put("NB", 4);
            put("MLP", 5);
            put("SMO", 6);
            put("VotedPerceptron", 7);
            put("DecisionTable", 8);
            put("Logistic", 9);
        }
    };
	public static final HashMap<String, String> classifierOpts = new HashMap<String, String>(){
        {
            put("J48", "-C 0.25 -M 2");
            put("BN", "-D -Q weka.classifiers.bayes.net.search.local.K2 -- -P 1 -S BAYES -E weka.classifiers.bayes.net.estimate.SimpleEstimator -- -A 0.5");
            put("RandForest", "-I 10 -K 0 -S 1");
            put("NB", "");
            put("MLP", "-L 0.3 -M 0.2 -N 500 -V 0 -S 0 -E 20 -H a");
            put("SMO", "-C 1.0 -L 0.001 -P 1.0E-12 -N 0 -V -1 -W 1 -K \"weka.classifiers.functions.supportVector.RBFKernel -C 250007 -G 0.01\"");
            put("VotedPerceptron", "-I 1 -E 1.0 -S 1 -M 10000");
            put("DecisionTable", "-X 1 -S \"weka.attributeSelection.BestFirst -D 1 -N 5\"");
            put("Logistic", "-R 1.0E-8 -M -1");

        }
    };
	public static final HashMap<String, String> classifierClass = new HashMap<String, String>(){
        {
            put("J48", "weka.classifiers.trees.J48");
            put("BN", "weka.classifiers.bayes.BayesNet");
            put("RandForest", "weka.classifiers.trees.RandomForest");
            put("NB", "weka.classifiers.bayes.NaiveBayes");
            put("MLP", "weka.classifiers.functions.MultilayerPerceptron");
            put("SMO", "weka.classifiers.functions.SMO");
            put("VotedPerceptron", "weka.classifiers.functions.VotedPerceptron");
            put("DecisionTable", "weka.classifiers.rules.DecisionTable");
            put("Logistic", "weka.classifiers.functions.Logistic");
        }
    };
    
	private Instances trainData;
	private Instances testData;
	weka.classifiers.trees.J48 J48;
	weka.classifiers.bayes.NaiveBayes  NB;
    weka.classifiers.bayes.BayesNet  BN;
    weka.classifiers.trees.RandomForest RandForest;
    weka.classifiers.functions.MultilayerPerceptron MLP;
    weka.classifiers.functions.SMO SMO;
    weka.classifiers.functions.VotedPerceptron VotedPerceptron;
    weka.classifiers.rules.DecisionTable DecisionTable;
    weka.classifiers.functions.Logistic Logistic;
    weka.classifiers.meta.AdaBoostM1 AdaBoost;
    weka.classifiers.meta.Bagging Bagging;
    weka.classifiers.meta.Vote vote;
    
	public HashMap<Integer, String> runWeka(String trainFile, String testFile) throws Exception {
		HashMap<Integer, Integer> scoresList1 = new HashMap<Integer, Integer>();
		HashMap<Integer, String> scoresList2 = new HashMap<Integer, String>();
		
//		classifyTrain(trainFile, classifiers, "../outWeka.txt");
//		classifyTrain(trainFile, testFile, classifiers, "../outWeka.txt");
		scoresList1 = classifyTest(trainFile, testFile, classifiersML, "../outWeka.txt");
		
		for(int i=0; i<scoresList1.size(); i++){
			scoresList2.put(i, String.valueOf(scoresList1.get(i)-1));
		}		
		return scoresList2;
	}
	
	private void classifyTrain(String trainFile, String testFile, String[] learnerIdxs, String output) throws Exception {
		DataSource source1 = new DataSource(trainFile);
		trainData = source1.getDataSet();
		if (trainData.classIndex() == -1)
			trainData.setClassIndex(trainData.numAttributes() - 1);
		
		DataSource sourceTe = new DataSource(testFile);
		testData = sourceTe.getDataSet();
		if (testData.classIndex() == -1)
			testData.setClassIndex(testData.numAttributes() - 1);
			
			
		Writer out = new BufferedWriter(new FileWriter(new File(output)));
		for (String key: learnerIdxs)
		{
			trainLearner(classifierIdxs.get(key), trainData);	
			Evaluation eval = new Evaluation(trainData);
			testLearner(classifierIdxs.get(key), testData, eval);
			System.out.println(eval.toSummaryString("\n==========================\n  Results 1 classifier\n==========================\n", false));
			out.write(eval.toSummaryString("\n==========================\n  "+ key + " Results 1 classifier\n==========================\n", false));
			
//			Evaluation eval1 = crossValidation(key);
//			out.write(eval1.toSummaryString("\n=====\n " + key + " crossValidation \n=====\n", false));

			
			Bagging(key, trainData);
			Evaluation eval2 = new Evaluation(trainData);
			eval2.evaluateModel(Bagging, testData);
			System.out.println(eval2.toSummaryString("\n==========================\n  Bagging\n==========================\n", false));
			out.write(eval2.toSummaryString("\n=======\n " + key + " Bagging \n=======\n", false));
			
//			AdaBoostM1(key, trainData);
//			Evaluation eval3 = new Evaluation(trainData);
//			eval3.evaluateModel(AdaBoost, testData);
//			System.out.println(eval3.toSummaryString("\n==========================\n  AdaBoost\n==========================\n", false));
//			out.write(eval3.toSummaryString("\n======\n " + key + " AdaBoost \n======\n", false));

		}	
		
		majorityVoting(trainData, learnerIdxs);
		Evaluation eval4 = new Evaluation(trainData);
		eval4.evaluateModel(vote, testData);
		System.out.println(eval4.toSummaryString("\n==========================\n  Majority Voting\n==========================\n", false));
		out.write(eval4.toSummaryString("\n==========================\n  Majority Voting\n==========================\n", false));
		out.close();
		
	}

	public void classifyTrain(String trainFile, String [] learnerIdxs, String output) throws Exception
	{
		DataSource source1 = new DataSource(trainFile);
		Instances trainData1 = source1.getDataSet();
		if (trainData1.classIndex() == -1)
			trainData1.setClassIndex(trainData1.numAttributes() - 1);
		
//		weka.filters.unsupervised.attribute.StringToNominal filter = new weka.filters.unsupervised.attribute.StringToNominal();
//		filter.
		
		int seed = 1;		
		Random rand = new Random(seed);   // create seeded number generator
		Instances randData = new Instances(trainData1);   // create copy of original data
		randData.randomize(rand); 
		
		double percent = 80.0; 
		int trainSize1 = (int) Math.round(randData.numInstances() * percent / 100); 
		int testSize1 = randData.numInstances() - trainSize1; 
		trainData = new Instances(randData, 0, trainSize1); 
		testData = new Instances(randData, trainSize1, testSize1); 
		
			
		Writer out = new BufferedWriter(new FileWriter(new File(output)));
		for (String key: learnerIdxs)
		{
			trainLearner(classifierIdxs.get(key), trainData);	
			Evaluation eval = new Evaluation(trainData);
			testLearner(classifierIdxs.get(key), testData, eval);
			System.out.println(eval.toSummaryString("\n==========================\n  Results 1 classifier\n==========================\n", false));
			out.write(eval.toSummaryString("\n==========================\n  "+ key + " Results 1 classifier\n==========================\n", false));
			
//			Evaluation eval1 = crossValidation(key);
//			out.write(eval1.toSummaryString("\n=====\n " + key + " crossValidation \n=====\n", false));

			
			Bagging(key, trainData);
			Evaluation eval2 = new Evaluation(trainData);
			eval2.evaluateModel(Bagging, testData);
			System.out.println(eval2.toSummaryString("\n==========================\n  Bagging\n==========================\n", false));
			out.write(eval2.toSummaryString("\n=======\n " + key + " Bagging \n=======\n", false));
			
//			AdaBoostM1(key, trainData);
//			Evaluation eval3 = new Evaluation(trainData);
//			eval3.evaluateModel(AdaBoost, testData);
//			System.out.println(eval3.toSummaryString("\n==========================\n  AdaBoost\n==========================\n", false));
//			out.write(eval3.toSummaryString("\n======\n " + key + " AdaBoost \n======\n", false));

		}	
		
		majorityVoting(trainData, learnerIdxs);
		Evaluation eval4 = new Evaluation(trainData);
		eval4.evaluateModel(vote, testData);
		System.out.println(eval4.toSummaryString("\n==========================\n  Majority Voting\n==========================\n", false));
		out.write(eval4.toSummaryString("\n==========================\n  Majority Voting\n==========================\n", false));
		out.close();
	}
	
	public HashMap<Integer, Integer> classifyTest(String trainFile, String testFile, String [] learnerIdxs, String output) throws Exception
	{
		HashMap<Integer, Integer> scoresList = new HashMap<Integer, Integer>();
		DataSource source = new DataSource(trainFile);
		trainData = source.getDataSet();
		if (trainData.classIndex() == -1)
			trainData.setClassIndex(trainData.numAttributes() - 1);
		 
		DataSource sourceTe = new DataSource(testFile);
		testData = sourceTe.getDataSet();
		if (testData.classIndex() == -1)
			testData.setClassIndex(testData.numAttributes() - 1);
			
		for (String key: learnerIdxs)
		{
//			trainLearner(classifierIdxs.get(key), trainData);	
			Bagging(key, trainData);
			// label instances
			for (int i = 0; i < testData.numInstances(); i++) {
				int clsLabel = (int) Bagging.classifyInstance(testData.instance(i));
				scoresList.put(i, clsLabel);
			}

		}	
		return scoresList;
	}
	private void saveModel(String LearnerType, String modelFile) throws Exception
	{
			//ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("./models/"+leanerName[LearnerType]+".model"));
			String fileName =  modelFile;
			switch (classifierIdxs.get(LearnerType)) {
	 		  case 1:
	 			 weka.core.SerializationHelper.write(fileName, J48);
	 			 break;
	 		  case 2:
	 			 weka.core.SerializationHelper.write(fileName, BN);
	 			 break;
	 		  case 3:
	 			 weka.core.SerializationHelper.write(fileName, RandForest);
	 			 break;
	 		  case 4:
	 			 weka.core.SerializationHelper.write(fileName, NB);
	 			 break;
	 		  case 5:
	 			 weka.core.SerializationHelper.write(fileName, MLP);
	 			 break;
	 		  case 6:
	 			 weka.core.SerializationHelper.write(fileName, SMO);
	 			 break;
	 		  case 7:
	 			 weka.core.SerializationHelper.write(fileName, VotedPerceptron);
	 			 break;
	 		  case 8:
	 			 weka.core.SerializationHelper.write(fileName, DecisionTable);
	 			 break;
	 		  case 9:
	 			 weka.core.SerializationHelper.write(fileName, Logistic);
	 			 break;
	 		  default:
	 			  System.out.print("error in Learner type= "+LearnerType);
	 		}		
	}
	
	public void loadModel(String modelFile, String LearnerType) throws Exception
	{
			String fileName =  modelFile;
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName));
			switch (classifierIdxs.get(LearnerType)) {
	 		  case 1:
	 			 J48 = (weka.classifiers.trees.J48) ois.readObject();
	 			 break;
	 		  case 2:
	 			 BN = (weka.classifiers.bayes.BayesNet) ois.readObject();
	 			 break;
	 		  case 3:
	 			 RandForest = (weka.classifiers.trees.RandomForest) ois.readObject();
	 			 break;
	 		  case 4:
	 			 RandForest = (weka.classifiers.trees.RandomForest) ois.readObject();
	 			 break;

	 			 //TODO: continue till 9 classifiers. 
	 		  default:
	 			  System.out.print("error in Learner type= "+LearnerType);
	 		}		
	}
	/**
	 * Majority Voting of all classifiers
	 * @param trainData2
	 * @param learnerIdxs
	 * @return
	 * @throws Exception
	 */
	private int majorityVoting(Instances trainData2, String[] learnerIdxs) throws Exception {
		String opts = "";
		for (String key: learnerIdxs)
		{
			opts += "-B \"" + classifierClass.get(key) + " " + classifierOpts.get(key).replace("\"", "\\\"") + "\" "; 
		}
		vote = new weka.classifiers.meta.Vote();
		vote.setOptions(weka.core.Utils.splitOptions("-S 1 " + opts + "-R AVG"));
		vote.buildClassifier(trainData2);
		return 0;	
	}
	
	/**
	 * Ensemble methods : Bagging
	 * @param key
	 */
	private int Bagging(String key, Instances train) throws Exception{
		Bagging = new weka.classifiers.meta.Bagging();
		Bagging.setOptions(weka.core.Utils.splitOptions("-P 100 -S 1 -I 10 -W " + classifierClass.get(key) +" -- " +classifierOpts.get(key)));
		Bagging.buildClassifier(train);
		return 0;		
	}
	
	/**
	 * Ensemble methods : AdaBoosting
	 * @param key
	 */
	private int AdaBoostM1(String key, Instances train) throws Exception{
		AdaBoost = new weka.classifiers.meta.AdaBoostM1();
		AdaBoost.setOptions(weka.core.Utils.splitOptions("-P 100 -S 1 -I 10 -W " + classifierClass.get(key) +" -- " +classifierOpts.get(key)));
		AdaBoost.buildClassifier(train);
		return 0;		
	}

	/**
	 * Cross Validation
	 * @throws Exception 
	 */
	private Evaluation crossValidation(String LearnerType) throws Exception {
		int seed;
//		for (int i = 0; i < folds; i++) {
//			seed = i+1;  // every run gets a new, but defined seed value
			seed = 1;
			
			// see: randomize the data
			Random rand = new Random(seed);   // create seeded number generator
			Instances randData = new Instances(trainData);   // create copy of original data
			randData.randomize(rand); 
			 
			Instances randDataTest = new Instances(testData);   // create copy of original data
			randDataTest.randomize(rand); 
			
			Evaluation eval1 = new Evaluation(randData);
			// see: generate the folds
			for (int n = 0; n < folds; n++) {
				Instances train = randData.trainCV(folds, n);
				Instances test = randDataTest.testCV(folds, n);				 
				// further processing, classification, etc.
				trainLearner(classifierIdxs.get(LearnerType), train);	
				testLearner(classifierIdxs.get(LearnerType), test, eval1);
			}
			System.out.println(eval1.toSummaryString(" 10-fold Cross Validation Results\n======\n", false));
			return eval1;
//		}
	}

	/**
	 * Train
	 * @param LearnerType
	 * @throws Exception
	 */
	private void trainLearner(int LearnerType, Instances train) throws Exception
 	{
		
 		switch (LearnerType) {
 		  case 1:
 			 trainJ48(train);
 			 break;
 		  case 2:
 			 trainBN(train);
 			 break;
 		  case 3:
 			 trainRandForest(train);
 			 break;
 		  case 4:
 			 trainNaiveBayes(train);
 			 break;
 		  case 5:
 			 trainMLP(train);
 			 break;
 		  case 6:
 			 trainSMO(train);
 			 break;
 		  case 7:
 			 trainVotedPerceptron(train);
 			 break;
 		  case 8:
 			 trainDecisionTable(train);
 			 break;
 		  case 9:
 			 trainLogisticReg(train);
 			 break;
 		  default:
 			 System.out.print("error in Learner type= "+LearnerType);
 		}
 	}
	
	/**
	 * 
	 * Test
	 * @param LearnerType
	 * @throws Exception
	 */
	private void testLearner(int LearnerType, Instances test, Evaluation eval2) throws Exception
 	{
 		switch (LearnerType) {
 		  case 1:
 			 eval2.evaluateModel(J48, test);
 			 System.out.print("J48  ");
 			 break;
 		  case 2:
 			 eval2.evaluateModel(BN, test);
 			 System.out.print("Bayes Net  ");
 			 break;
 		  case 3:
 			 eval2.evaluateModel(RandForest, test);
 			 System.out.print("Random Forest  ");
 			 break;
 		  case 4:
 			 eval2.evaluateModel(NB, test);
 			 System.out.print("Naive Bayes  ");
 			 break;
 		  case 5:
 			 eval2.evaluateModel(MLP, test);
 			 System.out.print("MLP  ");
 			 break;
 		  case 6:
 			 eval2.evaluateModel(SMO, test);
 			 System.out.print("SMO  ");
 			 break;
 		  case 7:
 			 eval2.evaluateModel(VotedPerceptron, test);
 			 System.out.print("VotedPerceptron  ");
 			 break;
 		  case 8:
 			 eval2.evaluateModel(DecisionTable, test);
 			 System.out.print("DecisionTable  ");
 			 break;
 		  case 9:
 			 eval2.evaluateModel(Logistic, test);
 			 System.out.print("Logistic  ");
 			 break;
 		  default:
 			 System.out.print("error in Learner type= "+LearnerType);
 		}
  	}
		
	private  int trainJ48(Instances train) throws Exception
	{
		J48 = new weka.classifiers.trees.J48();
		J48.setOptions(weka.core.Utils.splitOptions(classifierOpts.get("J48")));
		J48.buildClassifier(train);
		return 0;
	}
	private  int trainBN(Instances train) throws Exception
	{
        BN = new weka.classifiers.bayes.BayesNet();
        BN.setOptions(weka.core.Utils.splitOptions(classifierOpts.get("BN")));
        BN.buildClassifier(train);
		return 0;
	}
	
	private  int trainRandForest(Instances train) throws Exception
	{
        RandForest = new weka.classifiers.trees.RandomForest();
        RandForest.setOptions(weka.core.Utils.splitOptions(classifierOpts.get("RandForest")));
        RandForest.buildClassifier(train);
		return 0;
	}
	
	private  int trainNaiveBayes(Instances train) throws Exception
	{
        NB = new weka.classifiers.bayes.NaiveBayes();
        NB.buildClassifier(train);
		return 0;
	}
	
	private  int trainMLP(Instances train) throws Exception
	{
		MLP = new weka.classifiers.functions.MultilayerPerceptron();
		MLP.setOptions(weka.core.Utils.splitOptions(classifierOpts.get("MLP")));
		MLP.buildClassifier(train);
		return 0;
	}
	
	private  int trainSMO(Instances train) throws Exception
	{
		SMO = new weka.classifiers.functions.SMO();
        SMO.setOptions(weka.core.Utils.splitOptions(classifierOpts.get("SMO")));
        SMO.buildClassifier(train);
		return 0;
	}
	
	private  int trainVotedPerceptron(Instances train) throws Exception
	{
		VotedPerceptron = new weka.classifiers.functions.VotedPerceptron();
		VotedPerceptron.setOptions(weka.core.Utils.splitOptions(classifierOpts.get("VotedPerceptron")));
		VotedPerceptron.buildClassifier(train);
		return 0;
	}
	
	private  int trainDecisionTable(Instances train) throws Exception
	{
		DecisionTable = new weka.classifiers.rules.DecisionTable();
		DecisionTable.setOptions(weka.core.Utils.splitOptions(classifierOpts.get("DecisionTable")));
		DecisionTable.buildClassifier(train);
		return 0;
	}
	
	private  int trainLogisticReg(Instances train) throws Exception
	{
		Logistic = new weka.classifiers.functions.Logistic();
		Logistic.setOptions(weka.core.Utils.splitOptions(classifierOpts.get("Logistic")));
		Logistic.buildClassifier(train);
		return 0;
	}
	

}
