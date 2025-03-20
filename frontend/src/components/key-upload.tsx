import type React from "react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { FileKey, KeyRound } from "lucide-react";

interface KeyUploadProps {
  file?: File | null;
  onKeyUpload: (file: File) => void;
  onFileSelect?: (file: File | null) => void;
  disabled?: boolean;
}

export function KeyUpload({
  file,
  onKeyUpload,
  onFileSelect,
  disabled = false,
}: KeyUploadProps) {
  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      const selectedFile = e.target.files[0];
      onFileSelect?.(selectedFile);
      onKeyUpload(selectedFile);
    }
  };

  return (
    <div className="space-y-6">
      <div className="text-center space-y-2">
        <div className="flex justify-center">
          <div className="rounded-full bg-primary/10 p-3">
            <KeyRound className="h-6 w-6 text-primary" />
          </div>
        </div>
        <h3 className="text-lg font-medium">Upload Your Private Key</h3>
        <p className="text-sm text-muted-foreground max-w-md mx-auto">
          To decrypt this file, you need to provide your private key. The system
          will check if your attributes match the file's access policy.
        </p>
      </div>

      {file ? (
        <div className="space-y-4">
          <div className="p-4 bg-muted/50 rounded-lg border flex items-center justify-between">
            <div className="flex items-center space-x-3">
              <FileKey className="h-5 w-5 text-primary" />
              <div className="space-y-1">
                <p className="text-sm font-medium">{file.name}</p>
                <p className="text-xs text-muted-foreground">
                  {(file.size / 1024).toFixed(1)} KB
                </p>
              </div>
            </div>
            <Button
              variant="ghost"
              size="sm"
              onClick={() => onFileSelect?.(null)}
              disabled={disabled}
            >
              Change
            </Button>
          </div>
        </div>
      ) : (
        <div className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="key-file">Select Your Private Key File</Label>
            <Input
              id="key-file"
              type="file"
              onChange={handleFileChange}
              accept=".dat"
              disabled={disabled}
            />
          </div>
        </div>
      )}
    </div>
  );
}
