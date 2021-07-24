package org.cryptimeleon.incentive.app.basket

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import org.cryptimeleon.incentive.app.BaseFragment
import org.cryptimeleon.incentive.app.R
import org.cryptimeleon.incentive.app.databinding.FragmentBasketBinding

class BasketFragment : BaseFragment() {

    private lateinit var binding: FragmentBasketBinding
    private lateinit var viewModel: BasketViewModel


    /**
     * Setup view model and data binding on creating of the view.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_basket,
            container,
            false
        )

        binding.basketItemList.adapter = BasketItemRecyclerViewAdapter(
            BasketItemRecyclerViewAdapter.OnClickListener(
                {
                    viewModel.setItemCount(it.item.id, it.count + 1)
                },
                {
                    viewModel.setItemCount(it.item.id, it.count - 1)
                }
            )
        )

        viewModel = ViewModelProvider(this).get(BasketViewModel::class.java)

        binding.basketViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }
}