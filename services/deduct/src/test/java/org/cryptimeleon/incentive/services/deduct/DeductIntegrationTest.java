package org.cryptimeleon.incentive.services.deduct;

import org.cryptimeleon.incentive.crypto.IncentiveSystem;
import org.cryptimeleon.incentive.crypto.Setup;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.rings.zn.Zn;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigInteger;

import static org.mockito.Mockito.when;

/**
 * Integration test for the Deduct web service.
 * Basket and info service are mocked (means that their expected responses to requests are hard-coded).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DeductIntegrationTest {
    @MockBean
    private CryptoRepository cryptoRepository;

    @MockBean
    private BasketRepository basketRepository;

    /**
     * Lets the Deduct service handle a valid spend request.
     */
    @Test
    public void validSpendOperationTest() {
        // setup the incentive system for the test
        IncentivePublicParameters pp = Setup.trustedSetup(128, Setup.BilinearGroupChoice.Debug); // generate public parameters
        IncentiveSystem incentiveSystem = new IncentiveSystem(pp);
        ProviderKeyPair pkp = Setup.providerKeyGen(pp);

        // create a JSON converter for (de-)serialization
        JSONConverter jsonConverter = new JSONConverter();

        // setup mocked crypto repository
        when(cryptoRepository.getIncentiveSystem()).thenReturn(incentiveSystem); // the system instance
        when(cryptoRepository.getPp()).thenReturn(pp); // public parameters
        when(cryptoRepository.getPk()).thenReturn(pkp.getPk()); // provider public key
        when(cryptoRepository.getSk()).thenReturn(pkp.getSk()); // provider secret key

        // setup mocked basket repository
        // TODO: can only be done when BasketRepository is implemented (which can only be done after addressing endpoint issue for basket server)

        // generate fresh user key pair
        UserKeyPair ukp = Setup.userKeyGen(pp);

        // generate token by simulating Issue-Join
        var joinRequest = incentiveSystem.generateJoinRequest(pkp.getPk(), ukp);
        var joinResponse = incentiveSystem.generateJoinRequestResponse(pkp, ukp.getPk().getUpk(), joinRequest);
        var token = incentiveSystem.handleJoinRequestResponse(pkp.getPk(), ukp, joinRequest, joinResponse);

        // add points to token by simulating Credit-Earn
        BigInteger earnAmount = new BigInteger("231");
        var earnRequest = incentiveSystem.generateEarnRequest(token, pkp.getPk(), ukp);
        var signatureResponse = incentiveSystem.generateEarnRequestResponse(earnRequest, earnAmount, pkp);
        token = incentiveSystem.handleEarnRequestResponse(earnRequest, signatureResponse, earnAmount, token, pkp.getPk(), ukp);

        // generate and serialize spend request
        Zn.ZnElement transactionID = pp.getBg().getZn().getOneElement(); // TODO: hard-coded until tid endpoint for basket service works
        BigInteger spendAmount = new BigInteger("42");
        var spendRequest = incentiveSystem.generateSpendRequest(token, pkp.getPk(), spendAmount, ukp, transactionID);
        // TODO: continue here, serialize spend request

        // spend points from token by executing Deduct
    }
}
