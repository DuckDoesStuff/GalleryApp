package com.example.gallery;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
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
        gridView.setOnItemClickListener((parent, view1, position, id) ->
                Toast.makeText(getContext(), "ITEM CLICKED AT " + position, Toast.LENGTH_SHORT).show()
        );
        gridView.setOnItemLongClickListener((parent, view12, position, id) -> {
            Toast.makeText(getContext(), "LONG CLICKED AT " + position, Toast.LENGTH_LONG).show();
            return false;
        });
        return view;
    }
}