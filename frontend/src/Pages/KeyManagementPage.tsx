import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Badge } from "@/components/ui/badge"
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle, DialogTrigger } from "@/components/ui/dialog"
import {toast} from "sonner";
import { Download, Eye, EyeOff, Key, Plus, RefreshCw, Trash } from "lucide-react"

export default function KeyManagementPage() {
  const [showPrivateKey, setShowPrivateKey] = useState(false)
  const [isGenerating, setIsGenerating] = useState(false)
  const [isDeleting, setIsDeleting] = useState(false)
  const [selectedKey, setSelectedKey] = useState<string | null>(null)

  const handleGenerateKey = async () => {
    setIsGenerating(true)
    try {
      await new Promise((resolve) => setTimeout(resolve, 1500))
      toast("Key generated successfully", { description: "Your new key pair has been created" })
    } catch {
      toast("Failed to generate key", { description: "There was an error generating your key pair" })
    } finally {
      setIsGenerating(false)
    }
  }

  const handleDeleteKey = async (keyId: string) => {
    setIsDeleting(true)
    try {
      await new Promise((resolve) => setTimeout(resolve, 1000))
      toast("Key deleted", { description: "The key has been deleted successfully" })
    } catch {
      toast("Failed to delete key", { description: "There was an error deleting the key"})
    } finally {
      setIsDeleting(false)
      setSelectedKey(null)
    }
  }

  const handleDownloadKey = (keyId: string, keyType: string) => {
    toast("Key downloaded", { description: `Your ${keyType} key has been downloaded` })
  }

  const masterKeys = [{ id: "mk-1", name: "Master Key 1", created: "2025-01-15", status: "active" }]

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Key Management</h1>
          <p className="text-muted-foreground">Manage your cryptographic keys for secure access control</p>
        </div>
        <Button onClick={handleGenerateKey} disabled={isGenerating} size="lg">
          {isGenerating ? <RefreshCw className="mr-2 h-4 w-4 animate-spin" /> : <Plus className="mr-2 h-4 w-4" />}
          {isGenerating ? "Generating..." : "Generate New Key"}
        </Button>
      </div>

      <Tabs defaultValue="master">
        <TabsList className="grid w-full grid-cols-3">
          <TabsTrigger value="master">Master Keys</TabsTrigger>
          <TabsTrigger value="public">Public Keys</TabsTrigger>
          <TabsTrigger value="private">Private Keys</TabsTrigger>
        </TabsList>

        <TabsContent value="master" className="space-y-4">
          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {masterKeys.map((key) => (
              <Card key={key.id} className="shadow-md border border-muted rounded-xl">
                <CardHeader>
                  <div className="flex items-center justify-between">
                    <CardTitle>{key.name}</CardTitle>
                    <Badge>{key.status}</Badge>
                  </div>
                  <CardDescription>Created on {key.created}</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="flex items-center space-x-2 rounded-md border p-2">
                    <Key className="h-4 w-4 text-muted-foreground" />
                    <div className="text-xs font-medium">
                      {showPrivateKey ? "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6" : "••••••••••••••••••••••••••••••••"}
                    </div>
                    <Button variant="ghost" size="icon" className="ml-auto h-6 w-6" onClick={() => setShowPrivateKey(!showPrivateKey)}>
                      {showPrivateKey ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                    </Button>
                  </div>
                </CardContent>
                <CardFooter className="flex gap-2">
                  <Button variant="outline" size="sm" className="w-full" onClick={() => handleDownloadKey(key.id, "master")}>Download</Button>
                  <Dialog>
                    <DialogTrigger asChild>
                      <Button variant="destructive" size="sm" className="w-full" onClick={() => setSelectedKey(key.id)}>Delete</Button>
                    </DialogTrigger>
                    <DialogContent>
                      <DialogHeader>
                        <DialogTitle>Delete Master Key</DialogTitle>
                        <DialogDescription>Are you sure you want to delete this master key? This action cannot be undone.</DialogDescription>
                      </DialogHeader>
                      <DialogFooter>
                        <Button variant="outline" onClick={() => setSelectedKey(null)}>Cancel</Button>
                        <Button variant="destructive" onClick={() => handleDeleteKey(key.id)} disabled={isDeleting}>{isDeleting ? "Deleting..." : "Delete"}</Button>
                      </DialogFooter>
                    </DialogContent>
                  </Dialog>
                </CardFooter>
              </Card>
            ))}
          </div>
        </TabsContent>
      </Tabs>
    </div>
  )
}
