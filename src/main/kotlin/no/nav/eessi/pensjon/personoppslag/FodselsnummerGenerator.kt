package no.nav.eessi.pensjon.personoppslag

import java.time.LocalDate

object FodselsnummerGenerator {

    /**
    Kun for generering av fnr for test
     */
    fun generateFnrForTest(alder: Int): String {
        val fnrdate = LocalDate.now().minusYears(alder.toLong())
        val y = fnrdate.year.toString()
        var fnr = ""
        var kontrollsiffer = 35
        var dag = 11
        do {
            val month = withLeadingZero(fnrdate.month.minus (1).value.toString())
            val fixedyear = y.substring(2, y.length)
            val indivdnr = indvididnr(fnrdate.year)
            fnr = "${dag++}" + month + fixedyear + indivdnr + kontrollsiffer++
            if (kontrollsiffer > 99) kontrollsiffer = 0
            if (dag > 27) dag = 1
            val fodselsnummer = Fodselsnummer.fra(fnr)
        } while (fodselsnummer == null || (fodselsnummer.getAge() != alder))
        return fnr
    }


    private fun withLeadingZero(str: String) = if (str.length == 1) "0$str" else str

    private fun indvididnr(year: Int) =
            when (year) {
                in 1900..1999 -> "433"
                in 1940..1999 -> "954"
                in 2000..2039 -> "543"
                else -> "739"
            }
}
