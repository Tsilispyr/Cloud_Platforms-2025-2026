<template>
  <div>
    <h2>Διαθέσιμα Ζώα</h2>
    <router-link v-if="hasRole('ROLE_ADMIN') || hasRole('ROLE_SHELTER')" to="/animals/add" class="btn">Προσθήκη Ζώου</router-link>
    <div v-if="availableAnimals.length === 0">Κανένα ζώο διαθέσιμο.</div>
    <ul v-else>
      <li v-for="a in availableAnimals" :key="a.id" class="animal-card">
        <div class="animal-content">
          <div class="animal-image-container">
            <div v-if="a.imageUrl" class="animal-image">
              <img :src="getAnimalImageUrl(a.imageUrl)" :alt="a.name" @error="handleImageError" />
            </div>
            <div v-else class="animal-image-placeholder">
              <span>Δεν υπάρχει εικόνα</span>
            </div>
          </div>
          <div class="animal-info">
            <h3>{{ a.name }}</h3>
            <p><strong>Είδος:</strong> {{ a.type }}</p>
            <p><strong>Φύλο:</strong> {{ a.gender === 'Male' ? 'Αρσενικό' : 'Θηλυκό' }}</p>
            <p><strong>Ηλικία:</strong> {{ a.age }} {{ a.age === 1 ? 'έτος' : 'έτη' }}</p>
            <div class="animal-actions">
                    <button @click="requestAnimal(a.id)" v-if="a.req === 0" class="btn-request">Αίτηση Υιοθεσίας</button>
              <template v-if="a.req === 1 && (hasRole('ROLE_ADMIN') || hasRole('ROLE_SHELTER'))">
                <button @click="approveAnimal(a.id)" class="btn-approve">Έγκριση</button>
                <button @click="denyAnimal(a.id)" class="btn-deny">Απόρριψη</button>
              </template>
              <button @click="deleteAnimal(a.id)" v-if="hasRole('ROLE_ADMIN')" class="btn-delete">Διαγραφή</button>
            </div>
          </div>
        </div>
      </li>
    </ul>
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
    return { animals: [] }
  },
  mounted() {
    this.loadAnimals();
  },
  computed: {
    availableAnimals() {
      // Φιλτράρουμε μόνο τα ζώα που δεν έχουν αίτηση (req === 0)
      return this.animals.filter(a => a.req === 0);
    }
  },
  methods: {
    async requestAnimal(id) {
      try {
        const response = await fetch(`http://localhost:8080/api/animals/Request/${id}`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            Authorization: `Bearer ${localStorage.getItem('jwt_token')}`
          }
        });
        
        if (!response.ok) {
          throw new Error('Failed to submit request');
        }
        
        // Αφαιρούμε το ζώο από τη λίστα (το φιλτράρουμε με computed property)
        this.loadAnimals();
        
        // Ανακατεύθυνση στη σελίδα αιτήσεων
        this.$router.push('/requests');
      } catch (error) {
        alert('Σφάλμα κατά την υποβολή της αίτησης');
        console.error(error);
      }
    },
    async approveAnimal(id) {
      await fetch(`http://localhost:8080/api/animals/${id}/accept-adoption`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${localStorage.getItem('jwt_token')}` }
      });
      this.reload();
    },
    async denyAnimal(id) {
      await fetch(`http://localhost:8080/api/animals/Deny/${id}`, {
        method: 'PUT',
        headers: { Authorization: `Bearer ${localStorage.getItem('jwt_token')}` }
      });
      this.reload();
    },
    async deleteAnimal(id) {
      await fetch(`http://localhost:8080/api/animals/${id}`, {
        method: 'DELETE',
        headers: { Authorization: `Bearer ${localStorage.getItem('jwt_token')}` }
      });
      this.reload();
    },
    loadAnimals() {
      fetch('http://localhost:8080/api/animals', {
        headers: { Authorization: `Bearer ${localStorage.getItem('jwt_token')}` }
      })
        .then(r => r.json())
        .then(data => (this.animals = data))
        .catch(() => (this.animals = []));
    },
    reload() {
      this.loadAnimals();
    },
    hasRole(role) {
      const token = localStorage.getItem('jwt_token');
      const payload = parseJwt(token);
      const roles = payload.roles ? payload.roles.map(r => r.name ? r.name.toLowerCase() : r.toLowerCase()) : [];
      return roles.includes(role.toLowerCase());
    },
    getAnimalImageUrl(imageUrl) {
      if (!imageUrl) return '';
      return `http://localhost:8080/api/files/image/${imageUrl}`;
    },
    handleImageError(event) {
      // Αν αποτύχει το load της εικόνας, εμφάνισε placeholder
      event.target.style.display = 'none';
      const placeholder = event.target.parentElement.querySelector('.animal-image-placeholder');
      if (placeholder) {
        placeholder.style.display = 'flex';
      }
    }
  }
}
</script>

<style scoped>
.animal-card {
  border: 1px solid #ddd;
  padding: 15px;
  margin-bottom: 20px;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  transition: box-shadow 0.3s;
}

.animal-card:hover {
  box-shadow: 0 4px 8px rgba(0,0,0,0.15);
}

.animal-content {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.animal-image-container {
  flex-shrink: 0;
  width: 250px;
  height: 200px;
}

.animal-image {
  width: 100%;
  height: 100%;
  overflow: hidden;
  border-radius: 8px;
  background: #f5f5f5;
  display: flex;
  align-items: center;
  justify-content: center;
}

.animal-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.animal-image-placeholder {
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

.animal-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.animal-info h3 {
  margin: 0 0 10px 0;
  color: #333;
  font-size: 1.5em;
}

.animal-info p {
  margin: 5px 0;
  color: #555;
}

.animal-info strong {
  color: #333;
}

.animal-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 15px;
}

.btn, .btn-request, .btn-approve, .btn-deny, .btn-delete {
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

.btn-request {
  background: #28a745;
  color: white;
}

.btn-request:hover {
  background: #218838;
}

.btn-approve {
  background: #28a745;
  color: white;
}

.btn-approve:hover {
  background: #218838;
}

.btn-deny {
  background: #ffc107;
  color: #000;
}

.btn-deny:hover {
  background: #e0a800;
}

.btn-delete {
  background: #dc3545;
  color: white;
}

.btn-delete:hover {
  background: #c82333;
}

@media (max-width: 768px) {
  .animal-content {
    flex-direction: column;
  }
  
  .animal-image-container {
    width: 100%;
    height: 250px;
  }
}
</style>