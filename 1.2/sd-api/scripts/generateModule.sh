#!/bin/bash
root_dir=${1}
module_dir=${root_dir}/modules
module_file=${module_dir}/module.xml
lib_dir=${root_dir}/lib
if [ -d "$module_dir" ]; then
  rm -rf $module_dir
fi
mkdir -p $module_dir

if [ -f ${module_file} ]; then
  rm -f ${module_file}
fi

echo '<module xmlns="urn:jboss:module:1.1" name="com.cisco.oss.foundation.directory" slot="main">'  > $module_file 
echo '  <resources>' >> $module_file
for jar in $(ls $lib_dir); do
   echo "    <resource-root path=\"$jar\" />" >> $module_file
   cp ${lib_dir}/$jar ${module_dir}
done
echo '  </resources>' >> $module_file
echo '</module>' >> $module_file
