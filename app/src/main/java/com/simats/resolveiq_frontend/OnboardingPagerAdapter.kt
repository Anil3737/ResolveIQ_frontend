package com.simats.resolveiq_frontend

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class OnboardingPagerAdapter(private val layouts: List<Int>) : 
    RecyclerView.Adapter<OnboardingPagerAdapter.PagerViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return PagerViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        // Binding logic can be added here if needed
    }
    
    override fun getItemCount(): Int = layouts.size
    
    override fun getItemViewType(position: Int): Int = layouts[position]
    
    class PagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
