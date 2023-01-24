package com.mycompany.myapp.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.NumeroInventaire;
import com.mycompany.myapp.repository.NumeroInventaireRepository;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;

/**
 * Integration tests for the {@link NumeroInventaireResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class NumeroInventaireResourceIT {

    private static final String DEFAULT_TYPE = "AAAAAAAAAA";
    private static final String UPDATED_TYPE = "BBBBBBBBBB";

    private static final Boolean DEFAULT_DISPONIBLE = false;
    private static final Boolean UPDATED_DISPONIBLE = true;

    private static final String DEFAULT_ANCIEN_MATERIEL = "AAAAAAAAAA";
    private static final String UPDATED_ANCIEN_MATERIEL = "BBBBBBBBBB";

    private static final LocalDate DEFAULT_DATE_MODIFICATION = LocalDate.ofEpochDay(0L);
    private static final LocalDate UPDATED_DATE_MODIFICATION = LocalDate.now(ZoneId.systemDefault());

    private static final String DEFAULT_COMMENTAIRE = "AAAAAAAAAA";
    private static final String UPDATED_COMMENTAIRE = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/numero-inventaires";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private NumeroInventaireRepository numeroInventaireRepository;

    @Autowired
    private WebTestClient webTestClient;

    private NumeroInventaire numeroInventaire;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static NumeroInventaire createEntity() {
        NumeroInventaire numeroInventaire = new NumeroInventaire()
            .type(DEFAULT_TYPE)
            .disponible(DEFAULT_DISPONIBLE)
            .ancienMateriel(DEFAULT_ANCIEN_MATERIEL)
            .dateModification(DEFAULT_DATE_MODIFICATION)
            .commentaire(DEFAULT_COMMENTAIRE);
        return numeroInventaire;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static NumeroInventaire createUpdatedEntity() {
        NumeroInventaire numeroInventaire = new NumeroInventaire()
            .type(UPDATED_TYPE)
            .disponible(UPDATED_DISPONIBLE)
            .ancienMateriel(UPDATED_ANCIEN_MATERIEL)
            .dateModification(UPDATED_DATE_MODIFICATION)
            .commentaire(UPDATED_COMMENTAIRE);
        return numeroInventaire;
    }

    @BeforeEach
    public void initTest() {
        numeroInventaireRepository.deleteAll().block();
        numeroInventaire = createEntity();
    }

    @Test
    void createNumeroInventaire() throws Exception {
        int databaseSizeBeforeCreate = numeroInventaireRepository.findAll().collectList().block().size();
        // Create the NumeroInventaire
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(numeroInventaire))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the NumeroInventaire in the database
        List<NumeroInventaire> numeroInventaireList = numeroInventaireRepository.findAll().collectList().block();
        assertThat(numeroInventaireList).hasSize(databaseSizeBeforeCreate + 1);
        NumeroInventaire testNumeroInventaire = numeroInventaireList.get(numeroInventaireList.size() - 1);
        assertThat(testNumeroInventaire.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testNumeroInventaire.getDisponible()).isEqualTo(DEFAULT_DISPONIBLE);
        assertThat(testNumeroInventaire.getAncienMateriel()).isEqualTo(DEFAULT_ANCIEN_MATERIEL);
        assertThat(testNumeroInventaire.getDateModification()).isEqualTo(DEFAULT_DATE_MODIFICATION);
        assertThat(testNumeroInventaire.getCommentaire()).isEqualTo(DEFAULT_COMMENTAIRE);
    }

    @Test
    void createNumeroInventaireWithExistingId() throws Exception {
        // Create the NumeroInventaire with an existing ID
        numeroInventaire.setId("existing_id");

        int databaseSizeBeforeCreate = numeroInventaireRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(numeroInventaire))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the NumeroInventaire in the database
        List<NumeroInventaire> numeroInventaireList = numeroInventaireRepository.findAll().collectList().block();
        assertThat(numeroInventaireList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void getAllNumeroInventairesAsStream() {
        // Initialize the database
        numeroInventaireRepository.save(numeroInventaire).block();

        List<NumeroInventaire> numeroInventaireList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(NumeroInventaire.class)
            .getResponseBody()
            .filter(numeroInventaire::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(numeroInventaireList).isNotNull();
        assertThat(numeroInventaireList).hasSize(1);
        NumeroInventaire testNumeroInventaire = numeroInventaireList.get(0);
        assertThat(testNumeroInventaire.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testNumeroInventaire.getDisponible()).isEqualTo(DEFAULT_DISPONIBLE);
        assertThat(testNumeroInventaire.getAncienMateriel()).isEqualTo(DEFAULT_ANCIEN_MATERIEL);
        assertThat(testNumeroInventaire.getDateModification()).isEqualTo(DEFAULT_DATE_MODIFICATION);
        assertThat(testNumeroInventaire.getCommentaire()).isEqualTo(DEFAULT_COMMENTAIRE);
    }

    @Test
    void getAllNumeroInventaires() {
        // Initialize the database
        numeroInventaireRepository.save(numeroInventaire).block();

        // Get all the numeroInventaireList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(numeroInventaire.getId()))
            .jsonPath("$.[*].type")
            .value(hasItem(DEFAULT_TYPE))
            .jsonPath("$.[*].disponible")
            .value(hasItem(DEFAULT_DISPONIBLE.booleanValue()))
            .jsonPath("$.[*].ancienMateriel")
            .value(hasItem(DEFAULT_ANCIEN_MATERIEL))
            .jsonPath("$.[*].dateModification")
            .value(hasItem(DEFAULT_DATE_MODIFICATION.toString()))
            .jsonPath("$.[*].commentaire")
            .value(hasItem(DEFAULT_COMMENTAIRE));
    }

    @Test
    void getNumeroInventaire() {
        // Initialize the database
        numeroInventaireRepository.save(numeroInventaire).block();

        // Get the numeroInventaire
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, numeroInventaire.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(numeroInventaire.getId()))
            .jsonPath("$.type")
            .value(is(DEFAULT_TYPE))
            .jsonPath("$.disponible")
            .value(is(DEFAULT_DISPONIBLE.booleanValue()))
            .jsonPath("$.ancienMateriel")
            .value(is(DEFAULT_ANCIEN_MATERIEL))
            .jsonPath("$.dateModification")
            .value(is(DEFAULT_DATE_MODIFICATION.toString()))
            .jsonPath("$.commentaire")
            .value(is(DEFAULT_COMMENTAIRE));
    }

    @Test
    void getNonExistingNumeroInventaire() {
        // Get the numeroInventaire
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingNumeroInventaire() throws Exception {
        // Initialize the database
        numeroInventaireRepository.save(numeroInventaire).block();

        int databaseSizeBeforeUpdate = numeroInventaireRepository.findAll().collectList().block().size();

        // Update the numeroInventaire
        NumeroInventaire updatedNumeroInventaire = numeroInventaireRepository.findById(numeroInventaire.getId()).block();
        updatedNumeroInventaire
            .type(UPDATED_TYPE)
            .disponible(UPDATED_DISPONIBLE)
            .ancienMateriel(UPDATED_ANCIEN_MATERIEL)
            .dateModification(UPDATED_DATE_MODIFICATION)
            .commentaire(UPDATED_COMMENTAIRE);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedNumeroInventaire.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedNumeroInventaire))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the NumeroInventaire in the database
        List<NumeroInventaire> numeroInventaireList = numeroInventaireRepository.findAll().collectList().block();
        assertThat(numeroInventaireList).hasSize(databaseSizeBeforeUpdate);
        NumeroInventaire testNumeroInventaire = numeroInventaireList.get(numeroInventaireList.size() - 1);
        assertThat(testNumeroInventaire.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testNumeroInventaire.getDisponible()).isEqualTo(UPDATED_DISPONIBLE);
        assertThat(testNumeroInventaire.getAncienMateriel()).isEqualTo(UPDATED_ANCIEN_MATERIEL);
        assertThat(testNumeroInventaire.getDateModification()).isEqualTo(UPDATED_DATE_MODIFICATION);
        assertThat(testNumeroInventaire.getCommentaire()).isEqualTo(UPDATED_COMMENTAIRE);
    }

    @Test
    void putNonExistingNumeroInventaire() throws Exception {
        int databaseSizeBeforeUpdate = numeroInventaireRepository.findAll().collectList().block().size();
        numeroInventaire.setId(UUID.randomUUID().toString());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, numeroInventaire.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(numeroInventaire))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the NumeroInventaire in the database
        List<NumeroInventaire> numeroInventaireList = numeroInventaireRepository.findAll().collectList().block();
        assertThat(numeroInventaireList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchNumeroInventaire() throws Exception {
        int databaseSizeBeforeUpdate = numeroInventaireRepository.findAll().collectList().block().size();
        numeroInventaire.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(numeroInventaire))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the NumeroInventaire in the database
        List<NumeroInventaire> numeroInventaireList = numeroInventaireRepository.findAll().collectList().block();
        assertThat(numeroInventaireList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamNumeroInventaire() throws Exception {
        int databaseSizeBeforeUpdate = numeroInventaireRepository.findAll().collectList().block().size();
        numeroInventaire.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(numeroInventaire))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the NumeroInventaire in the database
        List<NumeroInventaire> numeroInventaireList = numeroInventaireRepository.findAll().collectList().block();
        assertThat(numeroInventaireList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateNumeroInventaireWithPatch() throws Exception {
        // Initialize the database
        numeroInventaireRepository.save(numeroInventaire).block();

        int databaseSizeBeforeUpdate = numeroInventaireRepository.findAll().collectList().block().size();

        // Update the numeroInventaire using partial update
        NumeroInventaire partialUpdatedNumeroInventaire = new NumeroInventaire();
        partialUpdatedNumeroInventaire.setId(numeroInventaire.getId());

        partialUpdatedNumeroInventaire
            .disponible(UPDATED_DISPONIBLE)
            .ancienMateriel(UPDATED_ANCIEN_MATERIEL)
            .dateModification(UPDATED_DATE_MODIFICATION)
            .commentaire(UPDATED_COMMENTAIRE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedNumeroInventaire.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedNumeroInventaire))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the NumeroInventaire in the database
        List<NumeroInventaire> numeroInventaireList = numeroInventaireRepository.findAll().collectList().block();
        assertThat(numeroInventaireList).hasSize(databaseSizeBeforeUpdate);
        NumeroInventaire testNumeroInventaire = numeroInventaireList.get(numeroInventaireList.size() - 1);
        assertThat(testNumeroInventaire.getType()).isEqualTo(DEFAULT_TYPE);
        assertThat(testNumeroInventaire.getDisponible()).isEqualTo(UPDATED_DISPONIBLE);
        assertThat(testNumeroInventaire.getAncienMateriel()).isEqualTo(UPDATED_ANCIEN_MATERIEL);
        assertThat(testNumeroInventaire.getDateModification()).isEqualTo(UPDATED_DATE_MODIFICATION);
        assertThat(testNumeroInventaire.getCommentaire()).isEqualTo(UPDATED_COMMENTAIRE);
    }

    @Test
    void fullUpdateNumeroInventaireWithPatch() throws Exception {
        // Initialize the database
        numeroInventaireRepository.save(numeroInventaire).block();

        int databaseSizeBeforeUpdate = numeroInventaireRepository.findAll().collectList().block().size();

        // Update the numeroInventaire using partial update
        NumeroInventaire partialUpdatedNumeroInventaire = new NumeroInventaire();
        partialUpdatedNumeroInventaire.setId(numeroInventaire.getId());

        partialUpdatedNumeroInventaire
            .type(UPDATED_TYPE)
            .disponible(UPDATED_DISPONIBLE)
            .ancienMateriel(UPDATED_ANCIEN_MATERIEL)
            .dateModification(UPDATED_DATE_MODIFICATION)
            .commentaire(UPDATED_COMMENTAIRE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedNumeroInventaire.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedNumeroInventaire))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the NumeroInventaire in the database
        List<NumeroInventaire> numeroInventaireList = numeroInventaireRepository.findAll().collectList().block();
        assertThat(numeroInventaireList).hasSize(databaseSizeBeforeUpdate);
        NumeroInventaire testNumeroInventaire = numeroInventaireList.get(numeroInventaireList.size() - 1);
        assertThat(testNumeroInventaire.getType()).isEqualTo(UPDATED_TYPE);
        assertThat(testNumeroInventaire.getDisponible()).isEqualTo(UPDATED_DISPONIBLE);
        assertThat(testNumeroInventaire.getAncienMateriel()).isEqualTo(UPDATED_ANCIEN_MATERIEL);
        assertThat(testNumeroInventaire.getDateModification()).isEqualTo(UPDATED_DATE_MODIFICATION);
        assertThat(testNumeroInventaire.getCommentaire()).isEqualTo(UPDATED_COMMENTAIRE);
    }

    @Test
    void patchNonExistingNumeroInventaire() throws Exception {
        int databaseSizeBeforeUpdate = numeroInventaireRepository.findAll().collectList().block().size();
        numeroInventaire.setId(UUID.randomUUID().toString());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, numeroInventaire.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(numeroInventaire))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the NumeroInventaire in the database
        List<NumeroInventaire> numeroInventaireList = numeroInventaireRepository.findAll().collectList().block();
        assertThat(numeroInventaireList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchNumeroInventaire() throws Exception {
        int databaseSizeBeforeUpdate = numeroInventaireRepository.findAll().collectList().block().size();
        numeroInventaire.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(numeroInventaire))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the NumeroInventaire in the database
        List<NumeroInventaire> numeroInventaireList = numeroInventaireRepository.findAll().collectList().block();
        assertThat(numeroInventaireList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamNumeroInventaire() throws Exception {
        int databaseSizeBeforeUpdate = numeroInventaireRepository.findAll().collectList().block().size();
        numeroInventaire.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(numeroInventaire))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the NumeroInventaire in the database
        List<NumeroInventaire> numeroInventaireList = numeroInventaireRepository.findAll().collectList().block();
        assertThat(numeroInventaireList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteNumeroInventaire() {
        // Initialize the database
        numeroInventaireRepository.save(numeroInventaire).block();

        int databaseSizeBeforeDelete = numeroInventaireRepository.findAll().collectList().block().size();

        // Delete the numeroInventaire
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, numeroInventaire.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<NumeroInventaire> numeroInventaireList = numeroInventaireRepository.findAll().collectList().block();
        assertThat(numeroInventaireList).hasSize(databaseSizeBeforeDelete - 1);
    }
}
