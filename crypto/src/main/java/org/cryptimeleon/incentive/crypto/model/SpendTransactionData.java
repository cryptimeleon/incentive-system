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
import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;

public class SpendTransactionData implements Representable {

    private final BigInteger promotionId;
    private final Zn.ZnElement dsid;
    private final UUID basketId;
    private final SPSEQSignature tokenSignature;
    private final ECDSASignature couponSignature;
    private final StorePublicKey storePublicKey;
    private final Zn.ZnElement c;
    private final Zn.ZnElement gamma;
    private final GroupElement c0;
    private final GroupElement cPre0;
    private final GroupElement cPre1;
    private final FiatShamirProof proof;

    public SpendTransactionData(BigInteger promotionId,
                                Zn.ZnElement dsid,
                                UUID basketId,
                                SPSEQSignature tokenSignature,
                                ECDSASignature couponSignature,
                                StorePublicKey storePublicKey,
                                Zn.ZnElement c,
                                Zn.ZnElement gamma,
                                GroupElement c0,
                                GroupElement cPre0,
                                GroupElement cPre1,
                                FiatShamirProof proof) {
        this.promotionId = promotionId;
        this.dsid = dsid;
        this.basketId = basketId;
        this.tokenSignature = tokenSignature;
        this.couponSignature = couponSignature;
        this.storePublicKey = storePublicKey;
        this.c = c;
        this.gamma = gamma;
        this.c0 = c0;
        this.cPre0 = cPre0;
        this.cPre1 = cPre1;
        this.proof = proof;
    }

    public SpendTransactionData(SpendStoreRequest spendStoreRequest,
                                BigInteger promotionId,
                                UUID basketId,
                                ECDSASignature couponSignature,
                                StorePublicKey storePublicKey,
                                Zn.ZnElement gamma) {
        this(promotionId,
                spendStoreRequest.getDsid(),
                basketId,
                spendStoreRequest.getSigma(),
                couponSignature,
                storePublicKey,
                spendStoreRequest.getC(),
                gamma,
                spendStoreRequest.getC0(),
                spendStoreRequest.getCPre0(),
                spendStoreRequest.getCPre1(),
                spendStoreRequest.getSpendZkp()
        );
    }

    public SpendTransactionData(Representation representation, IncentivePublicParameters pp, PromotionParameters promotionParameters, SpendDeductTree spendDeductTree, ProviderPublicKey providerPublicKey, UniqueByteRepresentable context) {
        Iterator<Representation> representationIterator = ((ListRepresentation) representation).iterator();

        SPSEQSignatureScheme spseqSignatureScheme = pp.getSpsEq();
        ECDSASignatureScheme ecdsaSignatureScheme = new ECDSASignatureScheme();
        Group group = pp.getBg().getG1();
        Zn zn = pp.getBg().getZn();

        this.promotionId = ((BigIntegerRepresentation) representationIterator.next()).get();
        this.dsid = zn.restoreElement(representationIterator.next());
        this.basketId = UUID.fromString(((StringRepresentation) representationIterator.next()).get());
        this.tokenSignature = spseqSignatureScheme.restoreSignature(representationIterator.next());
        this.couponSignature = (ECDSASignature) ecdsaSignatureScheme.restoreSignature(representationIterator.next());
        this.storePublicKey = new StorePublicKey(representationIterator.next());
        this.c = zn.restoreElement(representationIterator.next());
        this.gamma = zn.restoreElement(representationIterator.next());
        this.c0 = group.restoreElement(representationIterator.next());
        this.cPre0 = group.restoreElement(representationIterator.next());
        this.cPre1 = group.restoreElement(representationIterator.next());

        // Kinda nasty deserialization of zkp
        var gamma = Util.hashGamma(pp.getBg().getZn(), dsid, basketId, cPre0, cPre1, cPre1.pow(promotionParameters.getPromotionId()), context);
        var spendDeductCommonInput = new SpendDeductZkpCommonInput(gamma, c, dsid, cPre0, cPre1, c0);
        var fiatShamirProofSystem = new FiatShamirProofSystem(new SpendDeductBooleanZkp(spendDeductTree, pp, promotionParameters, providerPublicKey));
        this.proof = fiatShamirProofSystem.restoreProof(spendDeductCommonInput, representationIterator.next());
    }

    public DoubleSpendingTag computeDsTag() {
        return new DoubleSpendingTag(c, gamma);
    }

    public BigInteger getPromotionId() {
        return promotionId;
    }

    public Zn.ZnElement getDsid() {
        return dsid;
    }

    public UUID getBasketId() {
        return basketId;
    }

    public SPSEQSignature getTokenSignature() {
        return tokenSignature;
    }

    public ECDSASignature getCouponSignature() {
        return couponSignature;
    }

    public StorePublicKey getStorePublicKey() {
        return storePublicKey;
    }

    public Zn.ZnElement getC() {
        return c;
    }

    public Zn.ZnElement getGamma() {
        return gamma;
    }

    public GroupElement getC0() {
        return c0;
    }

    public GroupElement getcPre0() {
        return cPre0;
    }

    public GroupElement getcPre1() {
        return cPre1;
    }

    public FiatShamirProof getProof() {
        return proof;
    }

    @Override
    public Representation getRepresentation() {
        return new ListRepresentation(
                new BigIntegerRepresentation(promotionId),
                dsid.getRepresentation(),
                new StringRepresentation(basketId.toString()),
                tokenSignature.getRepresentation(),
                couponSignature.getRepresentation(),
                storePublicKey.getRepresentation(),
                c.getRepresentation(),
                gamma.getRepresentation(),
                c0.getRepresentation(),
                cPre0.getRepresentation(),
                cPre1.getRepresentation(),
                proof.getRepresentation()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpendTransactionData that = (SpendTransactionData) o;
        return Objects.equals(promotionId, that.promotionId) && Objects.equals(dsid, that.dsid) && Objects.equals(basketId, that.basketId) && Objects.equals(tokenSignature, that.tokenSignature) && Objects.equals(couponSignature, that.couponSignature) && Objects.equals(storePublicKey, that.storePublicKey) && Objects.equals(c, that.c) && Objects.equals(gamma, that.gamma) && Objects.equals(c0, that.c0) && Objects.equals(cPre0, that.cPre0) && Objects.equals(cPre1, that.cPre1) && Objects.equals(proof, that.proof);
    }

    @Override
    public int hashCode() {
        return Objects.hash(promotionId, dsid, basketId, tokenSignature, couponSignature, storePublicKey, c, gamma, c0, cPre0, cPre1, proof);
    }
}
