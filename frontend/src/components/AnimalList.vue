<template>
  <div>
    <h2>Available Animals</h2>
    <ul>
      <li v-for="animal in animals" :key="animal.id">
        {{ animal.name }} - {{ animal.status }}
        <button v-if="canAdopt" @click="adopt(animal.id)">Υποβολή Αίτησης Υιοθεσίας</button>
      </li>
    </ul>
  </div>
</template>
<script>
import api from '../api';
export default {
  data() { return { animals: [] }; },
  computed: {
    canAdopt() {
      const roles = this.$keycloak.tokenParsed?.realm_access?.roles || [];
      return roles.includes('USER');
    }
  },
  async mounted() {
    const res = await api.get('/animals/available');
    this.animals = res.data;
  },
  methods: {
    async adopt(id) {
      try {
        await api.post(`/adoptions`, { animalId: id });
        alert('Αίτηση υποβλήθηκε');
        // Ανακατεύθυνση στη σελίδα αιτήσεων
        this.$router.push('/requests');
      } catch (error) {
        alert('Σφάλμα κατά την υποβολή της αίτησης');
        console.error(error);
      }
    }
  }
}
</script> 