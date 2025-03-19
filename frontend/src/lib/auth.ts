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

    async validateToken(token: string) {
        try {
            const response = await fetch(`${API_URL}/validate`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            return response.ok;
        } catch {
            return false;
        }
    },

    getToken() {
        return localStorage.getItem('token');
    },

    async isAuthenticated() {
        const token = this.getToken();
        if (!token) return false;
        
        const isValid = await this.validateToken(token);
        if (!isValid) {
            this.logout();
            return false;
        }
        return true;
    },

    logout() {
        localStorage.removeItem('token');
        window.location.href = '/sign-in';
    }
};