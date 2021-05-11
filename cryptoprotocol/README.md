# Readme

All cryptographic protocols belong here. In particular, this includes Issue - Join, Credit - Earn and Spend - Deduct.

In the following you find a glossary explaining all important variables in the incentive system definition from the 2020 incentive system paper which can be found at https://eprint.iacr.org/2020/382.
The variables are referenced by their name in the paper and sorted by the lowest-level class in which they occur.
For each variable, we list its name(s) in the code and add a short and comprehensive explanation of what this variable stores/is good for.

### Setup

* BG
  * bilinear group used by algorithms of the incentive system
  * names in code: bg
* $\mathcal{H}$
  * hash function used to generate group elements $w$ and $h_7$
  * names in code: not yet implemented
* PRF
  * PRF used to (re-)generate pseudorandomness in cryptoprotocols
  * names in code: prfToZn
* $\mathcal{w}$
  * base of the user public keys (user public keys are powers of the type $w^{usk}$)
  * names in code: w
* $h_7$