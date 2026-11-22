package com.barny.healthtracker

import org.junit.Assert.*
import org.junit.Test

class PetSpeciesTest {
    @Test fun missingSpeciesIsNotDefaulted() {
        assertNull(PetSpecies.fromStorage(null))
        assertNull(PetSpecies.fromStorage("unknown"))
    }
    @Test fun persistedSpeciesRoundTrips() {
        PetSpecies.values().forEach { assertEquals(it, PetSpecies.fromStorage(it.storageValue)) }
    }
    @Test fun hematocrit38IsInsideCatButBelowDogReference() {
        val cat = requireNotNull(PetReferenceIntervals.laboratoryInterval("Гематокрит", PetSpecies.CAT))
        val dog = requireNotNull(PetReferenceIntervals.laboratoryInterval("Гематокрит", PetSpecies.DOG))
        assertTrue(38f in cat.min..cat.max)
        assertTrue(38f < dog.min)
    }
    @Test fun weightHasNoUniversalLaboratoryReference() {
        PetSpecies.values().forEach { assertNull(PetReferenceIntervals.laboratoryInterval("Вес", it)) }
    }
}
