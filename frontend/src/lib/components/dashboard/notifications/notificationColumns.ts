import type { ColumnDef } from '@tanstack/table-core';
import { renderComponent } from '$lib/components/ui/data-table/index.js';
import type { ModelNotification } from '$lib/api-client-axios';
import type { Writable } from 'svelte/store';
import TableSortableHeaderButton from '$lib/components/dashboard/TableSortableHeaderButton.svelte';
import { notificationTypeNames } from '$lib/components/dashboard/notifications/utils';
import NotificationActions from '$lib/components/dashboard/notifications/NotificationActions.svelte';

export const notificationColumns: ColumnDef<ModelNotification>[] = [
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
		cell: ({row}) => {
			return notificationTypeNames[row.original.type!]
		}
	},
	{
		id: 'actions',
		header: 'Actions',
		cell: ({ row, table }) => {
			const rows = table.options.meta! as Writable<ModelNotification[]>;
			return renderComponent(NotificationActions, { row: row.original, rows: rows });
		}
	}
];
