package no.nav.eessi.pensjon.personoppslag

import java.time.LocalDate

object FodselsnummerGenerator {

    /**
    Kun for generering av fnr for test
     */
    fun generateFnrForTest(alder: Int): String {
        var dag = 10
        var kontrollsiffer = 10
        val fnrdate = LocalDate.now().minusYears(alder.toLong() +1)
        val year = fnrdate.year.toString()
        val month = withLeadingZero(fnrdate.month.minus (1).value.toString())
        val fixedyear = year.substring(2, year.length)
        var fnr: String
        var fodselsnummer: Fodselsnummer? = null
        var errorCounter = 0
        do {
            fnr = "$dag" + month + fixedyear + indvididnr(fnrdate.year) + kontrollsiffer
            fodselsnummer = try {
                Fodselsnummer.fraMedValidation(fnr)
            } catch (ex: Exception) {
                if (ex.message == "Ugyldig kontrollnummer") {
                    kontrollsiffer++
                }
                errorCounter++
                null
            }
            if (kontrollsiffer >= 99) {
                kontrollsiffer = 10
                dag++
            }
            if (dag > 30) dag = 10
            if (errorCounter > 250) break;
            //println("fodselsnummer: $fodselsnummer, alder: ${fodselsnummer?.getAge()}")
        } while (fodselsnummer == null || (fodselsnummer.getAge() != alder))
        return fodselsnummer.toString()
    }
    private fun withLeadingZero(str: String) = if (str.length == 1) "0$str" else str
    private fun indvididnr(year: Int): String {
            val id = when (year) {
                in 1900..1999 -> "433"
                in 1940..1999 -> "954"
                in 2000..2039 -> "543"
                else -> "739"
            }
        return id
    }
}
