import { Lock, Key, ShieldCheck, Users } from "lucide-react";
import features from "../assets/features-2.jpeg";
import featuresDark from "../assets/features-2-dark.jpeg";

export default function FeaturesSection() {
  return (
    <section className="py-16 md:py-24">
      <div className="mx-auto max-w-6xl px-6">
        <div className="grid items-center gap-12 md:grid-cols-2 md:gap-12 lg:grid-cols-5 lg:gap-24">
          <div className="border-border/50 relative rounded-3xl border p-3 lg:col-span-3">
            <div className="bg-linear-to-b aspect-76/59 relative rounded-2xl from-zinc-300 to-transparent p-px dark:from-zinc-700">
              <img
                src={features}
                className="hidden rounded-[15px] dark:block"
                alt="payments illustration dark"
                width={1207}
                height={929}
              />
              <img
                src={features}
                className="rounded-[15px] shadow dark:hidden"
                alt="payments illustration light"
                width={1207}
                height={929}
              />
            </div>
          </div>
          <div className="lg:col-span-2">
            <div className="md:pr-6 lg:pr-0">
              <h2 className="text-4xl font-medium">
                Fine-Grained Access Control
              </h2>
              <p className="mt-6">
                Ciphertext-Policy Attribute-Based Encryption (CP-ABE) allows
                data to be encrypted with specific policies, ensuring that only
                authorized users can decrypt it.
              </p>
            </div>
            <ul className="mt-8 divide-y border-y *:flex *:items-center *:gap-3 *:py-3">
              <li>
                <Lock className="size-5" />
                Policy-Driven Encryption
              </li>
              <li>
                <Key className="size-5" />
                Attribute-Based Decryption
              </li>
              <li>
                <ShieldCheck className="size-5" />
                Secure and Scalable Access
              </li>
              <li>
                <Users className="size-5" />
                Flexible User Management
              </li>
            </ul>
          </div>
        </div>
      </div>
    </section>
  );
}
