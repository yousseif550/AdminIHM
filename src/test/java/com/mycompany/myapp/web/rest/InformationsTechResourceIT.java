package com.mycompany.myapp.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;

import com.mycompany.myapp.IntegrationTest;
import com.mycompany.myapp.domain.InformationsTech;
import com.mycompany.myapp.repository.InformationsTechRepository;
import java.time.Duration;
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
 * Integration tests for the {@link InformationsTechResource} REST controller.
 */
@IntegrationTest
@AutoConfigureWebTestClient(timeout = IntegrationTest.DEFAULT_ENTITY_TIMEOUT)
@WithMockUser
class InformationsTechResourceIT {

    private static final String DEFAULT_PC_DOM = "AAAAAAAAAA";
    private static final String UPDATED_PC_DOM = "BBBBBBBBBB";

    private static final String DEFAULT_PC_DG_FI_P = "AAAAAAAAAA";
    private static final String UPDATED_PC_DG_FI_P = "BBBBBBBBBB";

    private static final String DEFAULT_ADRESSE_SSG = "AAAAAAAAAA";
    private static final String UPDATED_ADRESSE_SSG = "BBBBBBBBBB";

    private static final String DEFAULT_ADRESSE_DG_FI_P = "AAAAAAAAAA";
    private static final String UPDATED_ADRESSE_DG_FI_P = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/informations-teches";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    @Autowired
    private InformationsTechRepository informationsTechRepository;

    @Autowired
    private WebTestClient webTestClient;

    private InformationsTech informationsTech;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static InformationsTech createEntity() {
        InformationsTech informationsTech = new InformationsTech()
            .pcDom(DEFAULT_PC_DOM)
            .pcDGFiP(DEFAULT_PC_DG_FI_P)
            .adresseSSG(DEFAULT_ADRESSE_SSG)
            .adresseDGFiP(DEFAULT_ADRESSE_DG_FI_P);
        return informationsTech;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static InformationsTech createUpdatedEntity() {
        InformationsTech informationsTech = new InformationsTech()
            .pcDom(UPDATED_PC_DOM)
            .pcDGFiP(UPDATED_PC_DG_FI_P)
            .adresseSSG(UPDATED_ADRESSE_SSG)
            .adresseDGFiP(UPDATED_ADRESSE_DG_FI_P);
        return informationsTech;
    }

    @BeforeEach
    public void initTest() {
        informationsTechRepository.deleteAll().block();
        informationsTech = createEntity();
    }

    @Test
    void createInformationsTech() throws Exception {
        int databaseSizeBeforeCreate = informationsTechRepository.findAll().collectList().block().size();
        // Create the InformationsTech
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(informationsTech))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the InformationsTech in the database
        List<InformationsTech> informationsTechList = informationsTechRepository.findAll().collectList().block();
        assertThat(informationsTechList).hasSize(databaseSizeBeforeCreate + 1);
        InformationsTech testInformationsTech = informationsTechList.get(informationsTechList.size() - 1);
        assertThat(testInformationsTech.getPcDom()).isEqualTo(DEFAULT_PC_DOM);
        assertThat(testInformationsTech.getPcDGFiP()).isEqualTo(DEFAULT_PC_DG_FI_P);
        assertThat(testInformationsTech.getAdresseSSG()).isEqualTo(DEFAULT_ADRESSE_SSG);
        assertThat(testInformationsTech.getAdresseDGFiP()).isEqualTo(DEFAULT_ADRESSE_DG_FI_P);
    }

    @Test
    void createInformationsTechWithExistingId() throws Exception {
        // Create the InformationsTech with an existing ID
        informationsTech.setId("existing_id");

        int databaseSizeBeforeCreate = informationsTechRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(informationsTech))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the InformationsTech in the database
        List<InformationsTech> informationsTechList = informationsTechRepository.findAll().collectList().block();
        assertThat(informationsTechList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    void getAllInformationsTechesAsStream() {
        // Initialize the database
        informationsTechRepository.save(informationsTech).block();

        List<InformationsTech> informationsTechList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(InformationsTech.class)
            .getResponseBody()
            .filter(informationsTech::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(informationsTechList).isNotNull();
        assertThat(informationsTechList).hasSize(1);
        InformationsTech testInformationsTech = informationsTechList.get(0);
        assertThat(testInformationsTech.getPcDom()).isEqualTo(DEFAULT_PC_DOM);
        assertThat(testInformationsTech.getPcDGFiP()).isEqualTo(DEFAULT_PC_DG_FI_P);
        assertThat(testInformationsTech.getAdresseSSG()).isEqualTo(DEFAULT_ADRESSE_SSG);
        assertThat(testInformationsTech.getAdresseDGFiP()).isEqualTo(DEFAULT_ADRESSE_DG_FI_P);
    }

    @Test
    void getAllInformationsTeches() {
        // Initialize the database
        informationsTechRepository.save(informationsTech).block();

        // Get all the informationsTechList
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
            .value(hasItem(informationsTech.getId()))
            .jsonPath("$.[*].pcDom")
            .value(hasItem(DEFAULT_PC_DOM))
            .jsonPath("$.[*].pcDGFiP")
            .value(hasItem(DEFAULT_PC_DG_FI_P))
            .jsonPath("$.[*].adresseSSG")
            .value(hasItem(DEFAULT_ADRESSE_SSG))
            .jsonPath("$.[*].adresseDGFiP")
            .value(hasItem(DEFAULT_ADRESSE_DG_FI_P));
    }

    @Test
    void getInformationsTech() {
        // Initialize the database
        informationsTechRepository.save(informationsTech).block();

        // Get the informationsTech
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, informationsTech.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(informationsTech.getId()))
            .jsonPath("$.pcDom")
            .value(is(DEFAULT_PC_DOM))
            .jsonPath("$.pcDGFiP")
            .value(is(DEFAULT_PC_DG_FI_P))
            .jsonPath("$.adresseSSG")
            .value(is(DEFAULT_ADRESSE_SSG))
            .jsonPath("$.adresseDGFiP")
            .value(is(DEFAULT_ADRESSE_DG_FI_P));
    }

    @Test
    void getNonExistingInformationsTech() {
        // Get the informationsTech
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putExistingInformationsTech() throws Exception {
        // Initialize the database
        informationsTechRepository.save(informationsTech).block();

        int databaseSizeBeforeUpdate = informationsTechRepository.findAll().collectList().block().size();

        // Update the informationsTech
        InformationsTech updatedInformationsTech = informationsTechRepository.findById(informationsTech.getId()).block();
        updatedInformationsTech
            .pcDom(UPDATED_PC_DOM)
            .pcDGFiP(UPDATED_PC_DG_FI_P)
            .adresseSSG(UPDATED_ADRESSE_SSG)
            .adresseDGFiP(UPDATED_ADRESSE_DG_FI_P);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedInformationsTech.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedInformationsTech))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the InformationsTech in the database
        List<InformationsTech> informationsTechList = informationsTechRepository.findAll().collectList().block();
        assertThat(informationsTechList).hasSize(databaseSizeBeforeUpdate);
        InformationsTech testInformationsTech = informationsTechList.get(informationsTechList.size() - 1);
        assertThat(testInformationsTech.getPcDom()).isEqualTo(UPDATED_PC_DOM);
        assertThat(testInformationsTech.getPcDGFiP()).isEqualTo(UPDATED_PC_DG_FI_P);
        assertThat(testInformationsTech.getAdresseSSG()).isEqualTo(UPDATED_ADRESSE_SSG);
        assertThat(testInformationsTech.getAdresseDGFiP()).isEqualTo(UPDATED_ADRESSE_DG_FI_P);
    }

    @Test
    void putNonExistingInformationsTech() throws Exception {
        int databaseSizeBeforeUpdate = informationsTechRepository.findAll().collectList().block().size();
        informationsTech.setId(UUID.randomUUID().toString());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, informationsTech.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(informationsTech))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the InformationsTech in the database
        List<InformationsTech> informationsTechList = informationsTechRepository.findAll().collectList().block();
        assertThat(informationsTechList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithIdMismatchInformationsTech() throws Exception {
        int databaseSizeBeforeUpdate = informationsTechRepository.findAll().collectList().block().size();
        informationsTech.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(informationsTech))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the InformationsTech in the database
        List<InformationsTech> informationsTechList = informationsTechRepository.findAll().collectList().block();
        assertThat(informationsTechList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void putWithMissingIdPathParamInformationsTech() throws Exception {
        int databaseSizeBeforeUpdate = informationsTechRepository.findAll().collectList().block().size();
        informationsTech.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(informationsTech))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the InformationsTech in the database
        List<InformationsTech> informationsTechList = informationsTechRepository.findAll().collectList().block();
        assertThat(informationsTechList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void partialUpdateInformationsTechWithPatch() throws Exception {
        // Initialize the database
        informationsTechRepository.save(informationsTech).block();

        int databaseSizeBeforeUpdate = informationsTechRepository.findAll().collectList().block().size();

        // Update the informationsTech using partial update
        InformationsTech partialUpdatedInformationsTech = new InformationsTech();
        partialUpdatedInformationsTech.setId(informationsTech.getId());

        partialUpdatedInformationsTech.adresseSSG(UPDATED_ADRESSE_SSG);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedInformationsTech.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedInformationsTech))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the InformationsTech in the database
        List<InformationsTech> informationsTechList = informationsTechRepository.findAll().collectList().block();
        assertThat(informationsTechList).hasSize(databaseSizeBeforeUpdate);
        InformationsTech testInformationsTech = informationsTechList.get(informationsTechList.size() - 1);
        assertThat(testInformationsTech.getPcDom()).isEqualTo(DEFAULT_PC_DOM);
        assertThat(testInformationsTech.getPcDGFiP()).isEqualTo(DEFAULT_PC_DG_FI_P);
        assertThat(testInformationsTech.getAdresseSSG()).isEqualTo(UPDATED_ADRESSE_SSG);
        assertThat(testInformationsTech.getAdresseDGFiP()).isEqualTo(DEFAULT_ADRESSE_DG_FI_P);
    }

    @Test
    void fullUpdateInformationsTechWithPatch() throws Exception {
        // Initialize the database
        informationsTechRepository.save(informationsTech).block();

        int databaseSizeBeforeUpdate = informationsTechRepository.findAll().collectList().block().size();

        // Update the informationsTech using partial update
        InformationsTech partialUpdatedInformationsTech = new InformationsTech();
        partialUpdatedInformationsTech.setId(informationsTech.getId());

        partialUpdatedInformationsTech
            .pcDom(UPDATED_PC_DOM)
            .pcDGFiP(UPDATED_PC_DG_FI_P)
            .adresseSSG(UPDATED_ADRESSE_SSG)
            .adresseDGFiP(UPDATED_ADRESSE_DG_FI_P);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedInformationsTech.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedInformationsTech))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the InformationsTech in the database
        List<InformationsTech> informationsTechList = informationsTechRepository.findAll().collectList().block();
        assertThat(informationsTechList).hasSize(databaseSizeBeforeUpdate);
        InformationsTech testInformationsTech = informationsTechList.get(informationsTechList.size() - 1);
        assertThat(testInformationsTech.getPcDom()).isEqualTo(UPDATED_PC_DOM);
        assertThat(testInformationsTech.getPcDGFiP()).isEqualTo(UPDATED_PC_DG_FI_P);
        assertThat(testInformationsTech.getAdresseSSG()).isEqualTo(UPDATED_ADRESSE_SSG);
        assertThat(testInformationsTech.getAdresseDGFiP()).isEqualTo(UPDATED_ADRESSE_DG_FI_P);
    }

    @Test
    void patchNonExistingInformationsTech() throws Exception {
        int databaseSizeBeforeUpdate = informationsTechRepository.findAll().collectList().block().size();
        informationsTech.setId(UUID.randomUUID().toString());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, informationsTech.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(informationsTech))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the InformationsTech in the database
        List<InformationsTech> informationsTechList = informationsTechRepository.findAll().collectList().block();
        assertThat(informationsTechList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithIdMismatchInformationsTech() throws Exception {
        int databaseSizeBeforeUpdate = informationsTechRepository.findAll().collectList().block().size();
        informationsTech.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, UUID.randomUUID().toString())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(informationsTech))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the InformationsTech in the database
        List<InformationsTech> informationsTechList = informationsTechRepository.findAll().collectList().block();
        assertThat(informationsTechList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void patchWithMissingIdPathParamInformationsTech() throws Exception {
        int databaseSizeBeforeUpdate = informationsTechRepository.findAll().collectList().block().size();
        informationsTech.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(informationsTech))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the InformationsTech in the database
        List<InformationsTech> informationsTechList = informationsTechRepository.findAll().collectList().block();
        assertThat(informationsTechList).hasSize(databaseSizeBeforeUpdate);
    }

    @Test
    void deleteInformationsTech() {
        // Initialize the database
        informationsTechRepository.save(informationsTech).block();

        int databaseSizeBeforeDelete = informationsTechRepository.findAll().collectList().block().size();

        // Delete the informationsTech
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, informationsTech.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<InformationsTech> informationsTechList = informationsTechRepository.findAll().collectList().block();
        assertThat(informationsTechList).hasSize(databaseSizeBeforeDelete - 1);
    }
}