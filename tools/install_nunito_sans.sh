#!/usr/bin/env bash
set -euo pipefail

# Official open-source Nunito Sans variable font (Google Fonts).
# License: SIL Open Font License 1.1, see docs/fonts/OFL-NunitoSans.txt
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUT="$ROOT/app/src/main/res/font"
mkdir -p "$OUT"
URL='https://raw.githubusercontent.com/google/fonts/main/ofl/nunitosans/NunitoSans%5BYTLC%2Copsz%2Cwdth%2Cwght%5D.ttf'
curl --fail --location --retry 3 --silent --show-error "$URL" -o "$OUT/nunito_sans.ttf"
test "$(wc -c < "$OUT/nunito_sans.ttf")" -gt 100000
head -c 4 "$OUT/nunito_sans.ttf" | od -An -tx1 | grep -Eq '00 01 00 00|4f 54 54 4f'
echo "Installed official Nunito Sans: $(wc -c < "$OUT/nunito_sans.ttf") bytes"
