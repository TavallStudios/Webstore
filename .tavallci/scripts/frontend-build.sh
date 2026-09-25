#!/bin/sh
set -eu
node --version
npm --version
npm ci
npm run build
