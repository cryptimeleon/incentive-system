package org.cryptimeleon.incentivesystem.cryptoprotocol;

import org.cryptimeleon.craco.common.ByteArrayImplementation;
import org.cryptimeleon.craco.common.plaintexts.GroupElementPlainText;
import org.cryptimeleon.craco.common.plaintexts.MessageBlock;
import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.*;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.user.UserKeyPair;
import org.cryptimeleon.incentivesystem.cryptoprotocol.proof.SpendDeductZkp;
import org.cryptimeleon.incentivesystem.cryptoprotocol.proof.SpendDeductZkpCommonInput;
import org.cryptimeleon.incentivesystem.cryptoprotocol.proof.SpendDeductZkpWitnessInput;
import org.cryptimeleon.math.hash.impl.ByteArrayAccumulator;
import org.cryptimeleon.math.structures.rings.cartesian.RingElementVector;
import org.cryptimeleon.math.structures.rings.integers.IntegerRing;
import org.cryptimeleon.math.structures.rings.zn.Zn;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.stream.Collectors;


/*
 * Contains all main algorithms of the incentive system.
 */
public class IncentiveSystem {

    // Public parameters
    public final IncentivePublicParameters pp;

    public IncentiveSystem(IncentivePublicParameters pp) {
        this.pp = pp;
    }

    /**
     * Generate public parameters for the incentive system.
     *
     * @param securityParameter   the security parameter used in the setup algorithm
     * @param bilinearGroupChoice the bilinear group to use. Especially useful for testing
     * @return public parameters for the incentive system
     */
    public static IncentivePublicParameters setup(int securityParameter, Setup.BilinearGroupChoice bilinearGroupChoice) {
        return Setup.trustedSetup(securityParameter, bilinearGroupChoice);
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
     * @return request to give to a provider
     */
    public EarnRequest generateEarnRequest(Token token, ProviderPublicKey providerPublicKey, UserKeyPair userKeyPair) {
        // Pseudorandom s
        var s = pp.getPrfToZn().hashThenPrfToZn(userKeyPair.getSk().getPrfKey(), token, "CreditEarn-s");

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
     * @param providerPublicKey public key of the provider
     * @param userKeyPair       keypair of the user
     * @return new token with value of the old token + k
     */
    public Token handleEarnRequestResponse(EarnRequest earnRequest, SPSEQSignature changedSignature, long k, Token token, ProviderPublicKey providerPublicKey, UserKeyPair userKeyPair) {

        // Pseudorandom s
        var s = pp.getPrfToZn().hashThenPrfToZn(userKeyPair.getSk().getPrfKey(), token, "CreditEarn-s");

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

    /**
     * Generates a request to add value k to token.
     *
     * @param token             the token
     * @param providerPublicKey public key of the provider
     * @param k                 amount to add to the token's value
     * @param userKeyPair       keypair of the user that owns the token
     * @param tid               transaction ID, provided by the provider
     * @return serializable spendRequest that can be sent to the provider
     */
    public SpendRequest generateSpendRequest(Token token,
                                             ProviderPublicKey providerPublicKey,
                                             BigInteger k,
                                             UserKeyPair userKeyPair,
                                             Zn.ZnElement tid
    ) {
        var zp = pp.getBg().getZn();
        var usk = userKeyPair.getSk().getUsk();
        var esk = token.getEncryptionSecretKey();
        var dsid = pp.getW().pow(esk);
        var vectorH = providerPublicKey.getH().append(pp.getH7());
        var vectorR = zp.getUniformlyRandomElements(pp.getNumEskDigits());


        // Compute pseudorandom values
        var prfZnElements = pp.getPrfToZn().hashThenPrfToZnVector(userKeyPair.getSk().getPrfKey(), token, 6, "SpendDeduct");
        Zn.ZnElement eskUsrS = (Zn.ZnElement) prfZnElements.get(0);
        Zn.ZnElement dsrnd0S = (Zn.ZnElement) prfZnElements.get(1);
        Zn.ZnElement dsrnd1S = (Zn.ZnElement) prfZnElements.get(2);
        Zn.ZnElement zS = (Zn.ZnElement) prfZnElements.get(3);
        Zn.ZnElement tS = (Zn.ZnElement) prfZnElements.get(4);
        Zn.ZnElement uS = (Zn.ZnElement) prfZnElements.get(5);

        var exponents = new RingElementVector(
                usk,
                eskUsrS,
                dsrnd0S,
                dsrnd1S,
                token.getPoints().sub(zp.valueOf(k)),
                zS,
                tS);
        var cPre0 = vectorH.innerProduct(exponents).pow(uS).compute();
        var cPre1 = pp.getG1().pow(uS).compute();

        var gamma = Util.hashGamma(zp, k, dsid, tid, cPre0, cPre1);

        var c0 = usk.mul(gamma).add(token.getDoubleSpendRandomness0());
        var c1 = esk.mul(gamma).add(token.getDoubleSpendRandomness1());

        var eskDecomp = new RingElementVector(Arrays.stream(
                IntegerRing.decomposeIntoDigits(eskUsrS.asInteger(), pp.getEskDecBase().asInteger(), pp.getNumEskDigits()))
                .map(zp::valueOf)
                .collect(Collectors.toList()));
        var ctrace0 = pp.getW().pow(vectorR).compute();
        var ctrace1 = ctrace0.pow(esk).op(pp.getW().pow(eskDecomp)).compute();

        // Send c0, c1, sigma, C, Cpre, ctrace
        // + ZKP
        var fiatShamirProofSystem = new FiatShamirProofSystem(new SpendDeductZkp(pp, providerPublicKey));

        var witness = new SpendDeductZkpWitnessInput(usk, token.getPoints(), token.getZ(), zS, token.getT(), tS, uS, esk, eskUsrS, token.getDoubleSpendRandomness0(), dsrnd0S, token.getDoubleSpendRandomness1(), dsrnd1S, eskDecomp, vectorR);
        var commonInput = new SpendDeductZkpCommonInput(k, gamma, c0, c1, dsid, cPre0, cPre1, token.getC1(), ctrace0, ctrace1);
        var proof = fiatShamirProofSystem.createProof(commonInput, witness);

        return new SpendRequest(dsid, proof, c0, c1, cPre0, cPre1, ctrace0, ctrace1, token.getC1(), token.getSignature());
    }

    /**
     * React to a legitimate spend request to allow the user retrieving an updated token with the value increased by k.
     * Returns additional data for double-spending protection.
     *
     * @param spendRequest    the user's request
     * @param providerKeyPair keypair of the provider
     * @param k               the amount to add to the user's token
     * @param tid             transaction id, should be verified by the provider
     * @return tuple of response to send to the user and information required for double-spending protection
     */
    public SpendProverOutput generateSpendRequestResponse(SpendRequest spendRequest, ProviderKeyPair providerKeyPair, BigInteger k, Zn.ZnElement tid) {
        // SPSEQ.verify
        var signatureValid = pp.getSpsEq().verify(
                providerKeyPair.getPk().getPkSpsEq(),
                spendRequest.getSigma(),
                spendRequest.getCommitmentC0(),
                pp.getG1() // C1 must be g1 according to ZKP in T2 paper. We omit the ZKP and use g1 instead of C1
        );

        if (!signatureValid) {
            throw new IllegalArgumentException("Signature of the request is not valid!");
        }

        // Validate ZKP
        var fiatShamirProofSystem = new FiatShamirProofSystem(new SpendDeductZkp(pp, providerKeyPair.getPk()));
        var gamma = Util.hashGamma(pp.getBg().getZn(), k, spendRequest.getDsid(), tid, spendRequest.getCPre0(), spendRequest.getCPre1());
        var commonInput = new SpendDeductZkpCommonInput(spendRequest, k, gamma);
        var proofValid = fiatShamirProofSystem.checkProof(commonInput, spendRequest.getSpendDeductZkp());

        if (!proofValid) {
            throw new IllegalArgumentException("ZPK of the request is not valid!");
        }

        // Retrieve esk via PRF
        var preimage = new ByteArrayAccumulator();
        preimage.escapeAndSeparate(commonInput.c0Pre);
        preimage.escapeAndSeparate(commonInput.c1Pre);
        var eskStarProv = pp.getPrfToZn().hashThenPrfToZn(providerKeyPair.getSk().getBetaProv(), new ByteArrayImplementation(preimage.extractBytes()), "eskStarProv");

        // Compute new Signature
        var cPre0 = spendRequest.getCPre0();
        var cPre1 = spendRequest.getCPre1();
        var sigmaPrime = (SPSEQSignature) pp.getSpsEq().sign(
                providerKeyPair.getSk().getSkSpsEq(),
                cPre0.op(cPre1.pow(eskStarProv.mul(providerKeyPair.getSk().getQ().get(1)))),
                cPre1
        );

        return new SpendProverOutput(
                new SpendResponse(sigmaPrime, eskStarProv),
                new DoubleSpendingTag(commonInput.c0, commonInput.c1, gamma, eskStarProv, commonInput.ctrace0, commonInput.ctrace1)
        );


    }

    /**
     * Process the response to a spendRequest and compute the updated token.
     *
     * @param spendResponse     the provider's response
     * @param spendRequest      the original request
     * @param token             the old token
     * @param providerPublicKey public key of the provider
     * @param k                 amount to add to the token
     * @param userKeyPair       keypair of the user
     * @return token with the value of the old token + k
     */
    public Token handleSpendRequestResponse(SpendResponse spendResponse,
                                            SpendRequest spendRequest,
                                            Token token,
                                            BigInteger k,
                                            ProviderPublicKey providerPublicKey,
                                            UserKeyPair userKeyPair) {
        // Re-compute pseudorandom values
        var prfZnElements = pp.getPrfToZn().hashThenPrfToZnVector(userKeyPair.getSk().getPrfKey(), token, 6, "SpendDeduct");
        Zn.ZnElement eskUsrS = (Zn.ZnElement) prfZnElements.get(0);
        Zn.ZnElement dsrnd0S = (Zn.ZnElement) prfZnElements.get(1);
        Zn.ZnElement dsrnd1S = (Zn.ZnElement) prfZnElements.get(2);
        Zn.ZnElement zS = (Zn.ZnElement) prfZnElements.get(3);
        Zn.ZnElement tS = (Zn.ZnElement) prfZnElements.get(4);
        Zn.ZnElement uS = (Zn.ZnElement) prfZnElements.get(5);

        // SPSEQ.verify and chgRep
        var cStar0 = spendRequest.getCPre0().op(providerPublicKey.getH().get(1).pow(spendResponse.getEskProvStar().mul(uS)));
        var cStar1 = spendRequest.getCPre1();
        var sigmaStar = (SPSEQSignature) pp.getSpsEq().chgRepWithVerify(
                new MessageBlock(new GroupElementPlainText(cStar0), new GroupElementPlainText(cStar1)),
                spendResponse.getSigma(),
                uS.inv(),
                providerPublicKey.getPkSpsEq());

        // Change representation of commitments
        cStar0 = cStar0.pow(uS.inv());
        cStar1 = pp.getG1(); // is the same as cStar1.pow(uS.inv())

        // Compute new esk
        var eskStar = eskUsrS.add(spendResponse.getEskProvStar());

        // Assemble new token
        return new Token(cStar0, cStar1, eskStar, dsrnd0S, dsrnd1S, zS, tS, token.getPoints().sub(pp.getBg().getZn().valueOf(k)), sigmaStar);
    }

    void link() {
    }

    void verifyDS() {
    }

    void trace() {
    }
}
