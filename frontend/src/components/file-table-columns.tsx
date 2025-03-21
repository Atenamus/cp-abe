import type { ColumnDef } from "@tanstack/react-table";
import {
  ArrowUpDown,
  Download,
  MoreHorizontal,
  Trash,
  Info,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { toast } from "sonner";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Badge } from "@/components/ui/badge";
import { ApiClient } from "@/lib/api-client";
import { formatBytes } from "@/lib/utils";

export type File = {
  id: number;
  name: string;
  fullName: string;
  path: string;
  size: number;
  createdAt: string;
  type?: string;
};

const handleDownload = async (file: File) => {
  try {
    toast("Download Started", { description: `Downloading ${file.name}` });
    const response = await ApiClient.downloadEncryptedFile(file.fullName);

    if (response.error) {
      toast.error("Download Failed", { description: response.error });
      return;
    }

    if (response.data) {
      // Create a download link and trigger it
      const url = window.URL.createObjectURL(response.data as Blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = file.fullName;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);

      toast.success("Download Complete", {
        description: `File ${file.name} downloaded successfully`,
      });
    }
  } catch (error) {
    toast.error("Download Failed", {
      description: `An error occurred: ${error}`,
    });
  }
};

const handleDelete = (fileId: number) => {
  // Placeholder for future delete functionality
  toast("File Deleted", { description: `File ${fileId} has been removed` });
};

export const createColumns = (
  onViewDetails: (file: File) => void
): ColumnDef<File>[] => [
  {
    accessorKey: "name",
    header: ({ column }) => {
      return (
        <Button
          variant="ghost"
          onClick={() => column.toggleSorting(column.getIsSorted() === "asc")}
        >
          File Name
          <ArrowUpDown className="ml-2 h-4 w-4" />
        </Button>
      );
    },
    cell: ({ row }) => (
      <Button
        variant="link"
        className="p-0 h-auto font-medium text-left justify-start"
        onClick={() => onViewDetails(row.original)}
      >
        {row.getValue("name")}
      </Button>
    ),
  },
  {
    accessorKey: "type",
    header: "Type",
    cell: ({ row }) => {
      const file = row.original;
      const name = file.name as string;
      const type = name.split(".").pop()?.toUpperCase() || "UNKNOWN";

      return (
        <Badge variant="outline" className="font-medium">
          {type}
        </Badge>
      );
    },
  },
  {
    accessorKey: "size",
    header: "Size",
    cell: ({ row }) => {
      return <div>{formatBytes(row.original.size)}</div>;
    },
  },
  {
    accessorKey: "createdAt",
    header: ({ column }) => {
      return (
        <Button
          variant="ghost"
          onClick={() => column.toggleSorting(column.getIsSorted() === "asc")}
        >
          Date
          <ArrowUpDown className="ml-2 h-4 w-4" />
        </Button>
      );
    },
    cell: ({ row }) => {
      const date = new Date(row.getValue("createdAt"));
      return <div>{date.toLocaleDateString()}</div>;
    },
  },
  {
    id: "actions",
    cell: ({ row }) => {
      const file = row.original;

      return (
        <div className="flex items-center gap-2">
          <Button
            variant="ghost"
            size="icon"
            onClick={() => handleDownload(file)}
            title="Download"
          >
            <Download className="h-4 w-4" />
          </Button>

          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant="ghost" size="icon">
                <MoreHorizontal className="h-4 w-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuLabel>Actions</DropdownMenuLabel>
              <DropdownMenuSeparator />
              <DropdownMenuItem onClick={() => onViewDetails(file)}>
                <Info className="mr-2 h-4 w-4" />
                View Details
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => handleDownload(file)}>
                <Download className="mr-2 h-4 w-4" />
                Download
              </DropdownMenuItem>
              <DropdownMenuItem
                onClick={() => handleDelete(file.id)}
                className="text-destructive focus:text-destructive"
              >
                <Trash className="mr-2 h-4 w-4" />
                Delete
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      );
    },
  },
];
