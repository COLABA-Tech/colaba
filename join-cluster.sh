#!/bin/bash
set -e

echo "Waiting for leader node rabbit@rabbitmq1 to start..."
until rabbitmqctl -n rabbit@rabbitmq1 status >/dev/null 2>&1; do
  sleep 5
done

echo "Waiting for local node to start..."
until rabbitmqctl status >/dev/null 2>&1; do
  sleep 5
done

echo "Joining cluster via rabbitmq1"
rabbitmqctl stop_app
rabbitmqctl reset
rabbitmqctl join_cluster rabbit@rabbitmq1
rabbitmqctl start_app