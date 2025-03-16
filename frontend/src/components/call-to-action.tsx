import type React from "react";

import { Button } from "@/components/ui/button";
import { Upload, FileUp, Lock, Loader2 } from "lucide-react";
import { useState, useRef } from "react";
import { Progress } from "@/components/ui/progress";
import { Link } from "react-router";

export default function FilePicker() {
  const [fileName, setFileName] = useState<string | null>(null);
  const [isDragging, setIsDragging] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [loadingProgress, setLoadingProgress] = useState(0);
  const [isFileReady, setIsFileReady] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      processFile(file);
    }
  };

  const processFile = (file: File) => {
    setFileName(file.name);
    setIsLoading(true);
    setIsFileReady(false);

    // Simulate loading progress for larger files
    // In a real app, this would be replaced with actual file processing logic
    let progress = 0;
    const interval = setInterval(() => {
      progress += 5;
      setLoadingProgress(progress);

      if (progress >= 100) {
        clearInterval(interval);
        setIsLoading(false);
        setIsFileReady(true);
      }
    }, 100);
  };

  const handleBrowseClick = () => {
    if (isFileReady) {
      // Handle encryption logic here
      setIsLoading(true);
      setLoadingProgress(0);

      // Simulate encryption process
      let progress = 0;
      const interval = setInterval(() => {
        progress += 10;
        setLoadingProgress(progress);

        if (progress >= 100) {
          clearInterval(interval);
          setIsLoading(false);
          alert("File encrypted successfully!");
        }
      }, 150);
    } else {
      fileInputRef.current?.click();
    }
  };

  const handleDragOver = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(true);
  };

  const handleDragLeave = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);
  };

  const handleDrop = (e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);

    const files = e.dataTransfer.files;
    if (files && files.length > 0) {
      processFile(files[0]);
    }
  };

  return (
    <section id="try-it-now" className="py-16 md:py-24">
      <div className="mx-auto max-w-5xl px-6">
        <div className="text-center">
          <h2 className="text-balance text-4xl font-medium">
            Secure Your Data Effortlessly
          </h2>
          <p className="mt-4 text-md">
            Encrypt with confidence and control access like never before.
          </p>

          <div className="mx-auto mt-10 max-w-md lg:mt-12">
            <div
              className={`bg-background relative rounded-[calc(var(--radius)+0.75rem)] border shadow shadow-zinc-950/5 overflow-hidden ${
                isDragging ? "border-primary border-2" : ""
              }`}
              onDragOver={handleDragOver}
              onDragLeave={handleDragLeave}
              onDrop={handleDrop}
            >
              <div className="p-8 text-center">
                <div className="mb-4 flex justify-center">
                  <div
                    className={`rounded-full ${
                      isFileReady
                        ? "bg-green-100 dark:bg-green-900/30"
                        : isDragging
                        ? "bg-primary/20"
                        : "bg-primary/10"
                    } p-3 transition-colors`}
                  >
                    {isFileReady ? (
                      <Lock className="size-6 text-green-600 dark:text-green-400" />
                    ) : (
                      <Upload className="size-6 text-primary" />
                    )}
                  </div>
                </div>

                <p className="mb-2 font-medium">
                  {fileName ? fileName : "Choose a file or drag and drop"}
                </p>
                <p className="text-sm text-muted-foreground">
                  Supports PDF, DOCX, XLSX, JPG, PNG (up to 10MB)
                </p>

                {isLoading && (
                  <div className="mt-4">
                    <Progress value={loadingProgress} className="h-2" />
                    <p className="mt-2 text-sm text-muted-foreground">
                      {loadingProgress < 100
                        ? `Processing file... ${loadingProgress}%`
                        : "Finalizing..."}
                    </p>
                  </div>
                )}

                <div className="mt-6 flex justify-center">
                  {isLoading ? (
                    <Button type="button" disabled>
                      <Loader2 className="mr-2 size-4 animate-spin" />
                      Processing...
                    </Button>
                  ) : isFileReady ? (
                    <Button
                      type="button"
                      className="bg-green-600 hover:bg-green-700"
                      asChild
                    >
                      <Link to="/dashboard/files/encrypt">
                        <Lock className="mr-2 size-4" />
                        Encrypt File
                      </Link>
                    </Button>
                  ) : (
                    <Button
                      onClick={handleBrowseClick}
                      type="button"
                      disabled={isLoading}
                    >
                      <FileUp className="mr-2 size-4" />
                      Browse Files
                    </Button>
                  )}
                  <input
                    ref={fileInputRef}
                    id="file-upload"
                    type="file"
                    className="sr-only"
                    onChange={handleFileChange}
                    aria-label="File upload"
                  />
                </div>
              </div>

              {/* Visual indicator for drag area */}
              {isDragging && (
                <div className="absolute inset-0 border-2 border-dashed border-primary/50 rounded-[calc(var(--radius)+0.5rem)] pointer-events-none" />
              )}
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}
