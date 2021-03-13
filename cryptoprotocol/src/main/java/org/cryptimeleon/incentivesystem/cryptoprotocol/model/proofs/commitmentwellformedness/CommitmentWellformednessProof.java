package org.cryptimeleon.incentivesystem.cryptoprotocol.model.proofs.commitmentwellformedness;

import org.cryptimeleon.craco.protocols.arguments.sigma.schnorr.DelegateProtocol;

/**
 * A protocol for proof of the well-formedness of the preliminary commitment in the initial user token as defined in the Issue<->Join protocol of
 * the Cryptimeleon incentive system.
 *
 * In particular, it proves knowledge of (usk, t, z, 1/u, eskUsr, dsrnd0, dsrnd1) in Z_p^6 s.t.
 * I    upk = w^usk
 * II   C'_0 = ( C^pre_0 ) ^ 1/u
 * III  C'_0 = h1^usk * h2^esk_usr * h3^dsrnd0 * h4^dsrnd1 * h6^z * h7^t
 * IV   g1 = ( C^pre_1 ) ^ 1/u
 *
 * Note that instead of u, 1/u is used to avoid implementation of double exponent proofs.
 */
public class CommitmentWellformednessProof extends DelegateProtocol {
    
}
