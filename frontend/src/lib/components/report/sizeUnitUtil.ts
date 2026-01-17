// copied from the v2 branch and adjusted for TS

//https://stackoverflow.com/a/39906526 with some changes - confusing xB with xiB kinda triggers me
const units = ['bytes', 'KiB', 'MiB', 'GiB', 'TiB', 'PiB', 'EiB', 'ZiB', 'YiB'];

export function convertSize(x: number): string {
	let l = 0;

	while (x >= 1024 && ++l) {
		x = x / 1024;
	}

	return x.toFixed(x < 10 && l > 0 ? 1 : 0) + ' ' + units[l];
}
