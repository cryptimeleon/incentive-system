package org.cryptimeleon.incentive.services.dsprotection;

import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.converter.JSONConverter;

/**
 * Class that contains utility functions used in the dsprotection service.
 */
public class Util {
    /**
     * Computes and returns a JSON-serialization of a representation of the passed representable object.
     */
    public static String computeSerializedRepresentation(Representable r) {
        JSONConverter jsonConverter = new JSONConverter();
        Representation repr = r.getRepresentation();
        return jsonConverter.serialize(repr);
    }
}
