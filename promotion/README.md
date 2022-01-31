# Promotion

The promotion package can be seen as a wrapper around the token update functionalities of the crypto package.

## Concepts / Naming
 
 - Promotion: We have one Promotion per token, promotions define how users can update their tokens, and which side effects these updates have.
 - Side Effect: Any change in the basket triggered by applying an update on a token, e.g. adding a free item, applying a discount, etc.
 - Earn: Updates according to the Credit-Earn protocol are fast, but limited in their expressiveness. 
    They can only add some amount to the user's token.
 - Spend: Spend-style updates are slow, but since they use ZKPs can have checks on the old and new token, e.g. range proofs and OR statements (to hide which update is performed).  

## Server's / Provider's Functionality

 1. Provide definitions of all promotions including their update and side effect options.
 2. Validate and perform token update requests typically containing:
    - _type of request_
    - _basket id_
    - _promotion id_
    - _requested side effect_
    - _additional metadata_ (for example public input to the ZKPs that must be validated before like timestamps)
    
## Client-Side Functionality

 1. Query promotion definitions
 2. Present promotion definitions in human-readable way (e.g. through textual description in addition to ZKP definitions)
 3. Compute set of possible updates based on token and current basket
   - Some might be parametrized by e.g. a floating-point number
 4. Perform corresponding update protocol with server
