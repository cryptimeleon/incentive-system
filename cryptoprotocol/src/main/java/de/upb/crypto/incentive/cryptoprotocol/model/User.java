package de.upb.crypto.incentive.cryptoprotocol.model;

import de.upb.crypto.incentive.cryptoprotocol.model.keys.provider.ProviderPublicKey;
import de.upb.crypto.incentive.cryptoprotocol.model.keys.user.UserKeyPair;
import de.upb.crypto.math.serialization.StringRepresentation;
import de.upb.crypto.math.serialization.annotations.ReprUtil;
import de.upb.crypto.math.structures.rings.zn.Zn;
import de.upb.crypto.math.structures.rings.zn.Zn.ZnElement;
import lombok.Data;

@Data
public class User
{
    private UserKeyPair userKeyPair;
    private Token userToken;

    /**
     * (effectively a) no args constructor, generating new key pair upon instantiation. Used when new session in app is started.
     */
    public User(PublicParameters pp, ProviderPublicKey pk)
    {
        // generate key pair
        this.userKeyPair = Setup.userKeyGen(pp);

        // draw random Zn elements for creation of token (esk, dsrnd0, dsrnd1, z, t)
        Zn usedZn = pp.getBg().getZn();
        ZnElement esk = usedZn.getUniformlyRandomElement();
        ZnElement dsrnd0 = usedZn.getUniformlyRandomElement();
        ZnElement dsrnd1 = usedZn.getUniformlyRandomElement();
        ZnElement z = usedZn.getUniformlyRandomElement();
        ZnElement t = usedZn.getUniformlyRandomElement();

        // initialize user token
        ZnElement USK = this.userKeyPair.getUserSecretKey().getUsk();
        this.userToken = new Token(pp, pk, USK, esk, dsrnd0, dsrnd1, z, t);
    }

    /**
     * constructor taking "dumb strings", for resuming previous session (e.g. after closing the shopping app)
     * @param serializedUserKeyPair user key pair representation
     * @param serializedUserToken user token representation
     */
    public User(String serializedUserKeyPair, String serializedUserToken)
    {
        // deserialize key pair
        StringRepresentation userKeyPairRepr = new StringRepresentation(serializedUserKeyPair); // create a string representation object from the plain string
        this.userKeyPair = new UserKeyPair(userKeyPairRepr); // create user key pair object from representation

        // deserialize token (analogous to user key pair)
        StringRepresentation userTokenRepr = new StringRepresentation(serializedUserToken);
        this.userToken = new Token(userTokenRepr);
    }

    /**
     * all args constructor (currently debug-only)
     * @param ukp user key pair
     * @param ut user token
     */
    public User(UserKeyPair ukp, Token ut)
    {
        this.userKeyPair = ukp;
        this.userToken = ut;
    }
}
