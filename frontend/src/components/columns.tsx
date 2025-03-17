import type { ColumnDef } from "@tanstack/react-table";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Download, Trash } from "lucide-react";

export type PrivateKey = {
  id: string;
  name: string;
  created: string;
  status: "active" | "expired" | "revoked";
  attributes: string[];
  lastUsed: string | null;
};

export const columns: ColumnDef<PrivateKey>[] = [
  {
    accessorKey: "name",
    header: "Name",
    cell: ({ row }) => (
      <div className="font-medium">{row.getValue("name")}</div>
    ),
  },
  {
    accessorKey: "created",
    header: "Created",
    cell: ({ row }) => {
      const date = row.getValue("created") as string;
      return <div>{date}</div>;
    },
  },
  {
    accessorKey: "attributes",
    header: "Attributes",
    cell: ({ row }) => {
      const attributes = row.getValue("attributes") as string[];
      return (
        <div className="flex flex-wrap gap-1">
          {attributes.map((attr) => (
            <Badge key={attr} variant="outline" className="text-xs">
              {attr}
            </Badge>
          ))}
        </div>
      );
    },
  },
  {
    accessorKey: "lastUsed",
    header: "Last Used",
    cell: ({ row }) => {
      const lastUsed = row.getValue("lastUsed") as string | null;
      return <div>{lastUsed || "Never"}</div>;
    },
  },
  {
    accessorKey: "status",
    header: "Status",
    cell: ({ row }) => {
      const status = row.getValue("status") as string;
      return (
        <Badge
          className={
            status === "active"
              ? "bg-green-100 text-green-800 hover:bg-green-100"
              : status === "expired"
              ? "bg-amber-100 text-amber-800 hover:bg-amber-100"
              : "bg-red-100 text-red-800 hover:bg-red-100"
          }
        >
          {status}
        </Badge>
      );
    },
  },
  {
    id: "actions",
    cell: ({ row, table }) => {
      const keyId = row.original.id;

      // Access the custom props passed to the DataTable component
      const { onDownload, onDelete } = table.options.meta as {
        onDownload: (id: string) => void;
        onDelete: (id: string) => void;
      };

      return (
        <div className="flex items-center justify-end gap-2">
          <Button
            variant="ghost"
            size="icon"
            className="h-8 w-8"
            onClick={() => onDownload(keyId)}
            title="Download key"
          >
            <Download className="h-4 w-4" />
          </Button>
          <Button
            variant="ghost"
            size="icon"
            className="h-8 w-8 text-muted-foreground hover:text-destructive"
            onClick={() => onDelete(keyId)}
            title="Delete key"
          >
            <Trash className="h-4 w-4" />
          </Button>
        </div>
      );
    },
  },
];
