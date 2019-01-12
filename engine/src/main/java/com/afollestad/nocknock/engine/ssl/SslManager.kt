/**
 * Designed and developed by Aidan Follestad (@afollestad)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.afollestad.nocknock.engine.ssl

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.annotation.CheckResult
import com.afollestad.nocknock.utilities.ext.toUri
import okhttp3.OkHttpClient
import java.io.BufferedInputStream
import java.io.FileInputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager
import timber.log.Timber.d as log

/** @author Aidan Follestad (@afollestad) */
interface SslManager {

  @CheckResult fun clientForCertificate(
    certUri: String,
    siteUri: String,
    client: OkHttpClient
  ): OkHttpClient
}

/** @author Aidan Follestad (@afollestad) **/
class RealSslManager(
  private val app: Application
) : SslManager {

  override fun clientForCertificate(
    certUri: String,
    siteUri: String,
    client: OkHttpClient
  ): OkHttpClient {
    val parsedCertUri = certUri.toUri()
    val parsedSiteUri = siteUri.toUri()
    val siteHost = parsedSiteUri.host ?: ""

    log("Loading certificate $certUri for host $siteHost")
    val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
    keyStore.load(null, null)

    val certInputStream = app.openUri(parsedCertUri)
    val bis = BufferedInputStream(certInputStream)
    val certificateFactory = CertificateFactory.getInstance("X.509")

    while (bis.available() > 0) {
      val cert = certificateFactory.generateCertificate(bis)
      keyStore.setCertificateEntry(siteHost, cert)
    }

    val trustManagerFactory =
      TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    trustManagerFactory.init(keyStore)

    val trustManagers = trustManagerFactory.trustManagers
    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(null, trustManagers, null)

    val trustManager = trustManagers.first() as X509TrustManager
    log("Loaded successfully!")
    return client.newBuilder()
        .sslSocketFactory(sslContext.socketFactory, trustManager)
        .hostnameVerifier { hostname, _ ->
          log("Verifying hostname $hostname")
          hostname == siteHost
        }
        .build()
  }
}

private fun Context.openUri(uri: Uri) = when (uri.scheme) {
  "content" -> {
    contentResolver.openInputStream(uri) ?: throw IllegalStateException(
        "Unable to open input stream to $uri"
    )
  }
  "file" -> FileInputStream(uri.path)
  else -> FileInputStream(uri.toString())
}
