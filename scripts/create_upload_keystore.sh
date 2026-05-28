#!/usr/bin/env bash
set -euo pipefail
keytool -genkeypair \
  -v \
  -keystore byqezi-upload.jks \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias byqezi_upload \
  -storepass "${1:-CHANGE_ME_STRONG_PASSWORD}" \
  -keypass "${2:-CHANGE_ME_STRONG_PASSWORD}" \
  -dname "CN=BYQEZI.AZ, OU=Android, O=BYQEZI, L=Baku, S=Baku, C=AZ"
echo "Created byqezi-upload.jks"
echo "Base64 for GitHub secret:"
base64 -w 0 byqezi-upload.jks
