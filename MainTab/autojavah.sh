#!/bin/sh
export ProjectPath=$(cd "/$(dirname "$1")"; pwd)
export TargetClassName="com.maintab.JniString"

export SourceFile="${ProjectPath}/src/com/maintab"
export TargetPath="${ProjectPath}/src/main/jni"

cd "${SourceFile}"
javah -d ${TargetPath} -classpath "${SourceFile}" "${TargetClassName}"
echo -d ${TargetPath} -classpath "${SourceFile}" "${TargetClassName}"
