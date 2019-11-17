# native binary failed to lookup RSA algorithm
```
bash-4.2# ./test
Exception in thread "main" java.security.NoSuchAlgorithmException: 1.2.840.113549.1.1.1 KeyFactory not available
        at java.security.KeyFactory.<init>(KeyFactory.java:138)
        at java.security.KeyFactory.getInstance(KeyFactory.java:172)
        at test.main(test.java:35)
```

## Bouncy Castle [load](https://github.com/bcgit/bc-java/blob/a66e904a431d992281c8378064fed4cbc4ab1d99/prov/src/main/java/org/bouncycastle/jce/provider/BouncyCastleProvider.java#L221) algoirthms via java reflection

To load the required algorithms, add the corresponding Mappings class in relfection config.
```
[
  {
    "name" : "org.bouncycastle.jcajce.provider.asymmetric.RSA$Mappings",
    "allPublicConstructors" : true,
    "allPublicMethods" : true
  }
]
```

## however, NoSuchAlgorithmException error remains
Let's trace 1.2.840.113549.1.1.1
```
~/bc-java$ ack '1.2.840.113549.1.1.1 ' --java -B1 -A1
core/src/main/java/org/bouncycastle/asn1/pkcs/PKCSObjectIdentifiers.java
13-    ASN1ObjectIdentifier    pkcs_1                    = new ASN1ObjectIdentifier("1.2.840.113549.1.1");
14:    /** PKCS#1: 1.2.840.113549.1.1.1 */
15-    ASN1ObjectIdentifier    rsaEncryption             = pkcs_1.branch("1");
```
And realize this is register at:
```
~/bc-java$ ack PKCSObjectIdentifiers.rsaEncryption --java|grep register
prov/src/main/java/org/bouncycastle/jcajce/provider/asymmetric/RSA.java:86:            registerOid(provider, PKCSObjectIdentifiers.rsaEncryption, "RSA", keyFact);
prov/src/main/java/org/bouncycastle/jcajce/provider/asymmetric/RSA.java:91:            registerOidAlgorithmParameters(provider, PKCSObjectIdentifiers.rsaEncryption, "RSA");
```
So, what is keyFact:
```
~/bc-java$ ack keyFact prov/src/main/java/org/bouncycastle/jcajce/provider/asymmetric/RSA.java
            AsymmetricKeyInfoConverter keyFact = new KeyFactorySpi();
            registerOid(provider, PKCSObjectIdentifiers.rsaEncryption, "RSA", keyFact);
            registerOid(provider, X509ObjectIdentifiers.id_ea_rsa, "RSA", keyFact);
            registerOid(provider, PKCSObjectIdentifiers.id_RSAES_OAEP, "RSA", keyFact);
            registerOid(provider, PKCSObjectIdentifiers.id_RSASSA_PSS, "RSA", keyFact);
```
Finally, update reflection config to include KeyFactorySpi
```
[
  {
    "name" : "org.bouncycastle.jcajce.provider.asymmetric.rsa.KeyFactorySpi",
    "allPublicConstructors" : true,
    "allPublicMethods" : true
  },
  {
    "name" : "org.bouncycastle.jcajce.provider.asymmetric.RSA$Mappings",
    "allPublicConstructors" : true,
    "allPublicMethods" : true
  }
]
```
## Da da
```
bash-4.2# make native;./test
native-image -cp bcprov-jdk15on-164.jar:. -H:ReflectionConfigurationFiles=reflection-config.json test
Build on Server(pid: 243, port: 37325)
[test:243]    classlist:   1,930.93 ms
[test:243]        (cap):     979.95 ms
[test:243]        setup:   1,390.11 ms
[test:243]   (typeflow):   4,624.12 ms
[test:243]    (objects):   3,530.20 ms
[test:243]   (features):     152.29 ms
[test:243]     analysis:   8,445.48 ms
[test:243]     (clinit):     163.43 ms
[test:243]     universe:     335.92 ms
[test:243]      (parse):   1,058.46 ms
[test:243]     (inline):   1,252.96 ms
[test:243]    (compile):   4,617.22 ms
[test:243]      compile:   7,276.65 ms
[test:243]        image:     500.08 ms
[test:243]        write:     104.66 ms
[test:243]      [total]:  20,058.31 ms
finished
```

# Misc
## list algorithm
```
bash-4.2# ./test -|grep KeyFactory|grep RSA
Service Type: KeyFactory Algorithm RSA
Service Type: KeyFactory Algorithm RSA
Service Type: KeyFactory Algorithm RSA
```
