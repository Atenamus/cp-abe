import { Button } from "@/components/ui/button";
import { ChevronRight } from "lucide-react";
import { Link } from "react-router";

export default function ContentSection() {
  return (
    <section className="py-16 md:pt-32">
      <div className="mx-auto max-w-6xl px-6">
        <div className="grid gap-6 md:grid-cols-2 md:gap-12">
          <h2 className="text-4xl font-medium ">
            Secure, Attribute-Based Encryption for Data Privacy
          </h2>
          <div className="space-y-6">
            <p>
              CP-ABE (Ciphertext-Policy Attribute-Based Encryption) is an
              advanced cryptographic technique that enables fine-grained access
              control over encrypted data. It ensures that only users with the
              right attributes can decrypt specific information, making it ideal
              for secure data sharing.
            </p>
            <p>
              Unlike traditional encryption, CP-ABE uses{" "}
              <span className="font-bold">policies</span> instead of keys to
              manage access. This allows for{" "}
              <span className="font-bold">dynamic and flexible</span> security,
              ensuring confidentiality and controlled access without relying on
              a central authority.
            </p>
            <Button
              asChild
              variant="secondary"
              size="sm"
              className="gap-1 pr-1.5"
            >
              <Link to="#">
                <span>Learn More</span>
                <ChevronRight className="size-2" />
              </Link>
            </Button>
          </div>
        </div>
      </div>
    </section>
  );
}
