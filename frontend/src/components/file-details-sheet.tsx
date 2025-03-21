import type { File } from "@/components/file-table-columns";
import {
  Sheet,
  SheetContent,
  SheetDescription,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
  Download,
  Lock,
  Users,
  Clock,
  Shield,
  FileText,
  History,
} from "lucide-react";
import { toast } from "sonner";
import { Separator } from "@/components/ui/separator";
import { ApiClient } from "@/lib/api-client";
import { formatBytes } from "@/lib/utils";

interface FileDetailsSheetProps {
  file: File | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export function FileDetailsSheet({
  file,
  open,
  onOpenChange,
}: FileDetailsSheetProps) {
  const handleDownload = async () => {
    if (!file) return;

    try {
      toast("Download Started", {
        description: `Downloading ${file.name}`,
      });

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

  // Mock data for file details (we could enhance this in the future with real data)
  const encryptionDetails = {
    algorithm: "CP-ABE (Ciphertext-Policy Attribute-Based Encryption)",
    encryptedOn: file?.createdAt || new Date().toISOString(),
  };

  const accessHistory = [
    {
      user: "Current User",
      action: "Viewed",
      date: new Date().toLocaleString(),
    },
    {
      user: "Current User",
      action: "Encrypted",
      date: file ? new Date(file.createdAt).toLocaleString() : "",
    },
  ];

  if (!file) return null;

  // Extract file extension for type
  const fileName = file.name;
  const fileType = fileName.split(".").pop()?.toUpperCase() || "UNKNOWN";

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="sm:max-w-md md:max-w-lg overflow-y-auto py-6 px-6">
        <div className="flex flex-row items-center justify-between">
          <SheetHeader className="p-0">
            <SheetTitle className="flex items-center gap-2">
              <FileText className="h-5 w-5" />
              {file.name}
            </SheetTitle>
            <SheetDescription className="flex flex-wrap items-center gap-2">
              <Badge variant="outline">{fileType}</Badge>
              <span>{formatBytes(file.size)}</span>
              <span>•</span>
              <span>
                Created: {new Date(file.createdAt).toLocaleDateString()}
              </span>
            </SheetDescription>
          </SheetHeader>
          <div className="flex justify-start py-4">
            <Button onClick={handleDownload} className="mr-2">
              <Download className="mr-2 h-4 w-4" /> Download
            </Button>
          </div>
        </div>

        <Separator className="my-4" />

        <Tabs defaultValue="encryption" className="w-full">
          <TabsList className="grid w-full grid-cols-3">
            <TabsTrigger value="encryption">
              <Lock className="mr-2 h-4 w-4" />
              <span className="hidden sm:inline">Encryption</span>
            </TabsTrigger>
            <TabsTrigger value="details">
              <FileText className="mr-2 h-4 w-4" />
              <span className="hidden sm:inline">Details</span>
            </TabsTrigger>
            <TabsTrigger value="history">
              <History className="mr-2 h-4 w-4" />
              <span className="hidden sm:inline">History</span>
            </TabsTrigger>
          </TabsList>

          <TabsContent value="encryption" className="space-y-4 pt-4">
            <div className="space-y-4">
              <div className="flex items-center">
                <Shield className="mr-2 h-5 w-5" />
                <h3 className="text-lg font-medium">Encryption Details</h3>
              </div>
              <p className="text-sm text-muted-foreground">
                Information about the file's encryption
              </p>

              <div className="grid grid-cols-2 gap-3 mt-4">
                <div className="text-sm font-medium">Algorithm</div>
                <div className="text-sm">{encryptionDetails.algorithm}</div>

                <div className="text-sm font-medium">Encrypted On</div>
                <div className="text-sm">
                  {new Date(encryptionDetails.encryptedOn).toLocaleDateString()}
                </div>

                <div className="text-sm font-medium">File Path</div>
                <div className="text-sm text-wrap break-all">{file.path}</div>
              </div>
            </div>
          </TabsContent>

          <TabsContent value="details" className="space-y-4 pt-4">
            <div className="space-y-4">
              <div className="flex items-center">
                <FileText className="mr-2 h-5 w-5" />
                <h3 className="text-lg font-medium">File Details</h3>
              </div>
              <p className="text-sm text-muted-foreground">
                Technical information about the file
              </p>

              <div className="grid grid-cols-2 gap-3 mt-4">
                <div className="text-sm font-medium">Original Filename</div>
                <div className="text-sm">{file.name}</div>

                <div className="text-sm font-medium">Encrypted Filename</div>
                <div className="text-sm">{file.fullName}</div>

                <div className="text-sm font-medium">File Size</div>
                <div className="text-sm">{formatBytes(file.size)}</div>

                <div className="text-sm font-medium">File Type</div>
                <div className="text-sm">
                  <Badge variant="outline">{fileType}</Badge>
                </div>

                <div className="text-sm font-medium">Created At</div>
                <div className="text-sm">
                  {new Date(file.createdAt).toLocaleString()}
                </div>
              </div>
            </div>
          </TabsContent>

          <TabsContent value="history" className="space-y-4 pt-4">
            <div className="space-y-4">
              <div className="flex items-center">
                <Clock className="mr-2 h-5 w-5" />
                <h3 className="text-lg font-medium">Access History</h3>
              </div>
              <p className="text-sm text-muted-foreground">
                Recent activity for this file
              </p>

              <div className="space-y-4 mt-4">
                {accessHistory.map((entry, index) => (
                  <div
                    key={index}
                    className="flex items-start justify-between border-b pb-3 last:border-0"
                  >
                    <div>
                      <div className="font-medium">{entry.user}</div>
                      <div className="text-sm text-muted-foreground">
                        {entry.action}
                      </div>
                    </div>
                    <div className="text-sm text-muted-foreground">
                      {entry.date}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </TabsContent>
        </Tabs>
      </SheetContent>
    </Sheet>
  );
}
