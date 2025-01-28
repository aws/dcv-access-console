# Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
# SPDX-License-Identifier: Apache-2.0

SAVE_DIRECTORY=$1
CERT_PASSWORD=$2
WEBCLIENT_ADDRESS=$3

ROOT_CA_PATH=$SAVE_DIRECTORY/rootCA.pem
ROOT_CA_KEY_PATH=$SAVE_DIRECTORY/rootCA.key
SERVER_CERT_PATH=$SAVE_DIRECTORY/server.csr
SERVER_KEY_PATH=$SAVE_DIRECTORY/server.key
SERVER_SIGNED_CERT_PATH=$SAVE_DIRECTORY/server.pem
KEYSTORE_PATH=$SAVE_DIRECTORY/keystore.p12

mkdir -p $SAVE_DIRECTORY
cd $SAVE_DIRECTORY

cert_subj="/CN=$WEBCLIENT_ADDRESS"

openssl req -new -x509 -nodes -newkey rsa:2048 -out $ROOT_CA_PATH -keyout $ROOT_CA_KEY_PATH -subj "$cert_subj" -days 1825
openssl req -new -sha256 -nodes -newkey rsa:2048 -out $SERVER_CERT_PATH -keyout $SERVER_KEY_PATH -passout pass:$CERT_PASSWORD -subj "$cert_subj"
openssl x509 -req -sha256 -in $SERVER_CERT_PATH -CA $ROOT_CA_PATH -CAkey $ROOT_CA_KEY_PATH -CAcreateserial -out $SERVER_SIGNED_CERT_PATH -days 1825

openssl pkcs12 -export -nodes -in $SERVER_SIGNED_CERT_PATH -inkey $SERVER_KEY_PATH -out $KEYSTORE_PATH -name server -passin pass:$CERT_PASSWORD -password pass:$CERT_PASSWORD

keytool -delete -alias rootca -cacerts -storepass $CERT_PASSWORD -noprompt > /dev/null
keytool -delete -alias server -cacerts -storepass $CERT_PASSWORD -noprompt > /dev/null

keytool -import -alias rootca -cacerts -storepass $CERT_PASSWORD -file $ROOT_CA_PATH -noprompt
keytool -import -alias server -cacerts -storepass $CERT_PASSWORD -file $SERVER_SIGNED_CERT_PATH -noprompt
