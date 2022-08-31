#!/bin/bash

VDB_BIN=$(dirname $0)

# Sets JRE_HOME
. $VDB_BIN/set-env.sh

# Starts command line utility
if [ -f $JRE_HOME/bin/java ]
then
	export CLASSPATH=$CLASSPATH:$VDB_BIN/../lib/*
	"$JRE_HOME/bin/java" com.vyhodb.admin.clu.Shrink $@
else
	. $VDB_BIN/no-env.sh
fi
