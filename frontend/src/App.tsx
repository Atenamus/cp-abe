import HeroSection from "./components/hero-section";
import ContentSection from "./components/content";
import FeaturesSection from "./components/features";
import CallToAction from "./components/call-to-action";
import FooterSection from "./components/footer";
import { HowItWorks } from "./components/how-it-works";

const features = [
  {
    step: "Step 1",
    title: "Define Access Policies",
    content:
      "Create encryption policies that determine who can access specific data based on attributes.",
    image:
      "https://images.unsplash.com/photo-1723958929247-ef054b525153?q=80&w=2070&auto=format&fit=crop",
  },
  {
    step: "Step 2",
    title: "Encrypt the Data",
    content:
      "Apply CP-ABE encryption to secure data while ensuring controlled access without central authority.",
    image:
      "https://images.unsplash.com/photo-1723931464622-b7df7c71e380?q=80&w=2070&auto=format&fit=crop",
  },
  {
    step: "Step 3",
    title: "Attribute-Based Decryption",
    content:
      "Users with matching attributes decrypt the data while unauthorized users remain restricted.",
    image:
      "https://images.unsplash.com/photo-1725961476494-efa87ae3106a?q=80&w=2070&auto=format&fit=crop",
  },
  {
    step: "Step 4",
    title: "Manage and Update Access",
    content:
      "Dynamically update encryption policies to manage access control efficiently over time.",
    image:
      "https://images.unsplash.com/photo-1723931464622-b7df7c71e380?q=80&w=2070&auto=format&fit=crop",
  },
];

function App() {
  return (
    <>
      <HeroSection />
      <ContentSection />
      <FeaturesSection />
      <HowItWorks
        features={features}
        title="The CP-ABE Workflow"
        autoPlayInterval={4000}
        imageHeight="h-[500px]"
      />
      <CallToAction />
      <FooterSection />
    </>
  );
}

export default App;
