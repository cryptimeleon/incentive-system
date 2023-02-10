package org.cryptimeleon.incentive.crypto.model;

import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProof;
import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem;
import org.cryptimeleon.craco.sig.ecdsa.ECDSASignature;
import org.cryptimeleon.craco.sig.ecdsa.ECDSASignatureScheme;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignatureScheme;
import org.cryptimeleon.incentive.crypto.Util;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentive.crypto.model.keys.store.StorePublicKey;
import org.cryptimeleon.incentive.crypto.proof.spend.tree.SpendDeductTree;
import org.cryptimeleon.incentive.crypto.proof.spend.zkp.SpendDeductBooleanZkp;
import org.cryptimeleon.incentive.crypto.proof.spend.zkp.SpendDeductZkpCommonInput;
import org.cryptimeleon.math.hash.UniqueByteRepresentable;
import org.cryptimeleon.math.serialization.*;
import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;
import java.util.Objects;
import java.util.UUID;

public class SpendRequestECDSA implements Representable {
    private final BigInteger promotionId;
    private final Zn.ZnElement doubleSpendingId;
    private final UUID basketId;
    private final Zn.ZnElement c;
    private final ECDSASignature couponSignature;
    private final SPSEQSignature tokenSignature;
    private final StorePublicKey storePublicKey;

    private final GroupElement cPre0;
    private final GroupElement cPre1;
    private final GroupElement c0;
    private final FiatShamirProof proof;

    public SpendRequestECDSA(BigInteger promotionId,
                             Zn.ZnElement doubleSpendingId,
                             UUID basketId, Zn.ZnElement c,
                             ECDSASignature couponSignature,
                             SPSEQSignature tokenSignature,
                             StorePublicKey storePublicKey, GroupElement cPre0,
                             GroupElement cPre1,
                             GroupElement c0,
                             FiatShamirProof proof) {
        this.promotionId = promotionId;
        this.doubleSpendingId = doubleSpendingId;
        this.basketId = basketId;
        this.c = c;
        this.couponSignature = couponSignature;
        this.tokenSignature = tokenSignature;
        this.storePublicKey = storePublicKey;
        this.cPre0 = cPre0;
        this.cPre1 = cPre1;
        this.c0 = c0;
        this.proof = proof;
    }

    public SpendRequestECDSA(SpendCouponRequest spendCouponRequest, SpendCouponSignature spendCouponSignature, PromotionParameters promotionParameters, UUID basketId) {
        this(promotionParameters.getPromotionId(),
                spendCouponRequest.getDsid(),
                basketId,
                spendCouponRequest.getC(),
                spendCouponSignature.getSignature(),
                spendCouponRequest.getSigma(),
                spendCouponSignature.getStorePublicKey(),
                spendCouponRequest.getCPre0(),
                spendCouponRequest.getCPre1(),
                spendCouponRequest.getC0(),
                spendCouponRequest.getSpendZkp());
    }

    public SpendRequestECDSA(Representation representation,
                             IncentivePublicParameters pp,
                             PromotionParameters promotionParameters,
                             SpendDeductTree spendDeductTree,
                             ProviderPublicKey providerPublicKey,
                             UniqueByteRepresentable context) {
        // TODO need promotion parameters but do not know promotionId yet!
        ListRepresentation listRepresentation = (ListRepresentation) representation;
        Zn zn = pp.getBg().getZn();
        Group group = pp.getBg().getG1();
        ECDSASignatureScheme ecdsaSignatureScheme = new ECDSASignatureScheme();
        SPSEQSignatureScheme spseqSignatureScheme = pp.getSpsEq();

        this.promotionId = ((BigIntegerRepresentation) listRepresentation.get(0)).get();
        this.doubleSpendingId = zn.restoreElement(listRepresentation.get(1));
        this.basketId = UUID.fromString(((StringRepresentation) listRepresentation.get(2)).get());
        this.c = zn.restoreElement(listRepresentation.get(3));
        this.couponSignature = (ECDSASignature) ecdsaSignatureScheme.restoreSignature(listRepresentation.get(4));
        this.tokenSignature = spseqSignatureScheme.restoreSignature(listRepresentation.get(5));
        this.storePublicKey = new StorePublicKey(listRepresentation.get(6));
        this.cPre0 = group.restoreElement(listRepresentation.get(7));
        this.cPre1 = group.restoreElement(listRepresentation.get(8));
        this.c0 = group.restoreElement(listRepresentation.get(9));

        Zn.ZnElement gamma = Util.hashGamma(pp.getBg().getZn(), doubleSpendingId, basketId, cPre0, cPre1, cPre1.pow(promotionParameters.getPromotionId()), context);
        SpendDeductZkpCommonInput spendDeductCommonInput = new SpendDeductZkpCommonInput(gamma, c, doubleSpendingId, cPre0, cPre1, c0);
        FiatShamirProofSystem fiatShamirProofSystem = new FiatShamirProofSystem(new SpendDeductBooleanZkp(spendDeductTree, pp, promotionParameters, providerPublicKey));
        this.proof = fiatShamirProofSystem.restoreProof(spendDeductCommonInput, listRepresentation.get(10));
    }

    public BigInteger getPromotionId() {
        return promotionId;
    }

    public Zn.ZnElement getDoubleSpendingId() {
        return doubleSpendingId;
    }

    public UUID getBasketId() {
        return basketId;
    }

    public Zn.ZnElement getC() {
        return c;
    }

    public ECDSASignature getCouponSignature() {
        return couponSignature;
    }

    public SPSEQSignature getTokenSignature() {
        return tokenSignature;
    }

    public StorePublicKey getStorePublicKey() {
        return storePublicKey;
    }

    public GroupElement getcPre0() {
        return cPre0;
    }

    public GroupElement getcPre1() {
        return cPre1;
    }

    public GroupElement getC0() {
        return c0;
    }

    public FiatShamirProof getProof() {
        return proof;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpendRequestECDSA that = (SpendRequestECDSA) o;
        return Objects.equals(promotionId, that.promotionId) && Objects.equals(doubleSpendingId, that.doubleSpendingId) && Objects.equals(basketId, that.basketId) && Objects.equals(c, that.c) && Objects.equals(couponSignature, that.couponSignature) && Objects.equals(tokenSignature, that.tokenSignature) && Objects.equals(cPre0, that.cPre0) && Objects.equals(cPre1, that.cPre1) && Objects.equals(c0, that.c0) && Objects.equals(proof, that.proof);
    }

    @Override
    public int hashCode() {
        return Objects.hash(promotionId, doubleSpendingId, basketId, c, couponSignature, tokenSignature, cPre0, cPre1, c0, proof);
    }

    @Override
    public Representation getRepresentation() {
        return new ListRepresentation(
                new BigIntegerRepresentation(promotionId),
                doubleSpendingId.getRepresentation(),
                new StringRepresentation(basketId.toString()),
                c.getRepresentation(),
                couponSignature.getRepresentation(),
                tokenSignature.getRepresentation(),
                storePublicKey.getRepresentation(),
                cPre0.getRepresentation(),
                cPre1.getRepresentation(),
                c0.getRepresentation(),
                proof.getRepresentation()
        );
    }
}
