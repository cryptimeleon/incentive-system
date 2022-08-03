package org.cryptimeleon.incentive.crypto.model;

import org.cryptimeleon.incentive.crypto.TestSuite;
import org.cryptimeleon.math.serialization.Representation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GenesisSignatureTest {

    @Test
    void genesisSignatureRepresentation() {
        GenesisSignature genesisSignature = TestSuite.incentiveSystem.signVerifiedUserPublicKey(TestSuite.providerKeyPair, TestSuite.userPublicKey);

        Representation repr = genesisSignature.getRepresentation();
        GenesisSignature recoveredGenesisSignature = new GenesisSignature(repr, TestSuite.pp);

        Assertions.assertEquals(genesisSignature, recoveredGenesisSignature);
    }
}