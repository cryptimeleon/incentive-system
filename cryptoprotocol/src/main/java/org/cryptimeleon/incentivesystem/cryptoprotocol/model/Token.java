package org.cryptimeleon.incentivesystem.cryptoprotocol.model;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.math.hash.ByteAccumulator;
import org.cryptimeleon.math.hash.UniqueByteRepresentable;
import org.cryptimeleon.math.hash.annotations.AnnotatedUbrUtil;
import org.cryptimeleon.math.hash.annotations.UniqueByteRepresented;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn.ZnElement;

/**
 * Data holding class representing a token from a mathematical point of view (meaning: as a bunch of group elements and exponents).
 */
@Value
@AllArgsConstructor
public class Token implements Representable, UniqueByteRepresentable {

    @NonFinal
    @Represented(restorer = "G1")
    @UniqueByteRepresented
    GroupElement commitment0; // the first part of the Pedersen commitment computed from the bases and the exponents, representing the actual token

    @NonFinal
    @Represented(restorer = "G1")
    @UniqueByteRepresented
    GroupElement commitment1; // the second part of the Pedersen commitment computed from the bases and the exponents, representing the actual token

    @NonFinal
    @Represented(restorer = "Zn")
    @UniqueByteRepresented
    ZnElement encryptionSecretKey; // secret key used for the ElGamal encryption in the Spend algorithm

    @NonFinal
    @Represented(restorer = "Zn")
    @UniqueByteRepresented
    ZnElement doubleSpendRandomness0; // randomness used for the first challenge generation in double spending protection

    @NonFinal
    @Represented(restorer = "Zn")
    @UniqueByteRepresented
    ZnElement doubleSpendRandomness1; // randomness used for the second challenge generation in double spending protection

    @NonFinal
    @Represented(restorer = "Zn")
    @UniqueByteRepresented
    ZnElement z; // first value for blinding the token group element

    @NonFinal
    @Represented(restorer = "Zn")
    @UniqueByteRepresented
    ZnElement t; // second value for blinding the token group element (needed for sophisticated proof reasons)

    @NonFinal
    @Represented(restorer = "Zn")
    @UniqueByteRepresented
    ZnElement points; // number of points that the token currently stores (initially 0), v in the 2020 paper

    @NonFinal
    @UniqueByteRepresented
    @Represented(restorer = "SPSEQ")
    SPSEQSignature signature; // the SPS-EQ certifying the commitment as well-formed and valid

    public Token(Representation repr, IncentivePublicParameters pp) {
        new ReprUtil(this)
                .register(pp.getBg().getZn(), "Zn")
                .register(pp.getBg().getG1(), "G1")
                .register(pp.getSpsEq(), "SPSEQ")
                .deserialize(repr);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
        return AnnotatedUbrUtil.autoAccumulate(accumulator, this);
    }
}

