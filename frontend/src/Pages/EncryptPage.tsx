import { useState } from "react";
import { Button } from "@/components/ui/button";
import { FileUpload } from "@/components/file-upload";
import { PolicySelector } from "@/components/policy-selector";
import { EncryptionComplete } from "@/components/encryption-complete";
import { toast } from "sonner";
import { ApiClient } from "@/lib/api-client";

export default function EncryptPage() {
  const [file, setFile] = useState<File | null>(null);
  const [policy, setPolicy] = useState("");
  const [isEncrypting, setIsEncrypting] = useState(false);
  const [encryptedFile, setEncryptedFile] = useState<Blob | null>(null);

  const handleEncrypt = async () => {
    if (!file) {
      toast("Select a file", {
        description: "Please select a file to encrypt",
      });
      return;
    }

    if (!policy) {
      toast("Set a policy", {
        description: "Please set an access policy for encryption",
      });
      return;
    }

    setIsEncrypting(true);

    try {
      const result = await ApiClient.encryptFile(file, policy);
      if (result.error) {
        throw new Error(result.error);
      }
      if (result.data) {
        setEncryptedFile(result.data);
      }
    } catch (error) {
      console.error("Error encrypting file:", error);
      toast("Failed to encrypt", {
        description: "There was an error encrypting your file",
      });
    } finally {
      setIsEncrypting(false);
    }
  };

  const handleReset = () => {
    setFile(null);
    setPolicy("");
    setEncryptedFile(null);
  };

  if (encryptedFile) {
    return (
      <EncryptionComplete
        fileName={file?.name || ""}
        encryptedFile={encryptedFile}
        originalFileName={file?.name || ""}
        onReset={handleReset}
      />
    );
  }

  return (
    <div className="space-y-6 max-w-2xl w-full mx-auto p-6">
      <div>
        <h1 className="text-3xl font-bold">Encrypt File</h1>
        <p className="text-muted-foreground py-1.5">
          Upload a file and set an access policy to encrypt it
        </p>
      </div>

      <FileUpload
        file={file}
        onFileSelect={setFile}
        onFileUpload={setFile}
        disabled={isEncrypting}
      />

      <PolicySelector
        value={policy}
        onChange={setPolicy}
        disabled={isEncrypting}
      />

      <Button
        onClick={handleEncrypt}
        disabled={!file || !policy || isEncrypting}
        className="w-full"
      >
        {isEncrypting ? "Encrypting..." : "Encrypt File"}
      </Button>
    </div>
  );
}
