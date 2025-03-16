import type React from "react";

import { useState } from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { FileKey, Upload } from "lucide-react";

interface KeyUploadProps {
  onKeyUpload: (userData: { name: string; attributes: string[] }) => void;
}

export function KeyUpload({ onKeyUpload }: KeyUploadProps) {
  const [keyFile, setKeyFile] = useState<File | null>(null);
  const [isUploading, setIsUploading] = useState(false);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      setKeyFile(e.target.files[0]);
    }
  };

  const handleUpload = () => {
    if (!keyFile) return;

    setIsUploading(true);

    // In a real app, we would read and validate the key file
    // For demo purposes, we'll simulate reading attributes from the key
    setTimeout(() => {
      // Simulate extracting user data from the key
      const mockUserData = {
        name: "John Doe",
        attributes: ["HR", "Manager", "Finance"],
      };

      setIsUploading(false);
      onKeyUpload(mockUserData);
    }, 1500);
  };

  return (
    <div className="space-y-6">
      <div className="text-center space-y-2">
        <div className="flex justify-center">
          <div className="rounded-full bg-primary/10 p-3">
            <FileKey className="h-6 w-6 text-primary" />
          </div>
        </div>
        <h3 className="text-lg font-medium">Upload Your Private Key</h3>
        <p className="text-sm text-muted-foreground max-w-md mx-auto">
          To decrypt this file, you need to provide your private key. The system
          will check if your attributes match the file's access policy.
        </p>
      </div>

      <div className="space-y-4">
        <div className="space-y-2">
          <Label htmlFor="key-file">Select Your Private Key File</Label>
          <Input
            id="key-file"
            type="file"
            onChange={handleFileChange}
            accept=".txt,.key,.pem"
          />
        </div>

        {keyFile && (
          <div className="p-3 bg-muted rounded-md">
            <div className="flex items-center">
              <FileKey className="h-4 w-4 mr-2" />
              <span className="text-sm font-medium">{keyFile.name}</span>
            </div>
          </div>
        )}

        <Button
          onClick={handleUpload}
          disabled={!keyFile || isUploading}
          className="w-full"
        >
          {isUploading ? (
            <>Validating Key...</>
          ) : (
            <>
              <Upload className="mr-2 h-4 w-4" />
              Upload Key & Attempt Decryption
            </>
          )}
        </Button>
      </div>
    </div>
  );
}
