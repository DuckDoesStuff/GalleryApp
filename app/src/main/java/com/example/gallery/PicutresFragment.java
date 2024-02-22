package com.example.gallery;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link PicutresFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PicutresFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public PicutresFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PicutresFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PicutresFragment newInstance(String param1, String param2) {
        PicutresFragment fragment = new PicutresFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_picutres, container, false);

        GridView gridView = view.findViewById(R.id.photo_grid);
        gridView.setAdapter(new FrameAdapter(getContext(), 20)); // Sử dụng adapter mới
        gridView.setOnItemClickListener((parent, view1, position, id) ->{
            ViewPictureFragment fragment = new ViewPictureFragment();

            // Pass any data to the fragment if needed using Bundle
            Bundle bundle = new Bundle();
            bundle.putInt("position", position); // Example: passing position to the fragment
            fragment.setArguments(bundle);

            // Replace the current fragment with the new one
            FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_layout, fragment);
            fragmentTransaction.addToBackStack(null); // Optional: Add the transaction to the back stack
            fragmentTransaction.commit();

                }

        );
        gridView.setOnItemLongClickListener((parent, view12, position, id) -> {
            Toast.makeText(getContext(), "LONG CLICKED AT " + position, Toast.LENGTH_LONG).show();
            return false;
        });
        ImageButton dropdownButton = view.findViewById(R.id.settings);
        dropdownButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(getContext(), v);
            popupMenu.getMenuInflater().inflate(R.menu.setting_dropdown, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                // Handle menu item click
                if(item.getItemId() == R.id.choice1) {
                    return true;
                }else if (item.getItemId() == R.id.choice2) {
                    return true;
                }else if (item.getItemId() == R.id.choice3) {
                    return true;}

                return true;

            });

            popupMenu.show();
        });

        return view;
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.top_nav_menu, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setQueryHint("Find your picture");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Perform your search operation here
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }


        });
        super.onCreateOptionsMenu(menu, inflater);
    }

}