package com.example.data

enum class PreferredMapApp(
    val displayName: String,
    val shortLabel: String,
    val packageName: String?,
) {
    AUTO(
        displayName = "자동 선택 (설치된 지도 앱)",
        shortLabel = "자동 지도",
        packageName = null,
    ),
    KAKAO(
        displayName = "카카오맵 (도보 길찾기)",
        shortLabel = "카카오맵",
        packageName = "net.daum.android.map",
    ),
    NAVER(
        displayName = "네이버 지도 (도보 길찾기)",
        shortLabel = "네이버 지도",
        packageName = "com.nhn.android.nmap",
    ),
    GOOGLE(
        displayName = "구글 지도 (도보 길찾기)",
        shortLabel = "구글 지도",
        packageName = "com.google.android.apps.maps",
    ),
    ;

    companion object {
        fun fromName(raw: String?): PreferredMapApp {
            return entries.firstOrNull { it.name == raw } ?: AUTO
        }
    }
}
