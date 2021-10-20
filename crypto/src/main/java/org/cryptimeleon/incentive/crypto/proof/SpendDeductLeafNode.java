package org.cryptimeleon.incentive.crypto.proof;

import org.cryptimeleon.craco.protocols.SecretInput;
import org.cryptimeleon.craco.protocols.arguments.sigma.SigmaProtocol;
import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.PromotionParameters;
import org.cryptimeleon.incentive.crypto.model.keys.provider.ProviderPublicKey;

public abstract class SpendDeductLeafNode extends SpendDeductTree {

    public abstract SigmaProtocol getProtocol(IncentivePublicParameters pp, PromotionParameters promotionParameters, ProviderPublicKey providerPublicKey);

    public abstract boolean isTrue();

    public abstract String getLeafName();
}
