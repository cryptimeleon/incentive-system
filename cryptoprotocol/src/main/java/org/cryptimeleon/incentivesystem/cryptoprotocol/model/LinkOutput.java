package org.cryptimeleon.incentivesystem.cryptoprotocol.model;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.cryptimeleon.incentivesystem.cryptoprotocol.model.keys.user.UserPublicKey;
import org.cryptimeleon.math.structures.rings.zn.Zn.ZnElement;

/**
 * A class modeling the output of the link algorithm of the 2020 inc sys paper.
 * Contains key material of an user that is suspected of double-spending as well as tracing information to trace transactions resulting
 * from said double spending attempt.
 */
@Value
@AllArgsConstructor
public class LinkOutput
{
    ZnElement dsBlame; // DLOG of the usk of the user blamed of double-spending
    UserPublicKey upk; // public key of the user blamed of double-spending
    ZnElement dsTrace; // used to trace further transactions resulting from the detected double-spending attempt
}
