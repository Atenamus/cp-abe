import HeroSection from "./components/hero-section";
import ContentSection from "./components/content";
import FeaturesSection from "./components/features";
import CallToAction from "./components/call-to-action";
import FooterSection from "./components/footer";
import { HowItWorks } from "./components/how-it-works";
import accessPolicy from "./assets/access-policy.jpg";
import encryption from "./assets/data-encryption.jpg";
import attribute from "./assets/attribute.jpg";
import access from "./assets/update-access.jpg";
const features = [
  {
    step: "Step 1",
    title: "Define Access Policies",
    content:
      "Create encryption policies that determine who can access specific data based on attributes.",
    image: accessPolicy,
  },
  {
    step: "Step 2",
    title: "Encrypt the Data",
    content:
      "Apply CP-ABE encryption to secure data while ensuring controlled access without central authority.",
    image: encryption,
  },
  {
    step: "Step 3",
    title: "Attribute-Based Decryption",
    content:
      "Users with matching attributes decrypt the data while unauthorized users remain restricted.",
    image: attribute,
  },
  {
    step: "Step 4",
    title: "Manage and Update Access",
    content:
      "Dynamically update encryption policies to manage access control efficiently over time.",
    image: access,
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
      {/* <CallToAction /> */}
      <FooterSection />
    </>
  );
}

export default App;
