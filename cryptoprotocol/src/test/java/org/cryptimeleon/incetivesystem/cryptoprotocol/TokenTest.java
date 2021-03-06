package org.cryptimeleon.incetivesystem.cryptoprotocol;

import org.cryptimeleon.craco.common.plaintexts.GroupElementPlainText;
import org.cryptimeleon.craco.common.plaintexts.MessageBlock;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentivesystem.cryptoprotocol.IncentiveSystem;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.Token;
import org.junit.jupiter.api.Test;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;


/*
 * Contains tests of the Token class.
 */
public class TokenTest {

    Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    @Test
    void testTokenSerialization() {
        var pp = IncentiveSystem.setup();
        var incentiveSystem = new IncentiveSystem(pp);
        var providerKeyPair = incentiveSystem.generateProviderKeys();

        // Create a dummy token.
        // This should be replaced by the actual methods that handle tokens when they are implemented.
        var g1 = pp.getBg().getG1();
        var zp = pp.getBg().getZn();
        var testMessages = new GroupElementPlainText[]{
                new GroupElementPlainText(pp.getBg().getG1().getUniformlyRandomElement()),
                new GroupElementPlainText(pp.getBg().getG1().getUniformlyRandomElement()),
        };
        var messageBlock = new MessageBlock(testMessages);

        logger.info("Testing represention of tokens");
        var token = new Token(
                g1.getUniformlyRandomElement(),
                zp.getUniformlyRandomNonzeroElement(),
                zp.getUniformlyRandomElement(),
                zp.getUniformlyRandomElement(),
                zp.getUniformlyRandomElement(),
                zp.getUniformlyRandomElement(),
                zp.getUniformlyRandomElement(),
                (SPSEQSignature) pp.getSpsEq().sign(
                        messageBlock,
                        providerKeyPair.getSk().getSkSpsEq()
                )
        );

        var deserializedToken = new Token(token.getRepresentation(), pp);
        assertEquals(deserializedToken, token);
    }
}