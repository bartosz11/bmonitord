import type { ColumnDef } from '@tanstack/table-core';
import type { ModelTarget } from '$lib/api-client-axios';
import { renderComponent } from '$lib/components/ui/data-table';
import TableSortableHeaderButton from '$lib/components/dashboard/TableSortableHeaderButton.svelte';
import { targetTypeNames } from '$lib/components/dashboard/targets/utils';
import TargetStatusCell from '$lib/components/dashboard/targets/TargetStatusCell.svelte';
import type { Writable } from 'svelte/store';
import TargetActions from '$lib/components/dashboard/targets/TargetActions.svelte';

export const targetColumns: ColumnDef<ModelTarget>[] = [
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
		accessorKey: 'type',
		header: ({ column }) =>
			renderComponent(TableSortableHeaderButton, {
				onclick: column.getToggleSortingHandler(),
				innerText: 'Type'
			}),
		cell: ({ row }) => targetTypeNames[row.original.type!]
	},
	{
		accessorKey: 'uptime',
		header: ({ column }) =>
			renderComponent(TableSortableHeaderButton, {
				onclick: column.getToggleSortingHandler(),
				innerText: 'Uptime'
			}),
		cell: ({ row }) => {
			const target = row.original;
			const checksUp = target.checksUp!;
			const checksDown = target.checksDown!;
			// Cannot divide by 0
			if (checksUp + checksDown == 0) return "0%";
			// TODO: maybe format this later
			return (checksUp / (checksDown + checksUp)) * 100 + "%";
		}
	},
	{
		id: 'lastStatus',
		header: 'Last status',
		cell: ({ row }) => renderComponent(TargetStatusCell, { target: row.original })
	},
	{
		id: 'actions',
		header: 'Actions',
		cell: ({ row, table }) => {
			const rows = table.options.meta! as Writable<ModelTarget[]>;
			return renderComponent(TargetActions, { row: row.original, rows: rows });
		}
	}
];
