import type { ColumnDef } from '@tanstack/table-core';
import { renderComponent } from '$lib/components/ui/data-table/index.js';
import type { ModelOrchestrator } from '$lib/api-client-axios';
import type { Writable } from 'svelte/store';
import TableSortableHeaderButton from '$lib/components/dashboard/TableSortableHeaderButton.svelte';
import OrchestratorActions from '$lib/components/dashboard/admin/orchestrators/OrchestratorActions.svelte';

export const orchestratorColumns: ColumnDef<ModelOrchestrator>[] = [
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
		accessorKey: 'host',
		header: ({ column }) =>
			renderComponent(TableSortableHeaderButton, {
				onclick: column.getToggleSortingHandler(),
				innerText: 'Host'
			})
	},
	{
		accessorKey: 'leader',
		header: ({ column }) =>
			renderComponent(TableSortableHeaderButton, {
				onclick: column.getToggleSortingHandler(),
				innerText: 'Leader'
			}),
		cell: ({ row }) => {
			return row.original.leader ? 'Yes' : 'No';
		}
	},
	{
		id: 'actions',
		header: 'Actions',
		cell: ({ row, table }) => {
			const rows = table.options.meta! as Writable<ModelOrchestrator[]>;
			return renderComponent(OrchestratorActions, { row: row.original, rows: rows });
		}
	}
];
