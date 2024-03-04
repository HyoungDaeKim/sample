import kr.cloudhsm.kms.client.*;
import com.ksmartech.key4c.*;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Base64.Encoder;

class KmsClientTest  {
    /***************************************************
    kms_cert_path=../certs/bb033d18488c49ee84d3e1d03ca7904c.crt
    kms_key_path=../certs/bb033d18488c49ee84d3e1d03ca7904c.key
    *****************************************************/
    static final String config_file_path = "./kms_cli.cfg";
    static final String keyid = "ee3d4348d388f778a1c365a2de4db6e1";
    	final static int KEY_JOB_GEN = 0;
	final static int KEY_JOB_IMPORT = 1;

	static KeyPair keypair = null;
	static KeystoreUtil keyUtil = null;

	private static void generateKeyAndSave(String alias, String keyfile)
	{
		PrivateKey privateKey = null;
		PublicKey publicKey = null;
		try{

			/* keypair generate with bouncycastle */
			keypair = keyUtil.generateKeyPair("RSA", "2048", "BC");
			System.out.println("rsa 2048 keypair generated");
			privateKey = keypair.getPrivate();
			publicKey = keypair.getPublic();

			/* make temp cert */
			Certificate cert = keyUtil.makeTempCert(publicKey, privateKey, alias);
			System.out.println("temporary certificate");
			/* import to HSM */
			keyUtil.importCertAndKey(privateKey, (X509Certificate)cert, alias);
			System.out.println("key imported");
			keyUtil.savePrivateKey(privateKey, keyfile);
			System.out.println("key saved");
		}
		catch(Exception e){
			e.printStackTrace();
				System.out.println(e.getMessage());
		}
	}

	private static void importKey(String alias, String keyfile)
	{
		PrivateKey privateKey = null;
		PublicKey publicKey = null;
		try{
			/* read private key */
			privateKey = keyUtil.readPrivateKey(keyfile);
			System.out.println("private key read");
			publicKey = keyUtil.getPublicKey(privateKey);
			System.out.println("public key extracted");

			/* make temp cert */
			Certificate cert = keyUtil.makeTempCert(publicKey, privateKey, alias);
			System.out.println("temporary certificate");
			/* import to HSM */
			keyUtil.importCertAndKey(privateKey, (X509Certificate)cert, alias);
			System.out.println("key imported");
		}
		catch(Exception e){
			e.printStackTrace();
				System.out.println(e.getMessage());
		}
	}
    
    private static String hexdump(byte[] data) {
        final int perRow = 16;
    
        final String hexChars = "0123456789ABCDEF";
        StringBuilder dump = new StringBuilder();
        StringBuilder chars = null;
        for (int i = 0; i < data.length; i++) {
            int offset = i % perRow;
            if (offset == 0) {
                chars = new StringBuilder();
                dump.append(String.format("%04x", i))
                    .append("  ");
            }
    
            int b = data[i] & 0xFF;
            dump.append(hexChars.charAt(b >>> 4))
                .append(hexChars.charAt(b & 0xF))
                .append(' ');
    
            chars.append((char) ((b >= ' ' && b <= '~') ? b : '.'));
    
            if (i == data.length - 1 || offset == perRow - 1) {
                for (int j = perRow - offset - 1; j > 0; j--)
                    dump.append("-- ");
                dump.append("  ")
                    .append(chars)
                    .append('\n');
            }
        }
        return dump.toString();
    }

    
    public static void main(String[] args) {
        try{

            /**/
            KeyPair keypair = null;
			PrivateKey privateKey = null;
			PublicKey publicKey = null;
            String alias = "BC Card";

			try {

				keyUtil = new KeystoreUtil();
				keyUtil.init("runningCow!1", "./key4c_cloudhsm.cfg");

				privateKey = keyUtil.getPrivateKey(alias);
				Certificate cert = keyUtil.getCertificate(alias);
				publicKey = cert.getPublicKey();
			}
			catch(K4CException e){
				System.out.println("no certificate");
				generateKeyAndSave(alias, alias + ".key");

				privateKey = keyUtil.getPrivateKey(alias);
				Certificate cert = keyUtil.getCertificate(alias);
				publicKey = cert.getPublicKey();
			}

			keyUtil.setSigner(privateKey);
			String signMsg = keyUtil.makeJwtSign(privateKey, alias, 1000*60*10, "12345");
			System.out.println("jwt: " + signMsg);
            
            if(keyUtil.verifyJwtToken(publicKey, signMsg))
                System.out.println("token verified");
            else
                System.out.println("token verification failed");

            /* */
            KMSClient kmsClient = KMSClient.loadFromConfig(config_file_path);

            System.out.println("\n\nTesting encrypt/decrypt");
            CryptoResult cryptoResult = null;
            byte[] iv = {0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF, 0x10, 0x32, 0x54, 0x76, 0x10, 0x32, 0x54, 0x76};
            /*************************************  
                암호화 API 
            **************************************/
            cryptoResult = kmsClient.encrypt(
                keyid,
                Algorithm.ENCRYPTION_ALG.AES_CBC,
                iv,
                // IV, GCM 12바이트 IV, CBC는 16바이트 IV, CTR은 12바이트 IV(Nonce)를 입력하면 
                // 내부적으로 4바이트 0을 추가하여 16바이트 IV를 완성시킴
                "nomeaning", // << AAD 데이터 
                "test".getBytes()); // << 암호화할 데이터 

            if(cryptoResult.getResultCode() != 0 ) {
                System.out.println("Error!");
                return;
            }

            // 암호화 결과값
            byte[] encrypted = cryptoResult.getData();
            if(encrypted != null) System.out.println("encryption finished: " + new String(encrypted));
            else {
                System.out.println("encryption failed");
                return;
            }

            /*************************************
                암호화 API 
            **************************************/
            cryptoResult = kmsClient.decrypt(
                keyid,
                Algorithm.ENCRYPTION_ALG.AES_CBC,
                iv,
                // IV, GCM 12바이트 IV, CBC는 16바이트 IV, CTR은 12바이트 IV(Nonce)를 입력하면 
                // 내부적으로 4바이트 0을 추가하여 16바이트 IV를 완성시킴
                "nomeaning", // << AAD 데이터 
                encrypted); // << 암호화할 데이터 

            if(cryptoResult.getResultCode() != 0 ) {
                System.out.println("Error!");
                return;
            }

            // 암호화 결과값
            byte[] derypted = cryptoResult.getData();
            if(encrypted != null) {
                System.out.println("decryption finished");
                System.out.println("Decrypted : " + new String(derypted) );
            }
            else {
                System.out.println("decryption failed");
                return;
            }
            /* 
            keyUtil = new KeystoreUtil();
			
			keyUtil.init("runningCow!1", "./key4c_cloudhsm.cfg");
            /* */
        }catch(Exception  e) {
           e.printStackTrace();   
        }
    }

}
