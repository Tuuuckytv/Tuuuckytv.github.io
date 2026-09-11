export const UPLOAD_CHUNK_BYTES = 8 * 1024 * 1024;
export function uploadLimit(kind: string) {
  if (kind === "rom") return { bytes: 16 * 1024 ** 3, label: "16 GB" };
  if (["book", "art", "bios"].includes(kind)) return { bytes: 32 * 1024 ** 2, label: "32 MB" };
  return { bytes: 1024 ** 3, label: "1 GB" };
}
