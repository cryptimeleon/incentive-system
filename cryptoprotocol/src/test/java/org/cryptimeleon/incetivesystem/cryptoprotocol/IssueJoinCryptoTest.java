package org.cryptimeleon.incetivesystem.cryptoprotocol;

import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProof;
import org.cryptimeleon.craco.protocols.arguments.fiatshamir.FiatShamirProofSystem;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignatureScheme;
import org.cryptimeleon.incentivesystem.cryptoprotocol.IncentiveSystem;
import org.cryptimeleon.incentivesystem.cryptoprotocol.Setup;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.IncentivePublicParameters;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.provider.ProviderPublicKey;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.messages.JoinRequest;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.messages.JoinResponse;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.proofs.CommitmentWellformednessCommonInput;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.proofs.CommitmentWellformednessProtocol;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.proofs.CommitmentWellformednessWitness;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.structures.groups.GroupElement;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.cryptimeleon.math.structures.rings.zn.Zn.ZnElement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

/**
 * contains tests of the crypto behind the issue <-> join protocol
 */
public class IssueJoinCryptoTest
{
    /**
     * generates a join request, responds to it and handles the response (i.e. a full run of the Issue-Join protocol).
     * Test case simulating a correct use of the protocol.
     */
    @Test
    void fullCorrectTestRun()
    {
        // create incentive system instance with fresh pp
        var incSys = new IncentiveSystem(IncentiveSystem.setup());

        // generate a provider key pair
        var pkp = incSys.generateProviderKeys();

        // generate a user key pair
        var ukp = incSys.generateUserKeys();

        // generate randomness for join request TODO: the 6 ZnElements shall be local variables of generateJoinRequest, the code below is just a workaround until PRF-based randomness generation has been figured out
        Zn usedZn = incSys.getPp().getBg().getZn();
        Zn.ZnElement eskUsr  = usedZn.getUniformlyRandomElement(); // user's share of the encryption secret key for the tracing information's encryption
        Zn.ZnElement dsrnd0  = usedZn.getUniformlyRandomElement(); // randomness for the first challenge generation in double-spending protection
        Zn.ZnElement dsrnd1  = usedZn.getUniformlyRandomElement(); // randomness for the second challenge generation in double-spending protection
        Zn.ZnElement z  = usedZn.getUniformlyRandomElement(); // blinding randomness needed to make (C^u, g^u) uniformly random
        Zn.ZnElement t  = usedZn.getUniformlyRandomElement(); // blinding randomness needed for special DDH trick in a proof
        Zn.ZnElement u  = usedZn.getUniformlyRandomNonzeroElement(); // != 0 needed for trick in the commitment well-formedness proof

        // create join request
        var testRequest = incSys.generateJoinRequest(
            incSys.getPp(),
            pkp.getPk(),
            ukp,
            eskUsr,
            dsrnd0,
            dsrnd1,
            z,
            t,
            u
        );

        // pass join request to issue logic, generate join response
        var testResponse = incSys.generateJoinRequestResponse(incSys.getPp(), pkp, ukp.getPk().getUpk(), testRequest);

        // pass join response to second part of join logic, generate join output
        var testOutput = incSys.handleJoinRequestResponse(incSys.getPp(), pkp.getPk(), ukp, testRequest, testResponse, eskUsr, dsrnd0, dsrnd1, z, t, u);

        // check output token for sanity (certficate valid, zero points)
        SPSEQSignatureScheme usedSpsEq = incSys.getPp().getSpsEq();
        Assertions.assertTrue(usedSpsEq.verify(pkp.getPk().getPkSpsEq(), testOutput.getSignature(), testOutput.getCommitment0(), testOutput.getCommitment1()));
        Assertions.assertTrue(testOutput.getPoints().asInteger().compareTo(BigInteger.ZERO) == 0);
    }

    /**
     * generates a dummy join request, then serializes and deserializes it,
     * testing whether the result of the deserialization is equal to the original request object.
     */
    @Test
    void joinRequestRepresentationTest()
    {
        // generate public parameters to ensure that correctly generated groups are used for testing
        IncentivePublicParameters pp = Setup.trustedSetup(Setup.PRF_KEY_LENGTH);

        // extract group from which commitments are drawn
        Group group1 = pp.getBg().getG1();

        // generate dummy commitments (i.e. random group elements)
        GroupElement c0Pre = group1.getUniformlyRandomElement();
        GroupElement c1Pre = group1.getUniformlyRandomElement();

        // create dummy values for creation of dummy cwf proof (upk, randomness) TODO: use PRFtoZn to generate exponents
        GroupElement upk = group1.getUniformlyRandomElement(); // create dummy user public key
        ZnElement usk = pp.getBg().getZn().getUniformlyRandomElement();
        ZnElement eskUsr = pp.getBg().getZn().getUniformlyRandomElement();
        ZnElement dsrnd0 = pp.getBg().getZn().getUniformlyRandomElement();
        ZnElement dsrnd1 = pp.getBg().getZn().getUniformlyRandomElement();
        ZnElement z = pp.getBg().getZn().getUniformlyRandomElement();
        ZnElement t = pp.getBg().getZn().getUniformlyRandomElement();
        ZnElement uInverse = pp.getBg().getZn().getUniformlyRandomElement();

        // create a dummy cwf proof + common input
        ProviderKeyPair pkp = Setup.providerKeyGen(pp);
        CommitmentWellformednessCommonInput cwfProofCommonInput = new CommitmentWellformednessCommonInput(upk, c0Pre, c1Pre);
        CommitmentWellformednessWitness cwfProofWitness = new CommitmentWellformednessWitness(usk, eskUsr, dsrnd0, dsrnd1, z, t, uInverse);
        FiatShamirProofSystem fsps = new FiatShamirProofSystem(new CommitmentWellformednessProtocol(pp, pkp.getPk()));
        FiatShamirProof cwfProof = fsps.createProof(cwfProofCommonInput, cwfProofWitness);

        // generate dummy join request
        JoinRequest jReq = new JoinRequest(c0Pre, c1Pre, cwfProof);

        // serialize + deserialize join request
        Representation jReqRepr = jReq.getRepresentation();
        JoinRequest deserializedJReq = new JoinRequest(jReqRepr, pp, fsps, cwfProofCommonInput);

        // check original and deserialized join request for equality (commitment parts + proofs)
        Assertions.assertTrue(jReq.getPreCommitment0().equals(deserializedJReq.getPreCommitment0()));
        Assertions.assertTrue(jReq.getPreCommitment1().equals(deserializedJReq.getPreCommitment1()));
        Assertions.assertTrue(jReq.getCwfProof().equals(deserializedJReq.getCwfProof()));
    }

    /**
     * generates a dummy join response, then serializes and deserializes it,
     * testing whether the result of the deserialization is equal to the original response object.
     */
    @Test
    void joinResponseRepresentationTest()
    {
        // generate public parameters to ensure that correctly generated remainder class rings are used for testing
        IncentivePublicParameters pp = Setup.trustedSetup(Setup.PRF_KEY_LENGTH);

        // extract used remainder class ring from public parameters
        Zn usedZn = pp.getBg().getZn();

        // extract group from which elements of the SPSEQ signature are drawn
        Group group1 = pp.getBg().getG1();

        // draw random group elements and form a valid SPS-EQ signature
        GroupElement a1 = group1.getUniformlyRandomElement();
        GroupElement a2 = group1.getUniformlyRandomElement();
        GroupElement a3 = group1.getUniformlyRandomElement();
        SPSEQSignature preCertificate = new SPSEQSignature(a1, a2, a3);

        // draw random provider share for the encryption secret key for the join response
        ZnElement eskProv = usedZn.getUniformlyRandomElement();

        // generate dummy join response
        JoinResponse jRes = new JoinResponse(preCertificate, eskProv);

        // serialize + deserialize join response
        Representation jResRepr = jRes.getRepresentation();
        JoinResponse deserializedJRes = new JoinResponse(jResRepr, pp);

        // check original and deserialized join response for equality
        Assertions.assertTrue(jRes.getPreCertificate().equals(deserializedJRes.getPreCertificate()));
        Assertions.assertTrue(jRes.getEskProv().equals(deserializedJRes.getEskProv()));
    }
}
