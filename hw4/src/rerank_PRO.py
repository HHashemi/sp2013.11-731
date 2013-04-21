'''
@author: hashemi
'''

#!/usr/bin/env python
import optparse
import sys
import bleu
import random
import math
import string
from collections import namedtuple

# write feature vector based on Pairwise Ranking Optimization (PRO) approach on
# "Tuning as Ranking" paper


optparser = optparse.OptionParser()
optparser.add_option("-d", "--kbest-dev", dest="dev", default="data/dev.100best", help="100-best translation lists of dev data")
optparser.add_option("-r", "--ref-dev", dest="ref", default="data/dev.ref", help="reference sentences in English")
optparser.add_option("-s", "--src-dev", dest="src_dev", default="data/dev.src", help="source sentences in Russian")

optparser.add_option("-k", "--kbest-test", dest="test", default="data/test.100best", help="100-best translation lists of test data")
optparser.add_option("-c", "--src-test", dest="src_test", default="data/test.src", help="source sentences in Russian")

optparser.add_option("-w", "--weights", dest="weights", default=None, help="List of weights in the order of : LM,tm1(p(e|f)),tm2(p_lex(f|e)),enSize")

optparser.add_option("-t", "--tau", dest="tau", default=5000, type="int", help="PRO samples per input sentence (tau, default=5000)")
optparser.add_option("-a", "--alpha", dest="alpha", default=0.05, type="float", help="Sampler acceptance cutoff (alpha, default=0.05)")
optparser.add_option("-x", "--xi", dest="xi", default=50, type="int", help="PRO training instances per input sentence (xi, default=50)")

(opts,_) = optparser.parse_args()

# step 1: This method writes feature vector based on PRO

# this function calculated the number of not translated words in hypothesis
def calcNotTranslatedWords(src, hyp):
    hypWords = hyp.strip().split()
    c = 0
    for srcW in src.strip().split():
        for char in srcW:
            if char not in set(string.printable):
                if srcW in hypWords: 
                    c = c+1
                break
    return c
        
def writeFeatureVector():
    hypothesis_sentences = namedtuple("hyp", "features, bleu")
    ref = [line.strip().split() for line in open(opts.ref)][:sys.maxint]
    src_dev = [line.strip().split("|||")[1] for line in open(opts.src_dev)][:sys.maxint]
    
    sys.stderr.write("reading dev data...")
    nbests = [[] for _ in ref]
    all_hyps = [pair.split(' ||| ') for pair in open(opts.dev)]
    num_sents = len(all_hyps) / 100
    for s in xrange(0, num_sents):
        hyps_for_one_sent = all_hyps[s * 100:s * 100 + 100]
        for (num, hyp, feats) in hyps_for_one_sent:           
            feats = [float(h.split('=')[1]) for h in feats.strip().split()]
            stats = tuple(bleu.bleu_stats(hyp.strip().split(), ref[s]))
            #TODO: add extra feature here
            # 1. adding number of target words
            enWordsNO = len(hyp.strip().split())
            feats.append(enWordsNO)
            
            #2. adding number of untranslated source words
            feats.append(calcNotTranslatedWords(src_dev[s], hyp))
            
            nbests[s].append(hypothesis_sentences(feats, bleu.bleu(stats)))
        
    # pairwise sampling. Figure 4 of the paper
    random.seed(0)
    sampling_hypothesis = namedtuple("sample", "hyp1, hyp2, gDiff")
    def sampling():
        V = []
        for _ in xrange(opts.tau):
            c1 = random.choice(nbest)
            c2 = random.choice(nbest)
            if c1 != c2 and math.fabs(c1.bleu - c2.bleu) > opts.alpha:
                V.append(sampling_hypothesis(c1, c2, math.fabs(c1.bleu - c2.bleu))) 
        return V
    
    x = []
    nbest_count = 0
    for nbest in nbests:
        nbest_count = nbest_count +1
        
        V = sampling()
        sortedV = sorted(V , key=lambda h: h.gDiff, reverse=True)[:opts.xi]  
        x_count = 0
        for idx, sample in enumerate(sortedV):
            x_count = x_count + 1
             
            tmp = [c1j-c2j for c1j,c2j in zip(sample.hyp1.features, sample.hyp2.features)]
            tmp.append(cmp(sample.hyp1.bleu , sample.hyp2.bleu))
            x.append(tmp)
            tmp = [c2j-c1j for c1j,c2j in zip(sample.hyp1.features, sample.hyp2.features)]
            tmp.append(cmp(sample.hyp2.bleu , sample.hyp1.bleu))
            x.append(tmp)
            
        if x_count != opts.xi: 
            sys.stderr.write("%d\n" % (x_count))
    
        
    #writing feature vector
    for f in x:
        print ",".join(str(f0) for f0 in f)

# step 3:  after the weights are calculated based on a binary classifier such as SVM, LR
# This method finds the best output sentences from the 100 best list of test data
def writeOutputFile():
    weights = [float(w.strip()) for w in opts.weights.split(',')]
    src_test = [line.strip().split("|||")[1] for line in open(opts.src_test)][:sys.maxint]

    all_hyps = [pair.split(' ||| ') for pair in open(opts.test)]
    num_sents = len(all_hyps) / 100
    for s in xrange(0, num_sents):
        hyps_for_one_sent = all_hyps[s * 100:s * 100 + 100]
        (best_score, best) = (-1e300, '')
        for (num, hyp, feats) in hyps_for_one_sent:
            score = 0.0
            feats = [float(h.split('=')[1]) for h in feats.strip().split()]
            
            #TODO: add extra feature here
            # 1. adding number of target words
            enWordsNO = len(hyp.strip().split())
            feats.append(enWordsNO)
            #2. adding number of untranslated source words
            feats.append(calcNotTranslatedWords(src_test[s], hyp))
            
            for idx, f in enumerate(feats):
                score += weights[idx] * f
            if score > best_score:
                (best_score, best) = (score, hyp)
        try: 
            sys.stdout.write("%s\n" % best)
        except (Exception):
            sys.exit(1)

# main method
def main():
    # step 1 : extract feature vectors 
    if opts.weights is None: writeFeatureVector()
    # step 2: calculate weights using binary classifiers. I used matlab
    # step 3: find the best output using weight vector
    else: writeOutputFile()


if __name__ == "__main__":
    main()

