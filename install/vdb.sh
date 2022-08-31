#!/bin/bash
# vyhodb start script 

clear

# Changes current directory
OLD_DIR=$(pwd)
VDB_HOME=$(dirname $0)
cd $VDB_HOME

# Sets JRE_HOME
. bin-sh/set-env.sh

# Starts vyhodb
if [ -f $JRE_HOME/bin/java ]
then
	export CLASSPATH=$CLASSPATH:./lib/*:./services/*
	"$JRE_HOME/bin/java" -server -XX:CompileThreshold=1 com.vyhodb.server.Standalone $@
else
	. bin-sh/no-env.sh
fi

# Restores current directory
cd $OLD_DIR
