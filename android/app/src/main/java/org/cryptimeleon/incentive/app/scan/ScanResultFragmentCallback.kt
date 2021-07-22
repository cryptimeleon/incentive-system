package org.cryptimeleon.incentive.app.scan

/**
 * Interface for passing information of the result fragment back to the scan fragment.
 */
interface ScanResultFragmentCallback {
    fun scanResultFragmentCanceled()
    fun scanResultFragmentDismissed()
}
