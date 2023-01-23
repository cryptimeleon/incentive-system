package org.cryptimeleon.incentive.crypto;

import org.cryptimeleon.incentive.crypto.model.IncentivePublicParameters;
import org.cryptimeleon.incentive.crypto.model.keys.user.UserPublicKey;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.RepresentationRestorer;

import java.lang.reflect.Type;

public class IncentiveSystemRestorer implements RepresentationRestorer {

    public static final String RESTORER_NAME = "IncentiveSystemRestorer";

    final IncentivePublicParameters pp;

    public IncentiveSystemRestorer(IncentivePublicParameters incentivePublicParameters) {
        this.pp = incentivePublicParameters;
    }

    private UserPublicKey restoreUserPublicKey(Representation repr) {
        return new UserPublicKey(repr, pp);
    }

    @Override
    public Object restoreFromRepresentation(Type type, Representation repr) throws IllegalArgumentException {
        if (type instanceof Class) {
            //noinspection rawtypes
            if (UserPublicKey.class.isAssignableFrom((Class) type)) {
                return this.restoreUserPublicKey(repr);
            }
        }
        throw new IllegalArgumentException("Cannot restore object of type: " + type.getTypeName());
    }
}
