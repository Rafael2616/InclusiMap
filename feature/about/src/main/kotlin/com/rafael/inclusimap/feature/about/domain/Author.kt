package com.rafael.inclusimap.feature.about.domain

data class Author(
    val name: String,
    val site: String,
    val imageUri: String,
    val position: String,
) {
    companion object {
        val authors =
            listOf(
                Author(
                    name = "Rafael de Moura Nascimento",
                    site = "https://github.com/Rafael2616",
                    imageUri = "https://avatars.githubusercontent.com/u/93414086?s=96&v=4",
                    position = "Tech Lider/Desenvolvedor",
                ),
                Author(
                    name = "Alcilia Maria",
                    site = "https://github.com/",
                    imageUri = "https://admiral.digital/wp-content/uploads/2023/08/404_page-not-found-1024x576.png",
                    position = "Gerente de Produto",
                ),
                Author(
                    name = "Cléber Felix",
                    site = "https://github.com/",
                    imageUri = "https://admiral.digital/wp-content/uploads/2023/08/404_page-not-found-1024x576.png",
                    position = "Scrum Master",
                ),
            )
    }
}
