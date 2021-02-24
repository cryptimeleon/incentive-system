package org.cryptimeleon.incetivesystem.cryptoprotocol;

import org.cryptimeleon.craco.common.plaintexts.GroupElementPlainText;
import org.cryptimeleon.craco.common.plaintexts.MessageBlock;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentivesystem.cryptoprotocol.IncentiveSystem;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.Token;
import org.cryptimeleon.math.serialization.converter.JSONPrettyConverter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TokenTest {

    JSONPrettyConverter jsonPrettyConverter = new JSONPrettyConverter();

    @Test
    void testTokenSerialization() {
        var pp = IncentiveSystem.setup();
        var incentiveSystem = new IncentiveSystem(pp);
        var providerKeyPair = incentiveSystem.generateProviderKeys();
        var g1 = pp.getBg().getG1();
        var zp = pp.getBg().getZn();

        var testMessages = new GroupElementPlainText[]{
                new GroupElementPlainText(pp.getBg().getG1().getUniformlyRandomElement()),
                new GroupElementPlainText(pp.getBg().getG1().getUniformlyRandomElement()),
        };
        var messageBlock = new MessageBlock(testMessages);

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

        var serializedToken = jsonPrettyConverter.serialize(token.getRepresentation());
        var deserializedToken = new Token(jsonPrettyConverter.deserialize(serializedToken), pp);

        assertThat(deserializedToken).isEqualTo(token);
    }
}