import type { Metadata } from 'next'
import './globals.css'

export const metadata: Metadata = {
  title: 'Cp-abe',
  description: 'Created with me',
  generator: 'me',
}

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode
}>) {
  return (
    <html lang="en">
      <body>{children}</body>
    </html>
  )
}
