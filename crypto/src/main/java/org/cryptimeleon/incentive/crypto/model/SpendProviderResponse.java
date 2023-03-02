package org.cryptimeleon.incentive.crypto.model;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignatureScheme;
import org.cryptimeleon.math.serialization.ListRepresentation;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.util.Iterator;
import java.util.Objects;

public class SpendProviderResponse implements Representable {
    private final SPSEQSignature signature;
    private final Zn.ZnElement dsidStarProv;

    public SpendProviderResponse(SPSEQSignature signature, Zn.ZnElement dsidStarProv) {
        this.signature = signature;
        this.dsidStarProv = dsidStarProv;
    }

    public SpendProviderResponse(Representation representation, IncentivePublicParameters pp) {
        Iterator<Representation> representationIterator = ((ListRepresentation) representation).iterator();
        SPSEQSignatureScheme spseqSignatureScheme = pp.getSpsEq();

        this.signature = spseqSignatureScheme.restoreSignature(representationIterator.next());
        this.dsidStarProv = pp.getBg().getZn().restoreElement(representationIterator.next());
    }

    public SPSEQSignature getSignature() {
        return signature;
    }

    public Zn.ZnElement getDsidStarProv() {
        return dsidStarProv;
    }

    @Override
    public Representation getRepresentation() {
        return new ListRepresentation(
                signature.getRepresentation(),
                dsidStarProv.getRepresentation()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpendProviderResponse that = (SpendProviderResponse) o;
        return Objects.equals(signature, that.signature) && Objects.equals(dsidStarProv, that.dsidStarProv);
    }

    @Override
    public int hashCode() {
        return Objects.hash(signature, dsidStarProv);
    }
}
