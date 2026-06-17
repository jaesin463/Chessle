package com.xinxe.chessle.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.xinxe.chessle.BuildConfig

class AdMobManager(private val context: Context) {
    private var rewardedAd: RewardedAd? = null
    private val TAG = "AdMobManager"

    fun loadRewardedAd() {
        val adUnitId = BuildConfig.REWARDED_AD_UNIT_ID
        if (adUnitId.isBlank()) {
            Log.e(TAG, "Rewarded ad unit ID is not configured.")
            rewardedAd = null
            return
        }

        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(context, adUnitId, adRequest, object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, "광고 로드 실패: ${adError.message}")
                rewardedAd = null
            }

            override fun onAdLoaded(ad: RewardedAd) {
                Log.d(TAG, "광고 로드 완료")
                rewardedAd = ad
            }
        })
    }

    // 광고 보여주기
    fun showRewardedAd(activity: Activity, onRewardEarned: () -> Unit): Boolean {
        val ad = rewardedAd ?: run {
            Log.d(TAG, "광고가 아직 준비되지 않았습니다.")
            loadRewardedAd()
            return false
        }

        ad.show(activity) {
            Log.d(TAG, "사용자가 보상을 획득했습니다.")
            onRewardEarned()
            loadRewardedAd()
        }
        return true
    }
}
