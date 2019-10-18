package ind.syu.kubeIsp.utils

import com.google.common.io.BaseEncoding

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.util.Random

object HmacSHA256 {
    val HMAC_SHA256 = "HmacSHA256"


    fun generate(key: String, salt: String): String {
        var sha256_HMAC: Mac? = null
        try {
            sha256_HMAC = Mac.getInstance(HMAC_SHA256)
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

        val secret_key = SecretKeySpec(salt.toByteArray(), HMAC_SHA256)
        try {
            sha256_HMAC!!.init(secret_key)
        } catch (e: InvalidKeyException) {
            e.printStackTrace()
        }

        return BaseEncoding.base16().lowerCase().encode(sha256_HMAC!!.doFinal(key.toByteArray()))
    }

    fun createSalt(): String {
        val chars = "ABCDEFGHJKMNPQRSTWXYZabcdefhijkmnprstwxyz2345678"
        var salt = ""
        val random = Random()
        for (i in 0..10) {
            val index = random.nextInt(chars.length)
            salt += chars[index]
        }
        return salt
    }

}