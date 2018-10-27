package com.projectRa.utils

import org.asynchttpclient.DefaultAsyncHttpClientConfig.Builder
import javax.net.ssl.SSLContext
import javax.net.ssl.X509TrustManager
import io.netty.handler.ssl.JdkSslContext
import io.netty.handler.ssl.ClientAuth
import java.security.cert.X509Certificate

trait HttpConfigTrustAllCertificates {
  
  /**
   * Configura la conexión http para que admita cualquier certificado http
   */
  def trustAllCertificates(b: Builder): Builder = {
    val sslContext = SSLContext.getInstance("SSL")
    sslContext.init(null,
                    Seq(new X509TrustManager {
                          def checkClientTrusted(chain: Array[X509Certificate], authType: String): Unit = { }
                          def checkServerTrusted(chain: Array[X509Certificate], authType: String): Unit = { }
                          def getAcceptedIssuers() = { Array.empty[X509Certificate] }}).toArray,
                    null)
    b.setSslContext(new JdkSslContext(sslContext, true, ClientAuth.NONE))
  }
}