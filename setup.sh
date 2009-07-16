# !/bin/sh

echo "Setting up Environment"
echo "============================"

if [ -f profiles.xml ]; then
   cat <<EOT

profiles.xml already exist. Please remove it before running setup.sh

(If you are used to the former usage of (". ./setup.sh") please note that
the usage has changed in favor of using a vanilla maven installation with
profiles. Normally you dont have to call setup.sh a second time.
Refer to the documentation on cmas.consol.de for more information)

EOT
else
   PROJECT_HOME=`pwd`
   echo "Copying profiles.xml.template --> profiles.xml"
   echo "Using ${PROJECT_HOME} as PROJECT_HOME"

   cat profiles.xml.template | sed "s|\[\[PROJECT_HOME\]\]|${PROJECT_HOME}|" > profiles.xml
fi
