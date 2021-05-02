/*
 * Copyright 2021 Toshiki Iga
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.oiyokan.util;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

/**
 * 暗号化ツール.
 */
public class OiyoEncryptUtil {
    private OiyoEncryptUtil() {
    }

    /**
     * Encrypt.
     * 
     * @param source     Source text.
     * @param passphrase Pass phrase.
     * @return Enc text.
     */
    public static String encrypt(final String source, final String passphrase) {
        try {
            final MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            final byte[] key = Arrays.copyOf(sha256.digest(passphrase.getBytes("UTF-8")), 32);
            final SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            final IvParameterSpec ivParamSpec = new IvParameterSpec(Arrays.copyOf(key, 16));
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParamSpec);
            final byte[] byteCipherText = cipher.doFinal(source.getBytes("UTF-8"));
            return Base64.encodeBase64String(byteCipherText);
        } catch (GeneralSecurityException | IOException ex) {
            throw new IllegalArgumentException("Unexpected Exception.: " + ex.toString(), ex);
        }
    }

    /**
     * 
     * @param encSource  Enc text.
     * @param passphrase Pass phrase.
     * @return Source text.
     */
    public static String decrypt(final String encSource, final String passphrase) {
        try {
            final MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            final byte[] key = Arrays.copyOf(sha256.digest(passphrase.getBytes("UTF-8")), 32);
            final SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
            final IvParameterSpec ivParamSPec = new IvParameterSpec(Arrays.copyOf(key, 16));
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParamSPec);
            final byte[] decoded = Base64.decodeBase64(encSource);
            final byte[] decodedBytes = cipher.doFinal(decoded);
            return new String(decodedBytes, "UTF-8");
        } catch (GeneralSecurityException | IOException ex) {
            throw new IllegalArgumentException("Unexpected Exception.: " + ex.toString(), ex);
        }
    }

    private static Cipher getCipher(final String passphrase) throws GeneralSecurityException, IOException {
        final MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        final byte[] sha256Key = Arrays.copyOf(sha256.digest(passphrase.getBytes("UTF-8")), 32);
        final SecretKeySpec keySpec = new SecretKeySpec(sha256Key, "AES");
        final IvParameterSpec ivParamSpec = new IvParameterSpec(Arrays.copyOf(sha256Key, 16));
        final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParamSpec);
        return cipher;
    }
}
