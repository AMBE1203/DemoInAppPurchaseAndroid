package com.ambe.demoinapppurchase

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode

class MainActivity : AppCompatActivity(), BillingClientStateListener, PurchasesUpdatedListener,
  ProductDetailsResponseListener, PurchasesResponseListener {

  private lateinit var rcvItem: RecyclerView
  private lateinit var billingClient: BillingClient
  private lateinit var itemAdapter: ItemAdapter
  private val productsWithProductDetails: MutableList<ProductDetails> = arrayListOf()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    initViews()
    initializeBillingClient()
  }

  override fun onDestroy() {
    super.onDestroy()
    if (billingClient.isReady) {
      billingClient.endConnection()
    }
  }

  private fun initializeBillingClient() {
    billingClient = BillingClient.newBuilder(this)
      .enablePendingPurchases()
      .setListener(this)
      .build()
    if (!billingClient.isReady) {
      billingClient.startConnection(this)
    }
  }

  private fun initViews() {
    rcvItem = findViewById(R.id.rcv_item)
    itemAdapter = ItemAdapter {

      // todo purchase flow
      val productDetailsParamsList = listOf(
        BillingFlowParams.ProductDetailsParams.newBuilder()
          .setProductDetails(productsWithProductDetails[it])
          .build()
      )

      val billingFlowParams =
        BillingFlowParams.newBuilder().setProductDetailsParamsList(productDetailsParamsList)
          .build()
      launchBillingFlow(billingFlowParams)
    }
  }

  private fun launchBillingFlow(params: BillingFlowParams): Int {
    if (!billingClient.isReady) {
      Log.e("AMBE1203", "launchBillingFlow: BillingClient is not ready")
    }
    val billingResult = billingClient.launchBillingFlow(this, params)

    return billingResult.responseCode
  }

  override fun onBillingServiceDisconnected() {
    // TODO: Try connecting again with exponential backoff
  }

  override fun onBillingSetupFinished(billingResult: BillingResult) {
    if (billingResult.responseCode == BillingResponseCode.OK) {
      queryProductDetails() // get list product to show UI
      queryPurchases() // valid purchase
    }
  }

  private fun queryPurchases() {
    if (!billingClient.isReady) {
      billingClient.startConnection(this)
    }
    billingClient.queryPurchasesAsync(
      QueryPurchasesParams.newBuilder()
        .setProductType(BillingClient.ProductType.INAPP)
        .build(), this
    )
  }

  private fun queryProductDetails() {
    val params = QueryProductDetailsParams.newBuilder()
    val productList: MutableList<QueryProductDetailsParams.Product> = arrayListOf()


    val consumableIds = mutableListOf<String>()

    consumableIds.add("consumable_id_1")
    consumableIds.add("consumable_id_2")
    consumableIds.add("consumable_id_3")

    for (id in consumableIds) {
      productList.add(
        QueryProductDetailsParams.Product.newBuilder()
          .setProductId(id)
          .setProductType(BillingClient.ProductType.INAPP)
          .build()
      )
    }

    params.setProductList(productList).let {
      billingClient.queryProductDetailsAsync(it.build(), this)
    }
  }

  override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
    when (billingResult.responseCode) {
      BillingResponseCode.OK -> {
        if (purchases == null) {
          processPurchases(null)
        } else {
          processPurchases(purchases)
        }
      }
    }
  }

  private fun processPurchases(purchasesList: MutableList<Purchase>?) {
    if (purchasesList == null) {
      return
    }

    purchasesList.forEach {
      val consumeParams = ConsumeParams.newBuilder()
        .setPurchaseToken(it.purchaseToken)
        .build()
      billingClient.consumeAsync(
        consumeParams
      ) { billingResult: BillingResult, _: String? ->
        if (billingResult.responseCode == BillingResponseCode.OK) {
          // log
        } else {
          // log
        }
      }
    }
  }

  override fun onProductDetailsResponse(
    billingResult: BillingResult,
    productDetailsList: MutableList<ProductDetails>
  ) {
    when (billingResult.responseCode) {
      BillingResponseCode.OK -> {
        if (productDetailsList.isNullOrEmpty()) {
          Log.e("AMBE1203", "Found null ProductDetails.")
        } else {
          productsWithProductDetails.addAll(productDetailsList)
          itemAdapter.submitList(productsWithProductDetails)
        }
      }
    }
  }

  override fun onQueryPurchasesResponse(
    billingResult: BillingResult,
    purchasesList: MutableList<Purchase>
  ) {
    processPurchases(purchasesList)
  }
}