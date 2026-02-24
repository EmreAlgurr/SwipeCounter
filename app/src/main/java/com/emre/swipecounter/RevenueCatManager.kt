package com.emre.swipecounter

import android.app.Activity
import android.app.Application
import android.util.Log
import com.revenuecat.purchases.CustomerInfo
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Package
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.interfaces.PurchaseCallback
import com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback
import com.revenuecat.purchases.models.StoreTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object RevenueCatManager {

    private const val TAG = "RevenueCat"
    private val API_KEY = BuildConfig.REVENUECAT_API_KEY
    private const val ENTITLEMENT_ID = "premium"

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private var currentOffering: Package? = null

    fun init(application: Application) {
        Purchases.logLevel = LogLevel.DEBUG
        Purchases.configure(
            PurchasesConfiguration.Builder(application, API_KEY).build()
        )
        Log.d(TAG, "RevenueCat configured with API key")

        // Check initial entitlement status
        refreshPurchaseStatus()
    }

    /**
     * Refreshes the current customer's entitlement status from RevenueCat servers.
     */
    fun refreshPurchaseStatus() {
        Purchases.sharedInstance.getCustomerInfo(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                val hasPremium = customerInfo.entitlements[ENTITLEMENT_ID]?.isActive == true
                _isPremium.value = hasPremium
                Log.d(TAG, "Customer info received. Premium active: $hasPremium")
            }

            override fun onError(error: PurchasesError) {
                Log.e(TAG, "Error fetching customer info: ${error.message}")
            }
        })
    }

    /**
     * Fetches available offerings and caches the first package.
     */
    fun fetchOfferings(onSuccess: (Package) -> Unit, onError: (String) -> Unit) {
        Purchases.sharedInstance.getOfferingsWith(
            onError = { error ->
                Log.e(TAG, "Error fetching offerings: ${error.message}")
                onError(error.message)
            },
            onSuccess = { offerings ->
                val pkg = offerings.current?.availablePackages?.firstOrNull()
                if (pkg != null) {
                    currentOffering = pkg
                    Log.d(TAG, "Offering fetched: ${pkg.identifier}")
                    onSuccess(pkg)
                } else {
                    onError("No packages available")
                }
            }
        )
    }

    /**
     * Initiates a purchase flow for the premium entitlement.
     */
    fun purchasePremium(activity: Activity, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val pkg = currentOffering
        if (pkg == null) {
            // Fetch offerings first, then purchase
            fetchOfferings(
                onSuccess = { fetchedPkg ->
                    executePurchase(activity, fetchedPkg, onSuccess, onError)
                },
                onError = onError
            )
        } else {
            executePurchase(activity, pkg, onSuccess, onError)
        }
    }

    private fun executePurchase(
        activity: Activity,
        pkg: Package,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        Purchases.sharedInstance.purchase(
            PurchaseParams.Builder(activity, pkg).build(),
            object : PurchaseCallback {
                override fun onCompleted(storeTransaction: StoreTransaction, customerInfo: CustomerInfo) {
                    val hasPremium = customerInfo.entitlements[ENTITLEMENT_ID]?.isActive == true
                    _isPremium.value = hasPremium
                    if (hasPremium) {
                        Log.d(TAG, "Purchase successful! Premium unlocked.")
                        onSuccess()
                    } else {
                        Log.w(TAG, "Purchase completed but entitlement not active")
                        onError("Purchase completed but premium not activated")
                    }
                }

                override fun onError(error: PurchasesError, userCancelled: Boolean) {
                    if (userCancelled) {
                        Log.d(TAG, "User cancelled purchase")
                        onError("Purchase cancelled")
                    } else {
                        Log.e(TAG, "Purchase error: ${error.message}")
                        onError(error.message)
                    }
                }
            }
        )
    }

    /**
     * Restores previous purchases for the user.
     */
    fun restorePurchases(onSuccess: () -> Unit, onError: (String) -> Unit) {
        Purchases.sharedInstance.restorePurchases(object : ReceiveCustomerInfoCallback {
            override fun onReceived(customerInfo: CustomerInfo) {
                val hasPremium = customerInfo.entitlements[ENTITLEMENT_ID]?.isActive == true
                _isPremium.value = hasPremium
                if (hasPremium) {
                    Log.d(TAG, "Restore successful! Premium unlocked.")
                    onSuccess()
                } else {
                    Log.d(TAG, "Restore completed but no active entitlements found")
                    onError("No active purchases to restore")
                }
            }

            override fun onError(error: PurchasesError) {
                Log.e(TAG, "Restore error: ${error.message}")
                onError(error.message)
            }
        })
    }
}
