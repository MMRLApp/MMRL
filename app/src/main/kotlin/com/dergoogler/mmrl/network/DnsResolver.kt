package com.dergoogler.mmrl.network

import okhttp3.Dns
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.dnsoverhttps.DnsOverHttps
import java.net.InetAddress
import java.net.UnknownHostException

class DnsResolver(
    private val client: OkHttpClient,
    private val useDoh: Boolean,
) : Dns {
    private val doh by lazy {
        DnsOverHttps
            .Builder()
            .client(client)
            .url("https://cloudflare-dns.com/dns-query".toHttpUrl())
            .bootstrapDnsHosts(
                listOf(
                    InetAddress.getByName("162.159.36.1"),
                    InetAddress.getByName("162.159.46.1"),
                    InetAddress.getByName("1.1.1.1"),
                    InetAddress.getByName("1.0.0.1"),
                    InetAddress.getByName("2606:4700:4700::1111"),
                    InetAddress.getByName("2606:4700:4700::1001"),
                    InetAddress.getByName("2606:4700:4700::0064"),
                    InetAddress.getByName("2606:4700:4700::6400"),
                ),
            ).resolvePrivateAddresses(true)
            .build()
    }

    override fun lookup(hostname: String): List<InetAddress> {
        if (useDoh) {
            try {
                return doh.lookup(hostname)
            } catch (_: UnknownHostException) {
            }
        }
        return Dns.SYSTEM.lookup(hostname)
    }
}
