package org.cryptimeleon.incentive.crypto.model;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.math.hash.ByteAccumulator;
import org.cryptimeleon.math.hash.UniqueByteRepresentable;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;

@Value
@AllArgsConstructor
public class GenesisSignature implements Representable, UniqueByteRepresentable {

    @NonFinal
    @Represented(restorer = "SPSEQ")
    SPSEQSignature signature;

    public GenesisSignature(Representation representation, IncentivePublicParameters incentivePublicParameters) {
        ReprUtil reprUtil = new ReprUtil(this);
        reprUtil.register(incentivePublicParameters.getSpsEq(), "SPSEQ");
        reprUtil.deserialize(representation);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public ByteAccumulator updateAccumulator(ByteAccumulator accumulator) {
        return signature.updateAccumulator(accumulator);
    }
}
