package org.cryptimeleon.incentivesystem.cryptoprotocol.model.messages;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.cryptimeleon.craco.sig.ps18.PS18ROMSignature;
import org.cryptimeleon.craco.sig.ps18.PS18ROMSignatureScheme;
import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.IncentivePublicParameters;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.structures.rings.zn.Zn.ZnElement;

/**
 * A class representing the third message of the Issue <-> Join protocol.
 */
@Value
@AllArgsConstructor
public class JoinResponse implements Representable
{
    @NonFinal
    @Represented(restorer = "SPS-EQ")
    private SPSEQSignature preCertificate; // preliminary certificate for the user token

    @NonFinal
    @Represented(restorer = "Zn")
    private ZnElement eskProv; // the share of the provider for the ElGamal encryption key for the initial token of the user

    public JoinResponse(Representation repr, IncentivePublicParameters pp)
    {
        new ReprUtil(this)
                .register(pp.getBg().getZn(), "Zn")
                .register(pp.getSpsEq(), "SPS-EQ")
                .deserialize(repr);
    }

    @Override
    public Representation getRepresentation() { return ReprUtil.serialize(this); }
}
