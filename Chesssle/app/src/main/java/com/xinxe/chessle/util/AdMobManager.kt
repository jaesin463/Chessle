package com.xinxe.chessle.util

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

class AdMobManager(private val context: Context) {
    private var rewardedAd: RewardedAd? = null
    private val TAG = "AdMobManager"

    fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        // 테스트용 보상형 광고 ID
        val adUnitId = "ca-app-pub-3940256099942544/5224354917"

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
    fun showRewardedAd(activity: Activity, onRewardEarned: () -> Unit) {
        rewardedAd?.let { ad ->
            ad.show(activity) { rewardItem ->
                Log.d(TAG, "사용자가 보상을 획득했습니다.")
                onRewardEarned()
                loadRewardedAd()
            }
        } ?: run {
            Log.d(TAG, "광고가 아직 준비되지 않았습니다.")
            onRewardEarned()
            loadRewardedAd()
        }
    }
}