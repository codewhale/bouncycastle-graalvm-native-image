import java.security.spec.PKCS8EncodedKeySpec;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import java.io.ByteArrayInputStream;
import java.security.KeyFactory;
import java.io.File;
import java.io.FileInputStream;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import java.security.*;

import java.security.Provider;
import java.security.Provider.Service;
import java.security.Security;
import java.util.Set;

public class test {
    static byte[] read(String fname) throws Exception {
        long offset = 0;
        File f = new File(fname);
        long length = f.length();
        byte[] image = new byte[(int)length];
        FileInputStream fis = new FileInputStream(f);
        while (offset < length) {
            offset += fis.read(image, (int)offset, (int)(length - offset));
        }
        fis.close();
        return image;
    }

    public static void main(String[] args) throws Exception {

        Security.addProvider(new BouncyCastleProvider());

	if (args.length != 0)
	{
		Provider [] providerList = Security.getProviders();
		for (Provider provider : providerList)
		{
			System.out.println("Name: "  + provider.getName());
			System.out.println("Information:\n" + provider.getInfo());

			Set<Service> serviceList = provider.getServices();
			for (Service service : serviceList)
			{
				System.out.println("Service Type: " + service.getType() + " Algorithm " + service.getAlgorithm());
			}
		}

	}

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(read("key.pk8"));

        ASN1InputStream bIn = new ASN1InputStream(new ByteArrayInputStream(spec.getEncoded()));
	
        PrivateKeyInfo pki = PrivateKeyInfo.getInstance(bIn.readObject());
        String algOid = pki.getPrivateKeyAlgorithm().getAlgorithm().getId();

        KeyFactory.getInstance(algOid).generatePrivate(spec);

	System.out.println("finished\n");
    }
}
