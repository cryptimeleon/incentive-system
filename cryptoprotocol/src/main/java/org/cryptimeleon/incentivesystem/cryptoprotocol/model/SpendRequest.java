package org.cryptimeleon.incentivesystem.cryptoprotocol.model;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProof;
import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem;
import org.cryptimeleon.incentivesystem.cryptoprotocol.proof.SpendDeductCommonInput;
import org.cryptimeleon.math.serialization.ObjectRepresentation;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.groups.GroupElement;

@Value
@AllArgsConstructor
public class SpendRequest implements Representable {
    @NonFinal
    GroupElement dsid;

    @NonFinal
    FiatShamirProof spendDeductZkp;

    @Override
    public Representation getRepresentation() {
        return new ObjectRepresentation("dsid", dsid.getRepresentation(), "spendDeductZkp", spendDeductZkp.getRepresentation());
    }

    public SpendRequest(Representation repr, IncentivePublicParameters pp, FiatShamirProofSystem fiatShamirProofSystem) {
        this.dsid = pp.getBg().getG1().restoreElement(repr.obj().get("dsid"));
        var spendDeductCommonInput = new SpendDeductCommonInput(this.dsid, pp.getW());
        this.spendDeductZkp = fiatShamirProofSystem.restoreProof(spendDeductCommonInput, repr.obj().get("spendDeductZkp"));
    }
}
