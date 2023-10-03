package shop.auth

import java.security.SecureRandom
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.SecretKeySpec
import javax.crypto.Cipher
import java.util.Base64

import shop.domain.auth._
import shop.config.types.PasswordSalt

import cats.effect._
import cats.syntax.all._
import eu.timepit.refined.auto._

trait Crypto {
  def encrypt(value: Password): EncryptedPassword
  def decrypt(value: EncryptedPassword): Password
}

object Crypto {
  def make[F[_]: Sync](passwordSalt: PasswordSalt): F[Crypto] =
    Sync[F]
      .delay {
        val randomn = new SecureRandom()
        val ivBytes = new Array[Byte](16)
        randomn.nextBytes(ivBytes)
        val iv       = new IvParameterSpec(ivBytes)
        val salt     = passwordSalt.secret.value.getBytes("UTF-8")
        val keySpec  = new PBEKeySpec("password".toCharArray(), salt, 65536, 256)
        val factory  = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val bytes    = factory.generateSecret(keySpec).getEncoded()
        val sKeySpec = new SecretKeySpec(bytes, "AES")
        val eCipher  = EncryptCipher(Cipher.getInstance("AES/CBC/PKCS5Padding"))
        eCipher.value.init(Cipher.ENCRYPT_MODE, sKeySpec, iv)
        val dCipher = DecryptCipher(Cipher.getInstance("AES/CBC/PKCS5Padding"))
        dCipher.value.init(Cipher.DECRYPT_MODE, sKeySpec, iv)
        (eCipher, dCipher)
      }
      .map {
        case (ec, dc) =>
          new Crypto {
            override def encrypt(password: Password): EncryptedPassword = {
              val base64 = Base64.getEncoder()
              val bytes  = password.value.getBytes("UTF-8")
              val result = new String(base64.encode(ec.value.doFinal(bytes)), "UTF-8")
              EncryptedPassword(result)
            }

            override def decrypt(password: EncryptedPassword): Password = {
              val base64 = Base64.getDecoder()
              val bytes  = base64.decode(password.value.getBytes("UTF-8"))
              val result = new String(dc.value.doFinal(bytes), "UTF-8")
              Password(result)
            }
          }
      }
}
