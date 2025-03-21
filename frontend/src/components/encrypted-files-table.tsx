import { useState, useEffect } from "react";
import { DataTable } from "@/components/file-data-table";
import { createColumns, type File } from "@/components/file-table-columns";
import { FileDetailsSheet } from "@/components/file-details-sheet";
import { ApiClient } from "@/lib/api-client";
import { toast } from "sonner";

interface EncryptedFilesTableProps {
  searchQuery?: string;
  refreshTrigger?: number;
}

export function EncryptedFilesTable({
  searchQuery,
  refreshTrigger = 0,
}: EncryptedFilesTableProps) {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [detailsOpen, setDetailsOpen] = useState(false);
  const [encryptedFiles, setEncryptedFiles] = useState<File[]>([]);
  const [loading, setLoading] = useState(false);

  const handleViewDetails = (file: File) => {
    setSelectedFile(file);
    setDetailsOpen(true);
  };

  // Fetch encrypted files when component mounts or refreshTrigger changes
  useEffect(() => {
    async function fetchEncryptedFiles() {
      setLoading(true);
      try {
        const response = await ApiClient.listEncryptedFiles();
        if (response.error) {
          toast.error("Failed to load files", { description: response.error });
          return;
        }

        if (response.data) {
          // Add file type based on file name extension
          const files = (response.data as File[]).map((file) => {
            const fileNameParts = file.name.split(".");
            const type =
              fileNameParts.length > 1
                ? fileNameParts[fileNameParts.length - 1].toUpperCase()
                : "UNKNOWN";

            return {
              ...file,
              type,
            };
          });

          setEncryptedFiles(files);
        }
      } catch (error) {
        toast.error("An error occurred", {
          description: "Could not load encrypted files",
        });
      } finally {
        setLoading(false);
      }
    }

    fetchEncryptedFiles();
  }, [refreshTrigger]);

  const columns = createColumns(handleViewDetails);

  return (
    <div className="space-y-4">
      {loading ? (
        <div className="flex justify-center p-4">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary" />
        </div>
      ) : encryptedFiles.length === 0 ? (
        <div className="flex flex-col items-center justify-center p-8 text-center">
          <h3 className="text-lg font-medium mb-2">No encrypted files found</h3>
          <p className="text-muted-foreground">
            Encrypt some files to see them appear here
          </p>
        </div>
      ) : (
        <DataTable
          columns={columns}
          data={encryptedFiles}
          searchQuery={searchQuery}
          searchColumn="name"
        />
      )}

      <FileDetailsSheet
        file={selectedFile}
        open={detailsOpen}
        onOpenChange={setDetailsOpen}
      />
    </div>
  );
}
