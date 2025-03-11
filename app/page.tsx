import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import Link from "next/link"
import { ArrowRight, Key, Lock, Shield, FileText } from "lucide-react"
import Image from "next/image"

export default function Home() {
  return (
    <div className="relative flex min-h-screen flex-col bg-black text-white">
      <Image
        src="/futuristic-bg-1.jpg"
        alt="Futuristic Background"
        layout="fill"
        objectFit="cover"
        className="absolute inset-0 -z-10 opacity-70"
      />
      <header className="border-b border-gray-700 bg-black/40 backdrop-blur-lg py-4">
        <div className="container flex h-16 items-center px-4 sm:px-6 lg:px-8">
          <h1 className="text-xl font-bold text-white drop-shadow-md">CryptoGuard</h1>
          <div className="ml-auto flex items-center space-x-4">
            <Button asChild variant="ghost" size="sm" className="text-white border border-white hover:bg-white hover:text-black">
              <Link href="/login">Login</Link>
            </Button>
            <Button asChild size="sm" className="bg-white text-black hover:bg-gray-300">
              <Link href="/register">Register</Link>
            </Button>
          </div>
        </div>
      </header>
      <main className="flex-1">
        <section className="container grid items-center gap-6 pb-8 pt-6 md:py-10 text-center">
          <h1 className="text-5xl font-extrabold leading-tight tracking-wider text-white drop-shadow-lg">
            Secure Access Control
          </h1>
          <p className="max-w-[700px] mx-auto text-lg text-gray-200">
            Protect your sensitive data with fine-grained access control policies. Encrypt once, control access forever.
          </p>
          <div className="flex justify-center gap-4">
            <Button asChild className="bg-white text-black hover:bg-gray-300">
              <Link href="/register">
                Get Started
                <ArrowRight className="ml-2 h-4 w-4" />
              </Link>
            </Button>
            <Button variant="outline" asChild className="border border-white text-white hover:bg-white hover:text-black">
              <Link href="/learn">Learn More</Link>
            </Button>
          </div>
        </section>
        <section className="container py-12 grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
          {[{ icon: Shield, title: "Authentication", desc: "Secure user authentication with registration and login functionality." },
            { icon: Key, title: "Key Management", desc: "Generate, view, and manage your cryptographic keys securely." },
            { icon: Lock, title: "Policy Building", desc: "Create attribute-based access policies with an intuitive builder interface." },
            { icon: FileText, title: "File Encryption", desc: "Encrypt files with your defined policies and manage encrypted documents." }].map((item, index) => (
              <Card key={index} className="bg-black/50 border border-gray-500 shadow-lg backdrop-blur-lg">
                <CardHeader className="flex flex-col items-center text-center">
                  <item.icon className="h-10 w-10 text-white" />
                  <CardTitle className="mt-2 text-white">{item.title}</CardTitle>
                </CardHeader>
                <CardContent>
                  <CardDescription className="text-gray-300">{item.desc}</CardDescription>
                </CardContent>
              </Card>
          ))}
        </section>
      </main>
      <footer className="border-t border-gray-700 bg-black/40 backdrop-blur-lg py-4">
        <div className="container flex justify-center text-sm text-gray-400">
          © 2025 CryptoGuard. All rights reserved.
        </div>
      </footer>
    </div>
  )
}
