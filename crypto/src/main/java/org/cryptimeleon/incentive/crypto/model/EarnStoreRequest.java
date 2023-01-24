package org.cryptimeleon.incentive.crypto.model;

import org.cryptimeleon.math.serialization.ByteArrayRepresentation;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;

import java.util.Arrays;

public class EarnStoreRequest implements Representable {
    private final byte[] h;

    public EarnStoreRequest(byte[] h) {
        this.h = h;
    }

    public EarnStoreRequest(Representation representation) {
        this.h = ((ByteArrayRepresentation) representation).get();
    }

    public byte[] getH() {
        return h;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EarnStoreRequest that = (EarnStoreRequest) o;
        return Arrays.equals(h, that.h);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(h);
    }

    @Override
    public Representation getRepresentation() {
        return new ByteArrayRepresentation(h);
    }
}
