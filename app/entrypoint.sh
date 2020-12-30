#!/bin/bash
passwd -d root
ssh-keygen -A
mkdir -p ~/.ssh
chmod 0700 ~/.ssh
echo $SSH_PUBLIC_KEY > ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys
/usr/sbin/sshd
java -jar /app.jar
