#!/usr/bin/env bash
set -euo pipefail

KUBEVIRT_NAMESPACE="${KUBEVIRT_NAMESPACE:-kubevirt}"
KUBEVIRT_RESOURCE_NAME="${KUBEVIRT_RESOURCE_NAME:-kubevirt}"
DEDICATED_SELECTOR_VALUE="${DEDICATED_SELECTOR_VALUE:-dedicated-kubevirt}"
NESTED_SELECTOR_VALUE="${NESTED_SELECTOR_VALUE:-nested-kubevirt}"
STRICT_MODE="${STRICT_MODE:-false}"

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
    return 1
  fi
  ${kubectl_bin} "$@"
}

detect_node_name() {
  if [[ -n "${NODE_NAME:-}" ]]; then
    echo "${NODE_NAME}"
    return
  fi
  run_kubectl get nodes -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo ""
}

arch="$(uname -m)"
virtualization="$(systemd-detect-virt 2>/dev/null || echo none)"
node_name="$(detect_node_name)"
has_kvm="false"
if [[ -e /dev/kvm ]]; then
  has_kvm="true"
fi

emulation_enabled="unknown"
if run_kubectl get kubevirt "${KUBEVIRT_RESOURCE_NAME}" -n "${KUBEVIRT_NAMESPACE}" >/dev/null 2>&1; then
  emulation_enabled="$(run_kubectl get kubevirt "${KUBEVIRT_RESOURCE_NAME}" -n "${KUBEVIRT_NAMESPACE}" -o jsonpath='{.spec.configuration.developerConfiguration.useEmulation}' 2>/dev/null || true)"
  emulation_enabled="${emulation_enabled:-false}"
fi

recommended_profile="DEDICATED_KUBEVIRT"
selector_value="${DEDICATED_SELECTOR_VALUE}"
compatible="true"
reason="Host exposes /dev/kvm directly, so dedicated KubeVirt placement is appropriate."

if [[ "${virtualization}" != "none" ]]; then
  recommended_profile="NESTED_KUBEVIRT"
  selector_value="${NESTED_SELECTOR_VALUE}"
  if [[ "${has_kvm}" == "true" ]]; then
    reason="Nested host still exposes /dev/kvm, so nested KubeVirt can use hardware acceleration."
  elif [[ "${arch}" == "x86_64" || "${arch}" == "amd64" ]]; then
    reason="Nested host does not expose /dev/kvm. Nested KubeVirt needs software emulation enabled on an x86_64 node pool."
  else
    compatible="false"
    reason="Nested arm64 host does not expose /dev/kvm. KubeVirt software emulation is not a supported tenant runtime target on arm64."
  fi
fi

cat <<EOF
Host architecture: ${arch}
Host virtualization: ${virtualization}
Detected node: ${node_name:-unknown}
/dev/kvm exposed: ${has_kvm}
KubeVirt useEmulation: ${emulation_enabled}
Recommended platform profile: ${recommended_profile}
Recommended node selector value: ${selector_value}
Compatibility verdict: ${compatible}
Reason: ${reason}

Suggested platform env:
PLATFORM_KUBEVIRT_INFRASTRUCTURE_PROFILE=${recommended_profile}
PLATFORM_KUBEVIRT_DEDICATED_NODE_SELECTOR=platform.tavall/runtime-profile=${DEDICATED_SELECTOR_VALUE}
PLATFORM_KUBEVIRT_NESTED_NODE_SELECTOR=platform.tavall/runtime-profile=${NESTED_SELECTOR_VALUE}
EOF

if [[ "${STRICT_MODE}" == "true" && "${compatible}" != "true" ]]; then
  exit 1
fi
