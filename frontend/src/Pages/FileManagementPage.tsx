import { useState } from "react";
import { Upload, Search } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { EncryptedFilesTable } from "@/components/encrypted-files-table";
import { Link } from "react-router-dom";

export default function FileManagementPage() {
  const [searchQuery, setSearchQuery] = useState("");
  const [refreshTrigger, setRefreshTrigger] = useState(0);

  // Function to trigger a refresh of the file list
  const refreshFiles = () => {
    setRefreshTrigger((prev) => prev + 1);
  };

  return (
    <div className="p-6 space-y-6 max-w-7xl w-full mx-auto">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold">File Management</h1>
        <div className="flex gap-2">
          <Button variant="outline" onClick={refreshFiles}>
            Refresh
          </Button>
          <Button asChild>
            <Link to="/dashboard/files/encrypt">
              <Upload className="mr-2 h-4 w-4" /> Encrypt File
            </Link>
          </Button>
        </div>
      </div>
      <div className="flex items-center space-x-2 py-2">
        <Input
          placeholder="Search files..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="max-w-md"
        />
      </div>
      <EncryptedFilesTable
        searchQuery={searchQuery}
        refreshTrigger={refreshTrigger}
      />
    </div>
  );
}
