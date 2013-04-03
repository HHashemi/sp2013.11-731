HW3 - Stack decoder implementation

I modified the simple stack decoder by adding the following features:

-generalized the simple decoder so that stacks[i] contains translations of any i words by adding coverage to the hypothesis.

-Marginalizing over all alignments.

-Reordering words over the winner sentence.

-Adding estimated future cost to prune stacks.

-Finally, combining different output files in order to find the best translations.