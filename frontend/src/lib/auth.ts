const API_URL = 'http://localhost:8080/api/auth';

export interface SignInData {
    email: string;
    password: string;
}

export interface SignUpData {
    fullName: string;
    email: string;
    password: string;
    attributes: string[];
}

export const auth = {
    async signIn(data: SignInData) {
        const response = await fetch(`${API_URL}/signin`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data),
        });

        if (!response.ok) {
            throw new Error('Authentication failed');
        }

        const result = await response.json();
        localStorage.setItem('token', result.token);
        return result;
    },

    async signUp(data: SignUpData) {
        const response = await fetch(`${API_URL}/signup`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data),
        });

        if (!response.ok) {
            throw new Error('Registration failed');
        }

        const result = await response.json();
        localStorage.setItem('token', result.token);
        return result;
    },

    getToken() {
        return localStorage.getItem('token');
    },

    isAuthenticated() {
        return !!this.getToken();
    },

    logout() {
        localStorage.removeItem('token');
    },
};