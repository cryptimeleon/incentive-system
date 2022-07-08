package org.cryptimeleon.incentive.crypto;

import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.Token;
import org.cryptimeleon.math.hash.impl.SHA256HashFunction;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.groups.GroupElement;

/**
 * A token hash code similar to git commit hashes.
 * Basically hex string of hash of dsid json string (to be cryptimeleon-agnostic and work e.g. in a JS frontend).
 */
public class TokenDsidHashMaker {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    private static final int SHORT_HASH_LENGTH = 6;

    public static String hashToken(Token token, IncentivePublicParameters pp) {
        return hashDsid(token.computeDsid(pp));
    }

    public static String hashDsid(GroupElement dsid) {
        JSONConverter jsonConverter = new JSONConverter();
        SHA256HashFunction sha256 = new SHA256HashFunction();

        String dsidRepr = jsonConverter.serialize(dsid.getRepresentation());
        byte[] digest = sha256.hash(dsidRepr);
        return bytesToHex(digest);
    }

    public static String shortHash(String hash) {
        return hash.substring(0, SHORT_HASH_LENGTH);
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }
}
