package com.dergoogler.mmrl.app

import android.os.Environment
import com.dergoogler.mmrl.platform.PlatformManager
import java.io.File

typealias PLM = PlatformManager

object Const {
    val PUBLIC_DOWNLOADS: File = Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_DOWNLOADS
    )

    const val PRIVACY_POLICY_URL = "https://mmrl.dev/legal/privacy"
    const val TERMS_OF_SERVICE_URL = "https://mmrl.dev/legal/terms"
    const val GOOGLE_PLAY_DOWNLOAD = "https://play.google.com/store/apps/details?id=com.dergoogler.mmrl"
    const val GITHUB_DOWNLOAD = "https://github.com/MMRLApp/MMRL/releases"
    const val SANMER_GITHUB_URL = "https://github.com/SanmerDev"
    const val GOOGLER_GITHUB_URL = "https://github.com/DerGoogler"
    const val RESOURCES_URL = "https://mmrl.dev"
    const val TRANSLATE_URL = "https://hosted.weblate.org/projects/mmrl/"
    const val GITHUB_URL = "https://github.com/MMRLApp/MMRL"
    const val GITHUB_ISSUES_URL = "https://github.com/MMRLApp/MMRL/issues"
    const val TELEGRAM_URL = "https://t.me/MMRLApp"
    const val SPONSORS_URL = "https://github.com/sponsors/MMRLApp"
    const val DEMO_REPO_URL = "https://gr.dergoogler.com/gmr/"
    const val SPDX_URL = "https://spdx.org/licenses/%s.json"

    val WEBUI_DOMAIN_SAFE_REGEX = Regex("^https?://mui\\.kernelsu\\.org(/.*)?$")
    val WEBUI_DOMAIN_REMOTE_SAFE_REGEX = Regex(
        "^(https?://)?(localhost|127\\.0\\.0\\.1|::1|10(?:\\.\\d{1,3}){3}|172\\.(?:1[6-9]|2\\d|3[01])(?:\\.\\d{1,3}){2}|192\\.168(?:\\.\\d{1,3}){2})(?::([0-9]{1,5}))?$"
    )

    const val CLEAR_CMD = "[H[J"
}