package org.cryptimeleon.incetivesystem.cryptoprotocol;

import org.cryptimeleon.craco.common.plaintexts.GroupElementPlainText;
import org.cryptimeleon.craco.sig.SigningKey;
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
        var userKeyPair = incentiveSystem.generateUserKeys();
        var g1 = pp.getBg().getG1();
        var zp = pp.getBg().getZn();
        var token = new Token(
                g1.getUniformlyRandomElement(),
                zp.getUniformlyRandomNonzeroElement(),
                zp.getUniformlyRandomElement(),
                zp.getUniformlyRandomElement(),
                zp.getUniformlyRandomElement(),
                zp.getUniformlyRandomElement(),
                zp.getUniformlyRandomElement(),
                (SPSEQSignature) pp.getSpsEq().sign(
                        new GroupElementPlainText(
                                g1.getUniformlyRandomElement()
                        ),
                        (SigningKey) userKeyPair.getSk().getUsk()
                )
        );

        var serializedToken = jsonPrettyConverter.serialize(token.getRepresentation());
        var deserializedToken = new Token(jsonPrettyConverter.deserialize(serializedToken), pp);

        assertThat(deserializedToken).isEqualTo(token);
    }
}