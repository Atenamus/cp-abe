import type React from "react";

import { useState, useRef } from "react";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { UploadIcon, FileIcon } from "lucide-react";

interface FileUploadProps {
  file?: File | null;
  onFileUpload: (file: File) => void;
  onFileSelect?: (file: File | null) => void;
  forDecryption?: boolean;
  disabled?: boolean;
  accept?: string;
}

export function FileUpload({
  file,
  onFileUpload,
  onFileSelect,
  forDecryption = false,
  disabled = false,
  accept,
}: FileUploadProps) {
  const [dragActive, setDragActive] = useState(false);
  const inputRef = useRef<HTMLInputElement>(null);

  const handleDrag = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === "dragenter" || e.type === "dragover") {
      setDragActive(true);
    } else if (e.type === "dragleave") {
      setDragActive(false);
    }
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);

    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      const file = e.dataTransfer.files[0];
      onFileUpload(file);
      onFileSelect?.(file);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    e.preventDefault();
    if (e.target.files && e.target.files[0]) {
      const file = e.target.files[0];
      onFileUpload(file);
      onFileSelect?.(file);
    }
  };

  const handleClick = () => {
    inputRef.current?.click();
  };

  // Use accept=".cpabe" for decryption and allow all file types for encryption
  const acceptFileTypes = accept || (forDecryption ? ".cpabe" : "*/*");

  return (
    <Card
      className={`border-2 border-dashed p-8 text-center ${
        dragActive
          ? "border-primary bg-primary/5"
          : "border-muted-foreground/30"
      }`}
      onDragEnter={handleDrag}
      onDragLeave={handleDrag}
      onDragOver={handleDrag}
      onDrop={handleDrop}
    >
      <div className="flex flex-col items-center justify-center space-y-4">
        {file ? (
          <>
            <div className="rounded-full bg-primary/10 p-3">
              <FileIcon className="h-6 w-6 text-primary" />
            </div>
            <div className="space-y-2">
              <h3 className="text-lg font-medium text-primary">
                File Selected
              </h3>
              <div className="flex flex-col items-center text-sm text-muted-foreground">
                <span className="font-medium">{file.name}</span>
                <span>{(file.size / 1024).toFixed(1)} KB</span>
              </div>
            </div>
            <Button
              variant="outline"
              onClick={() => {
                onFileSelect?.(null);
                if (inputRef.current) {
                  inputRef.current.value = "";
                }
              }}
            >
              Change File
            </Button>
          </>
        ) : (
          <>
            <div className="rounded-full bg-muted p-3">
              <UploadIcon className="h-6 w-6" />
            </div>
            <div className="space-y-2">
              <h3 className="text-lg font-medium">Drag and drop your file</h3>
              <p className="text-sm text-muted-foreground">
                or click to browse your files
              </p>
              {forDecryption && (
                <p className="text-xs text-muted-foreground">
                  (Only .cpabe files are accepted for decryption)
                </p>
              )}
            </div>
            <input
              ref={inputRef}
              type="file"
              className="hidden"
              onChange={handleChange}
              accept={acceptFileTypes}
              disabled={disabled}
            />
            <Button variant="outline" onClick={handleClick} disabled={disabled}>
              Select File
            </Button>
          </>
        )}
      </div>
    </Card>
  );
}
