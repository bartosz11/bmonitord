import type { ColumnDef } from '@tanstack/table-core';
import type { ModelHeartbeat, ModelIncident } from '$lib/api-client-axios';
import dayjs from 'dayjs';
import { targetStatusAsText } from '$lib/components/dashboard/targets/utils';
import { humanizeDuration } from '$lib/components/report/timeUtils';
import type { DiskInfo } from '$lib/components/heartbeatUtil';
import { convertSize } from '$lib/components/report/sizeUnitUtil';

export const perLocationColumns: ColumnDef<ModelHeartbeat>[] = [
	{
		accessorKey: 'checker.id',
		header: 'Node',
		cell: ({ row }) => {
			const checker = row.original!.checker!;
			return `${checker.name}${checker.location ? ' - ' : ''}${checker.location ?? ''}`;
		}
	},
	{
		accessorKey: 'status',
		header: 'Status',
		cell: ({ row }) => {
			return targetStatusAsText[row.original.status!];
		}
	},
	{
		accessorKey: 'latency',
		header: 'Latency (ms)',
		cell: ({ row }) => {
			return row.original!.latency;
		}
	},
	{
		accessorKey: 'timestamp',
		header: 'Last check',
		cell: ({ row }) => {
			return dayjs().to(dayjs(row.original.timestamp));
		}
	}
];

export const incidentColumns: ColumnDef<ModelIncident>[] = [
	{
		accessorKey: 'start',
		header: 'Start date',
		cell: ({ row }) => {
			return new Date(row.original.start!).toLocaleString(navigator.language);
		}
	},
	{
		accessorKey: 'end',
		header: 'End date',
		cell: ({ row }) => {
			const endStr = row.original.end;
			if (!endStr) return "Still ongoing";
			const endDate = new Date(endStr);
			if (endDate.getFullYear() === 1) {
				return "Still ongoing";
			}
			return endDate.toLocaleString(navigator.language);
		}
	},
	{
		accessorKey: 'duration',
		header: 'Duration',
		cell: ({ row }) => {
			// I guess the fallback is accurate enough
			const endDate = BigInt(Date.now()) * 1_000_000n;
			const startDate = BigInt(new Date(row.original.start!).getTime()) * 1_000_000n;
			const duration = row.original.ongoing === false ? BigInt(row.original.duration!) : endDate - startDate;
			return humanizeDuration(duration);
		}
	},
	{
		accessorKey: 'cause',
		header: 'Cause',
		cell: ({ row }) => row.original.cause
	}
];

export const diskColumns: ColumnDef<DiskInfo>[] = [
	{
		accessorKey: 'mount',
		header: 'Disk',
	},
	{
		accessorKey: 'used',
		header: 'Used space',
		cell: ({ row }) => {
			return convertSize(row.original.used);
		}
	},
	{
		accessorKey: 'available',
		header: 'Available space',
		cell: ({ row }) => {
			return convertSize(row.original.total - row.original.used);
		}
	},
	{
		accessorKey: 'total',
		header: 'Total space',
		cell: ({ row }) => {
			return convertSize(row.original.total);
		}
	},
];
