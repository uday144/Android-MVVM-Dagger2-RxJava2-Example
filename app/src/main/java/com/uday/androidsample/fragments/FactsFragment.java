package com.uday.androidsample.fragments;

import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.uday.androidsample.R;
import com.uday.androidsample.adapter.CountryFactsAdapter;
import com.uday.androidsample.app.Constant;
import com.uday.androidsample.model.Country;
import com.uday.androidsample.model.Facts;
import com.uday.androidsample.network.Api;
import com.uday.androidsample.utils.MyDividerItemDecoration;
import com.uday.androidsample.viewmodel.FactsViewModel;

import java.util.Arrays;
import java.util.List;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;
import retrofit2.converter.gson.GsonConverterFactory;

public class FactsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{
    RecyclerView recyclerView;
    CountryFactsAdapter adapter;
    SwipeRefreshLayout mSwipeRefreshLayout;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_factlist, container, false);
        recyclerView = view.findViewById(R.id.rvFacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addItemDecoration(new MyDividerItemDecoration(getActivity(), LinearLayoutManager.VERTICAL, 16));
// SwipeRefreshLayout
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        /**
         * Showing Swipe Refresh animation on activity create
         * As animation won't start on onCreate, post runnable is used
         */
        mSwipeRefreshLayout.post(new Runnable() {

            @Override
            public void run() {

                mSwipeRefreshLayout.setRefreshing(true);

                // Fetching data from server
                loadDataToViewModel();
            }
        });

        //getFacts();
        return view;
    }

    private void loadDataToViewModel() {
        // Showing refresh animation before making http call
        mSwipeRefreshLayout.setRefreshing(true);
        FactsViewModel model = ViewModelProviders.of(this).get(FactsViewModel.class);

        model.getFacts().observe(this, new Observer<List<Facts>>() {
            @Override
            public void onChanged(@Nullable List<Facts> factsList) {
                adapter = new CountryFactsAdapter(factsList, getActivity().getApplicationContext());
                recyclerView.setAdapter(adapter);
                // Stopping swipe refresh
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    /**
     * This method is called when swipe refresh is pulled down
     */
    @Override
    public void onRefresh() {

        // Fetching data from server
        loadDataToViewModel();
    }
    private void getFacts() {
      Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constant.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create()) //Here we are using the GsonConverterFactory to directly convert json data to object
                .build();

        Api api = retrofit.create(Api.class);

        Call<Country> call = api.getCountryFacts();

        call.enqueue(new Callback<Country>() {
            @Override
            public void onResponse(Call<Country> call, Response<Country> response) {
                Country country = response.body();


                List<Facts> facts =  Arrays.asList(country.getRows());
                recyclerView.setAdapter(new CountryFactsAdapter(facts, getActivity().getApplicationContext()));


            }

            @Override
            public void onFailure(Call<Country> call, Throwable t) {

                Toast.makeText(getActivity().getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}