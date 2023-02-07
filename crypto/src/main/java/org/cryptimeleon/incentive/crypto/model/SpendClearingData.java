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
import org.cryptimeleon.math.serialization.*;
import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;
import java.util.Objects;
import java.util.UUID;

public class SpendClearingData implements Representable {

    private final BigInteger promotionId;
    private final Zn.ZnElement dsid;
    private final UUID basketId;
    private final SPSEQSignature tokenSignature;
    private final ECDSASignature couponSignature;
    private final StorePublicKey storePublicKey;
    private final Zn.ZnElement c;
    private final GroupElement c0;
    private final GroupElement cPre0;
    private final GroupElement cPre1;
    private final FiatShamirProof proof;

    public SpendClearingData(BigInteger promotionId,
                             Zn.ZnElement dsid,
                             UUID basketId,
                             SPSEQSignature tokenSignature,
                             ECDSASignature couponSignature,
                             StorePublicKey storePublicKey,
                             Zn.ZnElement c,
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
        this.c0 = c0;
        this.cPre0 = cPre0;
        this.cPre1 = cPre1;
        this.proof = proof;
    }

    public SpendClearingData(Representation representation, IncentivePublicParameters pp, PromotionParameters promotionParameters, SpendDeductTree spendDeductTree, ProviderPublicKey providerPublicKey) {
        ListRepresentation listRepresentation = (ListRepresentation) representation;

        SPSEQSignatureScheme spseqSignatureScheme = pp.getSpsEq();
        ECDSASignatureScheme ecdsaSignatureScheme = new ECDSASignatureScheme();
        Group group = pp.getBg().getG1();
        Zn zn = pp.getBg().getZn();

        this.promotionId = ((BigIntegerRepresentation) listRepresentation.get(0)).get();
        this.dsid = zn.restoreElement(listRepresentation.get(1));
        this.basketId = UUID.fromString(((StringRepresentation) listRepresentation.get(2)).get());
        this.tokenSignature = spseqSignatureScheme.restoreSignature(listRepresentation.get(3));
        this.couponSignature = (ECDSASignature) ecdsaSignatureScheme.restoreSignature(listRepresentation.get(4));
        this.storePublicKey = new StorePublicKey(listRepresentation.get(5));
        this.c = zn.restoreElement(listRepresentation.get(6));
        this.c0 = group.restoreElement(listRepresentation.get(7));
        this.cPre0 = group.restoreElement(listRepresentation.get(8));
        this.cPre1 = group.restoreElement(listRepresentation.get(9));

        // Kinda nasty deserialization of zkp
        var gamma = Util.hashGamma(pp.getBg().getZn(), dsid, basketId, cPre0, cPre1, cPre1.pow(promotionParameters.getPromotionId())); // TODO include all user choices
        var spendDeductCommonInput = new SpendDeductZkpCommonInput(gamma, c, dsid, cPre0, cPre1, c0);
        var fiatShamirProofSystem = new FiatShamirProofSystem(new SpendDeductBooleanZkp(spendDeductTree, pp, promotionParameters, providerPublicKey));
        this.proof = fiatShamirProofSystem.restoreProof(spendDeductCommonInput, listRepresentation.get(10));
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
        SpendClearingData that = (SpendClearingData) o;
        return Objects.equals(promotionId, that.promotionId) && Objects.equals(dsid, that.dsid) && Objects.equals(basketId, that.basketId) && Objects.equals(tokenSignature, that.tokenSignature) && Objects.equals(couponSignature, that.couponSignature) && Objects.equals(c, that.c) && Objects.equals(c0, that.c0) && Objects.equals(cPre0, that.cPre0) && Objects.equals(cPre1, that.cPre1) && Objects.equals(proof, that.proof);
    }

    @Override
    public int hashCode() {
        return Objects.hash(promotionId, dsid, basketId, tokenSignature, couponSignature, c, c0, cPre0, cPre1, proof);
    }
}
