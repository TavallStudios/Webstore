# KubeVirt Host Profiles

These scripts configure a Kubernetes node and the platform env for the two supported tenant-runtime topologies:

- `DEDICATED_KUBEVIRT` targets a node pool with hardware virtualization exposed as allocatable `/dev/kvm`.
- `NESTED_KUBEVIRT` targets a guest-node pool where tenant VMs either get nested `/dev/kvm` or, on x86_64 only, run under KubeVirt software emulation.

The platform control plane uses:

- `PLATFORM_KUBEVIRT_INFRASTRUCTURE_PROFILE` to decide which runtime contract a tenant site should target by default.
- `PLATFORM_KUBEVIRT_DEDICATED_NODE_SELECTOR` and `PLATFORM_KUBEVIRT_NESTED_NODE_SELECTOR` to steer VM scheduling to the right node pool.

The companion scripts are:

- `detect-kubevirt-host-profile.sh`, which inspects the current host, node, and KubeVirt settings and prints the recommended platform env values.
- `configure-kubevirt-node.sh`, which labels the selected node, toggles KubeVirt `useEmulation` when needed, and prints the env block to drop into `platform-webview.env`.

Typical flow:

```bash
ops/kubevirt/detect-kubevirt-host-profile.sh
ops/kubevirt/configure-kubevirt-node.sh --profile nested
```
