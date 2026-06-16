package com.guzhyo.nfcattendance.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.guzhyo.nfcattendance.AttendanceRecord
import com.guzhyo.nfcattendance.RecordStore
import com.guzhyo.nfcattendance.databinding.FragmentRecordsBinding

class RecordsFragment : Fragment() {

    private var _binding: FragmentRecordsBinding? = null
    private val binding get() = _binding!!
    private var allRecords = listOf<AttendanceRecord>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRecordsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recordList.layoutManager = LinearLayoutManager(requireContext())

        binding.filterSpinner.adapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_spinner_dropdown_item, listOf("全部"))

        binding.filterSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) {
                refreshList()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        binding.clearBtn.setOnClickListener {
            RecordStore.clear(requireContext())
            allRecords = emptyList()
            refreshList()
            Toast.makeText(requireContext(), "已清空", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        allRecords = RecordStore.load(requireContext())
        updateFilter()
        refreshList()
    }

    private fun updateFilter() {
        val months = allRecords.map { it.date.take(7) }.distinct().sortedDescending()
        val items = listOf("全部") + months
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, items)
        binding.filterSpinner.adapter = adapter
    }

    private fun refreshList() {
        val selected = binding.filterSpinner.selectedItem?.toString() ?: "全部"
        val filtered = if (selected == "全部") allRecords
        else allRecords.filter { it.date.startsWith(selected) }

        binding.countText.text = "${filtered.size} 条记录"
        binding.recordList.adapter = RecordListAdapter(filtered)
    }
}

class RecordListAdapter(private val items: List<AttendanceRecord>) :
    androidx.recyclerview.widget.RecyclerView.Adapter<RecordListAdapter.VH>() {

    class VH(view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view) {
        val dateText: android.widget.TextView = view.findViewById(android.R.id.text1)
        val timeText: android.widget.TextView = view.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val r = items[position]
        holder.dateText.text = "${position + 1}.  ${r.date}  ${r.time}"
        holder.timeText.text = "${r.source}${if (r.detail.isNotBlank()) " · ${r.detail}" else ""}"
    }

    override fun getItemCount() = items.size
}
