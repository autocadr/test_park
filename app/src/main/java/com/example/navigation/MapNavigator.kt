package com.example.navigation

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import com.example.data.PreferredMapApp

data class MapLaunchResult(
    val success: Boolean,
    val launchedAppName: String? = null,
    val fallbackNotice: String? = null,
    val errorMessage: String? = null,
)

object MapNavigator {

    fun detectInstalledMapApps(context: Context): Set<PreferredMapApp> {
        val pm = context.packageManager
        return buildSet {
            PreferredMapApp.entries
                .filter { it.packageName != null }
                .forEach { app ->
                    if (isPackageInstalled(pm, app.packageName!!)) {
                        add(app)
                    }
                }
        }
    }

    fun launchWalkingDirections(
        context: Context,
        latitude: Double,
        longitude: Double,
        label: String,
        preferredApp: PreferredMapApp,
    ): MapLaunchResult {
        val cleanLabel = label.ifBlank { "내 차 주차 위치" }

        if (preferredApp != PreferredMapApp.AUTO) {
            val directIntent = buildWalkingIntent(context, preferredApp, latitude, longitude, cleanLabel)
            if (directIntent != null && tryStartActivity(context, directIntent)) {
                return MapLaunchResult(success = true, launchedAppName = preferredApp.shortLabel)
            }
        }

        val installedOrder = listOf(
            PreferredMapApp.KAKAO,
            PreferredMapApp.NAVER,
            PreferredMapApp.GOOGLE,
        )
        val installedSet = detectInstalledMapApps(context)

        for (candidate in installedOrder) {
            if (candidate in installedSet) {
                val intent = buildWalkingIntent(context, candidate, latitude, longitude, cleanLabel)
                if (intent != null && tryStartActivity(context, intent)) {
                    val notice = if (preferredApp != PreferredMapApp.AUTO && preferredApp != candidate) {
                        "${preferredApp.shortLabel} 미설치로 ${candidate.shortLabel}에서 도보 길찾기를 실행합니다."
                    } else {
                        null
                    }
                    return MapLaunchResult(
                        success = true,
                        launchedAppName = candidate.shortLabel,
                        fallbackNotice = notice,
                    )
                }
            }
        }

        val geoUri = Uri.parse(
            "geo:$latitude,$longitude?q=$latitude,$longitude(${Uri.encode(cleanLabel)})",
        )
        val geoIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (tryStartActivity(context, geoIntent)) {
            return MapLaunchResult(success = true, launchedAppName = "기본 지도")
        }

        val webUri = Uri.parse(
            "https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude&travelmode=walking",
        )
        val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        if (tryStartActivity(context, webIntent)) {
            return MapLaunchResult(success = true, launchedAppName = "웹 지도")
        }

        return MapLaunchResult(
            success = false,
            errorMessage = "실행할 수 있는 지도 앱이나 브라우저를 찾을 수 없습니다.",
        )
    }

    internal fun buildWalkingIntent(
        context: Context,
        app: PreferredMapApp,
        latitude: Double,
        longitude: Double,
        label: String,
    ): Intent? {
        return when (app) {
            PreferredMapApp.KAKAO -> Intent(
                Intent.ACTION_VIEW,
                Uri.parse("kakaomap://route?ep=$latitude,$longitude&by=FOOT"),
            ).apply {
                setPackage(app.packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            PreferredMapApp.NAVER -> Intent(
                Intent.ACTION_VIEW,
                Uri.parse(
                    "nmap://route/walk?dlat=$latitude&dlng=$longitude" +
                        "&dname=${Uri.encode(label)}&appname=${Uri.encode(context.packageName)}",
                ),
            ).apply {
                setPackage(app.packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            PreferredMapApp.GOOGLE -> Intent(
                Intent.ACTION_VIEW,
                Uri.parse("google.navigation:q=$latitude,$longitude&mode=w"),
            ).apply {
                setPackage(app.packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            PreferredMapApp.AUTO -> null
        }
    }

    private fun tryStartActivity(context: Context, intent: Intent): Boolean {
        return try {
            context.startActivity(intent)
            true
        } catch (_: ActivityNotFoundException) {
            false
        } catch (_: SecurityException) {
            false
        }
    }

    private fun isPackageInstalled(pm: PackageManager, packageName: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(packageName, 0)
            }
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }
}
