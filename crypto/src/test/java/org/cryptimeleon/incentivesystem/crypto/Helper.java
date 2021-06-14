package org.cryptimeleon.incentivesystem.crypto;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentivesystem.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentivesystem.crypto.model.Token;
import org.cryptimeleon.incentivesystem.crypto.model.keys.provider.ProviderKeyPair;
import org.cryptimeleon.incentivesystem.crypto.model.keys.user.UserKeyPair;

import java.math.BigInteger;

public class Helper {
    static Token generateToken(IncentivePublicParameters pp, UserKeyPair userKeyPair, ProviderKeyPair providerKeyPair) {
        return generateToken(pp, userKeyPair, providerKeyPair, BigInteger.valueOf(0));
    }

    static Token generateToken(IncentivePublicParameters pp, UserKeyPair userKeyPair, ProviderKeyPair providerKeyPair, BigInteger points) {
        var vectorH = providerKeyPair.getPk().getH();
        var zp = pp.getBg().getZn();
        // Manually create a token since issue-join is not yet implemented
        var encryptionSecretKey = zp.getUniformlyRandomNonzeroElement();
        var dsrd1 = zp.getUniformlyRandomElement();
        var dsrd2 = zp.getUniformlyRandomElement();
        var z = zp.getUniformlyRandomElement();
        var t = zp.getUniformlyRandomElement();
        var pointsZp = zp.valueOf(points);
        var c1 = vectorH.get(0).pow(userKeyPair.getSk().getUsk())
                .op(vectorH.get(1).pow(encryptionSecretKey))
                .op(vectorH.get(2).pow(dsrd1))
                .op(vectorH.get(3).pow(dsrd2))
                .op(vectorH.get(4).pow(pointsZp))
                .op(vectorH.get(5).pow(z))
                .op(pp.getH7().pow(t)).compute();
        var c2 = pp.getG1Generator();

        return new Token(
                c1,
                c2,
                encryptionSecretKey,
                dsrd1,
                dsrd2,
                z,
                t,
                pointsZp,
                (SPSEQSignature) pp.getSpsEq().sign(
                        providerKeyPair.getSk().getSkSpsEq(),
                        c1,
                        c2
                )
        );

    }

}
