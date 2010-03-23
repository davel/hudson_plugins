#!/bin/bash

rm -rf $HUDSON_HOME/plugins/analysis-*
rm -rf $HUDSON_HOME/plugins/checkstyle*
rm -rf $HUDSON_HOME/plugins/dry*
rm -rf $HUDSON_HOME/plugins/findbugs*
rm -rf $HUDSON_HOME/plugins/pmd*
rm -rf $HUDSON_HOME/plugins/tasks*
rm -rf $HUDSON_HOME/plugins/warnings*

mvn install

cd ../
cp */target/*.hpi $HUDSON_HOME/plugins

cd $HUDSON_HOME
java -jar hudson.war


