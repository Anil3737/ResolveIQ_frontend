package com.simats.resolveiq_frontend

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.simats.resolveiq_frontend.data.model.KnowledgeArticle
import com.simats.resolveiq_frontend.databinding.ActivityKnowledgeDetailBinding

class KnowledgeDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityKnowledgeDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityKnowledgeDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val article = intent.getSerializableExtra("article") as? KnowledgeArticle

        setupToolbar()
        article?.let { displayArticle(it) }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun displayArticle(article: KnowledgeArticle) {
        binding.tvDetailTitle.text = article.title
        binding.tvDetailCategory.text = article.category.uppercase()
        binding.tvDetailDate.text = "Published on ${article.date}"
        binding.tvDetailContent.text = article.content
    }
}
