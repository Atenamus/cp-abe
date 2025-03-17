import { useState } from "react";
import { Upload, Search } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { EncryptedFilesTable } from "@/components/encrypted-files-table";
import { DecryptedFilesTable } from "@/components/decrypted-files-table";
import { Link } from "react-router";

export default function FileManagementPage() {
  const [searchQuery, setSearchQuery] = useState("");

  return (
    <div className="p-6 space-y-6 max-w-7xl w-full mx-auto">
      <div className="flex items-center justify-between">
        <h1 className="text-3xl font-bold">File Management</h1>
        <Button asChild>
          <Link to="/dashboard/files/encrypt">
            <Upload className="mr-2 h-4 w-4" /> Encrypt File
          </Link>
        </Button>
      </div>

      <Tabs defaultValue="encrypted" className="w-full">
        <TabsList className="grid w-full max-w-md grid-cols-2">
          <TabsTrigger value="encrypted">Encrypted</TabsTrigger>
          <TabsTrigger value="decrypted">Decrypted</TabsTrigger>
        </TabsList>

        <div className="flex items-center space-x-2 py-2">
          <Input
            placeholder="Search files..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="max-w-md"
          />
        </div>

        <TabsContent value="encrypted">
          <EncryptedFilesTable searchQuery={searchQuery} />
        </TabsContent>

        <TabsContent value="decrypted">
          <DecryptedFilesTable searchQuery={searchQuery} />
        </TabsContent>
      </Tabs>
    </div>
  );
}
