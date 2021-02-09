package de.upb.crypto.incentive.cryptoprotocol.model;

import de.upb.crypto.incentive.cryptoprotocol.model.keys.provider.ProviderKeyPair;
import lombok.Data;

@Data
public class Provider
{
    private ProviderKeyPair providerKeyPair;
    // TODO: does it need more fields?

    /**
     * effectively a no args constructor, generating a new key pair. Used for starting a new session.
     * @param pp public parameters
     */
    public Provider(PublicParameters pp)
    {
        this.providerKeyPair = Setup.providerKeyGen(pp);
    }

    /**
     * constructor taking "dumb string", for resuming previous session (e.g. after closing the provider app)
     * @param serializedProviderKeyPair
     */
    public Provider(String serializedProviderKeyPair)
    {
        // deserialize key pair

        // initialize object variables

    }

    /**
     * all args constructor (currently debug-only
     * @param pkp provider key pair
     */
    public Provider(ProviderKeyPair pkp)
    {
        this.providerKeyPair = pkp;
    }

}
