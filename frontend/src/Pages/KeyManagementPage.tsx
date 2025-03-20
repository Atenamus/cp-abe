import { useState, useEffect } from "react";
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
import { Label } from "@/components/ui/label";
import { Checkbox } from "@/components/ui/checkbox";
import { ApiClient } from "@/lib/api-client";

// Extend ApiClient response types
type ApiResponse<T> = {
  data?: T;
  error?: string;
};

type PrivateKey = {
  id: string;
  name: string;
  created: string;
  status: "active" | "expired" | "revoked";
  attributes: string[];
  lastUsed: string | null;
};

type KeyData = {
  keyData: ArrayBuffer;
};

const attributeOptions = [
  { id: "department:HR", label: "HR Department" },
  { id: "department:Engineering", label: "Engineering Department" },
  { id: "department:Finance", label: "Finance Department" },
  { id: "role:Admin", label: "Admin Role" },
  { id: "role:Manager", label: "Manager Role" },
  { id: "experience:2+", label: "2+ Years Experience" },
  { id: "experience:5+", label: "5+ Years Experience" },
];

export default function KeyManagementPage() {
  const [isGenerating, setIsGenerating] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [selectedKey, setSelectedKey] = useState<string | null>(null);
  const [privateKeys, setPrivateKeys] = useState<PrivateKey[]>([]);
  const [showAttributeDialog, setShowAttributeDialog] = useState(false);
  const [selectedAttributes, setSelectedAttributes] = useState<string[]>([]);

  useEffect(() => {
    fetchKeys();
  }, []);

  const fetchKeys = async () => {
    try {
      const result = (await ApiClient.listKeys()) as ApiResponse<PrivateKey[]>;
      if (result.error) {
        throw new Error(result.error);
      }
      if (result.data) {
        setPrivateKeys(result.data);
      }
    } catch (error) {
      console.error("Error fetching keys:", error);
      toast("Failed to fetch keys", {
        description: "There was an error retrieving your private keys",
      });
    }
  };

  const handleAttributeSelect = (attributeId: string) => {
    setSelectedAttributes((current) =>
      current.includes(attributeId)
        ? current.filter((id) => id !== attributeId)
        : [...current, attributeId]
    );
  };

  const handleGenerateKey = async () => {
    if (selectedAttributes.length === 0) {
      toast("Select attributes", {
        description: "Please select at least one attribute for the key",
      });
      return;
    }

    setIsGenerating(true);
    try {
      const result = (await ApiClient.generateKey(
        selectedAttributes
      )) as ApiResponse<KeyData>;
      if (result.error) {
        throw new Error(result.error);
      }

      if (result.data) {
        // Download the key file
        const blob = new Blob([new Uint8Array(result.data.keyData)], {
          type: "application/octet-stream",
        });
        const url = URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = `private_key_${new Date().getTime()}.dat`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
      }

      // Update the keys list
      await fetchKeys();

      // Reset selected attributes and close dialog
      setSelectedAttributes([]);
      setShowAttributeDialog(false);

      toast("Key generated successfully", {
        description: "Your new private key has been created and downloaded",
      });
    } catch (error) {
      console.error("Error generating key:", error);
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
      const result = await ApiClient.deleteKey(keyId);
      if (result.error) {
        throw new Error(result.error);
      }

      // Update local state
      setPrivateKeys(privateKeys.filter((key) => key.id !== keyId));

      toast("Key deleted", {
        description: "The private key has been deleted successfully",
      });
    } catch (error) {
      console.error("Error deleting key:", error);
      toast("Failed to delete key", {
        description: "There was an error deleting the private key",
      });
    } finally {
      setIsDeleting(false);
      setSelectedKey(null);
    }
  };

  const handleDownloadKey = async (keyId: string) => {
    const key = privateKeys.find((k) => k.id === keyId);
    if (!key) return;

    try {
      const result = (await ApiClient.generateKey(
        key.attributes
      )) as ApiResponse<KeyData>;
      if (result.error) {
        throw new Error(result.error);
      }

      if (result.data) {
        // Download the key file
        const blob = new Blob([new Uint8Array(result.data.keyData)], {
          type: "application/octet-stream",
        });
        const url = URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = `${key.name.replace(/\s+/g, "_")}.dat`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
      }

      toast("Key downloaded", {
        description: "Your private key has been downloaded successfully",
      });
    } catch (error) {
      console.error("Error downloading key:", error);
      toast("Failed to download key", {
        description: "There was an error downloading the private key",
      });
    }
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
        <Button
          onClick={() => setShowAttributeDialog(true)}
          disabled={isGenerating}
          size="lg"
        >
          <Plus className="mr-2 h-4 w-4" />
          Generate New Key
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

      {/* Delete Confirmation Dialog */}
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

      {/* Attribute Selection Dialog */}
      <Dialog
        open={showAttributeDialog}
        onOpenChange={(open) => !open && setShowAttributeDialog(false)}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Select Key Attributes</DialogTitle>
            <DialogDescription>
              Choose the attributes that will be embedded in your private key.
              These attributes determine which encrypted files you can access.
            </DialogDescription>
          </DialogHeader>

          <div className="grid gap-4 py-4">
            <div className="space-y-4">
              {attributeOptions.map((attr) => (
                <div key={attr.id} className="flex items-center space-x-2">
                  <Checkbox
                    id={attr.id}
                    checked={selectedAttributes.includes(attr.id)}
                    onCheckedChange={() => handleAttributeSelect(attr.id)}
                  />
                  <Label htmlFor={attr.id}>{attr.label}</Label>
                </div>
              ))}
            </div>
          </div>

          <DialogFooter>
            <Button
              variant="outline"
              onClick={() => {
                setShowAttributeDialog(false);
                setSelectedAttributes([]);
              }}
            >
              Cancel
            </Button>
            <Button
              onClick={handleGenerateKey}
              disabled={selectedAttributes.length === 0 || isGenerating}
            >
              {isGenerating ? (
                <>
                  <RefreshCw className="mr-2 h-4 w-4 animate-spin" />
                  Generating...
                </>
              ) : (
                "Generate Key"
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
