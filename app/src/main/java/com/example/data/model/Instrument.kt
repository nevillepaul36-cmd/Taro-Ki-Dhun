package com.example.data.model

enum class PegSide {
    LEFT, RIGHT
}

enum class InstrumentCategory(val titleHindi: String, val titleEnglish: String) {
    INDIAN_CLASSICAL("Indian Classical", "Indian Classical"),
    WESTERN_STRINGS("Western Strings", "Western Strings"),
    FOLK_ACOUSTIC("Folk & Acoustic", "Folk & Acoustic")
}

data class InstrumentString(
    val index: Int, // 1 is highest pitch typically in western, or ordered
    val name: String, // e.g., "6E", "1E", "Baaj (Ma)"
    val note: String, // Note letter: "E", "A", "D", "G", "B", "C#"
    val swara: String, // "Sa", "Re", "Ga", "Ma", "Pa", "Dha", "Ni" or empty
    val targetFrequencyHz: Float,
    val octave: Int,
    val pegSide: PegSide
)

data class Instrument(
    val id: String,
    val nameHindi: String,
    val nameEnglish: String,
    val category: InstrumentCategory,
    val descriptionHindi: String,
    val descriptionEnglish: String,
    val tuningName: String,
    val strings: List<InstrumentString>
)

object InstrumentCatalog {
    val instruments: List<Instrument> = listOf(
        // 1. Acoustic Guitar
        Instrument(
            id = "guitar_standard",
            nameHindi = "Acoustic Guitar",
            nameEnglish = "Acoustic Guitar",
            category = InstrumentCategory.WESTERN_STRINGS,
            descriptionHindi = "Standard 6-string acoustic guitar tuning (E-A-D-G-B-E). The most versatile and widely played string instrument.",
            descriptionEnglish = "Standard 6-string acoustic guitar tuning (E-A-D-G-B-E). The most versatile and widely played string instrument.",
            tuningName = "Standard E (EADGBE)",
            strings = listOf(
                InstrumentString(1, "6E (Low E)", "E", "", 82.41f, 2, PegSide.LEFT),
                InstrumentString(2, "5A (A)", "A", "", 110.00f, 2, PegSide.LEFT),
                InstrumentString(3, "4D (D)", "D", "", 146.83f, 3, PegSide.LEFT),
                InstrumentString(4, "3G (G)", "G", "", 196.00f, 3, PegSide.RIGHT),
                InstrumentString(5, "2B (B)", "B", "", 246.94f, 3, PegSide.RIGHT),
                InstrumentString(6, "1E (High E)", "E", "", 329.63f, 4, PegSide.RIGHT)
            )
        ),

        // 2. Veena (सरस्वती वीणा)
        Instrument(
            id = "veena_saraswati",
            nameHindi = "Saraswati Veena",
            nameEnglish = "Saraswati Veena",
            category = InstrumentCategory.INDIAN_CLASSICAL,
            descriptionHindi = "Venerated South Indian Carnatic instrument with 4 main melody strings and 3 drone/talam strings.",
            descriptionEnglish = "Venerated South Indian Carnatic instrument with 4 main melody strings and 3 drone/talam strings.",
            tuningName = "Standard Carnatic (P-S-P-S)",
            strings = listOf(
                InstrumentString(1, "Sarani (Pa)", "G", "Pa", 196.00f, 3, PegSide.LEFT),
                InstrumentString(2, "Panchamam (Sa)", "C", "Sa", 130.81f, 3, PegSide.LEFT),
                InstrumentString(3, "Mandra (Pa)", "G", "Pa", 98.00f, 2, PegSide.LEFT),
                InstrumentString(4, "Anumandra (Sa)", "C", "Sa", 65.41f, 2, PegSide.LEFT),
                InstrumentString(5, "Pakka Sarani", "C", "Sa", 261.63f, 4, PegSide.RIGHT),
                InstrumentString(6, "Pakka Panchamam", "G", "Pa", 196.00f, 3, PegSide.RIGHT),
                InstrumentString(7, "Hecchu Sarani", "C", "Sa", 130.81f, 3, PegSide.RIGHT)
            )
        ),

        // 3. Sitar (सितार)
        Instrument(
            id = "sitar_standard",
            nameHindi = "Classical Sitar",
            nameEnglish = "Classical Sitar",
            category = InstrumentCategory.INDIAN_CLASSICAL,
            descriptionHindi = "North Indian classical 7-string tuning (Pt. Ravi Shankar tradition: Baaj, Joda, Gandhar, Chikari).",
            descriptionEnglish = "North Indian classical 7-string tuning (Pt. Ravi Shankar tradition: Baaj, Joda, Gandhar, Chikari).",
            tuningName = "Kharaj-Pancham (C# / F)",
            strings = listOf(
                InstrumentString(1, "Baaj (Ma)", "F", "Ma", 174.61f, 3, PegSide.LEFT),
                InstrumentString(2, "Joda (Sa)", "C", "Sa", 261.63f, 4, PegSide.LEFT),
                InstrumentString(3, "Gandhar (Ga)", "E", "Ga", 164.81f, 3, PegSide.LEFT),
                InstrumentString(4, "Pancham (Pa)", "G", "Pa", 196.00f, 3, PegSide.LEFT),
                InstrumentString(5, "Kharaj (Sa)", "C", "Sa", 130.81f, 3, PegSide.RIGHT),
                InstrumentString(6, "Small Chikari", "C", "Sa", 261.63f, 4, PegSide.RIGHT),
                InstrumentString(7, "Main Chikari", "C", "Sa", 523.25f, 5, PegSide.RIGHT)
            )
        ),

        // 4. Sarod (सरोद)
        Instrument(
            id = "sarod_classical",
            nameHindi = "Indian Sarod",
            nameEnglish = "Indian Sarod",
            category = InstrumentCategory.INDIAN_CLASSICAL,
            descriptionHindi = "Traditional 6-string concert sarod tuning (Ustad Amjad Ali Khan & Allauddin Khan traditions).",
            descriptionEnglish = "Traditional 6-string concert sarod tuning (Ustad Amjad Ali Khan & Allauddin Khan traditions).",
            tuningName = "Standard Concert (Ma-Sa-Pa-Sa)",
            strings = listOf(
                InstrumentString(1, "Tar Sa (Ma)", "F", "Ma", 174.61f, 3, PegSide.LEFT),
                InstrumentString(2, "Jodi (Sa)", "C", "Sa", 261.63f, 4, PegSide.LEFT),
                InstrumentString(3, "Pancham (Pa)", "G", "Pa", 196.00f, 3, PegSide.LEFT),
                InstrumentString(4, "Kharaj (Sa)", "C", "Sa", 130.81f, 3, PegSide.RIGHT),
                InstrumentString(5, "Laraj (Pa)", "G", "Pa", 98.00f, 2, PegSide.RIGHT),
                InstrumentString(6, "Chikari (Sa)", "C", "Sa", 523.25f, 5, PegSide.RIGHT)
            )
        ),

        // 5. Tanpura (तानपुरा)
        Instrument(
            id = "tanpura_male",
            nameHindi = "Indian Tanpura",
            nameEnglish = "Indian Tanpura Drone",
            category = InstrumentCategory.INDIAN_CLASSICAL,
            descriptionHindi = "Fundamental drone harmonic reference for classical music. 4 strings: Pancham, two Jodi Sa, and Kharaj.",
            descriptionEnglish = "Fundamental drone harmonic reference for classical music. 4 strings: Pancham, two Jodi Sa, and Kharaj.",
            tuningName = "Pancham Note (Pa-Sa-Sa-Sa)",
            strings = listOf(
                InstrumentString(1, "1st String (Pa)", "G", "Pa", 196.00f, 3, PegSide.LEFT),
                InstrumentString(2, "Jodi 1 (Sa)", "C", "Sa", 130.81f, 3, PegSide.LEFT),
                InstrumentString(3, "Jodi 2 (Sa)", "C", "Sa", 130.81f, 3, PegSide.RIGHT),
                InstrumentString(4, "Kharaj (Sa)", "C", "Sa", 65.41f, 2, PegSide.RIGHT)
            )
        ),

        // 6. Ukulele (यूकुलेले)
        Instrument(
            id = "ukulele_soprano",
            nameHindi = "Concert Ukulele",
            nameEnglish = "Soprano / Concert Ukulele",
            category = InstrumentCategory.WESTERN_STRINGS,
            descriptionHindi = "Standard soprano and concert ukulele in classic re-entrant C tuning (G-C-E-A).",
            descriptionEnglish = "Standard soprano and concert ukulele in classic re-entrant C tuning (G-C-E-A).",
            tuningName = "Standard C (GCEA)",
            strings = listOf(
                InstrumentString(1, "4G", "G", "", 392.00f, 4, PegSide.LEFT),
                InstrumentString(2, "3C", "C", "", 261.63f, 4, PegSide.LEFT),
                InstrumentString(3, "2E", "E", "", 329.63f, 4, PegSide.RIGHT),
                InstrumentString(4, "1A", "A", "", 440.00f, 4, PegSide.RIGHT)
            )
        ),

        // 7. Violin (वायलिन)
        Instrument(
            id = "violin_standard",
            nameHindi = "Acoustic Violin",
            nameEnglish = "Acoustic Violin",
            category = InstrumentCategory.WESTERN_STRINGS,
            descriptionHindi = "Standard classical and orchestral 4-string violin tuning in perfect fifths (G-D-A-E).",
            descriptionEnglish = "Standard classical and orchestral 4-string violin tuning in perfect fifths (G-D-A-E).",
            tuningName = "Standard (GDAE)",
            strings = listOf(
                InstrumentString(1, "4G", "G", "", 196.00f, 3, PegSide.LEFT),
                InstrumentString(2, "3D", "D", "", 293.66f, 4, PegSide.LEFT),
                InstrumentString(3, "2A", "A", "", 440.00f, 4, PegSide.RIGHT),
                InstrumentString(4, "1E", "E", "", 659.25f, 5, PegSide.RIGHT)
            )
        ),

        // 8. Bass Guitar (बास गिटार)
        Instrument(
            id = "bass_guitar_4",
            nameHindi = "4-String Bass Guitar",
            nameEnglish = "4-String Bass Guitar",
            category = InstrumentCategory.WESTERN_STRINGS,
            descriptionHindi = "Standard 4-string bass guitar tuning for deep low-end pitch (E-A-D-G).",
            descriptionEnglish = "Standard 4-string bass guitar tuning for deep low-end pitch (E-A-D-G).",
            tuningName = "Standard Bass (EADG)",
            strings = listOf(
                InstrumentString(1, "4E", "E", "", 41.20f, 1, PegSide.LEFT),
                InstrumentString(2, "3A", "A", "", 55.00f, 1, PegSide.LEFT),
                InstrumentString(3, "2D", "D", "", 73.42f, 2, PegSide.LEFT),
                InstrumentString(4, "1G", "G", "", 98.00f, 2, PegSide.LEFT)
            )
        ),

        // 9. Mandolin (मैंडोलिन)
        Instrument(
            id = "mandolin_standard",
            nameHindi = "8-String Mandolin",
            nameEnglish = "8-String Mandolin",
            category = InstrumentCategory.WESTERN_STRINGS,
            descriptionHindi = "Paired double-string courses in fifths (G-D-A-E) for folk, bluegrass, and acoustic melodies.",
            descriptionEnglish = "Paired double-string courses in fifths (G-D-A-E) for folk, bluegrass, and acoustic melodies.",
            tuningName = "Standard Pairs (GDAE)",
            strings = listOf(
                InstrumentString(1, "4G", "G", "", 196.00f, 3, PegSide.LEFT),
                InstrumentString(2, "3D", "D", "", 293.66f, 4, PegSide.LEFT),
                InstrumentString(3, "2A", "A", "", 440.00f, 4, PegSide.RIGHT),
                InstrumentString(4, "1E", "E", "", 659.25f, 5, PegSide.RIGHT)
            )
        ),

        // 10. Banjo (बैंजो)
        Instrument(
            id = "banjo_5string",
            nameHindi = "5-String Banjo",
            nameEnglish = "5-String Banjo",
            category = InstrumentCategory.FOLK_ACOUSTIC,
            descriptionHindi = "Traditional 5-string banjo with high thumb drone string in standard Open G (G-D-G-B-D).",
            descriptionEnglish = "Traditional 5-string banjo with high thumb drone string in standard Open G (G-D-G-B-D).",
            tuningName = "Open G (GDGBD)",
            strings = listOf(
                InstrumentString(1, "5G (Drone)", "G", "", 392.00f, 4, PegSide.LEFT),
                InstrumentString(2, "4D", "D", "", 146.83f, 3, PegSide.LEFT),
                InstrumentString(3, "3G", "G", "", 196.00f, 3, PegSide.RIGHT),
                InstrumentString(4, "2B", "B", "", 246.94f, 3, PegSide.RIGHT),
                InstrumentString(5, "1D", "D", "", 293.66f, 4, PegSide.RIGHT)
            )
        ),

        // 11. Santoor (संतूर)
        Instrument(
            id = "santoor_kashmiri",
            nameHindi = "Indian Santoor",
            nameEnglish = "Indian Santoor",
            category = InstrumentCategory.INDIAN_CLASSICAL,
            descriptionHindi = "Sacred 100-string hammered dulcimer from the Kashmir valley with melodic octave courses.",
            descriptionEnglish = "Sacred 100-string hammered dulcimer from the Kashmir valley with melodic octave courses.",
            tuningName = "Raga Bilawal Octave (C-D-E-F-G-A-B)",
            strings = listOf(
                InstrumentString(1, "Sa (Mandra)", "C", "Sa", 130.81f, 3, PegSide.LEFT),
                InstrumentString(2, "Re", "D", "Re", 146.83f, 3, PegSide.LEFT),
                InstrumentString(3, "Ga", "E", "Ga", 164.81f, 3, PegSide.LEFT),
                InstrumentString(4, "Ma", "F", "Ma", 174.61f, 3, PegSide.LEFT),
                InstrumentString(5, "Pa", "G", "Pa", 196.00f, 3, PegSide.RIGHT),
                InstrumentString(6, "Dha", "A", "Dha", 220.00f, 3, PegSide.RIGHT),
                InstrumentString(7, "Ni", "B", "Ni", 246.94f, 3, PegSide.RIGHT),
                InstrumentString(8, "Sa (Madhya)", "C", "Sa", 261.63f, 4, PegSide.RIGHT)
            )
        ),

        // 12. Sarangi (सारंगी)
        Instrument(
            id = "sarangi_classical",
            nameHindi = "Indian Sarangi",
            nameEnglish = "Indian Bowed Sarangi",
            category = InstrumentCategory.INDIAN_CLASSICAL,
            descriptionHindi = "Bowed acoustic instrument with 3 main gut strings and a brass drone (Sa-Pa-Sa tuning).",
            descriptionEnglish = "Bowed acoustic instrument with 3 main gut strings and a brass drone (Sa-Pa-Sa tuning).",
            tuningName = "Sa-Pa-Sa Tuning",
            strings = listOf(
                InstrumentString(1, "1st String (Sa)", "C", "Sa", 261.63f, 4, PegSide.LEFT),
                InstrumentString(2, "2nd String (Pa)", "G", "Pa", 196.00f, 3, PegSide.LEFT),
                InstrumentString(3, "3rd String (Sa)", "C", "Sa", 130.81f, 3, PegSide.RIGHT),
                InstrumentString(4, "4th Drone (Pa)", "G", "Pa", 98.00f, 2, PegSide.RIGHT)
            )
        )
    )

    fun getById(id: String): Instrument {
        return instruments.find { it.id == id } ?: instruments.first()
    }
}
