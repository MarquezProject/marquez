#!/bin/sh

# Author: Tim Brody <T.D.Brody@soton.ac.uk>
# Date: 2015-02-11
#
# Retrieve the signing certificate from an ADFS instance in PEM format.

ADFS_SERVER=$1

if [ "$#" -ne "1" ]; then
  echo "Usage: $0 <adfs server URL>"
  exit 1
fi

URL=$ADFS_SERVER/FederationMetadata/2007-06/FederationMetadata.xml
TEMPFILE=$(mktemp)

if [[ $(command -v wget) ]]; then
    wget --no-check-certificate -q -O $TEMPFILE $URL
elif [[ $(command -v curl) ]]; then
        curl -sk $URL -o $TEMPFILE
    else
        echo "Neither curl or wget was found"
        exit 127
fi

if [ $? -ne 0 ]; then
  echo "Error requesting $URL"
  exit 1
fi

echo "-----BEGIN CERTIFICATE-----"
(xmllint --shell $TEMPFILE | grep -v '^/ >' | grep -v '^ ----' | fold -w 64) << EndOfScript
setns a=urn:oasis:names:tc:SAML:2.0:metadata
setns b=http://www.w3.org/2000/09/xmldsig#
cat /a:EntityDescriptor/b:Signature/b:KeyInfo/b:X509Data/b:X509Certificate/text()
EndOfScript
echo "-----END CERTIFICATE-----"

unlink $TEMPFILE
