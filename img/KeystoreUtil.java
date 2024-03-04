package com.ksmartech.key4c;

import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;

import javax.security.auth.login.LoginException;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.nio.charset.UnsupportedCharsetException;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;

public class KeystoreUtil {

    public KeystoreUtil() {
        //this.config = config;
    }

    private KeyStore keyStore;
    private String HsmProvider = "SunPKCS11";
    private String HsmPassword = "runningCow!1";
    private String HsmConfigFile = "./key4c_cloudhsm.cfg";
    private Provider provObj;
    private Provider provBC;

    public void init(String pin, String configfile) throws KeyStoreException, K4CException {

        this.HsmPassword = pin;
        this.HsmConfigFile = configfile;

        this.provBC = new BouncyCastleProvider();
        Security.addProvider(provBC);
        
        if(HsmProvider.equals("SunPKCS11"))
            this.keyStore = getKeyStoreByConfig(HsmConfigFile, HsmPassword, HsmProvider);
        else {
            try {
                this.keyStore = getKeyStore(HsmProvider, HsmPassword, null);
            } catch (NoSuchAlgorithmException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (CertificateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchProviderException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public void finit() {

        if ((this.keyStore != null) && (this.keyStore.getProvider() != null)) {
            Security.removeProvider(this.keyStore.getProvider().getName());
        }
    }


    public KeyPair generateKeyPair(String keyAlgorithm, String keyLength) throws K4CException
    {
        long time = System.currentTimeMillis();


        KeyPair keyPair = null;

        try {

            if (this.HsmProvider.equals("SunPKCS11")) {

                System.out.println("generate keypair using HSM");

                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(keyAlgorithm, this.keyStore.getProvider());
                if (keyAlgorithm.contains("EC")) {
                    keyPairGenerator.initialize(new ECGenParameterSpec(keyLength), new SecureRandom());
                } else {
                    keyPairGenerator.initialize(Integer.parseInt(keyLength), new SecureRandom());
                }

                keyPair = keyPairGenerator.generateKeyPair();

            } else if (this.HsmProvider.equals("BC")) {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(keyAlgorithm, provBC);
                if (keyAlgorithm.contains("EC")) {
                    keyPairGenerator.initialize(new ECGenParameterSpec(keyLength), new SecureRandom());
                } else {
                    keyPairGenerator.initialize(Integer.parseInt(keyLength), new SecureRandom());
                }

                keyPair = keyPairGenerator.generateKeyPair();
            } else {
                throw new K4CException("Provider(" + keyStore.getProvider() + ") not Supported.");
            }
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | K4CException e) {
            System.out.println(e.getStackTrace().toString());
            throw new K4CException(e.getMessage());
        }

        System.out.println("generateKeyPair end, time spend: " + (System.currentTimeMillis() - time) + "ms");

        return keyPair;
    }

    public KeyPair generateKeyPair(String keyAlgorithm, String keyLength, String provider) throws K4CException
    {
        long time = System.currentTimeMillis();

        KeyPair keyPair = null;

        try {

            if (provider.equals("SunPKCS11")) {

                System.out.println("generate keypair using HSM");

                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(keyAlgorithm, this.keyStore.getProvider());
                if (keyAlgorithm.contains("EC")) {
                    keyPairGenerator.initialize(new ECGenParameterSpec(keyLength), new SecureRandom());
                } else {
                    keyPairGenerator.initialize(Integer.parseInt(keyLength), new SecureRandom());
                }

                keyPair = keyPairGenerator.generateKeyPair();

            } else if (provider.equals("BC")) {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(keyAlgorithm, "BC");
                if (keyAlgorithm.contains("EC")) {
                    keyPairGenerator.initialize(new ECGenParameterSpec(keyLength), new SecureRandom());
                } else {
                    keyPairGenerator.initialize(Integer.parseInt(keyLength), new SecureRandom());
                }

                keyPair = keyPairGenerator.generateKeyPair();
            } else {
                throw new K4CException("Provider(" + keyStore.getProvider() + ") not Supported.");
            }
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | K4CException | NoSuchProviderException e) {
            System.out.println(e.getStackTrace().toString());
            throw new K4CException(e.getMessage());
        }

        System.out.println("generateKeyPair end, time spend: " + (System.currentTimeMillis() - time) + "ms");

        return keyPair;
    }

    public X509Certificate getCertificate(String alias) throws K4CException {

        X509Certificate retCert = null;


        try {
            if(this.provObj == null){
                Provider provider = Security.getProvider("SunPKCS11");
                if(provider != null){
                    AuthProvider aprov = (AuthProvider)provider.configure(this.HsmConfigFile);
                    System.out.println("configured provider");
                    aprov.logout();
                    aprov.login(null, new PasswordInputCallback(this.HsmPassword.toCharArray()));
                    System.out.println("login");
                    this.provObj = aprov;
                } else {
                    throw new K4CException("SunPKCS11 is not allowed");
                }
            }
            if(this.keyStore == null){
                this.keyStore = KeyStore.getInstance("PKCS11", provObj.getName());

                this.keyStore.load(null, this.HsmPassword.toCharArray());
            }

            Certificate cert = keyStore.getCertificate(alias);
            if (cert == null) {
                throw new K4CException("Could not find " + alias + " cert.");
            }

            retCert = (X509Certificate)cert;
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException | NoSuchProviderException | LoginException e) {
            throw new K4CException(e.getMessage());
        }

        return retCert;
    }

    //jwt signer
    private RSASSASigner signer;
    //jwt signer setting
    public void setSigner(PrivateKey key){
        signer = new RSASSASigner(key);
        signer.getJCAContext().setProvider(provObj);
    }

    // Create an RSA signer and configure it to use the HSM
    public String makeJwtSign(PrivateKey key, String alias, int expT, String sub){
        Date expireTime = new Date();
        expireTime.setTime(expireTime.getTime() + expT);
        String jwtString;
        try {
            SignedJWT jwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(alias).build(),
                new JWTClaimsSet.Builder()
                    .expirationTime(expireTime)
                    .claim("typ", "JWT")
                    .claim("sub", sub)
                    .build());
            jwt.sign(signer);

            jwtString = jwt.serialize();
        }
        catch(JOSEException e){
            throw new K4CException(e.getMessage());
        }
        return jwtString;
    }
    public boolean verifyJwtToken(PublicKey key, String token){
        Jws<Claims> claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
        return !claims.getPayload().getExpiration().before(new Date());
    }
    // get private key
    public PrivateKey getPrivateKey(String alias) throws K4CException {

        PrivateKey privateKey = null;

        try {

            if (!keyStore.containsAlias(alias)) {
                throw new K4CException("failed to find " + alias);
            }

            if (!keyStore.isKeyEntry(alias)) {
                throw new K4CException("failed to find " + alias + " key.");
            }

            privateKey = (PrivateKey) keyStore.getKey(alias, HsmPassword.toCharArray());

        } catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
            throw new K4CException(e.getMessage());
        }

        return privateKey;
    }

    public void savePrivateKey(PrivateKey privateKey, String keyfile)  throws K4CException {
        try {
            FileUtils.writeByteArrayToFile(new File(keyfile), privateKey.getEncoded());
        } catch (NullPointerException | IOException | UnsupportedCharsetException e) {
            throw new K4CException("failed to save key file:" + e.getMessage());
        }
    }

    public PrivateKey readPrivateKey(String keyfile)  throws K4CException {
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA", "BC");
			PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(FileUtils.readFileToByteArray(new File(keyfile))));
            return privateKey;
        } catch (NullPointerException | IOException | NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
            throw new K4CException("failed to save key file:" + e.getMessage());
        }
    }
    // get public key
    public PublicKey getPublicKey(String alias) throws K4CException {

        PublicKey publicKey = null;

        try {

            if (!this.keyStore.containsAlias(alias)) {
                throw new K4CException("failed to find " + alias);
            }

            if (!this.keyStore.isKeyEntry(alias)) {
                throw new K4CException("failed to find " + alias + " key.");
            }

            publicKey = (PublicKey) this.keyStore.getKey(alias, HsmPassword.toCharArray());

        } catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
            throw new K4CException(e.getMessage());
        }
        
        return publicKey;
    }

    // get public key
    public PublicKey getPublicKey(PrivateKey privkey) throws K4CException {

        PublicKey publicKey = null;

        try {

            RSAPrivateCrtKey privk = (RSAPrivateCrtKey)privkey;
            RSAPublicKeySpec publicKeySpec = new java.security.spec.RSAPublicKeySpec(privk.getModulus(), privk.getPublicExponent());
            KeyFactory keyFactory = KeyFactory.getInstance("RSA", "BC");
            publicKey = keyFactory.generatePublic(publicKeySpec);

        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException e) {
            throw new K4CException("failed to extract public key from private key" + e.getMessage());
        }
        
        return publicKey;
    }
    // delete key and cert
    public void deleteAlias(String alias) throws K4CException {
        try {
            if (keyStore.containsAlias(alias)) {
                keyStore.deleteEntry(alias);
                keyStore.store(null, null);
            }
        } catch (Exception e) {
            throw new K4CException(e.getMessage());
        }
    }

    // list key and cert in hsm
    public void printKeyStoreAlias() throws KeyStoreException {

        long time = System.currentTimeMillis();

        Enumeration<String> enu = this.keyStore.aliases();
        String alias = null;

        System.out.println("size of contents: " + keyStore.size());

        while (enu.hasMoreElements()) {

            alias = (String) enu.nextElement();

            if (keyStore.isKeyEntry(alias)) {
                System.out.println("Key ==> " + alias);
            }

            if (keyStore.isCertificateEntry(alias)) {
                System.out.println("Certificate ==> " + alias);
            }

        }

        System.out.println("printKeyStoreAlias end, time spend: " + (System.currentTimeMillis() - time) + "ms");
    }
    public X509Certificate makeTempCert(PublicKey publicKey, PrivateKey privateKey, String alias) throws K4CException {

        //long time = System.currentTimeMillis();
        System.out.println("makeTempCert start");

        X509Certificate retCert = null;

        String subject = "cn="+alias+",o=ksmartech,c=kr";
        String issuer = "cn="+alias+",o=ksmartech,c=kr";

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, 365 * 3);

        X509v3CertificateBuilder certBldr = new JcaX509v3CertificateBuilder(
                new X500Name(issuer),
                new BigInteger(1, new SecureRandom().generateSeed(8)),
                new Date(),
                cal.getTime(),
                new X500Name(subject),
                publicKey);

        String algorithm = "RSA";
        if (publicKey.getAlgorithm().contains("EC")) {
            algorithm = "ECDSA";
        }

        String signAlgo = "SHA256with" + algorithm;

        try {
            System.out.println("sign algo: " + signAlgo);
            //ContentSigner signer = new JcaContentSignerBuilder(signAlgo)
            //        .setProvider(this.keyStore.getProvider()).build(privateKey);
            ContentSigner signer = new JcaContentSignerBuilder(signAlgo)
                    .setProvider(provBC).build(privateKey);

            JcaX509CertificateConverter converter = new JcaX509CertificateConverter();
            retCert = converter.getCertificate(certBldr.build(signer));
        } catch (OperatorCreationException | CertificateException e) {
            throw new K4CException(e.getMessage());
        }

        //System.out.println("makeTempCert end, time spend: " + (System.currentTimeMillis() - time) + "ms");

        return retCert;
    }
    // save cert into keystore
    public void importCert(X509Certificate cert, String alias) throws K4CException
    {
        long time = System.currentTimeMillis();

        System.out.println("importCert start");

        try {
            this.keyStore.store(null, this.HsmPassword.toCharArray());
            this.keyStore.setCertificateEntry(alias, cert);
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            throw new K4CException(e.getMessage());
        }
        System.out.println("importCert end, time spend: " + (System.currentTimeMillis() - time) + "ms");
    }
    // save private key and cert into keystore
    public void importCertAndKey(PrivateKey privateKey, X509Certificate cert, String alias) throws K4CException
    {
        long time = System.currentTimeMillis();

        try {
            X509Certificate[] chain = new X509Certificate[1];
            chain[0] = cert;

            long time_ = System.currentTimeMillis();
            System.out.println("keyStore.setKeyEntry start");

            this.keyStore.setKeyEntry(alias, privateKey, this.HsmPassword.toCharArray(), chain);

            System.out.println("keyStore.setKeyEntry end, time spend: " + (System.currentTimeMillis() - time_) + "ms");

            time_ = System.currentTimeMillis();
            System.out.println("keyStore.store start");

            this.keyStore.store(null, this.HsmPassword.toCharArray());

            System.out.println("keyStore.store end, time spend: " + (System.currentTimeMillis() - time_) + "ms");

        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
            throw new K4CException(e.getMessage());
        }

        System.out.println("importCertAndKey end, time spend: " + (System.currentTimeMillis() - time) + "ms");
    }
    public KeyStore getKeyStoreByConfig(String configFilePath, String password,
                                        String keyStoreInfo) throws KeyStoreException, K4CException {

        long time = System.currentTimeMillis();
        KeyStore ks = null;

        //Provider provider = new SunPKCS11(configFilePath);
        Provider provider = null;
        AuthProvider aprov = null;

        int javaVersion = 0;
        try {
            javaVersion = getJavaVersion();
        } catch (Exception e1) {
            System.out.println(e1);
        }

        if(javaVersion <= 8){
            Class<?> providerClass;
            try {
                providerClass = Class.forName("sun.security.pkcs11.SunPKCS11");
                Constructor<?> constructor = providerClass.getConstructor(String.class);
                aprov = (AuthProvider) constructor.newInstance(configFilePath);
                aprov.logout();
                aprov.login(null, new PasswordInputCallback(password.toCharArray()));
            } catch (ClassNotFoundException | NoSuchMethodException | SecurityException |
                     InstantiationException | IllegalAccessException | IllegalArgumentException |
                     InvocationTargetException | LoginException e) {
                System.out.println(e);
                throw new K4CException("HSMKeyStoreLoadError.getMessage() " + configFilePath);
            }
        } else if(javaVersion > 8){
            try {
                provider = Security.getProvider("SunPKCS11");
                if(provider != null){
                    aprov = (AuthProvider)provider.configure(configFilePath);
                    System.out.println("configured provider");
                    aprov.logout();
                    aprov.login(null, new PasswordInputCallback(password.toCharArray()));
                    System.out.println("login");
                } else {
                    throw new K4CException("HSMKeyStoreLoadError.getMessage() +  " + configFilePath);
                }
            } catch (SecurityException | IllegalArgumentException | LoginException e) {
                System.out.println(e);
                throw new K4CException("HSMKeyStoreLoadError.getMessage() " + configFilePath);
            }
        }

        if (Security.addProvider(aprov) == -1) {
            System.out.println("failed to add hsm provider");
            throw new K4CException("HSMKeyStoreLoadError.getMessage() " + configFilePath);
        }
        this.provObj = aprov;
        if (password == null || password.isEmpty()) {
            System.out.println("Invalid HSM password");
            throw new K4CException("HSMKeyStoreLoadError.getMessage() " + configFilePath);
        }

        try {

            System.out.println("Install provider success, calling KeyStoreUtil.getKeyStore()");
            ks = getKeyStore(aprov.getName(), new String(password), keyStoreInfo);

        } catch (KeyStoreException | NoSuchAlgorithmException | IOException | NoSuchProviderException |
                 CertificateException e) {
            System.out.println(e);
            throw new K4CException("HSMKeyStoreLoadError.getMessage() " + configFilePath);
        }

        if(ks == null) {
            System.out.println("Cannot load keystore, check the keystore configuration options");
            throw new K4CException("HSMKeyStoreLoadError.getMessage() " + configFilePath);
        }

        System.out.println("getKeyStoreByConfig end, time spend: " + (System.currentTimeMillis() - time) + "ms");
        this.keyStore = ks;
        return ks;

    }

    public KeyStore getKeyStore(String providerName, String password, String keyStoreFilePath)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException, NoSuchProviderException, CertificateException {

        long time = System.currentTimeMillis();
       
        KeyStore ks = null;

        try {

            if(providerName != null)
                ks = KeyStore.getInstance("PKCS11", Security.getProvider(providerName));
            else
                ks = KeyStore.getInstance("PKCS11");

            ks.load(null, password.toCharArray());
            this.keyStore = ks;
        } finally {

        }

        System.out.println("getKeyStore end, time spend: " + (System.currentTimeMillis() - time) + "ms");
        
        return ks;
    }
    public Provider getProvider() {
        return provObj;
    }
    public KeyStore getKeyStore() {
        return this.keyStore;
    }
    public int getJavaVersion() {
		/*
		 Java 8 or lower: 1.6.0_23, 1.7.0, 1.7.0_80, 1.8.0_211
		 Java 9 or higher: 9.0.1, 11.0.4
		 */

        long time = System.currentTimeMillis();
        System.out.println("getJavaVersion start");

        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if (dot != -1) {
                version = version.substring(0, dot);
            }
        }

        System.out.println("getJavaVersion end, time spend: " + (System.currentTimeMillis() - time) + "ms");

        System.out.println("Current Java version: " + version);
        
        return Integer.parseInt(version);
    }

}
