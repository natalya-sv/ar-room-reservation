package nl.natalya.roomreservation

import nl.natalya.roomreservation.repositories.AugmentedImagesDatabase

object ServiceLocator {

    private var augmentedImagesDatabase: AugmentedImagesDatabase? = null

    fun getAugmentedImagesDatabase(): AugmentedImagesDatabase {
        val augmentedImagesDatabase = augmentedImagesDatabase

        if (augmentedImagesDatabase != null) {
            return augmentedImagesDatabase
        }
        val newAugmentedImagesDatabase = AugmentedImagesDatabase()
        this.augmentedImagesDatabase = newAugmentedImagesDatabase

        return newAugmentedImagesDatabase
    }
}