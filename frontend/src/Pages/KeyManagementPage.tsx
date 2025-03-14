"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Badge } from "@/components/ui/badge"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { toast } from "sonner"
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
      toast("Failed to delete key", { description: "There was an error deleting the key" })
    } finally {
      setIsDeleting(false)
      setSelectedKey(null)
    }
  }

  const handleDownloadKey = (keyId: string) => {
    const keyContent = `Key ID: ${keyId}\nKey Data: ***********`
    const blob = new Blob([keyContent], { type: "text/plain" })
    const url = URL.createObjectURL(blob)
    const a = document.createElement("a")
    a.href = url
    a.download = `${keyId}.txt`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
    toast("Key downloaded", { description: "Your key has been downloaded successfully" })
  }

  const masterKeys = [{ id: "mk-1", name: "Master Key 1", created: "2025-01-15", status: "active" }]

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Key Management</h1>
          <p className="text-muted-foreground py-1.5">Manage your cryptographic keys for secure access control</p>
        </div>
        <Button onClick={handleGenerateKey} disabled={isGenerating} size="lg">
          {isGenerating ? <RefreshCw className="mr-2 h-4 w-4 animate-spin" /> : <Plus className="mr-2 h-4 w-4" />}
          {isGenerating ? "Generating..." : "Generate New Key"}
        </Button>
      </div>

      <Tabs defaultValue="master">
        <TabsList className="grid w-full grid-cols-3 min-w-[900px]">
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
                <CardFooter className="flex justify-end gap-2">
                  <Button
                    variant="ghost"
                    size="icon"
                    className="h-8 w-8 text-muted-foreground hover:text-foreground"
                    onClick={() => handleDownloadKey(key.id)}
                    title="Download key"
                  >
                    <Download className="h-4 w-4" />
                  </Button>
                  <Dialog>
                    <DialogTrigger asChild>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-8 w-8 text-muted-foreground hover:text-destructive"
                        onClick={() => setSelectedKey(key.id)}
                        title="Delete key"
                      >
                        <Trash className="h-4 w-4" />
                      </Button>
                    </DialogTrigger>
                    <DialogContent>
                      <DialogHeader>
                        <DialogTitle>Delete Master Key</DialogTitle>
                        <DialogDescription>
                          Are you sure you want to delete this master key? This action cannot be undone.
                        </DialogDescription>
                      </DialogHeader>
                      <DialogFooter>
                        <Button variant="outline" onClick={() => setSelectedKey(null)}>
                          Cancel
                        </Button>
                        <Button variant="destructive" onClick={() => handleDeleteKey(key.id)} disabled={isDeleting}>
                          {isDeleting ? "Deleting..." : "Delete"}
                        </Button>
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
