package no.nav.eessi.pensjon.personoppslag

import no.nav.eessi.pensjon.shared.person.Fodselsnummer
import no.nav.eessi.pensjon.shared.person.FodselsnummerGenerator.generateFnrForTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class FodselsnummerTest {

    companion object {
        private val LEALAUS_KAKE = Fodselsnummer.fra("22117320034")!!          //1973
        private val STERK_BUSK = Fodselsnummer.fra("12011577847")!!            //2015
        private val KRAFTIG_VEGGPRYD = Fodselsnummer.fra("11067122781")!!      //1971
        private val SLAPP_SKILPADDE = Fodselsnummer.fra("09035225916")!!       //1952
        private val GOD_BOLLE = Fodselsnummer.fra("08115525221")!!             //1955
        private val VELDIG_GAMMEL_SYKKEL = Fodselsnummer.fra("08118974914")!!  //1889
        private val DNUMMER_GYLDIG = Fodselsnummer.fra("41060094231")!!        //2000

        private val fnrOver62 = generateFnrForTest(63)
        private val fnrUnder62 = generateFnrForTest(61)
    }

    @Test
    fun `Should be null if invalid value`() {
        assertNull(Fodselsnummer.fra("12011522222"))
        assertNull(Fodselsnummer.fra("01234567890"))
        assertNull(Fodselsnummer.fra("11111111111"))
        assertNull(Fodselsnummer.fra("22222222222"))
        assertNull(Fodselsnummer.fra("19191919191"))
        assertNull(Fodselsnummer.fra(null))
    }

    @Test
    fun `Should remove everything that isnt a digit`() {
        assertNotNull(Fodselsnummer.fra("     22117320034"))            // LEALAUS_KAKE
        assertNotNull(Fodselsnummer.fra("  12011577847     "))          // STERK_BUSK
        assertNotNull(Fodselsnummer.fra("asdf 11067122781 jqwroij"))    // KRAFTIG_VEGGPRYD
        assertNotNull(Fodselsnummer.fra("j-asjd09-035-225916 "))        // SLAPP_SKILPADDE
        assertNotNull(Fodselsnummer.fra("081155 25221"))                // GOD_BOLLE
    }

    @Test
    fun `syntetic fdata should be valid`() {
        assertNotNull(Fodselsnummer.fra("17912099997"))
        assertNotNull(Fodselsnummer.fra("29822099635"))
        assertNotNull(Fodselsnummer.fra("05840399895"))
        assertNotNull(Fodselsnummer.fra("12829499914"))
        assertNotNull(Fodselsnummer.fra("12905299938"))
        assertNotNull(Fodselsnummer.fra("21883649874"))
        assertNotNull(Fodselsnummer.fra("21929774873"))
        assertNotNull(Fodselsnummer.fra("54496214261"))
    }

    @Test
    fun `npid fdata should be valid`() {
        assertNotNull(Fodselsnummer.fra("01220049651"))
        assert(Fodselsnummer.fra("01220049651")?.erNpid == true)
    }

    @Test
    fun `syntetic date and birthdate`() {
        val synt = "54496214261" //KAFFI DØLL
        val fnr = Fodselsnummer.fra(synt)
        assertNotNull(fnr)
        assertEquals("1962-09-14", fnr?.getBirthDateAsIso())
        assertTrue(fnr!!.getAge() > 58)
        assertEquals(Fodselsnummer.Kjoenn.KVINNE, fnr.kjoenn)
    }

    @Test
    fun `Validate birthdate as ISO 8603`() {
        assertEquals("1973-11-22", LEALAUS_KAKE.getBirthDateAsIso())
        assertEquals("2015-01-12", STERK_BUSK.getBirthDateAsIso())
        assertEquals("1971-06-11", KRAFTIG_VEGGPRYD.getBirthDateAsIso())
        assertEquals("1952-03-09", SLAPP_SKILPADDE.getBirthDateAsIso())
        assertEquals("1955-11-08", GOD_BOLLE.getBirthDateAsIso())
        assertEquals("2000-06-01", DNUMMER_GYLDIG.getBirthDateAsIso())
    }

    @Test
    fun `Validate birthdate as LocalDate`() {
        assertEquals(LocalDate.of(1973, 11, 22), LEALAUS_KAKE.getBirthDate())
        assertEquals(LocalDate.of(2015, 1, 12), STERK_BUSK.getBirthDate())
        assertEquals(LocalDate.of(1971, 6, 11), KRAFTIG_VEGGPRYD.getBirthDate())
        assertEquals(LocalDate.of(1952, 3, 9), SLAPP_SKILPADDE.getBirthDate())
        assertEquals(LocalDate.of(1955, 11, 8), GOD_BOLLE.getBirthDate())
        assertEquals(LocalDate.of(2000, 6, 1), DNUMMER_GYLDIG.getBirthDate())
        assertEquals(LocalDate.of(1889, 11, 8), VELDIG_GAMMEL_SYKKEL.getBirthDate())

    }

    @Test
    fun `Validate equals operator`() {
        assertTrue(LEALAUS_KAKE == LEALAUS_KAKE)
        assertTrue(KRAFTIG_VEGGPRYD == KRAFTIG_VEGGPRYD)
        assertTrue(STERK_BUSK == STERK_BUSK)

        assertFalse(SLAPP_SKILPADDE == GOD_BOLLE)
        assertFalse(KRAFTIG_VEGGPRYD == STERK_BUSK)
        assertFalse(LEALAUS_KAKE == GOD_BOLLE)
    }

    @Test
    fun `Validate notEquals operator`() {
        assertTrue(SLAPP_SKILPADDE != GOD_BOLLE)
        assertTrue(KRAFTIG_VEGGPRYD != STERK_BUSK)
        assertTrue(LEALAUS_KAKE != GOD_BOLLE)

        assertFalse(LEALAUS_KAKE != LEALAUS_KAKE)
        assertFalse(KRAFTIG_VEGGPRYD != KRAFTIG_VEGGPRYD)
        assertFalse(STERK_BUSK != STERK_BUSK)
    }

    @Test
    fun `Sjekk på FhNumber`() {
        assertNull( Fodselsnummer.fra("82117320034") )
        assertNull( Fodselsnummer.fra("92117320034") )
        assertNull( Fodselsnummer.fra("F2117320034") )
    }

    @Test
    fun `Test på bruker fnr 20år`() {
        val fnr = generateFnrForTest(20)
        val navfnr = Fodselsnummer.fraMedValidation(fnr)
        assertEquals(20, navfnr?.getAge())
        assertEquals(false, navfnr?.isUnder18Year())
    }

    @Test
    fun `Is 17 year old under 18year`() {
        val fnr = generateFnrForTest(17)
        val navfnr = Fodselsnummer.fraMedValidation(fnr)
        assertEquals(17, navfnr?.getAge())
        assertEquals(true, navfnr?.isUnder18Year())
    }

    @Test
    fun `Is 16 year old under 18year`() {
        val fnr = generateFnrForTest(16)
        val navfnr = Fodselsnummer.fraMedValidation(fnr)

        assertEquals(16, navfnr?.getAge())
        assertEquals(true, navfnr?.isUnder18Year())
    }

    @Test
    fun `not valid pension very young age`() {
        val fnr = generateFnrForTest(10)
        val navfnr = Fodselsnummer.fraMedValidation(fnr)
        assertEquals(10, navfnr?.getAge())
        assertEquals(true, navfnr?.isUnder18Year())
    }

    @Test
    fun `valid check for age`() {
        val fnr = generateFnrForTest(48)
        val navfnr = Fodselsnummer.fra(fnr)
        assertEquals(48, navfnr?.getAge())
    }

    @Test
    fun `valid check for old age`() {
        val fnr = generateFnrForTest(72)
        val navfnr = Fodselsnummer.fra(fnr)
        assertEquals(72, navfnr?.getAge())
    }

    @Test
    fun `valid pension age`() {
        val fnr = generateFnrForTest(67)
        val navfnr = Fodselsnummer.fra(fnr)
        assertEquals(67, navfnr?.getAge())
    }

    @Test
    fun erOver62() {
        assertEquals(true, Fodselsnummer.fra(fnrOver62)?.erOverAlder(62))
    }

    @Test
    fun erUnder62() {
        assertEquals(true, Fodselsnummer.fra(fnrUnder62)?.erUnderAlder(62))
    }
}
