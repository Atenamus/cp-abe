import { useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { toast } from "sonner";
import { Plus, RefreshCw } from "lucide-react";
import { DataTable } from "@/components/data-table";
import { columns } from "@/components/columns";

type PrivateKey = {
  id: string;
  name: string;
  created: string;
  status: "active" | "expired" | "revoked";
  attributes: string[];
  lastUsed: string | null;
};

export default function KeyManagementPage() {
  const [isGenerating, setIsGenerating] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [selectedKey, setSelectedKey] = useState<string | null>(null);

  // Sample data for private keys
  const [privateKeys, setPrivateKeys] = useState<PrivateKey[]>([
    {
      id: "pk-1",
      name: "Private Key 1",
      created: "2025-01-15",
      status: "active",
      attributes: ["Admin", "HR"],
      lastUsed: "2025-03-10",
    },
    {
      id: "pk-2",
      name: "Private Key 2",
      created: "2025-02-20",
      status: "active",
      attributes: ["Finance", "Manager"],
      lastUsed: "2025-03-12",
    },
    {
      id: "pk-3",
      name: "Private Key 3",
      created: "2025-03-05",
      status: "expired",
      attributes: ["Engineering"],
      lastUsed: null,
    },
  ]);

  const handleGenerateKey = async () => {
    setIsGenerating(true);
    try {
      await new Promise((resolve) => setTimeout(resolve, 1500));

      // Create a new key with a random ID
      const newKey: PrivateKey = {
        id: `pk-${Math.floor(Math.random() * 1000)}`,
        name: `Private Key ${privateKeys.length + 1}`,
        created: new Date().toISOString().split("T")[0],
        status: "active",
        attributes: ["Admin"], // Default attribute
        lastUsed: null,
      };

      setPrivateKeys([...privateKeys, newKey]);

      toast("Key generated successfully", {
        description: "Your new private key has been created",
      });
    } catch {
      toast("Failed to generate key", {
        description: "There was an error generating your private key",
      });
    } finally {
      setIsGenerating(false);
    }
  };

  const handleDeleteKey = async (keyId: string) => {
    setIsDeleting(true);
    try {
      await new Promise((resolve) => setTimeout(resolve, 1000));

      // Remove the key from the state
      setPrivateKeys(privateKeys.filter((key) => key.id !== keyId));

      toast("Key deleted", {
        description: "The private key has been deleted successfully",
      });
    } catch {
      toast("Failed to delete key", {
        description: "There was an error deleting the private key",
      });
    } finally {
      setIsDeleting(false);
      setSelectedKey(null);
    }
  };

  const handleDownloadKey = (keyId: string) => {
    const key = privateKeys.find((k) => k.id === keyId);
    if (!key) return;

    const keyContent = `Key ID: ${key.id}
Name: ${key.name}
Created: ${key.created}
Status: ${key.status}
Attributes: ${key.attributes.join(", ")}
Private Key Data: ****************************************`;

    const blob = new Blob([keyContent], { type: "text/plain" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = `${key.name.replace(/\s+/g, "_")}.txt`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);

    toast("Key downloaded", {
      description: "Your private key has been downloaded successfully",
    });
  };

  return (
    <div className="space-y-6 max-w-7xl w-full mx-auto p-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Private Key Management</h1>
          <p className="text-muted-foreground py-1.5">
            Manage your attribute-based private keys for secure access control
          </p>
        </div>
        <Button onClick={handleGenerateKey} disabled={isGenerating} size="lg">
          {isGenerating ? (
            <RefreshCw className="mr-2 h-4 w-4 animate-spin" />
          ) : (
            <Plus className="mr-2 h-4 w-4" />
          )}
          {isGenerating ? "Generating..." : "Generate New Key"}
        </Button>
      </div>

      <div>
        <DataTable
          columns={columns}
          data={privateKeys}
          onDownload={handleDownloadKey}
          onDelete={(keyId) => setSelectedKey(keyId)}
        />
      </div>

      <Dialog
        open={!!selectedKey}
        onOpenChange={(open) => !open && setSelectedKey(null)}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Delete Private Key</DialogTitle>
            <DialogDescription>
              Are you sure you want to delete this private key? This action
              cannot be undone.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setSelectedKey(null)}>
              Cancel
            </Button>
            <Button
              variant="destructive"
              onClick={() => selectedKey && handleDeleteKey(selectedKey)}
              disabled={isDeleting}
            >
              {isDeleting ? "Deleting..." : "Delete"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
