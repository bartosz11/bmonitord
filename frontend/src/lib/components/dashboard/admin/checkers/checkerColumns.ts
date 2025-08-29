import type { ColumnDef } from '@tanstack/table-core';
import { renderComponent } from '$lib/components/ui/data-table/index.js';
import type { ModelChecker } from '$lib/api-client-axios';
import CheckerActions from '$lib/components/dashboard/admin/checkers/CheckerActions.svelte';
import type { Writable } from 'svelte/store';
import TableSortableHeaderButton from '$lib/components/dashboard/TableSortableHeaderButton.svelte';

export const checkerColumns: ColumnDef<ModelChecker>[] = [
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
		accessorKey: 'location',
		header: ({ column }) =>
			renderComponent(TableSortableHeaderButton, {
				onclick: column.getToggleSortingHandler(),
				innerText: 'Location'
			})
	},
	{
		id: 'actions',
		header: 'Actions',
		cell: ({ row, table }) => {
			const modelCheckerTableMeta = table.options.meta! as Writable<ModelChecker[]>;
			return renderComponent(CheckerActions, { row: row.original, rows: modelCheckerTableMeta });
		}
	}
];
