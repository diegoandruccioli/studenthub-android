package com.unibo.android.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.unibo.android.domain.di.UseCasesProvider
import com.unibo.android.ui.adapters.AccomodationAdapter
import com.unibo.android.ui.R
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_ID_PRODUCT = "id_product"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by lazy {
        HomeViewModel(
            startFetchAccommodationListUseCase = UseCasesProvider.startFetchAccommodationListUseCase,
            fetchAccommodationListUpdatesUseCase = UseCasesProvider.fetchAccommodationListUpdatesUseCase
        )
    }

    // TODO: Rename and change types of parameters
    private var idProduct: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { arguments ->
            idProduct = arguments.getString(ARG_ID_PRODUCT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = AccomodationAdapter(
            dataSet = mutableListOf() //AccommodationMockProvider.getMockUiAccomodationTypes()
        )
        fetchAccommodationListUpdates()
        viewModel.startFetchAccommodationList()
    }

    private fun fetchAccommodationListUpdates() {
        lifecycleScope.launch {
            val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerView)
            viewModel.accommodationTypeList.collect { accommodationTypeList ->
                (recyclerView?.adapter as? AccomodationAdapter)?.updateList(
                    newList = accommodationTypeList
                )
            }

            //repeatOnLifecycle(Lifecycle.State.STARTED) {}
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param idProduct Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(idProduct: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_ID_PRODUCT, idProduct)
                }
            }
    }
}