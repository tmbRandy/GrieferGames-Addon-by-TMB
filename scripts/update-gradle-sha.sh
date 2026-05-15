#!/usr/bin/env bash
# Pins distributionSha256Sum in gradle-wrapper.properties to the official
# checksum published by Gradle for the current distributionUrl.
set -euo pipefail

PROPS="$(dirname "$0")/../gradle/wrapper/gradle-wrapper.properties"

url="$(grep -E '^distributionUrl=' "$PROPS" | cut -d= -f2- | sed 's|\\:|:|g')"
[ -n "$url" ] || { echo "distributionUrl not found in $PROPS" >&2; exit 1; }

sha="$(curl -fsSL "${url}.sha256")"
[ -n "$sha" ] || { echo "failed to fetch ${url}.sha256" >&2; exit 1; }

if grep -qE '^distributionSha256Sum=' "$PROPS"; then
  sed -i.bak "s|^distributionSha256Sum=.*|distributionSha256Sum=${sha}|" "$PROPS"
  rm -f "${PROPS}.bak"
else
  printf 'distributionSha256Sum=%s\n' "$sha" >> "$PROPS"
fi

echo "pinned $(basename "$url") -> $sha"