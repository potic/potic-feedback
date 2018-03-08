#!/usr/bin/env sh

##############################################################################
##
##  Stop and kill currently running docker image, pull newest version and
##  run it.
##
##############################################################################

warn ( ) {
    echo "$*"
}

warn "Currently running docker images"
docker ps -a

warn "Killing currently running docker image..."
docker kill potic-feedback; docker rm potic-feedback

warn "Pulling latest docker image..."
docker pull potic/potic-feedback:$TAG_TO_DEPLOY

warn "Starting docker image..."
docker run -dit --name potic-feedback --restart on-failure --link potic-articles -e LOG_PATH=/mnt/logs -v /mnt/logs:/mnt/logs -e LOGZIO_TOKEN=$LOGZIO_TOKEN -p 40411:8080 potic/potic-feedback:$TAG_TO_DEPLOY

warn "Currently running docker images"
docker ps -a
