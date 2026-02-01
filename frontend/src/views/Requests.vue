<template>
  <div>
    <h2>Αιτήσεις Υιοθεσίας</h2>
    <button v-if="hasRole('ROLE_ADMIN') || hasRole('ROLE_SHELTER')" @click="showForm = true">Νέα Αίτηση</button>
    <form v-if="showForm" @submit.prevent="createRequest">
      <input v-model="newRequest.name" placeholder="Όνομα" required />
      <input v-model="newRequest.type" placeholder="Είδος" required />
      <select v-model="newRequest.gender" required>
        <option value="Male">Αρσενικό</option>
        <option value="Female">Θηλυκό</option>
      </select>
      <input v-model="newRequest.age" type="number" min="0" placeholder="Ηλικία" required />
      <button type="submit">Υποβολή</button>
      <button type="button" @click="showForm = false">Άκυρο</button>
    </form>
    <div v-if="requests.length > 0" class="requests-list">
      <div v-for="r in requests" :key="r.id" class="request-card">
        <div class="request-content">
          <div class="request-image-container">
            <div v-if="r.imageUrl" class="request-image">
              <img :src="getRequestImageUrl(r.imageUrl)" :alt="r.name" @error="handleImageError" />
            </div>
            <div v-else class="request-image-placeholder">
              <span>Δεν υπάρχει εικόνα</span>
            </div>
          </div>
          <div class="request-info">
            <h3>{{ r.name }}</h3>
            <p><strong>Είδος:</strong> {{ r.type }}</p>
            <p><strong>Φύλο:</strong> {{ r.gender === 'Male' ? 'Αρσενικό' : 'Θηλυκό' }}</p>
            <p><strong>Ηλικία:</strong> {{ r.age }} {{ r.age === 1 ? 'έτος' : 'έτη' }}</p>
            <div class="request-actions">
              <router-link :to="`/requests/${r.id}`" class="btn">Λεπτομέρειες</router-link>
              <button v-if="hasRole('ROLE_ADMIN')" @click="adminApprove(r.id)" class="btn-approve">Admin Approve</button>
              <button v-if="hasRole('ROLE_DOCTOR') || hasRole('ROLE_ADMIN')" @click="doctorApprove(r.id)" class="btn-approve">Doctor Approve</button>
              <button @click="deleteRequest(r.id)" class="btn-delete">Διαγραφή</button>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-else>Καμία αίτηση βρέθηκε.</div>
  </div>
</template>

<script>
function parseJwt(token) {
  if (!token) return {};
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));
    return JSON.parse(jsonPayload);
  } catch (e) {
    return {};
  }
}

export default {
  data() {
    return {
      showForm: false,
      newRequest: { name: '', type: '', gender: 'Male', age: 0 },
      requests: []
    }
  },
  mounted() {
    fetch('http://localhost:8080/api/requests', {
      headers: { Authorization: `Bearer ${localStorage.getItem('jwt_token')}` }
    })
      .then(r => r.json())
      .then(data => (this.requests = data))
      .catch(() => alert('Σφάλμα ανάκτησης αιτήσεων'))
  },
  methods: {
    async createRequest() {
      await fetch('http://localhost:8080/api/requests', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${localStorage.getItem('jwt_token')}`
        },
        body: JSON.stringify(this.newRequest)
      });
      this.showForm = false;
      this.newRequest = { name: '', type: '', gender: 'Male', age: 0 };
      this.reload();
    },
    adminApprove(id) {
      fetch(`http://localhost:8080/api/requests/Approve/${id}`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${localStorage.getItem('jwt_token')}` }
      })
        .then(() => this.reload())
        .catch(() => alert('Σφάλμα admin έγκρισης'))
    },
    doctorApprove(id) {
      fetch(`http://localhost:8080/api/requests/ApproveD/${id}`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${localStorage.getItem('jwt_token')}` }
      })
        .then(() => this.reload())
        .catch(() => alert('Σφάλμα doctor έγκρισης'))
    },
    deleteRequest(id) {
      fetch(`http://localhost:8080/api/requests/${id}`, {
        method: 'DELETE',
        headers: { Authorization: `Bearer ${localStorage.getItem('jwt_token')}` }
      })
        .then(response => {
          if (!response.ok) {
            throw new Error('Delete failed');
          }
          this.reload();
        })
        .catch(() => {
          alert('Σφάλμα διαγραφής');
        });
    },
    reload() {
      fetch('http://localhost:8080/api/requests', {
        headers: { Authorization: `Bearer ${localStorage.getItem('jwt_token')}` }
      })
        .then(r => {
          if (!r.ok) throw new Error('Failed to fetch requests');
          return r.json();
        })
        .then(data => {
          this.requests = data;
        })
        .catch(() => {
          this.requests = [];
        });
    },
    hasRole(role) {
      const token = localStorage.getItem('jwt_token');
      const payload = parseJwt(token);
      const roles = payload.roles ? payload.roles.map(r => r.name ? r.name.toLowerCase() : r.toLowerCase()) : [];
      return roles.includes(role.toLowerCase());
    },
    getRequestImageUrl(imageUrl) {
      if (!imageUrl) return '';
      return `http://localhost:8080/api/files/image/${imageUrl}`;
    },
    handleImageError(event) {
      event.target.style.display = 'none';
      const placeholder = event.target.parentElement.querySelector('.request-image-placeholder');
      if (placeholder) {
        placeholder.style.display = 'flex';
      }
    }
  }
}
</script>

<style scoped>
.requests-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.request-card {
  border: 1px solid #ddd;
  padding: 15px;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  transition: box-shadow 0.3s;
}

.request-card:hover {
  box-shadow: 0 4px 8px rgba(0,0,0,0.15);
}

.request-content {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.request-image-container {
  flex-shrink: 0;
  width: 250px;
  height: 200px;
}

.request-image {
  width: 100%;
  height: 100%;
  overflow: hidden;
  border-radius: 8px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.request-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.request-image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e0e0e0;
  color: #666;
  border-radius: 8px;
  font-size: 14px;
  text-align: center;
  padding: 10px;
}

.request-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.request-info h3 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 1.5em;
}

.request-info p {
  margin: 5px 0;
  color: #555;
}

.request-info strong {
  color: #333;
}

.request-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 15px;
}

.btn, .btn-approve, .btn-delete {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  text-decoration: none;
  display: inline-block;
  font-size: 14px;
  transition: background-color 0.3s;
}

.btn {
  background: #007bff;
  color: white;
}

.btn:hover {
  background: #0056b3;
}

.btn-approve {
  background: #28a745;
  color: white;
}

.btn-approve:hover {
  background: #218838;
}

.btn-delete {
  background: #dc3545;
  color: white;
}

.btn-delete:hover {
  background: #c82333;
}

@media (max-width: 768px) {
  .request-content {
    flex-direction: column;
  }
  
  .request-image-container {
    width: 100%;
    height: 250px;
  }
}
</style>
