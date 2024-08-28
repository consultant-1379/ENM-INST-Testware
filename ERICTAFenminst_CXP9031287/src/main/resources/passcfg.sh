#!/bin/bash
LITP_ADMIN_PASSWD=${1:-12shroot}
echo -e "\nLitp-admin passwd will be set to - [${LITP_ADMIN_PASSWD}] on all node(s)\n"
shift
ROOT_PASSWD=${1:-12shroot}
echo -e "Root passwd will be set to - [${ROOT_PASSWD}] on all node(s)\n"
prep_node_manifest()
{
    file=$1
    while :
    do
           bef=$(/usr/bin/wc -l ${file} | /bin/awk '{print $1}')
           /usr/bin/perl -i -pe "chomp if eof" ${file}
           aft=$(/usr/bin/wc -l ${file} | /bin/awk '{print $1}')
           if [[ ${bef} -eq ${aft} ]]; then
                         break
           fi
    done
    /usr/bin/head -n -1 ${file} > ${file}.ppp 2> /dev/null
    /bin/mv ${file}.ppp ${file} 2> /dev/null
}


mk_file_res () {
    echo -e "\nfile { \"/tmp/pw_reset\":"
    echo " content => \"[[ ! -f /etc/init.d/puppetmaster ]] && { echo root:${ROOT_PASSWD} | /usr/sbin/chpasswd; echo litp-admin:${LITP_ADMIN_PASSWD} | /usr/sbin/chpasswd; }\n\","
    echo -e "}\n"
}

mk_exec_res () {
    echo -e "\nexec { \"reset_pwd\":"
    echo -e "\ncommand => 'bash /tmp/pw_reset',"
    echo -e "\nprovider => shell"
    echo -e "}\n"
}

cleanUp() {
    echo "Almost done."
    cp -p ${NODE_PP}.$$ ${NODE_PP} > /dev/null 2>&1
    exit 9
}

rm -f /tmp/pw_reset 2> /dev/null
NODE_PP="/opt/ericsson/nms/litp/etc/puppet/modules/litp/manifests/common_node.pp"
cp -p ${NODE_PP}{,.$$}
trap cleanUp INT TERM
prep_node_manifest ${NODE_PP}
mk_file_res >> ${NODE_PP}
mk_exec_res >> ${NODE_PP}
echo -e "\n}\n" >> ${NODE_PP}
/usr/bin/puppet parser validate ${NODE_PP}
mco  service puppet stop -y > /dev/null 2>&1;
mco  service puppet start -y > /dev/null 2>&1;
sleep 30;
/usr/bin/mco puppet runall 10
echo -e "Please wait while resetting the password on all the node(s)."
sleep 60
/usr/bin/mco puppet runall 10 > /dev/null 2>&1
sleep 150
cp -p ${NODE_PP}.$$ ${NODE_PP} > /dev/null 2>&1
echo -e "\nDone."
