#!/bin/bash
if [[ ! $1 ]]; then echo "Usage: $0 /path/to/cacert.to.import.cert"; exit; fi
cert=$1
echo "Your cacert password is probably 'changeit', unless you changed it."
for k in `whereis keytool | sort`; do
	if [[ -x ${k} ]]; then
		if [[ ! -L ${k} ]]; then
			mkdir -p ${k%/bin/*}/jre/lib/security
			cacerts=${k%/bin/*}/jre/lib/security/cacerts
		else
			cacerts=/etc/pki/ca-trust/extracted/java/cacerts
		fi
		echo ""
		echo " == Loading cert with $k into $cacerts ... == "
		echo ""
		${k} -delete -alias ${1##*/} -keystore ${cacerts}
		${k} -import -alias ${1##*/} -keystore ${cacerts} -file ${cert}
		${k} -list -keystore ${cacerts} | grep -A1 ${1##*/}
	fi
done