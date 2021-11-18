package org.cryptimeleon.incentive.crypto.proof.wellformedness;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.cryptimeleon.craco.protocols.SecretInput;
import org.cryptimeleon.math.structures.rings.zn.Zn.ZnElement;

/**
 * Objects of this class represent witnesses used for proving the well-formedness of commitments.
 */
@Value
@AllArgsConstructor
public class CommitmentWellformednessWitness implements SecretInput {
    ZnElement usk; // user secret key
    ZnElement eskUsr;
    ZnElement dsrnd0;
    ZnElement dsrnd1;
    ZnElement z;
    ZnElement t;
    ZnElement uInverse;
}
