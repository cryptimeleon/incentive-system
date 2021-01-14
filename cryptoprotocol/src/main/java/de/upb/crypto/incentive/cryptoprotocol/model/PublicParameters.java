package de.upb.crypto.incentive.cryptoprotocol.model;

import de.upb.crypto.math.factory.BilinearGroup;
import de.upb.crypto.math.interfaces.structures.GroupElement;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * a class representing the public parameters of the 2020 incentive system
 * @author Patrick Schürmann
 */
@Data
@AllArgsConstructor
public class PublicParameters
{
    public BilinearGroup bg;
    public GroupElement w;
    public GroupElement h7;
}
