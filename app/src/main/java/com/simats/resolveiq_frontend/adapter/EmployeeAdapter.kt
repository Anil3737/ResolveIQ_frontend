package com.simats.resolveiq_frontend.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.simats.resolveiq_frontend.data.model.Employee
import com.simats.resolveiq_frontend.databinding.ItemEmployeeCardBinding
import java.util.Locale

class EmployeeAdapter(
    private var employees: List<Employee>,
    private val onItemClick: (Employee) -> Unit
) : RecyclerView.Adapter<EmployeeAdapter.EmployeeViewHolder>(), Filterable {

    private var filteredEmployees: List<Employee> = employees

    inner class EmployeeViewHolder(private val binding: ItemEmployeeCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(employee: Employee) {
            binding.tvEmpId.text = employee.employeeId
            binding.tvEmpName.text = employee.fullName
            
            binding.root.setOnClickListener {
                onItemClick(employee)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmployeeViewHolder {
        val binding = ItemEmployeeCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EmployeeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EmployeeViewHolder, position: Int) {
        holder.bind(filteredEmployees[position])
    }

    override fun getItemCount(): Int = filteredEmployees.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                filteredEmployees = if (charSearch.isEmpty()) {
                    employees
                } else {
                    val resultList = mutableListOf<Employee>()
                    for (row in employees) {
                        if (row.fullName.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT)) ||
                            row.employeeId.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))
                        ) {
                            resultList.add(row)
                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = filteredEmployees
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredEmployees = results?.values as List<Employee>
                notifyDataSetChanged()
            }
        }
    }
}
