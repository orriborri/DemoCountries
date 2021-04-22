package orriborri.Model

//name, alpha3Code, region, subregion, capital, population
case class Country(
    name: String,
    alpha3Code: String,
    region: String,
    subregion: String,
    capital: String,
    population: Int,
    flag: String
)
//http://www.danieluzunu.com/2015/10/04/reflecting-the-values-of-class-members-in-scala/
//This is need in order to sort the csv by
