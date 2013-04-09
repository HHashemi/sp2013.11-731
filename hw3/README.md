HW3 - Stack decoder implementation

I modified the simple stack decoder by adding the following features:

-generalized the simple decoder so that stacks[i] contains translations of any i words by adding coverage to the hypothesis.

-Marginalizing over all alignments.

-Reordering words over the winner sentence.

-Adding estimated future cost to prune stacks.

-Finally, combining different my output files in order to find the best translations. This combination is over only my outputs files which were based on different settings (such as stack size, reordering limit and pruning conditions) . In more details, each time that I changed one of the parameters of the system, I kept the output file and at the last run, I combined them altogether
