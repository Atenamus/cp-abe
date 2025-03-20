import { useState } from "react";
import { Button } from "@/components/ui/button";
import { FileUpload } from "@/components/file-upload";
import { KeyUpload } from "@/components/key-upload";
import { DecryptionResult } from "@/components/decryption-result";
import { toast } from "sonner";
import { ApiClient } from "@/lib/api-client";

export default function DecryptPage() {
  const [file, setFile] = useState<File | null>(null);
  const [key, setKey] = useState<File | null>(null);
  const [isDecrypting, setIsDecrypting] = useState(false);
  const [decryptedFile, setDecryptedFile] = useState<Blob | null>(null);
  const [decryptionSuccess, setDecryptionSuccess] = useState<boolean | null>(
    null
  );

  const handleDecrypt = async () => {
    if (!file || !key) {
      toast("Missing files", {
        description:
          "Please select both an encrypted file and your private key",
      });
      return;
    }

    setIsDecrypting(true);
    setDecryptionSuccess(null);

    try {
      const result = await ApiClient.decryptFile(file, key);
      if (result.error) {
        if (result.error === "Policy not satisfied") {
          setDecryptionSuccess(false);
        }
        throw new Error(result.error);
      }
      if (result.data) {
        setDecryptedFile(result.data);
        setDecryptionSuccess(true);
      }
    } catch (error) {
      console.error("Error decrypting file:", error);
      toast("Failed to decrypt", {
        description:
          error instanceof Error && error.message === "Policy not satisfied"
            ? "Your private key does not satisfy the access policy"
            : "There was an error decrypting your file",
      });
    } finally {
      setIsDecrypting(false);
    }
  };

  const handleReset = () => {
    setFile(null);
    setKey(null);
    setDecryptedFile(null);
    setDecryptionSuccess(null);
  };

  if (decryptionSuccess !== null) {
    return (
      <DecryptionResult
        success={decryptionSuccess}
        decryptedFile={decryptedFile || undefined}
        originalFileName={file?.name.replace(/\.cpabe$/, "") || ""}
        onReset={handleReset}
      />
    );
  }

  return (
    <div className="space-y-4 max-w-2xl w-full mx-auto p-6">
      <div>
        <h1 className="text-3xl font-bold">Decrypt File</h1>
        <p className="text-muted-foreground py-1.5">
          Upload an encrypted file and your private key to decrypt it
        </p>
      </div>

      <FileUpload
        file={file}
        onFileSelect={setFile}
        onFileUpload={setFile}
        disabled={isDecrypting}
        forDecryption={true}
      />

      <KeyUpload
        file={key}
        onFileSelect={setKey}
        onKeyUpload={setKey}
        disabled={isDecrypting}
      />

      <Button
        onClick={handleDecrypt}
        disabled={!file || !key || isDecrypting}
        className="w-full"
      >
        {isDecrypting ? "Decrypting..." : "Decrypt File"}
      </Button>
    </div>
  );
}
