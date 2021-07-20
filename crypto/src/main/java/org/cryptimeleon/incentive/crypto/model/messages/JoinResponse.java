package org.cryptimeleon.incentive.crypto.model.messages;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.math.serialization.ListRepresentation;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.rings.zn.Zn.ZnElement;

/**
 * A class representing the third message of the Issue  {@literal <}-{@literal >} Join protocol.
 */
@Value
@AllArgsConstructor
public class JoinResponse implements Representable
{
    @NonFinal
    private SPSEQSignature preCertificate; // preliminary certificate for the user token

    @NonFinal
    private ZnElement eskProv; // the share of the provider for the ElGamal encryption key for the initial token of the user

    public JoinResponse(Representation repr, IncentivePublicParameters pp)
    {
        // force passed representation into a list representation (does not throw class cast exception in intended use cases)
        var list = (ListRepresentation) repr;

        // retrieve restorers
        var usedZn = pp.getBg().getZn();
        var usedG1 = pp.getBg().getG1();
        var usedG2 = pp.getBg().getG2();

        // restore fields
        this.preCertificate = new SPSEQSignature(list.get(0), usedG1, usedG2);
        this.eskProv = usedZn.restoreElement(list.get(1));
    }

    @Override
    public Representation getRepresentation() {
        return new ListRepresentation(
                this.preCertificate.getRepresentation(),
                this.eskProv.getRepresentation()
        );
    }
}
