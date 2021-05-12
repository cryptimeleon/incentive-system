# Readme

All cryptographic protocols belong here. In particular, this includes Issue - Join, Credit - Earn and Spend - Deduct.

In the following you find a glossary explaining all important variables in the incentive system definition from the 2020 incentive system paper ("Cryptimeleon incentive system" in the following) which can be found at https://eprint.iacr.org/2020/382.
The variables are referenced by their name in the paper and grouped by their first occurence when traversing the algorithms/protocols in the order they are defined in the paper.
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
  * group element that is randomized and multiplied with the token's Pedersen commitment for sophisticated proof reasons
  * names in code: h7
* SPS-EQ
  * SPS-EQ scheme used to certify user tokens upon creation and update
  * names in code: spsEq
* $g_1$
  * generator of $G_1$, the first group of the used BG 
  * names in code: g1, g1Generator
* $g_2$
  * generator of $G_2$, the second group of the used BG 
  * names in code: g2, g2Generator
* base
  * base used for ElGamal encryption secret key decomposition
  * Setup.ESK_DEC_BASE is a default value for this
  * names in code: IncentivePublicParameters.eskDecBase
* pp
  * public parameters of the Cryptimeleon incentive system
  * names in code: pp
  
### U.KeyGen
  
* usk
  * user's secret exponent
  * names in code: usk
* $\beta_{usr}$
  * PRF key that the user uses to generate pseudorandomness
  * names in code: betaUsr
* USK
  * user secret key, consisting of secret exponent and PRF key
  * names in code: uSk, usk
* upk
  * user public key (single group element)
  * note that despite the user public key is just a single group element, a data class for it exists nontheless
  * names in code: upkElem, upk
  
### P.KeyGen
  
* $(pk_{SPS-EQ}, sk_{SPS-EQ})$
  * SPS-EQ key pair the provider uses to certify tokens
  * names in code: (pkSpsEq, skSpsEq)
* h<sub>1</sub>, ..., h<sub>6</sub>
  * bases used in the token's Pedersen commitment
  * are represented in the code as a single vector
  * names in code: h
* q<sub>1</sub>, ..., q<sub>6</sub>
  * DLOGs of the bases used in the token's Pedersen commitment
  * are represented in the code as a single vector
  * names in code: q
* &#03B2<sub>prov</sub>
  * PRF key that the provider uses to generate pseudorandomness
  * names in code: betaProv
* sk
  * provider secret key
  * names in code: sk
* pk
  * provider public key
  * names in code: pk
  
### Issue-Join

* esk^*_{usr}
  * fresh Zp exponent used for updating/initializing a token
  * represents the user share of the ElGamal encryption secret key used 
  * names in code: eskUsr
* dsrnd_0
  * fresh Zp exponent used for updating/initializing a token
  * represents the user share of the ElGamal encryption secret key used 
  * names in code: eskUsr