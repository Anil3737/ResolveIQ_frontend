package com.simats.resolveiq_frontend.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.simats.resolveiq_frontend.R
import com.simats.resolveiq_frontend.data.model.KnowledgeArticle

class KnowledgeArticleAdapter(
    private var articles: List<KnowledgeArticle>,
    private val onItemClick: (KnowledgeArticle) -> Unit
) : RecyclerView.Adapter<KnowledgeArticleAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvSummary: TextView = view.findViewById(R.id.tvSummary)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_knowledge_article, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val article = articles[position]
        holder.tvCategory.text = article.category.uppercase()
        holder.tvDate.text = article.date
        holder.tvTitle.text = article.title
        holder.tvSummary.text = article.summary

        holder.itemView.setOnClickListener { onItemClick(article) }
    }

    override fun getItemCount() = articles.size

    fun updateArticles(newArticles: List<KnowledgeArticle>) {
        articles = newArticles
        notifyDataSetChanged()
    }
}
