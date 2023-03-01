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
import org.cryptimeleon.math.serialization.ListRepresentation;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;

public class SpendProviderRequest implements Representable {
    private final Zn.ZnElement doubleSpendingId;
    private final Zn.ZnElement c;
    private final ECDSASignature couponSignature;
    private final SPSEQSignature tokenSignature;
    private final StorePublicKey storePublicKey;

    private final GroupElement cPre0;
    private final GroupElement cPre1;
    private final GroupElement c0;
    private final FiatShamirProof proof;

    public SpendProviderRequest(Zn.ZnElement doubleSpendingId,
                                Zn.ZnElement c,
                                ECDSASignature couponSignature,
                                SPSEQSignature tokenSignature,
                                StorePublicKey storePublicKey,
                                GroupElement cPre0,
                                GroupElement cPre1,
                                GroupElement c0,
                                FiatShamirProof proof) {
        this.doubleSpendingId = doubleSpendingId;
        this.c = c;
        this.couponSignature = couponSignature;
        this.tokenSignature = tokenSignature;
        this.storePublicKey = storePublicKey;
        this.cPre0 = cPre0;
        this.cPre1 = cPre1;
        this.c0 = c0;
        this.proof = proof;
    }

    public SpendProviderRequest(SpendStoreRequest spendStoreRequest, SpendStoreResponse spendCouponSignature) {
        this(
                spendStoreRequest.getDsid(),
                spendStoreRequest.getC(),
                spendCouponSignature.getSignature(),
                spendStoreRequest.getSigma(),
                spendCouponSignature.getStorePublicKey(),
                spendStoreRequest.getCPre0(),
                spendStoreRequest.getCPre1(),
                spendStoreRequest.getC0(),
                spendStoreRequest.getSpendZkp());
    }

    public SpendProviderRequest(Representation representation,
                                IncentivePublicParameters pp,
                                PromotionParameters promotionParameters,
                                UUID basketId,
                                SpendDeductTree spendDeductTree,
                                ProviderPublicKey providerPublicKey,
                                UniqueByteRepresentable context) {
        Iterator<Representation> representationIterator = ((ListRepresentation) representation).iterator();
        Zn zn = pp.getBg().getZn();
        Group group = pp.getBg().getG1();
        ECDSASignatureScheme ecdsaSignatureScheme = new ECDSASignatureScheme();
        SPSEQSignatureScheme spseqSignatureScheme = pp.getSpsEq();

        this.doubleSpendingId = zn.restoreElement(representationIterator.next());
        this.c = zn.restoreElement(representationIterator.next());
        this.couponSignature = (ECDSASignature) ecdsaSignatureScheme.restoreSignature(representationIterator.next());
        this.tokenSignature = spseqSignatureScheme.restoreSignature(representationIterator.next());
        this.storePublicKey = new StorePublicKey(representationIterator.next());
        this.cPre0 = group.restoreElement(representationIterator.next());
        this.cPre1 = group.restoreElement(representationIterator.next());
        this.c0 = group.restoreElement(representationIterator.next());

        Zn.ZnElement gamma = Util.hashGamma(pp.getBg().getZn(), doubleSpendingId, basketId, cPre0, cPre1, cPre1.pow(promotionParameters.getPromotionId()), context);
        SpendDeductZkpCommonInput spendDeductCommonInput = new SpendDeductZkpCommonInput(gamma, c, doubleSpendingId, cPre0, cPre1, c0);
        FiatShamirProofSystem fiatShamirProofSystem = new FiatShamirProofSystem(new SpendDeductBooleanZkp(spendDeductTree, pp, promotionParameters, providerPublicKey));
        this.proof = fiatShamirProofSystem.restoreProof(spendDeductCommonInput, representationIterator.next());
    }

    public Zn.ZnElement getDoubleSpendingId() {
        return doubleSpendingId;
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
        SpendProviderRequest that = (SpendProviderRequest) o;
        return Objects.equals(doubleSpendingId, that.doubleSpendingId) && Objects.equals(c, that.c) && Objects.equals(couponSignature, that.couponSignature) && Objects.equals(tokenSignature, that.tokenSignature) && Objects.equals(cPre0, that.cPre0) && Objects.equals(cPre1, that.cPre1) && Objects.equals(c0, that.c0) && Objects.equals(proof, that.proof);
    }

    @Override
    public int hashCode() {
        return Objects.hash(doubleSpendingId, c, couponSignature, tokenSignature, cPre0, cPre1, c0, proof);
    }

    @Override
    public Representation getRepresentation() {
        return new ListRepresentation(
                doubleSpendingId.getRepresentation(),
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
