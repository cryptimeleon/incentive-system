package de.upb.crypto.incentive.cryptoprotocol.model;


import de.upb.crypto.craco.common.WatersHash;
import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.factory.BilinearGroupFactory;
import de.upb.crypto.math.hash.impl.SHA512HashFunction;
import de.upb.crypto.math.interfaces.hash.HashIntoStructure;
import de.upb.crypto.math.interfaces.structures.Element;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import de.upb.crypto.math.pairings.bn.BarretoNaehrigBilinearGroupImpl;
import de.upb.crypto.math.structures.groups.basic.BasicBilinearGroup;

/**
 * a class implementing the incentive system algorithms that are not part of any protocol
 * @author Patrick Schürmann
 */
public class SingularAlgorithms
{
    /**
     * generates public parameters from security parameters
     */
    public static PublicParameters Setup(int securityParameter)
    {
        // generate a bilinear group from the security parameter
        BilinearGroupFactory bgFactory = new BilinearGroupFactory(securityParameter); // instantiate bilinear group generator
        bgFactory.setRequirements(BilinearGroup.Type.TYPE_3); // type 3 (Barreto-Naehrig) required for scheme from 2020 incsys paper
        BilinearGroup bg = bgFactory.createBilinearGroup();

        HashIntoStructure hf = bg.getHashIntoG1(); // hash function {0,1}^* -> G_1

        // compute w (base used in double spending protection, see 2020 incsys paper)
        GroupElement w = (GroupElement) hf.hashIntoStructure("w" + bg.toString());

        // compute seventh base for user token (h7 in 2020 incsys paper)
        GroupElement h7 = (GroupElement) hf.hashIntoStructure("h7"+bg.toString()); // TODO: what string to hash?

        // wrap up all values
        return new PublicParameters(bg, w, h7);
    }
}
