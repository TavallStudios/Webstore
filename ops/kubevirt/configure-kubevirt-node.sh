#!/usr/bin/env bash
set -euo pipefail

PROFILE=""
NODE_NAME="${NODE_NAME:-}"
KUBEVIRT_NAMESPACE="${KUBEVIRT_NAMESPACE:-kubevirt}"
KUBEVIRT_RESOURCE_NAME="${KUBEVIRT_RESOURCE_NAME:-kubevirt}"
DEDICATED_SELECTOR_VALUE="${DEDICATED_SELECTOR_VALUE:-dedicated-kubevirt}"
NESTED_SELECTOR_VALUE="${NESTED_SELECTOR_VALUE:-nested-kubevirt}"
FORCE_EMULATION="${FORCE_EMULATION:-}"

usage() {
  cat <<'EOF'
Usage: configure-kubevirt-node.sh --profile dedicated|nested [--node NODE_NAME] [--enable-emulation|--disable-emulation]
EOF
}

resolve_kubectl() {
  if [[ -n "${KUBECTL_BIN:-}" ]]; then
    echo "${KUBECTL_BIN}"
    return
  fi
  if command -v kubectl >/dev/null 2>&1; then
    echo "kubectl"
    return
  fi
  if command -v k3s >/dev/null 2>&1; then
    echo "sudo k3s kubectl"
    return
  fi
  echo ""
}

run_kubectl() {
  local kubectl_bin
  kubectl_bin="$(resolve_kubectl)"
  if [[ -z "${kubectl_bin}" ]]; then
    echo "kubectl or k3s kubectl is required on the target host." >&2
    exit 1
  fi
  ${kubectl_bin} "$@"
}

secure_kubeconfig() {
  if [[ -n "${KUBECONFIG:-}" && -f "${KUBECONFIG}" ]]; then
    chmod 600 "${KUBECONFIG}"
  fi
}

detect_single_node() {
  run_kubectl get nodes -o jsonpath='{.items[0].metadata.name}'
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --profile)
      PROFILE="$2"
      shift 2
      ;;
    --node)
      NODE_NAME="$2"
      shift 2
      ;;
    --enable-emulation)
      FORCE_EMULATION="true"
      shift
      ;;
    --disable-emulation)
      FORCE_EMULATION="false"
      shift
      ;;
    --help|-h)
      usage
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      exit 1
      ;;
  esac
done

if [[ -z "${PROFILE}" ]]; then
  echo "--profile is required." >&2
  usage
  exit 1
fi

case "${PROFILE}" in
  dedicated|DEDICATED|DEDICATED_KUBEVIRT)
    profile_env="DEDICATED_KUBEVIRT"
    selector_value="${DEDICATED_SELECTOR_VALUE}"
    ;;
  nested|NESTED|NESTED_KUBEVIRT)
    profile_env="NESTED_KUBEVIRT"
    selector_value="${NESTED_SELECTOR_VALUE}"
    ;;
  *)
    echo "Unsupported profile: ${PROFILE}" >&2
    exit 1
    ;;
esac

secure_kubeconfig

if [[ -z "${NODE_NAME}" ]]; then
  NODE_NAME="$(detect_single_node)"
fi

arch="$(uname -m)"
virtualization="$(systemd-detect-virt 2>/dev/null || echo none)"
has_kvm="false"
if [[ -e /dev/kvm ]]; then
  has_kvm="true"
fi

emulation_enabled="false"
if [[ -n "${FORCE_EMULATION}" ]]; then
  emulation_enabled="${FORCE_EMULATION}"
elif [[ "${profile_env}" == "NESTED_KUBEVIRT" && "${has_kvm}" != "true" ]]; then
  if [[ "${arch}" == "x86_64" || "${arch}" == "amd64" ]]; then
    emulation_enabled="true"
  else
    echo "Nested arm64 host without /dev/kvm is not a supported KubeVirt tenant target." >&2
    exit 1
  fi
fi

if [[ "${profile_env}" == "DEDICATED_KUBEVIRT" && "${has_kvm}" != "true" ]]; then
  echo "Dedicated KubeVirt profile requires allocatable /dev/kvm on the node." >&2
  exit 1
fi

run_kubectl label node "${NODE_NAME}" platform.tavall/runtime-profile="${selector_value}" --overwrite
run_kubectl patch kubevirt "${KUBEVIRT_RESOURCE_NAME}" \
  -n "${KUBEVIRT_NAMESPACE}" \
  --type merge \
  -p "{\"spec\":{\"configuration\":{\"developerConfiguration\":{\"useEmulation\":${emulation_enabled}}}}}"

cat <<EOF
Configured node ${NODE_NAME} for ${profile_env}.
Host virtualization: ${virtualization}
Host architecture: ${arch}
/dev/kvm exposed: ${has_kvm}
KubeVirt useEmulation set to: ${emulation_enabled}

Set these values in platform-webview.env:
PLATFORM_KUBEVIRT_INFRASTRUCTURE_PROFILE=${profile_env}
PLATFORM_KUBEVIRT_DEDICATED_NODE_SELECTOR=platform.tavall/runtime-profile=${DEDICATED_SELECTOR_VALUE}
PLATFORM_KUBEVIRT_NESTED_NODE_SELECTOR=platform.tavall/runtime-profile=${NESTED_SELECTOR_VALUE}
EOF
