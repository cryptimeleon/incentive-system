package org.cryptimeleon.incentive.crypto.model;

import org.cryptimeleon.math.serialization.ByteArrayRepresentation;
import org.cryptimeleon.math.serialization.ListRepresentation;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

public class EarnTransactionData implements Representable {
    private final EarnProviderRequest earnProviderRequest;
    private final byte[] h;

    public EarnTransactionData(EarnProviderRequest earnProviderRequest, byte[] h) {
        this.earnProviderRequest = earnProviderRequest;
        this.h = h;
    }

    public EarnTransactionData(Representation representation, IncentivePublicParameters pp) {
        Iterator<Representation> representationIterator = ((ListRepresentation) representation).iterator();

        this.earnProviderRequest = new EarnProviderRequest(representationIterator.next(), pp);
        this.h = ((ByteArrayRepresentation) representationIterator.next()).get();
    }

    @Override
    public Representation getRepresentation() {
        return new ListRepresentation(
                earnProviderRequest.getRepresentation(),
                new ByteArrayRepresentation(h)
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EarnTransactionData that = (EarnTransactionData) o;
        return Objects.equals(earnProviderRequest, that.earnProviderRequest) && Arrays.equals(h, that.h);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(earnProviderRequest);
        result = 31 * result + Arrays.hashCode(h);
        return result;
    }
}
