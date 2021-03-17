package org.cryptimeleon.incentivesystem.cryptoprotocol;

import org.cryptimeleon.craco.common.plaintexts.GroupElementPlainText;
import org.cryptimeleon.craco.common.plaintexts.MessageBlock;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.EarnRequest;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.IncentivePublicParameters;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.Token;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.user.UserKeyPair;
import org.cryptimeleon.math.structures.groups.cartesian.GroupElementVector;
import org.cryptimeleon.math.structures.rings.zn.Zn;


/*
 * Contains all main algorithms of the incentive system.
 */
public class IncentiveSystem {

    // Public parameters
    IncentivePublicParameters pp;

    public IncentiveSystem(IncentivePublicParameters pp) {
        this.pp = pp;
    }

    public static IncentivePublicParameters setup() {
        return Setup.trustedSetup(Setup.PRF_KEY_LENGTH);
    }

    public ProviderKeyPair generateProviderKeys() {
        return Setup.providerKeyGen(this.pp);
    }

    public UserKeyPair generateUserKeys() {
        return Setup.userKeyGen(this.pp);
    }


    // This is highly WIP
    void generateJoinRequest() {
    }

    void generateJoinRequestResponse() {
    }

    void handleJoinRequestResponse() {
    }

    public EarnRequest generateEarnRequest(Token token, UserKeyPair userKeyPair, ProviderPublicKey providerPublicKey, Zn.ZnElement s) {
        return new EarnRequest(
                (SPSEQSignature) pp.getSpsEq().chgRep(token.getSignature(), s, providerPublicKey.getPkSpsEq()),
                token.getC().pow(s)
        );
    }

    public SPSEQSignature generateEarnRequestResponse(EarnRequest earnRequest, long earnAmount, ProviderKeyPair providerKeyPair) {
        var signatureValid = pp.getSpsEq().verify(vectorToMessageBlock(earnRequest.getBlindedC()), earnRequest.getBlindedSignature(), providerKeyPair.getPk().getPkSpsEq());

        if (!signatureValid) {
            // TODO: better error handling
            throw new RuntimeException("Signature is not valid");
        }

        var newC = new GroupElementVector(
                earnRequest.getBlindedC().get(0).op(earnRequest.getBlindedC().get(1).pow(providerKeyPair.getSk().getQ().get(4).mul(earnAmount))),
                earnRequest.getBlindedC().get(1)
        );

        var changedSignature = pp.getSpsEq().sign(
                vectorToMessageBlock(newC),
                providerKeyPair.getSk().getSkSpsEq()
        );

        return (SPSEQSignature) changedSignature;
    }

    public Token handleEarnRequestResponse(EarnRequest earnRequest, SPSEQSignature changedSignature, long k, Token token, UserKeyPair userKeyPair, ProviderPublicKey providerPublicKey, Zn.ZnElement s) {

        var signedC = new GroupElementVector(
                earnRequest.getBlindedC().get(0).op((providerPublicKey.getH().get(4).pow(s)).pow(k)),
                earnRequest.getBlindedC().get(1)
        );

        var signatureValid = pp.getSpsEq().verify(vectorToMessageBlock(signedC), changedSignature, providerPublicKey.getPkSpsEq());
        if (!signatureValid) {
            // TODO: better error handling
            throw new RuntimeException("Signature is not valid");
        }

        var newSignature = pp.getSpsEq().chgRep(changedSignature, s.inv(), providerPublicKey.getPkSpsEq());
        var finalC = signedC.pow(s.inv());

        return new Token(
                finalC,
                token.getEncryptionSecretKey(),
                token.getDoubleSpendRandomness0(),
                token.getDoubleSpendRandomness1(),
                token.getZ(),
                token.getT(),
                token.getPoints().add(pp.getBg().getZn().valueOf(k)),
                (SPSEQSignature) newSignature
        );
    }

    public static MessageBlock vectorToMessageBlock(GroupElementVector groupElementVector) {
        return new MessageBlock(groupElementVector.map(groupElement -> new GroupElementPlainText(groupElement)));
    }

    void generateSpendRequest() {
    }

    void generateSpendRequestResponse() {
    }

    void handleSpendRequestResponse() {
    }

    void link() {
    }

    void verifyDS() {
    }

    void trace() {
    }
}
