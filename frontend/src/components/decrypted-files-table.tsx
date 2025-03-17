import { useState } from "react";
import { DataTable } from "@/components/file-data-table";
import { createColumns, type File } from "@/components/file-table-columns";
import { FileDetailsSheet } from "@/components/file-details-sheet";

interface DecryptedFilesTableProps {
  searchQuery?: string;
}

export function DecryptedFilesTable({ searchQuery }: DecryptedFilesTableProps) {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [detailsOpen, setDetailsOpen] = useState(false);

  const handleViewDetails = (file: File) => {
    setSelectedFile(file);
    setDetailsOpen(true);
  };

  const decryptedFiles: File[] = [
    {
      id: "8",
      name: "Financial_Report_Q1",
      size: "3.2 MB",
      date: "2025-03-05",
      type: "XLSX",
    },
    {
      id: "9",
      name: "Employee_Handbook",
      size: "4.7 MB",
      date: "2025-02-28",
      type: "PDF",
    },
    {
      id: "10",
      name: "Research_Findings",
      size: "2.9 MB",
      date: "2025-03-10",
      type: "DOCX",
    },
    {
      id: "11",
      name: "Sales_Data_2024",
      size: "6.1 MB",
      date: "2025-01-25",
      type: "XLSX",
    },
  ];

  const columns = createColumns(handleViewDetails);

  return (
    <div className="space-y-4">
      <DataTable
        columns={columns}
        data={decryptedFiles}
        searchQuery={searchQuery}
        searchColumn="name"
      />

      <FileDetailsSheet
        file={selectedFile}
        open={detailsOpen}
        onOpenChange={setDetailsOpen}
      />
    </div>
  );
}
