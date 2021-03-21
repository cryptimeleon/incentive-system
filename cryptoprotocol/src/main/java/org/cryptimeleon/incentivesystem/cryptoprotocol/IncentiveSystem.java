package org.cryptimeleon.incentivesystem.cryptoprotocol;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.EarnRequest;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.IncentivePublicParameters;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.Token;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.user.UserKeyPair;
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

    /**
     * Generate an earn request for adding to the value of users' tokens.
     *
     * @param token             the token to update
     * @param providerPublicKey the public key of the provider
     * @param s                 randomness, TODO replace by PRF(token)
     * @return request to give to a provider
     */
    public EarnRequest generateEarnRequest(Token token, ProviderPublicKey providerPublicKey, Zn.ZnElement s) {
        return new EarnRequest(
                (SPSEQSignature) pp.getSpsEq().chgRep(
                        token.getSignature(),
                        s,
                        providerPublicKey.getPkSpsEq()
                ),
                token.getC1().pow(s).compute(),  // Compute for concurrent computation
                token.getC2().pow(s).compute()
        );
    }

    /**
     * Generate the response for users' earn requests to update the blinded token.
     *
     * @param earnRequest     the earn request to process. It can be assumed, that all group elements of earnRequest
     *                        are already computed since they are usually directly parsed from a representation
     * @param k               the increase for the users token value
     * @param providerKeyPair the provider key pair
     * @return a signature on a blinded, updated token
     */
    public SPSEQSignature generateEarnRequestResponse(EarnRequest earnRequest, long k, ProviderKeyPair providerKeyPair) {
        var isSignatureValid = pp.getSpsEq().verify(
                providerKeyPair.getPk().getPkSpsEq(),
                earnRequest.getBlindedSignature(),
                earnRequest.getC1(),
                earnRequest.getC2()
        );

        if (!isSignatureValid) {
            throw new IllegalArgumentException("Signature is not valid");
        }

        var C1 = earnRequest.getC1();
        var C2 = earnRequest.getC2();
        var q4 = providerKeyPair.getSk().getQ().get(4);

        return (SPSEQSignature) pp.getSpsEq().sign(
                providerKeyPair.getSk().getSkSpsEq(),
                C1.op(C2.pow(q4.mul(k))).compute(),
                C2
        );
    }

    /**
     * @param earnRequest       the earn request that was originally sent. Again, it can be assumed that all elements
     *                          are computed since the earnRequest usually is serialized to a representation before.
     * @param changedSignature  the signature computed by the provider
     * @param k                 the increase for the users token value
     * @param token             the old token
     * @param userKeyPair       key pair of the user
     * @param providerPublicKey public key of the provider
     * @param s                 randomness TODO replace by PRF(token)
     * @return new token with value of the old token + k
     */
    public Token handleEarnRequestResponse(EarnRequest earnRequest, SPSEQSignature changedSignature, long k, Token token, UserKeyPair userKeyPair, ProviderPublicKey providerPublicKey, Zn.ZnElement s) {

        var c1 = earnRequest.getC1().op((providerPublicKey.getH().get(4).pow(s)).pow(k)).compute();
        var c2 = earnRequest.getC2();

        var signatureValid = pp.getSpsEq().verify(providerPublicKey.getPkSpsEq(), changedSignature, c1, c2);
        if (!signatureValid) {
            throw new IllegalArgumentException("Signature is not valid");
        }

        var newSignature = pp.getSpsEq().chgRep(changedSignature, s.inv(), providerPublicKey.getPkSpsEq());

        return new Token(
                c1.pow(s.inv()).compute(),
                c2.pow(s.inv()).compute(),
                token.getEncryptionSecretKey(),
                token.getDoubleSpendRandomness0(),
                token.getDoubleSpendRandomness1(),
                token.getZ(),
                token.getT(),
                token.getPoints().add(pp.getBg().getZn().valueOf(k)),
                (SPSEQSignature) newSignature
        );
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
