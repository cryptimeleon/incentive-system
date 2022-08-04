package org.cryptimeleon.incentive.crypto;

import org.cryptimeleon.incentive.crypto.cryptimeleon.incentive.crypto.TestSuite;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.crypto.model.Token;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;

class TokenDsidHashMakerTest {
    JSONConverter jsonConverter = new JSONConverter();
    PromotionParameters promotionParameters = IncentiveSystem.generatePromotionParameters(1);
    Token token = TestSuite.generateToken(promotionParameters);

    @Test
    void smokeTest() {
        String hash = TokenDsidHashMaker.hashToken(token, TestSuite.pp);
        String shortHash = TokenDsidHashMaker.shortHash(hash);
        System.out.println(hash);
        System.out.println(shortHash);
    }

    @Test
    void compareWithManualHash() throws NoSuchAlgorithmException {
        String hash = TokenDsidHashMaker.hashToken(token, TestSuite.pp);
        String serializedDsid = jsonConverter.serialize(token.computeDsid(TestSuite.pp).getRepresentation());
        String manualHashString = hashAndEncodeHexString(serializedDsid);

        assertThat(hash).isEqualToIgnoringCase(manualHashString);
    }

    // Manual implementation as it could be done in JS; agnostic of cryptimeleon
    // https://www.baeldung.com/java-byte-arrays-hex-strings
    private String hashAndEncodeHexString(String msg) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] byteArray = digest.digest(msg.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexStringBuffer = new StringBuilder();
        for (byte b : byteArray) {
            hexStringBuffer.append(byteToHex(b));
        }
        return hexStringBuffer.toString();
    }

    private String byteToHex(byte num) {
        char[] hexDigits = new char[2];
        hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
        hexDigits[1] = Character.forDigit((num & 0xF), 16);
        return new String(hexDigits);
    }
}
