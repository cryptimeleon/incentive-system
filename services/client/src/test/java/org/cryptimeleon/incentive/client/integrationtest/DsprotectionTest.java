package org.cryptimeleon.incentive.client.integrationtest;

import org.springframework.beans.factory.annotation.Value;

/**
 * Tests the methods for interacting with the double-spending database.
 */
public class DsprotectionTest {
    @Value("${dsprotection.url}")
    public String dsprotectionUrl;

    /**
     * Adds some transactions to the database.
     */
    public void addTransactionTest() {

    }
}
