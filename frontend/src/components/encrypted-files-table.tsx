import { useState } from "react";
import { DataTable } from "@/components/file-data-table";
import { createColumns, type File } from "@/components/file-table-columns";
import { FileDetailsSheet } from "@/components/file-details-sheet";

interface EncryptedFilesTableProps {
  searchQuery?: string;
}

export function EncryptedFilesTable({ searchQuery }: EncryptedFilesTableProps) {
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [detailsOpen, setDetailsOpen] = useState(false);

  const handleViewDetails = (file: File) => {
    setSelectedFile(file);
    setDetailsOpen(true);
  };

  const encryptedFiles: File[] = [
    {
      id: "1",
      name: "Project_Proposal",
      size: "2.4 MB",
      date: "2025-01-15",
      type: "PDF",
    },
    {
      id: "2",
      name: "Marketing_Strategy",
      size: "1.8 MB",
      date: "2025-02-20",
      type: "DOCX",
    },
    {
      id: "3",
      name: "Budget_2025",
      size: "3.5 MB",
      date: "2025-01-10",
      type: "XLSX",
    },
    {
      id: "4",
      name: "Client_Presentation",
      size: "5.2 MB",
      date: "2025-02-05",
      type: "PPTX",
    },
    {
      id: "5",
      name: "Legal_Contract",
      size: "1.2 MB",
      date: "2025-03-01",
      type: "PDF",
    },
    {
      id: "6",
      name: "Product_Roadmap",
      size: "2.1 MB",
      date: "2025-02-15",
      type: "PDF",
    },
    {
      id: "7",
      name: "Team_Structure",
      size: "0.8 MB",
      date: "2025-01-20",
      type: "DOCX",
    },
  ];

  const columns = createColumns(handleViewDetails);

  return (
    <div className="space-y-4">
      <DataTable
        columns={columns}
        data={encryptedFiles}
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
