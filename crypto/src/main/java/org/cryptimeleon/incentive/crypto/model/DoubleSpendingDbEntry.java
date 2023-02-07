package org.cryptimeleon.incentive.crypto.model;

import org.cryptimeleon.craco.sig.sps.eq.SPSEQSignature;
import org.cryptimeleon.math.structures.rings.zn.Zn;

public class DoubleSpendingDbEntry {
    private final Zn.ZnElement dsid;
    private final DoubleSpendingTag dstag;
    private final SPSEQSignature signature;
    private final Zn.ZnElement dsidProvStar;

    public DoubleSpendingDbEntry(Zn.ZnElement dsid, DoubleSpendingTag dstag, SPSEQSignature signature, Zn.ZnElement dsidProvStar) {
        this.dsid = dsid;
        this.dstag = dstag;
        this.signature = signature;
        this.dsidProvStar = dsidProvStar;
    }

    public Zn.ZnElement getDsid() {
        return dsid;
    }

    public DoubleSpendingTag getDstag() {
        return dstag;
    }

    public SPSEQSignature getSignature() {
        return signature;
    }

    public Zn.ZnElement getDsidProvStar() {
        return dsidProvStar;
    }
}
