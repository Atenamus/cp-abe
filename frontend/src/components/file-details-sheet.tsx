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
  const handleDownload = () => {
    if (file) {
      toast("Download Started", {
        description: `Downloading file ${file.name}`,
      });
    }
  };

  // Mock data for file details
  const encryptionDetails = {
    algorithm: "AES-256",
    encryptedOn: "2025-02-15",
    encryptedBy: "John Doe",
    policy: "Corporate-Standard",
    expiresOn: "2026-02-15",
  };

  const accessHistory = [
    { user: "Jane Smith", action: "Viewed", date: "2025-03-10 14:32" },
    { user: "Mike Johnson", action: "Downloaded", date: "2025-03-08 09:15" },
    { user: "John Doe", action: "Encrypted", date: "2025-02-15 11:20" },
  ];

  const sharingDetails = {
    sharedWith: ["Marketing Team", "Executive Board"],
    accessLevel: "Read-Only",
    sharingExpiry: "2025-06-15",
  };

  if (!file) return null;

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
              <Badge variant="outline">{file.type}</Badge>
              <span>{file.size}</span>
              <span>•</span>
              <span>Created: {new Date(file.date).toLocaleDateString()}</span>
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
            <TabsTrigger value="sharing">
              <Users className="mr-2 h-4 w-4" />
              <span className="hidden sm:inline">Sharing</span>
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
                <h3 className="text-lg font-medium">Encryption Policy</h3>
              </div>
              <p className="text-sm text-muted-foreground">
                Details about the file's encryption
              </p>

              <div className="grid grid-cols-2 gap-3 mt-4">
                <div className="text-sm font-medium">Algorithm</div>
                <div className="text-sm">{encryptionDetails.algorithm}</div>

                <div className="text-sm font-medium">Policy</div>
                <div className="text-sm">
                  <Badge variant="secondary">{encryptionDetails.policy}</Badge>
                </div>

                <div className="text-sm font-medium">Encrypted On</div>
                <div className="text-sm">
                  {new Date(encryptionDetails.encryptedOn).toLocaleDateString()}
                </div>

                <div className="text-sm font-medium">Encrypted By</div>
                <div className="text-sm">{encryptionDetails.encryptedBy}</div>

                <div className="text-sm font-medium">Expires On</div>
                <div className="text-sm">
                  {new Date(encryptionDetails.expiresOn).toLocaleDateString()}
                </div>
              </div>
            </div>
          </TabsContent>

          <TabsContent value="sharing" className="space-y-4 pt-4">
            <div className="space-y-4">
              <div className="flex items-center">
                <Users className="mr-2 h-5 w-5" />
                <h3 className="text-lg font-medium">Sharing Settings</h3>
              </div>
              <p className="text-sm text-muted-foreground">
                Who has access to this file
              </p>

              <div className="grid grid-cols-2 gap-3 mt-4">
                <div className="text-sm font-medium">Access Level</div>
                <div className="text-sm">
                  <Badge variant="outline">{sharingDetails.accessLevel}</Badge>
                </div>

                <div className="text-sm font-medium">Sharing Expiry</div>
                <div className="text-sm">
                  {new Date(sharingDetails.sharingExpiry).toLocaleDateString()}
                </div>

                <div className="text-sm font-medium col-span-2 mt-2">
                  Shared With
                </div>
                <div className="col-span-2 flex flex-wrap gap-2">
                  {sharingDetails.sharedWith.map((group) => (
                    <Badge key={group} variant="secondary">
                      {group}
                    </Badge>
                  ))}
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
