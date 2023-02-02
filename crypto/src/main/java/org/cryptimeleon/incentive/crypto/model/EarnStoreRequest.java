package org.cryptimeleon.incentive.crypto.model;

import org.cryptimeleon.math.serialization.*;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class EarnStoreRequest implements Representable {
    private final byte[] h;
    private final UUID basketId;
    private final BigInteger promotionId;

    public EarnStoreRequest(byte[] h, UUID basketId, BigInteger promotionId) {
        this.h = h;
        this.basketId = basketId;
        this.promotionId = promotionId;
    }

    public EarnStoreRequest(Representation representation) {
        ListRepresentation listRepresentation = (ListRepresentation) representation;
        this.h = ((ByteArrayRepresentation) listRepresentation.get(0)).get();
        this.basketId = UUID.fromString(((StringRepresentation) listRepresentation.get(1)).get());
        this.promotionId = new BigInteger(((StringRepresentation) listRepresentation.get(2)).get());
    }

    public byte[] getH() {
        return h;
    }

    public UUID getBasketId() {
        return basketId;
    }

    public BigInteger getPromotionId() {
        return promotionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EarnStoreRequest that = (EarnStoreRequest) o;
        return Arrays.equals(h, that.h) && Objects.equals(basketId, that.basketId) && Objects.equals(promotionId, that.promotionId);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(basketId, promotionId);
        result = 31 * result + Arrays.hashCode(h);
        return result;
    }

    @Override
    public Representation getRepresentation() {
        return new ListRepresentation(
                new ByteArrayRepresentation(h),
                new StringRepresentation(basketId.toString()),
                new StringRepresentation(promotionId.toString())
        );
    }
}
