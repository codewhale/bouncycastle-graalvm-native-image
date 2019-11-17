jar:
	wget https://www.bouncycastle.org/download/bcprov-jdk15on-164.jar

pk8:
	openssl genrsa -f4 2048 > key.pem
	openssl pkcs8 -in key.pem -topk8 -outform DER -out key.pk8 -nocrypt

docker:
	docker run -it -v ${PWD}:/root oracle/graalvm-ce bash

prep:
	gu install native-image

compile:
	javac -cp bcprov-jdk15on-164.jar test.java

jvm:
	java -cp bcprov-jdk15on-164.jar:. test

jvm-:
	java -cp bcprov-jdk15on-164.jar:. test -

native:
	native-image -cp bcprov-jdk15on-164.jar:. -H:ReflectionConfigurationFiles=reflection-config.json test

clean:
	rm *.class key.* *.jar
