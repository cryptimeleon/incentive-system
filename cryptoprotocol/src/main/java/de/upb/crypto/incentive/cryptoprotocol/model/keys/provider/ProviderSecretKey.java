package de.upb.crypto.incentive.cryptoprotocol.model.keys.provider;

import de.upb.crypto.craco.prf.PrfKey;
import de.upb.crypto.craco.sig.sps.eq.SPSEQSigningKey;
import de.upb.crypto.incentive.cryptoprotocol.exceptions.PedersenException;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.structures.zn.Zn.ZnElement;

public class ProviderSecretKey
{
    SPSEQSigningKey skSpsEq;
    ZnElement[] q; // dlogs of the first six bases used in the Pedersen commitment in the token
    PrfKey betaProv;

    public ProviderSecretKey(SPSEQSigningKey skSpsEq, ZnElement[] q, PrfKey betaProv) throws PedersenException
    {
        // assert that correct number of Zn exponents is passed ()
        if(q.length != 6)
        {
            throw new PedersenException("q is required to consist of 6 group elements, found: " + String.valueOf(q.length));
        }

        this.skSpsEq = skSpsEq;

        // deep copy just to be sure
        for(int i=0; i< q.length; i++)
        {
            this.q[i] = q[i];
        }

        this.betaProv = betaProv;
    }

    public SPSEQSigningKey getSkSpsEq() { return skSpsEq; }

    /**
     * @return deep copy of the exponents array
     */
    public ZnElement[] getQ()
    {
        ZnElement[] qCopy = new ZnElement[q.length];
        for (int i = 0; i < q.length; i++)
        {
            qCopy[i] = this.q[i];
        }
        return qCopy;
    }
}
