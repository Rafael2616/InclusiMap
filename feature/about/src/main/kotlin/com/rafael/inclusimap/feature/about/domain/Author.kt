package com.rafael.inclusimap.feature.about.domain

data class Author(
    val name: String,
    val site: String,
    val image: String,
    val position: String,
) {
    companion object {
        val authors =
            listOf(
                Author(
                    name = "Rafael de Moura Nascimento",
                    site = "https://github.com/Rafael2616",
                    image = "https://avatars.githubusercontent.com/u/93414086?s=96&v=4",
                    position = "Tech Lider/Desenvolvedor",
                ),
                Author(
                    name = "Alcilia Maria",
                    site = "https://github.com/",
                    image = "https://techcrunch.com/wp-content/uploads/2010/07/github-logo.png",
                    position = "Gerente de Produto",
                ),
                Author(
                    name = "Cléber Felix",
                    site = "https://github.com/",
                    image = "https://techcrunch.com/wp-content/uploads/2010/07/github-logo.png",
                    position = "Scrum Master",
                ),
            )
    }
}
