%%  Homa B. Hahsemi
%%  HW4
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

clc
clear
close all
load featureVector.txt
featureVector = featureVector;

cost = 1;
[w, b] = svml(featureVector(:,1:end-1), featureVector(:,end) ,cost)