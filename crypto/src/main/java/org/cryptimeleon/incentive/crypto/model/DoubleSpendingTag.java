package org.cryptimeleon.incentive.crypto.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.cryptimeleon.math.serialization.Representable;
import org.cryptimeleon.math.serialization.Representation;
import org.cryptimeleon.math.serialization.annotations.ReprUtil;
import org.cryptimeleon.math.serialization.annotations.Represented;
import org.cryptimeleon.math.serialization.converter.JSONConverter;
import org.cryptimeleon.math.structures.groups.Group;
import org.cryptimeleon.math.structures.groups.cartesian.GroupElementVector;
import org.cryptimeleon.math.structures.rings.zn.Zn;

/**
 * Data associated to a spend operation which the provider requires to trace double-spending.
 * c0, c1 and gamma allow to reveal a double-spending user's secret key and the ElGamal secret key
 * used to trace the remainder token of the malicious transaction.
 * Called 'dstag' in the cryptimeleon incentive system paper.
 */
@Getter
@AllArgsConstructor
public class DoubleSpendingTag implements Representable {
    @NonFinal
    @Represented(restorer = "Zn")
    Zn.ZnElement c0; // challenge for deriving the user secret key

    @NonFinal
    @Represented(restorer = "Zn")
    Zn.ZnElement c1; // challenge for deriving the encryption secret key

    @NonFinal
    @Represented(restorer = "Zn")
    Zn.ZnElement gamma; // challenge generation helper value

    @NonFinal
    @Represented(restorer = "Zn")
    Zn.ZnElement eskStarProv; // provider share for ElGamal encryption secret key

    @NonFinal
    @Represented(restorer = "G1")
    GroupElementVector ctrace0;

    @NonFinal
    @Represented(restorer = "G1")
    GroupElementVector ctrace1;

    public DoubleSpendingTag(Representation repr, IncentivePublicParameters pp) {
        new ReprUtil(this).register(pp.getBg().getZn(), "Zn").register(pp.getBg().getG1(), "G1").deserialize(repr);
    }

    /**
     * Constructs a double-spending tag from serialized representations of its components.
     */
    public DoubleSpendingTag(IncentivePublicParameters pp, String serializedC0Repr, String serializedC1Repr, String serializedGammaRepr, String serializedEskProvStarRepr, String serializedCTrace0Repr, String serializedCTrace1Repr) {
        Zn usedZn = pp.getBg().getZn();
        Group groupG1 = pp.getBg().getG1();
        JSONConverter jsonConverter = new JSONConverter();

        Representation c0Repr = jsonConverter.deserialize(serializedC0Repr);
        this.c0 = usedZn.restoreElement(c0Repr);

        Representation c1Repr = jsonConverter.deserialize(serializedC1Repr);
        this.c1 = usedZn.restoreElement(c1Repr);

        Representation gammaRepr = jsonConverter.deserialize(serializedGammaRepr);
        this.gamma = usedZn.restoreElement(gammaRepr);

        Representation eskProvRepr = jsonConverter.deserialize(serializedEskProvStarRepr);
        this.eskStarProv = usedZn.restoreElement(eskProvRepr);

        Representation cTrace0Repr = jsonConverter.deserialize(serializedCTrace0Repr);
        this.ctrace0 = groupG1.restoreVector(cTrace0Repr);

        Representation cTrace1Repr = jsonConverter.deserialize(serializedCTrace1Repr);
        this.ctrace1 = groupG1.restoreVector(cTrace1Repr);
    }

    @Override
    public Representation getRepresentation() {
        return ReprUtil.serialize(this);
    }

    @Override
    public boolean equals(Object o) {
        if(!o.getClass().equals(DoubleSpendingTag.class)) {
            return false;
        }
        else {
            DoubleSpendingTag otherTag = (DoubleSpendingTag) o;
            return otherTag.getC0().equals(this.c0)
                    && otherTag.getC1().equals(this.c1)
                    && otherTag.getEskStarProv().equals(this.eskStarProv)
                    && otherTag.getGamma().equals(this.gamma)
                    && otherTag.getCtrace0().equals(this.ctrace0)
                    && otherTag.getCtrace1().equals(this.ctrace1);
        }
    }

    @Override
    public String toString() {
        return this.c0.toString() + " "
                + this.c1.toString() + " "
                + this.eskStarProv.toString() + " "
                + this.gamma.toString() + " "
                + this.ctrace0.toString() + " "
                + this.ctrace1.toString();
    }
}
