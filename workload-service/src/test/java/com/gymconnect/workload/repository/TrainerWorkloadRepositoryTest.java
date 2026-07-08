package com.gymconnect.workload.repository;

import com.gymconnect.workload.model.TrainerWorkload;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.reverse.TransitionWalker;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration test for {@link TrainerWorkloadRepository} against an in-process MongoDB
 * (flapdoodle embedded — no Docker required). Verifies the nested document round-trips
 * and that the required indexes are actually created.
 */
@DataMongoTest
class TrainerWorkloadRepositoryTest {

    // Started once for the whole class; the connection string is injected below before
    // the Spring context (and therefore the Mongo client) is created.
    private static final TransitionWalker.ReachedState<RunningMongodProcess> MONGOD =
            Mongod.instance().start(Version.Main.V7_0);

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri",
                () -> "mongodb://" + MONGOD.current().getServerAddress() + "/workload-test");
        registry.add("spring.data.mongodb.auto-index-creation", () -> true);
    }

    @AfterAll
    static void stopMongo() {
        MONGOD.close();
    }

    @Autowired
    private TrainerWorkloadRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void cleanCollection() {
        repository.deleteAll();
    }

    @Test
    void save_and_findByUsername_shouldRoundTripNestedYearsAndMonths() {
        TrainerWorkload workload = new TrainerWorkload("Mike.Johnson", "Mike", "Johnson", true);
        workload.addDuration(2026, 5, 60);
        workload.addDuration(2026, 5, 30);
        workload.addDuration(2025, 12, 45);
        repository.save(workload);

        Optional<TrainerWorkload> loaded = repository.findByUsername("Mike.Johnson");

        assertTrue(loaded.isPresent());
        TrainerWorkload found = loaded.get();
        assertEquals("Mike", found.getFirstName());
        assertTrue(found.getActive());
        assertEquals(2025, found.getYears().get(0).getYear());
        assertEquals(2026, found.getYears().get(1).getYear());
        assertEquals(90, found.findYear(2026).flatMap(y -> y.findMonth(5)).orElseThrow()
                .getTrainingSummaryDuration());
    }

    @Test
    void save_shouldUpdateExistingDocument_byUsername() {
        repository.save(new TrainerWorkload("Anna.Lee", "Anna", "Lee", true));

        TrainerWorkload update = repository.findByUsername("Anna.Lee").orElseThrow();
        update.updateIdentity("Anna", "Lee-Smith", false);
        repository.save(update);

        assertEquals(1, repository.count());
        TrainerWorkload reloaded = repository.findByUsername("Anna.Lee").orElseThrow();
        assertEquals("Lee-Smith", reloaded.getLastName());
        assertEquals(false, reloaded.getActive());
    }

    @Test
    void findByUsername_shouldReturnEmpty_whenUnknown() {
        assertTrue(repository.findByUsername("Ghost").isEmpty());
    }

    @Test
    void mapping_shouldCreateCompoundIndexOnFirstAndLastName() {
        // Touch the collection so the entity is mapped and its indexes are created.
        repository.save(new TrainerWorkload("Mike.Johnson", "Mike", "Johnson", true));

        List<IndexInfo> indexes = mongoTemplate.indexOps(TrainerWorkload.class).getIndexInfo();

        boolean nameIndexExists = indexes.stream()
                .anyMatch(index -> "trainer_name_idx".equals(index.getName()));
        assertTrue(nameIndexExists, "Expected compound index 'trainer_name_idx' on firstName+lastName");
    }
}
