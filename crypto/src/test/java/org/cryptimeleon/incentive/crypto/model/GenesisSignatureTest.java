package org.cryptimeleon.incentive.crypto.model;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentive.crypto.TestSuite;
import org.cryptimeleon.math.serialization.Representation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GenesisSignatureTest {

    @Test
    void genesisSignatureRepresentation() {
        SPSEQSignature spseqSignature = TestSuite.incentiveSystem.signVerifiedUserPublicKey(TestSuite.providerKeyPair, TestSuite.userPublicKey);
        GenesisSignature genesisSignature = new GenesisSignature(spseqSignature);

        Representation repr = genesisSignature.getRepresentation();
        GenesisSignature recoveredGenesisSignature = new GenesisSignature(repr, TestSuite.pp);

        Assertions.assertEquals(genesisSignature, recoveredGenesisSignature);
    }
}