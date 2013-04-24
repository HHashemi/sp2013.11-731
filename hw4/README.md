HW4 - Reranking 

I have implemented the pairwise ranking optimization method. I used matlab SVM as the binary classifier.

Also, I added the following features too:
- difference of number of targer words
- difference of number of not translated words


3 steps in order to find the best outputs:

1. run the python file to make the feature file:

	python rerank_PRO.py > featureVector.txt
	
2. run the matlab svm file to find the weights:

	matlab svmMain.m
	
3. run the python file with the found weights as parameters

	python rerank_PRO.py -w -0.0269,-0.0189,-0.0154,0.0356,-0.1242 > output.txt
