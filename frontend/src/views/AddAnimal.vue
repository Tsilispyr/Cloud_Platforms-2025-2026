<template>
  <div>
    <h2>Προσθήκη Ζώου</h2>
    <div v-if="successMessage" class="success-message">
      {{ successMessage }}
    </div>
    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>
    <form @submit.prevent="addAnimal" v-if="!successMessage">
      <div>
        <label>Όνομα:</label>
        <input v-model="animal.name" required />
      </div>
      <div>
        <label>Είδος:</label>
        <input v-model="animal.type" required />
      </div>
      <div>
        <label>Φύλο:</label>
        <select v-model="animal.gender" required>
          <option value="Male">Αρσενικό</option>
          <option value="Female">Θηλυκό</option>
        </select>
      </div>
      <div>
        <label>Ηλικία:</label>
        <input v-model="animal.age" type="number" min="0" required />
      </div>
      <div>
        <label>Εικόνα:</label>
        <input type="file" @change="onFileChange" accept="image/*" />
        <small v-if="imageFile" style="display: block; margin-top: 5px; color: #666;">
          Επιλεγμένο: {{ imageFile.name }} ({{ (imageFile.size / 1024 / 1024).toFixed(2) }} MB)
        </small>
        <small style="display: block; margin-top: 5px; color: #999;">
          Μέγιστο μέγεθος: 10MB
        </small>
      </div>
      <button type="submit" :disabled="loading">
        {{ loading ? 'Προσθήκη...' : 'Προσθήκη' }}
      </button>
      <button type="button" @click="$router.back()" :disabled="loading">Ακύρωση</button>
    </form>
    <div v-if="successMessage" class="success-actions">
      <button @click="$router.push('/animals')">Προβολή Ζώων</button>
      <button @click="resetForm">Προσθήκη Νέου</button>
    </div>
  </div>
</template>

<script>
import api from '../api';
export default {
  data() {
    return {
      animal: { name: '', type: '', gender: 'Male', age: 0 },
      imageFile: null,
      loading: false,
      successMessage: '',
      errorMessage: ''
    }
  },
  methods: {
    onFileChange(e) {
      const file = e.target.files[0];
      if (file) {
        // Validate file size (10MB max)
        const maxSize = 10 * 1024 * 1024; // 10MB in bytes
        if (file.size > maxSize) {
          alert('Το αρχείο είναι πολύ μεγάλο. Μέγιστο μέγεθος: 10MB');
          e.target.value = ''; // Clear the input
          this.imageFile = null;
          return;
        }
        // Validate file type
        if (!file.type.startsWith('image/')) {
          alert('Παρακαλώ επιλέξτε αρχείο εικόνας');
          e.target.value = '';
          this.imageFile = null;
          return;
        }
        this.imageFile = file;
        console.log('File selected:', file.name, 'Size:', (file.size / 1024 / 1024).toFixed(2), 'MB');
      }
    },
    resetForm() {
      this.animal = { name: '', type: '', gender: 'Male', age: 0 };
      this.imageFile = null;
      this.successMessage = '';
      this.errorMessage = '';
      this.loading = false;
    },
    async addAnimal() {
      this.loading = true;
      this.successMessage = '';
      this.errorMessage = '';
      
      try {
        // 1. Δημιουργία ζώου
        const res = await api.post('/animals', this.animal);
        console.log('Animal created:', res.data);
        
        // Extract animal ID from response
        let animalId = null;
        if (res.data && res.data.id) {
          animalId = res.data.id;
        } else if (res.data && typeof res.data === 'object') {
          animalId = res.data.id || res.data.animalId;
        } else if (typeof res.data === 'number') {
          animalId = res.data;
        }
        
        console.log('Extracted animalId:', animalId);
        
        // 2. Αν υπάρχει εικόνα, κάνε upload
        let imageUploadSuccess = true;
        if (this.imageFile && animalId) {
          console.log('Uploading image for animal:', animalId);
          const formData = new FormData();
          formData.append('file', this.imageFile);
          
          try {
            const uploadRes = await api.post(`/files/upload-animal-image/${animalId}`, formData, {
              headers: { 'Content-Type': 'multipart/form-data' }
            });
            console.log('Image uploaded successfully:', uploadRes.data);
          } catch (uploadError) {
            console.error('Image upload error:', uploadError);
            imageUploadSuccess = false;
            
            let errorMsg = 'Σφάλμα ανέβασματος εικόνας';
            if (uploadError.response?.status === 413) {
              errorMsg = 'Το αρχείο είναι πολύ μεγάλο. Μέγιστο μέγεθος: 10MB';
            } else if (uploadError.response?.data?.error) {
              errorMsg = uploadError.response.data.error;
            } else if (uploadError.message) {
              errorMsg = uploadError.message;
            }
            
            // Ask user if they want to continue without image
            const continueWithoutImage = confirm(
              `${errorMsg}\n\nΘέλετε να συνεχίσετε χωρίς εικόνα;`
            );
            
            if (!continueWithoutImage) {
              this.loading = false;
              return; // User cancelled, don't redirect
            }
          }
        } else if (this.imageFile && !animalId) {
          console.error('Cannot upload image: animalId is null');
          const continueWithoutImage = confirm(
            'Το ζώο δημιουργήθηκε αλλά δεν ήταν δυνατό να ανέβει η εικόνα.\n\nΘέλετε να συνεχίσετε;'
          );
          if (!continueWithoutImage) {
            this.loading = false;
            return;
          }
        }
        
        // Εμφάνιση μηνύματος επιτυχίας
        this.successMessage = imageUploadSuccess && this.imageFile
          ? 'Το ζώο προστέθηκε επιτυχώς με εικόνα!'
          : 'Το ζώο προστέθηκε επιτυχώς!';
        
        this.loading = false;
      } catch (e) {
        console.error('Error adding animal:', e);
        this.loading = false;
        this.errorMessage = e.response?.data?.error || e.message || 'Σφάλμα προσθήκης ζώου';
      }
    }
  }
}
</script>

<style scoped>
.success-message {
  background: #d4edda;
  color: #155724;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  border: 1px solid #c3e6cb;
  font-weight: 500;
}

.error-message {
  background: #f8d7da;
  color: #721c24;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
  border: 1px solid #f5c6cb;
  font-weight: 500;
}

form {
  max-width: 500px;
  margin: 0 auto;
}

form > div {
  margin-bottom: 15px;
}

label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
  color: #333;
}

input[type="text"],
input[type="number"],
select {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

input[type="file"] {
  width: 100%;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}

small {
  font-size: 12px;
}

button {
  padding: 10px 20px;
  margin-right: 10px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: background-color 0.3s;
}

button[type="submit"] {
  background: #007bff;
  color: white;
}

button[type="submit"]:hover:not(:disabled) {
  background: #0056b3;
}

button[type="submit"]:disabled {
  background: #ccc;
  cursor: not-allowed;
}

button[type="button"] {
  background: #6c757d;
  color: white;
}

button[type="button"]:hover:not(:disabled) {
  background: #5a6268;
}

.success-actions {
  margin-top: 20px;
  text-align: center;
}

.success-actions button {
  background: #28a745;
  color: white;
  margin: 0 10px;
}

.success-actions button:hover {
  background: #218838;
}

.success-actions button:last-child {
  background: #007bff;
}

.success-actions button:last-child:hover {
  background: #0056b3;
}
</style> 