Service Directory Docker Image
==============================
Description
-----------
This repository contains a Dockerfile for creating a Docker image that runs a SD (Service Directory) instance.

Usage
-----
There are parameters that can be used to configure Service Directory (with defaults listed):

```
    --mode STANDALONE        start SD in standalone mode "
    --mode HA                start SD in HA mode"
    --my_id        <1..255>      number between 1 to 255"
    --cluster_name <name>        the name of SD cluster."
    --cluster_size <3..255>      nubmer between 3 to 255. normally the odd numbers like 3, 5, 7 ..."
    --sd_home <path>         the home path where SD is installed. \"/opt/cisco/sd\" as default. "
    --sd_share <path>        the shared data path which shared between SD constainers \"/sd_data\" as the default. "
    --watch_ip               start ip watcher for looking up ip changes, it disabled by default."
```

An example docker run command is:

```
docker run -dt engci-docker.cisco.com:5008/sd:<version>
```

NOTE: replace 'version' with relevant tag label.

Image Repository Location
-------------------------
The image can be found here: <site>:<port>/sd

