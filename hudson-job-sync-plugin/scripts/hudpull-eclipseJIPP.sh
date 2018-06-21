#!/bin/bash

# pull updated config.xml files from Hudson/Jenkins server

# TODO make sure this is the latest version, eg., 0.0.3-SNAPSHOT
if [[ ! -f ${HOME}/.m2/repository/org/jboss/maven/plugin/hudson-job-sync-plugin/0.0.3-SNAPSHOT/hudson-job-sync-plugin-0.0.3-SNAPSHOT.pom ]]; then
  # rebuild maven plugin
  pushd  /tmp >/dev/null
  	git clone https://github.com/nickboldt/maven-plugins.git
  	pushd /tmp/maven-plugins/hudson-job-sync-plugin >/dev/null
  		mvn install -DskipTests
  	popd >/dev/null
  popd >/dev/null
fi

# TODO configure this path to where you have a cache of your config.xml files checked out, eg., https://github.com/nickboldt/eclipse.ci.jobs
webtoolsJobsPath=${HOME}/4-eclipse.org/WTP/eclipse.ci.jobs
cd ${webtoolsJobsPath}
if [[ ! $1 ]]; then
  # TODO this file must exist. See example here: https://github.com/nickboldt/maven-plugins/blob/master/hudson-job-sync-plugin/pom-sync.xml
  vim pom-sync-internal-eclipse.xml
fi

#defaults
JENKINS_PATH=${webtoolsJobsPath}/cache/https/ci.eclipse.org/webtools/
OPTIONS="-DviewFilter=view/webtools_R3_10/"
regexFilter=".*"
while [[ "$#" -gt 0 ]]; do
  case $1 in
  	'-DhudsonURL='*)  hudsonURL=${1##*=};JENKINS_PATH=${webtoolsJobsPath}/cache/${hudsonURL/:\//}; OPTIONS="${OPTIONS} $1"; shift 1;;
	'-DregexFilter='*) OPTIONS="${OPTIONS} -DregexFilter=${1##*=}"; regexFilter="${1##*=}"; shift 1;;
    '-D'*) OPTIONS="${OPTIONS} $1"; shift 1;;
	*) OPTIONS="${OPTIONS} -DregexFilter=${1##*=}"; regexFilter="${1##*=}"; shift 1;;
  esac
done
#echo "hudsonURL = ${hudsonURL}"

tmplog=$(mktemp)
echo "mvn install -f pom-sync-internal-eclipse.xml -DoverwriteExistingConfigXMLFile=true -Doperation=pull ${OPTIONS}"
export JAVA_HOME=/usr/lib/jvm/java/; /opt/maven3/bin/mvn install -f pom-sync-internal-eclipse.xml -DoverwriteExistingConfigXMLFile=true \
-Doperation=pull ${OPTIONS} > ${tmplog}
foundRegex=$(egrep "${regexFilter}" ${tmplog})
if [[ $(egrep "SUCCESS" ${tmplog}) ]] && [[ $foundRegex ]] && [[ ! $(egrep "FAIL" ${tmplog}) ]]; then
	echo ""; egrep "${regexFilter}" ${tmplog} | grep "=="; echo ""
	rm -f ${tmplog}
else
	if [[ ! $foundRegex ]]; then
		echo ""; echo "[ERROR] Did not find regexFilter = ${regexFilter} - nothing happened!" | egrep "${regexFilter}"; echo ""
		rm -f ${tmplog}
	else
		echo ""; echo "[ERROR] Something bad happened. Check log for details"; echo ""
		cat ${tmplog}
		echo ""
		echo "Logfile: ${tmplog}"
	fi
fi

#echo JENKINS_PATH = ${JENKINS_PATH}
find ${JENKINS_PATH}/view/ -type f -name "config.2*.xml" -exec rm -f {} \;

viewFilter=${OPTIONS##*-DviewFilter=}; viewFilter=${viewFilter%% *}; # echo $viewFilter
cd ${JENKINS_PATH}/${viewFilter}job
git status .
