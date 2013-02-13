HW1 - IBM model 1 implementation

First, as the base model, I developed IBM Model 1 to calculate translation probabilities of t(e|f). By this point, the alignments AER was 0.42.
(note: I saved translation probabilities in a separate file to reuse it in post-processing step.)

Then, I tried some linguistic heuristics to refine the alignments. My final alignment result includes the following post-processing heuristics:
	- As mentioned in HMM alignment paper, the difference in position index in German and English sentences is smaller than 3. So, In my experiments, I set this difference heuristically to 10.
	- I also checked whether the aligned words could be matched in numbers and punctuations with each other. If they could not be matched, I just ignored that alignement.
	- I tried to use the given alignments in dev.align file as the gold training data, but the AER results in test data were not better than the last two heuristics, so I ignored it in the final submission.