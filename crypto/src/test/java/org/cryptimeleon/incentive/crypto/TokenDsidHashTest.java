package org.cryptimeleon.incentive.crypto;

import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.crypto.model.Token;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserKeyPair;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;

class TokenDsidHashTest {
    JSONConverter jsonConverter = new JSONConverter();
    IncentivePublicParameters pp = Setup.trustedSetup(128, Setup.BilinearGroupChoice.Debug);
    PromotionParameters promotionParameters = IncentiveSystem.generatePromotionParameters(1);
    IncentiveSystem incentiveSystem = new IncentiveSystem(pp);
    UserKeyPair userKeyPair = incentiveSystem.generateUserKeys();
    ProviderKeyPair providerKeyPair = incentiveSystem.generateProviderKeys();
    Token token = Helper.generateToken(pp, userKeyPair, providerKeyPair, promotionParameters);

    @Test
    void smokeTest() {
        String hash = TokenDsidHash.computeTokenHash(token, pp);
        String shortHash = TokenDsidHash.shortHash(hash);
        System.out.println(hash);
        System.out.println(shortHash);
    }

    @Test
    void compareWithManualHash() throws NoSuchAlgorithmException {
        String hash = TokenDsidHash.computeTokenHash(token, pp);
        String serializedDsid = jsonConverter.serialize(token.computeDsid(pp).getRepresentation());
        String manualHashString = hashAndEncodeHexString(serializedDsid);

        assertThat(hash).isEqualToIgnoringCase(manualHahsString);
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
