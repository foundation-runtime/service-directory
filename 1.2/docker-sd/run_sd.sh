#!/usr/bin/env bash

SD_SHARE=
SD_HOME=
SD_CONF=
IP_WATCHER=

function start_standalone(){
    echo "start SD mode=STANDALONE"
    service sdd start
    tail -f ${SD_HOME}/log/server.log
}
function start_ha(){
    local my_id=$1
    local cluster_name=$2
    local cluster_size=$3
    local watch_ip=$4
    echo "start SD mode=HA, my_id=$my_id, cluster_name=$cluster_name, cluster_size=$cluster_size, watch_ip=$watch_ip"
    create_cluser_if_need $cluster_name
    remove_ip_ok $my_id $cluster_name
    set_ip_ok $my_id $cluster_name
    wait_for_all_ip_ok $cluster_name $cluster_size
    echo "my_id=$my_id is ready to start"
    local quorum=$(get_quorum_string $cluster_name $cluster_size)
    if [ -z $quorum ];then
       echo "Error when get quorum string"
       exit -1
    fi
    echo "DEBUG: myID=$my_id quorum=$quorum"
    echo "com.cisco.oss.foundation.directory.server.deploy.mode=REPLICATED" > ${SD_CONF}
    echo "com.cisco.oss.foundation.directory.zookeeper.server.id=$my_id" >> ${SD_CONF}
    echo "com.cisco.oss.foundation.directory.zookeeper.cluster.servers=$quorum" >> ${SD_CONF}
    echo "com.cisco.oss.foundation.directory.zookeeper.client.port=2181" >> ${SD_CONF}

    # start SD service
    service sdd start

    # start ip change watcher
    if [ ! -f ./${IP_WATCHER} ]; then
        echo "ERROR: $IP_WATCHER not found. exit."
        exit -1
    fi

    if [ ${watch_ip} -eq 1 ]; then
        . ./${IP_WATCHER} ${my_id} ${cluster_name} ${cluster_size} &
    fi

    while [[ ! -f ${SD_HOME}/log/server.log ]]; do
        sleep 1
    done
    tail -F ${SD_HOME}/log/server.log

}

function create_cluser_if_need() {
    local cluster_name=$1
    if [ ! -d "$SD_SHARE/$cluster_name" ]; then
        echo "create folder $SD_SHARE/$cluster_name"
        mkdir $SD_SHARE/$cluster_name
    fi
}

function set_ip_ok(){
    local my_id=$1
    local cluster=$2
    local my_ip=$(cat /etc/hosts|grep $HOSTNAME|awk '{print $1}')
    if [ $? -eq 0 ] && [ ! -z "$my_ip" ]; then
        echo "my_id=$my_id, my_ip=$my_ip"
        echo "$my_ip" > $SD_SHARE/$cluster/$my_id".ip.ok"
    fi
}

function remove_ip_ok(){
    local my_id=$1
    local cluster=$2
    if [ -f $SD_SHARE/$cluster/$my_id".ip.ok" ];then
       rm -f $SD_SHARE/$cluster/$my_id".ip.ok"
    fi
}

function wait_for_all_ip_ok(){
    local MAX_WAIT=60
    local wait=0
    local cluster=$1
    local size=$2
    echo -n "waiting for all $size nodes' ip avaiable "
    local state="unavilable"
    while [[ "$state"  != "ok" ]]
    do
        count=$(ls -l $SD_SHARE/$cluster/*.ip.ok|wc -l 2>/dev/null)  #>/dev/null 2>&1
        #echo "count=$count"
        if [ $count -eq $size ];then
            state="ok"
        elif [[ $wait -gt $MAX_WAIT ]];then
            echo "" >&2
            echo "exit after waiting more than $MAX_WAIT seconds"
            exit -1
        else
            sleep 1
            let wait=wait+1
            echo -n "."
        fi
    done
    echo "" >&2
}

function get_quorum_string(){
    local name=$1
    local count=$2
    local str=""
    for node in `seq $count` ;do
        local ip=$(cat $SD_SHARE/$name/$node.ip.ok)
        str=$(echo $str"server."$node:$ip:2888:3888)
        if [ ! $node -eq $count ]; then
            str=$(echo $str";")
        fi
    done
    echo $str
}
function error_bad_arg(){
    local arg=$1
    local input=$2
    if [ -z ${input} ]; then
        input="null"
    fi
    echo "ERROR: invaild input [$input] for argment [$arg]"
    help_main
    exit -1
}

function is_number(){
    case $1 in
        ''|*[!0-9]*)
            return 1
            ;;
        *)  return 0
            ;;
    esac
}

function help_main(){
    local me=`basename $0`
    echo "Usage: $me [-h|--help] [ARGS...]"
    echo "    -h, --help              Print this help."
    echo "Start Service Directory Docker Containter... "
    echo "  --mode STANDALONE        start SD in standalone mode "
    echo "  --mode HA                start SD in HA mode"
    echo "    --my_id        <1..255>      number between 1 to 255"
    echo "    --cluster_name <name>        the name of SD cluster."
    echo "    --cluster_size <3..255>      nubmer between 3 to 255. normally the odd numbers like 3, 5, 7 ..."
    echo "  --sd_home <path>         the home path where SD is installed. \"/opt/cisco/sd\" as default. "
    echo "  --sd_share <path>        the shared data path which shared between SD constainers \"/sd_data\" as the default. "
    echo "  --watch_ip               start ip watcher for looking up ip changes, it disabled by default."
    echo
}


function main(){

    local mode=""
    local my_id=""
    local cluster_name=""
    local cluster_size=""
    #don't start ip watcher by default
    local watch_ip=0

    while [[ ! -z "$1" ]];do
        case "$1" in
            --mode)
                shift
                if [[ "$1" == "HA" ]] || [[ "$1" == "STANDALONE" ]]; then
                    mode="$1"
                    shift
                else
                    error_bad_arg "--mode" "$1"
                fi
                ;;
            --my_id)
                shift
                if [[ ! -z "$1" ]] && is_number "$1" ; then
                    my_id=$1
                    shift
                else
                    error_bad_arg "--my_id" $1
                fi
                ;;
            --cluster_name)
                shift
                if [[ ! -z "$1" ]]; then
                    cluster_name=$1
                    shift
                else
                    error_bad_arg "--cluster_name" $1
                fi
                ;;
            --cluster_size)
                shift
                if [[ ! -z "$1" ]] && is_number $1; then
                    cluster_size=$1
                    shift
                else
                error_bad_arg "--cluster_size" $1
                fi
                ;;
            --sd_home)
                shift
                if [[ ! -z "$1" ]]; then
                    SD_HOME=$1
                    shift
                else
                    error_bad_arg "--sd_home" $1
                fi
                ;;
            --sd_share)
                shift
                if [[ ! -z "$1" ]]; then
                    SD_SHARE=$1
                    shift
                else
                    error_bad_arg "--sd_share" $1
                fi
                ;;
            --watch_ip)
                shift
                watch_ip=1
                ;;
            -h|--help)
                help_main
                exit 0
                ;;
            **)
                error_bad_arg "UNKNOWN" $1
                ;;
        esac
    done

    if [ -z $SD_SHARE ]; then
        #SD_SHARE=$SD_SMOKETEST_DOCKER_DATA_VOLUME
        SD_SHARE=/sd_data
    fi
    if [ -z $SD_HOME ]; then
        SD_HOME=/opt/cisco/sd
    fi

    SD_CONF=${SD_HOME}/etc/config.properties

    IP_WATCHER=sd-ipchange-watcher.sh

    echo "DEBUG : SD_HOME  = $SD_HOME"
    echo "DEBUG : SD_SHARE = $SD_SHARE"
    echo "DEBUG : SD_CONF  = $SD_CONF"
    echo "DEBUG : IP_WATCHER = $IP_WATCHER"
    if [[ $mode == "STANDALONE" ]]; then
        echo "DEBUG: opts : --mode $mode"
        start_standalone
    else
        if [[ -z $my_id ]] || [[ -z $cluster_name ]] || [[ -z $cluster_size ]]; then
            help_main
            exit 1
        fi
        echo "DEBUG: opts : --mode $mode --my_id $my_id --cluster_name $cluster_name --cluster_size $cluster_size --watch_ip $watch_ip"
        start_ha $my_id $cluster_name $cluster_size $watch_ip
    fi
}

if [[ -z "$@" ]]; then
    help_main
else
    main "$@"
fi