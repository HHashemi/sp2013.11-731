'''
Created on Apr 2, 2013

@author: hashemi
'''
import argparse
import sys
import glob
import collections
import gradeOneSent
import models
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
french_sents = [tuple(line.strip().split()) for line in open(opts.input).readlines()[:opts.num_sents]]

outputs = glob.glob("./old/out*")
results = collections.defaultdict(list)

count = 0  
for output in outputs:
    count = count + 1
    out = [tuple(line.strip().split()) for line in open(output).readlines()]
    if (len(french_sents) != len(out)): continue
    sent_num = 0 
    for e in out:
        prob = gradeOneSent.gradeOneSentence(sent_num, tuple(e), french_sents[sent_num], lm, tm);
        results[sent_num].append((prob, e))
        sent_num = sent_num + 1

def printTuple(t):
        s = ""
        for i in range(len(t)):
            s = s + t[i] + " "
        return s
for sent_num in range(55):
    hypos = sorted(results[sent_num], key=lambda tup: tup[0], reverse=True) 
    print printTuple(hypos[0][1])
    
    
    
    