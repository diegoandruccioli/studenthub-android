package com.unibo.android.ui.libretto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.unibo.android.ui.databinding.FragmentLibrettoBinding
import kotlinx.coroutines.launch

class LibrettoFragment : Fragment() {

    private var _binding: FragmentLibrettoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LibrettoViewModel by viewModels()
    private lateinit var adapter: EsameAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibrettoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = EsameAdapter { esame -> viewModel.deleteEsame(esame) }
        binding.recyclerViewEsami.adapter = adapter
        binding.recyclerViewEsami.layoutManager = LinearLayoutManager(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.esami.collect { lista ->
                    adapter.submitList(lista)
                    binding.textEmpty.visibility =
                        if (lista.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }

        binding.fabAddEsame.setOnClickListener {
            // TODO: navigare verso AddEsameFragment
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
