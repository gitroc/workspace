#!/bin/sh
export ProjectPath=$(cd "../../$(dirname "$2")"; pwd)
export TargetClassName="com.maintab.JniString"

export SourceFile="${ProjectPath}/src"
export TargetPath="${ProjectPath}/jni"

cd "${SourceFile}"
javah -d ${TargetPath} -classpath ${SourceFile} ${TargetClassName}
echo -d ${TargetPath} -classpath ${SourceFile} ${TargetClassName}
