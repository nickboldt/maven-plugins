0. You will need to BUILD this project first.

	$ export JAVA_HOME=/opt/sun-java2-6.0/; mvn clean install

---

1. Once built, you can then configure its use. See pom-sync.xml for a sample invocation, and run it like this:

	$ export JAVA_HOME=/opt/sun-java2-6.0/; mvn clean install -f pom-sync.xml -Doperation=pull

---

2. NOTE: if your Jenkins/Hudson server runs on https, must first import its certificate or you may get this error:

	javax.net.ssl.SSLHandshakeException: sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target

AS USER (with Firefox):

Browse to https://jenkins.mw.lab.eng.bos.redhat.com/hudson/ & accept the cert. Log in using kerberos login.

	Edit > Preferences > Advanced > Encryption > View Certificates > find hudson cert > Export to file /tmp/jenkins.mw.lab.eng.bos.redhat.com.cert

AS ROOT (default password is "changeit"):

	# /opt/sun-java2-6.0/jre/bin/keytool -list -keystore /opt/sun-java2-6.0/jre/lib/security/cacerts | grep hudson
		# (if you need to replace a cert, delete the old one first)
		# /opt/sun-java2-6.0/jre/bin/keytool -delete -alias hudson.qa -keystore /opt/sun-java2-6.0/jre/lib/security/cacerts
	# /opt/sun-java2-6.0/jre/bin/keytool -import -alias hudson.qa -keystore /opt/sun-java2-6.0/jre/lib/security/cacerts -file /tmp/jenkins.mw.lab.eng.bos.redhat.com.cert
	# /opt/sun-java2-6.0/jre/bin/keytool -list -keystore /opt/sun-java2-6.0/jre/lib/security/cacerts | grep hudson

   Make sure that JAVA_HOME is set to the path where you imported the cert, eg.:

	$ export JAVA_HOME=/opt/sun-java2-6.0/; mvn clean install -f pom-sync.xml -Doperation=pull
 
