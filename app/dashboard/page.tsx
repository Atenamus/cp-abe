import { Mail, Phone, Facebook, Instagram } from "lucide-react";

const Footer = () => {
  return (
    <footer className="bg-blue-800 text-white py-12">
      <div className="container mx-auto px-8">
        <div className="grid md:grid-cols-3 gap-8 text-center md:text-left">
          
          {/* Contact Info Card */}
          <div className="bg-white p-6 rounded-lg shadow-lg shadow-blue-400 text-gray-800">
            <h3 className="text-2xl font-semibold text-blue-900">Contact Us</h3>
            <p className="mt-2 flex items-center justify-center md:justify-start">
              <Phone className="w-5 h-5 text-blue-600 mr-2" /> +123 456 7890
            </p>
            <p className="mt-2 flex items-center justify-center md:justify-start">
              <Mail className="w-5 h-5 text-blue-600 mr-2" /> support@cypherguard.com
            </p>
          </div>

          {/* Send Message Card */}
          <div className="bg-white p-6 rounded-lg shadow-lg shadow-blue-400 text-gray-800">
            <h3 className="text-2xl font-semibold text-blue-900">Send Us a Message</h3>
            <form className="mt-4">
              <input
                type="email"
                placeholder="Your Email"
                className="w-full p-2 border border-gray-300 rounded-md mb-2"
              />
              <textarea
                placeholder="Your Message"
                className="w-full p-2 border border-gray-300 rounded-md mb-2"
                rows="3"
              ></textarea>
              <button
                type="submit"
                className="bg-blue-600 hover:bg-blue-700 text-white font-semibold py-2 px-4 rounded-md w-full"
              >
                Send
              </button>
            </form>
          </div>

          {/* Social Media Card */}
          <div className="bg-white p-6 rounded-lg shadow-lg shadow-blue-400 text-gray-800">
            <h3 className="text-2xl font-semibold text-blue-900">Follow Us</h3>
            <div className="flex justify-center md:justify-start space-x-4 mt-4">
              <a href="#" className="hover:text-blue-600">
                <Instagram className="w-6 h-6 text-blue-600" />
              </a>
              <a href="#" className="hover:text-blue-600">
                <Facebook className="w-6 h-6 text-blue-600" />
              </a>
            </div>
          </div>

        </div>

        {/* Copyright Section */}
        <div className="mt-12 text-center border-t border-white/20 pt-6 text-gray-200">
          <p>&copy; {new Date().getFullYear()} Cypher Guard. All rights reserved.</p>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
