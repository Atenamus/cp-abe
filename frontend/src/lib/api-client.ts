import { auth } from "./auth";

const API_URL = "http://localhost:8080/api";

interface ApiResponse<T = any> {
  data?: T;
  error?: string;
}

export interface EncryptedFile {
  id: number;
  name: string;
  fullName: string;
  path: string;
  size: number;
  createdAt: string;
}

export interface UserActivity {
  id: number;
  type:
    | "file_encrypted"
    | "file_decrypted"
    | "policy_created"
    | "policy_updated"
    | "policy_deleted"
    | "key_generated";
  resourceName: string;
  details: string;
  timestamp: string;
}

export class ApiClient {
  private static async request<T>(
    endpoint: string,
    options: RequestInit = {}
  ): Promise<ApiResponse<T>> {
    try {
      const token = auth.getToken();
      const headers = {
        ...options.headers,
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      };

      const response = await fetch(`${API_URL}${endpoint}`, {
        ...options,
        headers,
      });

      if (response.status === 401) {
        auth.logout();
        throw new Error("Unauthorized");
      }

      if (!response.ok) {
        const error = await response.text();
        throw new Error(error || "An error occurred");
      }

      // Check if the response is a blob
      const contentType = response.headers.get("content-type");
      if (contentType?.includes("application/octet-stream")) {
        const blob = await response.blob();
        return { data: blob as unknown as T };
      }

      // For JSON responses
      if (contentType?.includes("application/json")) {
        const data = await response.json();
        return { data };
      }

      // For text responses
      const text = await response.text();
      return { data: text as unknown as T };
    } catch (error) {
      if (error instanceof Error) {
        return { error: error.message };
      }
      return { error: "An unknown error occurred" };
    }
  }

  static async get<T>(endpoint: string) {
    return this.request<T>(endpoint, { method: "GET" });
  }

  static async post<T>(endpoint: string, data?: FormData | object) {
    const options: RequestInit = {
      method: "POST",
    };

    if (data instanceof FormData) {
      options.body = data;
    } else if (data) {
      options.headers = {
        "Content-Type": "application/json",
      };
      options.body = JSON.stringify(data);
    }

    return this.request<T>(endpoint, options);
  }

  static async delete<T>(endpoint: string) {
    return this.request<T>(endpoint, { method: "DELETE" });
  }

  // CP-ABE specific methods
  static async encryptFile(file: File, policy: string) {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("policy", policy);
    return this.post<Blob>("/cpabe/encrypt", formData);
  }

  static async decryptFile(file: File, keyFile: File) {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("key", keyFile);
    return this.post<Blob>("/cpabe/decrypt", formData);
  }

  static async listKeys() {
    return this.get("/cpabe/keys");
  }

  static async generateKey(attributes: string[]) {
    return this.post("/cpabe/keygen", { attributes });
  }

  static async deleteKey(keyId: string) {
    return this.delete(`/cpabe/keys/${keyId}`);
  }

  // New methods for encrypted files
  static async listEncryptedFiles() {
    return this.get<EncryptedFile[]>("/cpabe/files");
  }

  static async downloadEncryptedFile(filename: string) {
    return this.get<Blob>(
      `/cpabe/files/download?filename=${encodeURIComponent(filename)}`
    );
  }

  static async getRecentActivities() {
    return this.get<UserActivity[]>("/activity/recent");
  }

  static async listPolicies<T = any>() {
    return this.get<T>("/user/get-policy");
  }

  static async validateKey(keyFile: File) {
    const formData = new FormData();
    formData.append("key", keyFile);
    return this.post<{
      valid: boolean;
      attributes: string[];
      issuedTo: string;
      issuedOn: string;
      expiresOn: string;
      message?: string;
    }>("/cpabe/validate-key", formData);
  }
}
