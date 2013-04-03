#!/usr/bin/env python
import argparse
import sys
import models
import heapq
import gradeOneSent
import math
from collections import namedtuple

parser = argparse.ArgumentParser(description='Simple phrase based decoder.')
parser.add_argument('-i', '--input', dest='input', default='data/input', help='File containing sentences to translate (default=data/input)')
parser.add_argument('-t', '--translation-model', dest='tm', default='data/tm', help='File containing translation model (default=data/tm)')
parser.add_argument('-s', '--stack-size', dest='s', default=1000, type=int, help='Maximum stack size (default=1)')
parser.add_argument('-n', '--num_sentences', dest='num_sents', default=sys.maxint, type=int, help='Number of sentences to decode (default=no limit)')
parser.add_argument('-l', '--language-model', dest='lm', default='data/lm', help='File containing ARPA-format language model (default=data/lm)')
parser.add_argument('-v', '--verbose', dest='verbose', action='store_true', default=False,  help='Verbose mode (default=off)')
opts = parser.parse_args()

tm = models.TM(opts.tm, sys.maxint)
lm = models.LM(opts.lm)
sys.stderr.write('Decoding %s...\n' % (opts.input,))
input_sents = [tuple(line.strip().split()) for line in open(opts.input).readlines()[:opts.num_sents]]

hypothesis = namedtuple('hypothesis', 'logprob, lm_state, predecessor, phrase, coverage, futureCost')

def coverage(sequence):
    # Generate a coverage for a sequence of indexes #
    # You can do things like:
    #   c1 | c2 to "add" coverages
    #   c1 & c2 will return 0 if c1 and c2 do NOT overlap
    #   c1 & c2 will be != 0 if c1 and c2 DO overlap
    return reduce(lambda x,y: x|y, map(lambda i: long(1) << i, sequence), 0)

def coverage2str(c, n, on=[1], off=[0]):
    # Generate a length-n string representation of coverage c #
    return [] if n==0 else (on if c&1==1 else off) + coverage2str(c>>1, n-1, on, off)


def Trans_option_idx (cov, lenF):
    goal = coverage(range(len(f)))
    s = coverage2str(cov, lenF);
    i = 0
    cnt = 0
    L = []
    while i < len(s):
        if s[i] == 1:
            i = i + 1 
            continue
        else:
            start = i
            while (s[i]== 0):
                i = i+1
                if i == len(s):
                    break
            end = i
            L = L + [(start,end)]
            cnt = cnt +1
    return L    


def calcFutureCostEstimatesTable(f):
    cost = {}
    for length in range(1, len(f) + 1):
        for start in range(0, len(f) + 1 - length):
            end = start + length
            tmCost = float("-inf")
            if f[start:end] in tm:  tmCost = tm[f[start:end]][0].logprob
            for i in range(start + 1, end):
                if cost[(start, i)][0] + cost[(i, end)][0] > tmCost: #because of minus I change the direction <
                    tmCost = cost[(start, i)][0] + cost[(i, end)][0]
            
            #adding lm cost
            lmCost = 0
            avgCost = 0
            count = 0
            if f[start:end] in tm:             
                for phrase in tm[f[start:end]]:
                    count = count + 1
                    local_lmCost = 0                   
                    lm_state = ()
                    for word in phrase.english.split():
                        (lm_state, word_logprob) = lm.score(lm_state, word)
                        local_lmCost += word_logprob
                    if count == 1: lmCost = local_lmCost #find lm cost only for the most probable tm phrase
                    
                    avgCost = avgCost + math.exp(phrase.logprob)*(phrase.logprob + local_lmCost)
            cost[(start, end)] = (tmCost, lmCost, avgCost)
    return cost

def findFutureCost(futureCostTable, coverage):
    TList = Trans_option_idx(h.coverage,len(f)) 
    fcost = 0   
    for opt in TList:
        start = opt[0]
        end = opt[1]
        fcost = fcost + futureCostTable[(start, end)][0] #this is tm
        fcost = fcost + futureCostTable[(start, end)][1] #this is lm
        #fcost = fcost + futureCostTable[(start, end)][2] #this is avg lm
    return fcost

sent_num = 0
for f in input_sents:
    # The following code implements a DP monotone decoding
    # algorithm (one that doesn't permute the target phrases).
    # Hence all hypotheses in stacks[i] represent translations of 
    # the first i words of the input sentence.
    # HINT: Generalize this so that stacks[i] contains translations
    # of any i words (remember to keep track of which words those
    # are, and to estimate future costs)
    # I have added this feature in this code
    
    futureCostTable = calcFutureCostEstimatesTable(f)
    initial_hypothesis = hypothesis(0.0, lm.begin(), None, None, 0, 0)

    stacks = [{} for _ in f] + [{}]
    stacks[0][lm.begin()] = initial_hypothesis
    for stackNo, stack in enumerate(stacks[:]): #:-1 removed
        # extend the top s hypotheses in the current stack
        for h in heapq.nlargest(opts.s, stack.itervalues(), key=lambda h: (h.logprob)): # prune (h.logprob + h.futureCost)
            TList = Trans_option_idx(h.coverage,len(f)) 
            count = 0
            for opt in TList:
                count = count + 1
                start = opt[0]
                end = opt[1]
                if count == 1 : start0 = start;   
                #if count >3 : break       #todo:
                for i in xrange(start,end):   
                    if (i-start0)>2:   #TODO: 
                        break
                    for j in xrange(start+1,end+1):
                        if f[i:j] in tm:
                            for phrase in tm[f[i:j]]:
                                logprob = h.logprob + phrase.logprob
                                lm_state = h.lm_state
                                for word in phrase.english.split():
                                    (lm_state, word_logprob) = lm.score(lm_state, word)
                                    logprob += word_logprob
                                logprob += lm.end(lm_state) if j == len(f) else 0.0
                                new_coverage = h.coverage | coverage(range(i,j))
                                new_futureCost = h.futureCost + findFutureCost(futureCostTable, new_coverage)
                                new_hypothesis = hypothesis(logprob, lm_state, h, phrase, new_coverage, new_futureCost)
                                new_StackNo = stackNo + (j-i)
                                if lm_state not in stacks[new_StackNo] or stacks[new_StackNo][lm_state].logprob < logprob: # second case is recombination
                                    stacks[new_StackNo][lm_state] = new_hypothesis 

    # find best translation by looking at the best scoring hypothesis
    # on the last stack
    winner = max(stacks[-1].itervalues(), key=lambda h: h.logprob)
    def extract_english_recursive(h):
        return '' if h.predecessor is None else '%s%s ' % (extract_english_recursive(h.predecessor), h.phrase.english)
    
    
    def extract_tm_logprob(h):
            return 0.0 if h.predecessor is None else h.phrase.logprob + extract_tm_logprob(h.predecessor)
    sent_num = sent_num +1
       
    # coding for Marginalization
    probMax = float("-inf")
    #for h0 in heapq.nlargest(1000, stacks[-1].itervalues(), key=lambda h: h.logprob): # TODO: only consider 1000 top hypothesis in the last stack
    for h0 in stacks[-1].itervalues(): # consider all the hypothesis in the last stack
        e = tuple(extract_english_recursive(h0).strip().split())
        prob = gradeOneSent.gradeOneSentence(sent_num, e, f, lm, tm)
        if prob > probMax:
            eMax = e
            probMax = prob
    
    e = eMax    
    
    # do all the reordering
    for i in range(len(e)-1): 
        #k = min(i+20,len(e)-1)  #TODO: 
        k = len(e)-1
        for j in range(i+1, k):            
            etmp = list(e)
            etmp[i] = e[j]
            etmp[j] = e[i]
            etmp = tuple(etmp)
            prob = gradeOneSent.gradeOneSentence(sent_num, etmp, f, lm, tm);
            if prob> probMax:
                e = etmp
                probMax = prob              
     
    def printTuple(t):
        s = ""
        for i in range(len(t)):
            s = s + t[i] + " "
        return s
     
    print printTuple(e)
    #print extract_english_recursive(winner)

    if opts.verbose:
        tm_logprob = extract_tm_logprob(winner)
        sys.stderr.write('LM = %f, TM = %f, Total = %f\n' % 
            (winner.logprob - tm_logprob, tm_logprob, winner.logprob))
