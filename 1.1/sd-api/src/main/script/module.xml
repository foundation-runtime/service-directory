#!/bin/bash
file=$1
lib=./lib
output=./lib/module.xml
list=`ls $lib`
echo '<module xmlns="urn:jboss:module:1.1" name="com.cisco.oss.foundation.directory">' > $output
echo '<resources>' >> $output

for f in $list
do
  echo "<resource-root path=\"$f\" />" >> $output
done

echo '</resources>' >> $output
echo '</module>' >> $output

cd $lib
jar cvf ../${file} *
cd ..
