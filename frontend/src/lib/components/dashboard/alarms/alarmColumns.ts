import type { ColumnDef } from '@tanstack/table-core';
import { renderComponent } from '$lib/components/ui/data-table/index.js';
import type { ModelAlarm, ModelNotification, ModelTarget } from '$lib/api-client-axios';
import type { Writable } from 'svelte/store';
import TableSortableHeaderButton from '$lib/components/dashboard/TableSortableHeaderButton.svelte';
import { alarmThresholdFieldMetas } from '$lib/components/dashboard/alarms/thresholdFieldInfo';
import AlarmActions from '$lib/components/dashboard/alarms/AlarmActions.svelte';

export type AlarmTableMeta = {
	alarms: Writable<ModelAlarm[]>;
	notifications: ModelNotification[];
	target: ModelTarget;
};

export const alarmColumns: ColumnDef<ModelAlarm>[] = [
	{
		accessorKey: 'id',
		header: ({ column }) =>
			renderComponent(TableSortableHeaderButton, {
				onclick: column.getToggleSortingHandler(),
				innerText: 'ID'
			})
	},
	{
		accessorKey: 'name',
		header: ({ column }) =>
			renderComponent(TableSortableHeaderButton, {
				onclick: column.getToggleSortingHandler(),
				innerText: 'Name'
			})
	},
	{
		accessorKey: 'trigger',
		header: 'Trigger',
		cell: ({ row }) => {
			return getTriggerDescription(row.original);
		}
	},
	{
		accessorKey: 'status',
		header: 'Status',
		cell: ({ row }) => {
			const alarm = row.original;
			return `${alarm.active ? 'Active' : 'Inactive'}, ${alarm.muted ? 'muted' : 'unmuted'}`;
		}
	},
	{
		id: 'actions',
		header: 'Actions',
		cell: ({ row, table }) => {
			const meta = table.options.meta! as AlarmTableMeta;
			return renderComponent(AlarmActions, { row: row.original, rows: meta.alarms, target: meta.target, notifications: meta.notifications });
		}
	}
];

function getTriggerDescription(alarm: ModelAlarm): string {
	let desc = '';
	switch (alarm.type!) {
		case 0:
			desc = 'When status changes to down';
			break;
		case 1: {
			const meta = alarmThresholdFieldMetas[alarm.thresholdField!];
			desc = `When ${meta.formattedName} goes above ${alarm.threshold}${meta.unit}`;
			break;
		}
	}
	return desc;
}
