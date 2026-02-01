<template>
  <div class="register">
    <h2>Register</h2>
    <form @submit.prevent="register">
      <input v-model="username" placeholder="Username" required />
      <input v-model="email" type="email" placeholder="Email" required />
      <input v-model="password" type="password" placeholder="Password" required />
      <input v-model="confirmPassword" type="password" placeholder="Confirm Password" required />
      <button type="submit" :disabled="loading">
        {{ loading ? 'Εγγραφή...' : 'Register' }}
      </button>
    </form>
    <div v-if="error" class="error">{{ error }}</div>
    <div v-if="success" class="success">{{ success }}</div>
    <div class="links">
      <router-link to="/login">Έχετε ήδη λογαριασμό; Συνδεθείτε</router-link>
    </div>
  </div>
</template>

<script>
import api from '../api';

export default {
  data() {
    return {
      username: '',
      email: '',
      password: '',
      confirmPassword: '',
      error: '',
      success: '',
      loading: false
    };
  },
  methods: {
    async register() {
      this.error = '';
      this.success = '';
      this.loading = true;
      
      if (this.password !== this.confirmPassword) {
        this.error = 'Passwords do not match';
        this.loading = false;
        return;
      }
      
      try {
        await api.post('/auth/register', {
          username: this.username,
          email: this.email,
          password: this.password
        });
        
        this.success = 'Registration successful! Please check your email to verify your account.';
        this.username = '';
        this.email = '';
        this.password = '';
        this.confirmPassword = '';
        
        // Redirect to login after 3 seconds
        setTimeout(() => {
          this.$router.push('/login');
        }, 3000);
        
      } catch (e) {
        console.error('Register error:', e);
        console.error('Error response:', e.response);
        console.error('Error data:', e.response?.data);
        
        // Better error message handling
        if (e.response?.data?.error) {
          this.error = e.response.data.error;
        } else if (e.response?.data?.message) {
          this.error = e.response.data.message;
        } else if (e.response?.status === 400) {
          this.error = 'Invalid registration data. Please check your input.';
        } else if (e.response?.status === 500) {
          this.error = 'Server error. Please try again later.';
        } else if (e.message) {
          this.error = `Registration failed: ${e.message}`;
        } else {
          this.error = 'Registration failed. Please try again.';
        }
      } finally {
        this.loading = false;
      }
    }
  }
};
</script>

<style scoped>
.register {
  max-width: 400px;
  margin: 2rem auto;
  padding: 2rem;
  border: 1px solid #ccc;
  border-radius: 8px;
  background: #fafafa;
}

.error {
  color: #b00;
  margin-top: 1rem;
  padding: 0.5rem;
  background: #ffe6e6;
  border-radius: 4px;
}

.success {
  color: #0a0;
  margin-top: 1rem;
  padding: 0.5rem;
  background: #e6ffe6;
  border-radius: 4px;
}

input {
  width: 100%;
  padding: 0.5rem;
  margin: 0.5rem 0;
  border: 1px solid #ccc;
  border-radius: 4px;
}

button {
  width: 100%;
  padding: 0.75rem;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin-top: 1rem;
}

button:hover:not(:disabled) {
  background: #0056b3;
}

button:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.links {
  margin-top: 1rem;
  text-align: center;
}

.links a {
  color: #007bff;
  text-decoration: none;
}

.links a:hover {
  text-decoration: underline;
}
</style> 