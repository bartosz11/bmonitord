//https://stackoverflow.com/a/39906526 with some changes - confusing xB with xiB kinda triggers me
const units = ["bytes", "KiB", "MiB", "GiB", "TiB", "PiB", "EiB", "ZiB", "YiB"];

export function convertSize(x) {
  let l = 0;
  let n = parseInt(x, 10) || 0;

  while (n >= 1024 && ++l) {
    n = n / 1024;
  }

  return n.toFixed(n < 10 && l > 0 ? 1 : 0) + " " + units[l];
}
